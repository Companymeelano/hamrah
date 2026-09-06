#!/usr/bin/env python3
"""
extract_api.py — Reverse-engineering helper for the AtiranLocalServices.dll
WCF REST service shipped inside AtiranHamrah.zip.

Reads the .NET metadata (ECMA-335) of the published DLL and prints:
  * the WCF operation contract (ILocalServices) — method names, signatures,
    UriTemplates / HTTP verbs / body styles,
  * DataContract DTO property names & C# types,
  * if --il is given: disassembled CIL for selected methods (via dncil).

Requires: pip install dnfile dncil

Usage:
  python3 tools/extract_api.py path/to/AtiranLocalServices.dll [--il]
"""
import argparse
import json
import struct
import sys

# --------------------------------------------------------------------------
# Minimal ECMA-335 metadata reader
# --------------------------------------------------------------------------
class MD:
    def __init__(self, data):
        self.data = data
        self.bsjb = data.find(b"BSJB")
        if self.bsjb < 0:
            raise ValueError("not a .NET PE (no BSJB)")
        o = self.bsjb + 12
        vlen = struct.unpack_from("<I", data, o)[0]
        o += 4 + vlen
        o = (o + 3) & ~3
        o += 2  # flags
        nstreams = struct.unpack_from("<H", data, o)[0]
        o += 2
        self.streams = {}
        for _ in range(nstreams):
            off, size = struct.unpack_from("<II", data, o)
            o += 8
            name = data[o:o + 64].split(b"\x00")[0].decode()
            o += (len(name) + 1 + 3) & ~3
            self.streams[name] = (self.bsjb + off, size)
        self.parse_tables()

    def parse_tables(self):
        d = self.data
        pos, _ = self.streams["#~"]
        hdr = d[pos:pos + 24]
        self.heapsizes = hdr[6]
        self.valid = struct.unpack_from("<Q", d, pos + 8)[0]
        p = pos + 24
        self.rows = {}
        for i in range(64):
            if (self.valid >> i) & 1:
                self.rows[i + 1] = struct.unpack_from("<I", d, p)[0]
                p += 4
        self.tables_pos = p
        self.sz_str = 4 if self.heapsizes & 1 else 2
        self.sz_guid = 4 if self.heapsizes & 2 else 2
        self.sz_blob = 4 if self.heapsizes & 4 else 2
        self.build_schema()

    def mi(self, table):
        return 4 if self.rows.get(table, 0) >= 0x10000 else 2

    def coded(self, tables):
        n = max(self.rows.get(t, 0) for t in tables)
        bits = (len(tables) - 1).bit_length()
        return 4 if n >= (1 << (16 - bits)) else 2

    def build_schema(self):
        T = {}
        T[1] = [("Generation", 2), ("Name", "S"), ("Mvid", "G"), ("EncId", "G"), ("EncBaseId", "G")]
        T[2] = [("ResolutionScope", "C:1,27,36,2"), ("TypeName", "S"), ("TypeNamespace", "S")]
        T[3] = [("Flags", 4), ("TypeName", "S"), ("TypeNamespace", "S"), ("Extends", "C:3,2,28"),
                 ("FieldList", "I:4"), ("MethodList", "I:6")]
        T[4] = [("Field", "I:4")]
        T[5] = [("Flags", 2), ("Name", "S"), ("Signature", "B")]
        T[6] = [("Method", "I:6")]
        T[7] = [("RVA", 4), ("ImplFlags", 2), ("Flags", 2), ("Name", "S"), ("Signature", "B"), ("ParamList", "I:8")]
        T[8] = [("Param", "I:8")]
        T[9] = [("Flags", 2), ("Sequence", 2), ("Name", "S")]
        T[10] = [("Class", "I:3"), ("Interface", "C:3,2,28")]
        T[11] = [("Class", "C:3,2,27,6,28"), ("Name", "S"), ("Signature", "B")]
        T[12] = [("Type", 1), ("Padding", 1), ("Parent", "C:5,9,24"), ("Value", "B")]
        T[13] = [("Parent", "HCA"), ("Type", "C:6,11"), ("Value", "B")]
        T[14] = [("Parent", "C:5,9"), ("NativeType", "B")]
        T[15] = [("Action", 2), ("Parent", "C:3,6,33"), ("PermissionSet", "B")]
        T[16] = [("PackingSize", 2), ("ClassSize", 4), ("Parent", "I:3")]
        T[17] = [("Offset", 4), ("Field", "I:4")]
        T[18] = [("Signature", "B")]
        T[19] = [("Parent", "I:3"), ("EventList", "I:21")]
        T[20] = [("Event", "I:21")]
        T[21] = [("EventFlags", 2), ("Name", "S"), ("EventType", "C:3,2,28")]
        T[22] = [("Parent", "I:3"), ("PropertyList", "I:24")]
        T[23] = [("Property", "I:24")]
        T[24] = [("Flags", 2), ("Name", "S"), ("Type", "B")]
        T[25] = [("Semantics", 2), ("Method", "I:6"), ("Association", "C:21,24")]
        T[26] = [("Class", "I:3"), ("MethodBody", "C:6,11"), ("MethodDeclaration", "C:6,11")]
        T[27] = [("Name", "S")]
        T[28] = [("Signature", "B")]
        T[29] = [("MappingFlags", 2), ("MemberForwarded", "C:5,6"), ("ImportName", "S"), ("ImportScope", "I:27")]
        T[30] = [("RVA", 4), ("Field", "I:4")]
        T[31] = [("Token", 4), ("FuncCode", 4)]
        T[32] = [("Token", 4)]
        T[33] = [("HashAlgId", 4), ("Major", 2), ("Minor", 2), ("Build", 2), ("Rev", 2), ("Flags", 4),
                 ("PublicKey", "B"), ("Name", "S"), ("Culture", "S")]
        T[34] = [("Processor", 4)]
        T[35] = [("OSPlatformID", 4), ("OSMajor", 4), ("OSMinor", 4)]
        T[36] = [("Major", 2), ("Minor", 2), ("Build", 2), ("Rev", 2), ("Flags", 4), ("PublicKeyOrToken", "B"),
                 ("Name", "S"), ("Culture", "S"), ("HashValue", "B")]
        T[37] = [("Processor", 4), ("AssemblyRef", "I:36")]
        T[38] = [("OSPlatformID", 4), ("OSMajor", 4), ("OSMinor", 4), ("AssemblyRef", "I:36")]
        T[39] = [("Flags", 4), ("Name", "S"), ("HashValue", "B")]
        T[40] = [("Flags", 4), ("TypeDefId", 4), ("TypeName", "S"), ("TypeNamespace", "S"),
                 ("Implementation", "C:39,36,40")]
        T[41] = [("Offset", 4), ("Flags", 4), ("Name", "S"), ("Implementation", "C:39,36,40")]
        T[42] = [("NestedClass", "I:3"), ("EnclosingClass", "I:3")]
        T[43] = [("Number", 2), ("Flags", 2), ("Owner", "C:3,6"), ("Name", "S")]
        T[44] = [("Method", "C:6,11"), ("Instantiation", "B")]
        T[45] = [("Owner", "I:43"), ("Constraint", "C:3,2,28")]
        self.schema = T
        HCA = [6, 5, 2, 3, 9, 10, 11, 1, 15, 24, 21, 18, 27, 28, 33, 36, 39, 40, 41, 43, 45, 44]
        self.colsize = {}
        for tno, cols in T.items():
            sizes = {}
            for cname, c in cols:
                if isinstance(c, int):
                    sz = c
                elif c == "S":
                    sz = self.sz_str
                elif c == "G":
                    sz = self.sz_guid
                elif c == "B":
                    sz = self.sz_blob
                elif c.startswith("I:"):
                    sz = self.mi(int(c[2:]))
                elif c == "HCA":
                    sz = self.coded(HCA)
                elif c.startswith("C:"):
                    sz = self.coded([int(x) for x in c[2:].split(",")])
                else:
                    raise ValueError(c)
                sizes[cname] = sz
            self.colsize[tno] = sizes

    def tables(self):
        p = self.tables_pos
        out = {}
        for tno in sorted(self.schema):
            n = self.rows.get(tno, 0)
            if not n:
                continue
            cols = self.schema[tno]
            sizes = self.colsize[tno]
            rows = []
            for _ in range(n):
                row = {}
                for cname, _ in cols:
                    sz = sizes[cname]
                    row[cname] = struct.unpack_from("<I" if sz == 4 else "<H", self.data, p)[0]
                    p += sz
                rows.append(row)
            out[tno] = rows
        return out


class ApiExtractor:
    def __init__(self, path):
        self.data = open(path, "rb").read()
        self.md = MD(self.data)
        self.spos = self.md.streams["#Strings"][0]
        self.bpos = self.md.streams["#Blob"][0]
        self.tabs = self.md.tables()
        self.td = self.tabs[3]
        self.tr = self.tabs[2]
        self.mr = self.tabs[11]
        self.methods = self.tabs[7]
        self.fields = self.tabs[5]
        self.cattrs = self.tabs[13]
        self.td_names = {i + 1: f"{self.s(r['TypeNamespace'])}.{self.s(r['TypeName'])}"
                         for i, r in enumerate(self.td)}
        self.tr_names = {i + 1: f"{self.s(r['TypeNamespace'])}.{self.s(r['TypeName'])}"
                         for i, r in enumerate(self.tr)}

    # -- heaps ------------------------------------------------------------
    def s(self, idx):
        if idx == 0:
            return ""
        p = self.spos + idx
        e = self.data.find(b"\x00", p)
        return self.data[p:e].decode("utf8", "replace")

    def blob(self, idx):
        p = self.bpos + idx
        b0 = self.data[p]
        p += 1
        if b0 & 0x80 == 0:
            n = b0
        elif b0 & 0xC0 == 0x80:
            n = ((b0 & 0x3F) << 8) | self.data[p]
            p += 1
        else:
            n = ((b0 & 0x1F) << 24) | (self.data[p] << 16) | (self.data[p + 1] << 8) | self.data[p + 2]
            p += 3
        return self.data[p:p + n]

    def cint(self, b, off):
        b0 = b[off]
        off += 1
        if b0 & 0x80 == 0:
            return b0, off
        if b0 & 0xC0 == 0x80:
            return ((b0 & 0x3F) << 8) | b[off], off + 1
        return ((b0 & 0x1F) << 24) | (b[off] << 16) | (b[off + 1] << 8) | b[off + 2], off + 3

    def resolve_tdr(self, v):
        tag, row = v & 3, v >> 2
        if tag == 0:
            return self.td_names.get(row, f"TypeDef#{row}")
        if tag == 1:
            return self.tr_names.get(row, f"TypeRef#{row}")
        return f"TypeSpec#{row}"

    def parse_type(self, b, off):
        e = b[off]
        off += 1
        prim = {0x02: "bool", 0x03: "char", 0x04: "sbyte", 0x05: "byte", 0x06: "int16", 0x07: "uint16",
                0x08: "int32", 0x09: "uint32", 0x0A: "int64", 0x0B: "uint64", 0x0C: "float32",
                0x0D: "float64", 0x0E: "string", 0x18: "void", 0x1C: "object", 0x16: "typedbyref"}
        if e in prim:
            return prim[e], off
        if e == 0x0F:
            t, off = self.parse_type(b, off)
            return f"ptr({t})", off
        if e == 0x10:
            t, off = self.parse_type(b, off)
            return f"byref({t})", off
        if e in (0x11, 0x12):
            v, off = self.cint(b, off)
            t = self.resolve_tdr(v)
            return ("valuetype " if e == 0x11 else "") + t, off
        if e in (0x13, 0x1E):
            v, off = self.cint(b, off)
            return f"!{v}", off
        if e == 0x1F:
            v, off = self.cint(b, off)
            return f"!!{v}", off
        if e == 0x14:
            t, off = self.parse_type(b, off)
            rank, off = self.cint(b, off)
            ns, off = self.cint(b, off)
            for _ in range(ns):
                _, off = self.cint(b, off)
            nl, off = self.cint(b, off)
            for _ in range(nl):
                _, off = self.cint(b, off)
            return f"array({t}[{rank}])", off
        if e == 0x15:
            t, off = self.parse_type(b, off)
            n, off = self.cint(b, off)
            args = []
            for _ in range(n):
                a, off = self.parse_type(b, off)
                args.append(a)
            return f"{t}<{','.join(args)}>", off
        if e == 0x1D:
            t, off = self.parse_type(b, off)
            return f"szarray({t})", off
        return f"<0x{e:02x}>", off

    def parse_method_sig(self, idx):
        b = self.blob(idx)
        off = 0
        calling = b[off]
        off += 1
        genp = 0
        if calling & 0x10:
            genp, off = self.cint(b, off)
        pcnt, off = self.cint(b, off)
        ret, off = self.parse_type(b, off)
        params = []
        for _ in range(pcnt):
            t, off = self.parse_type(b, off)
            params.append(t)
        return {"calling": calling, "genparams": genp, "ret": ret, "params": params}

    def parse_field_sig(self, idx):
        b = self.blob(idx)
        off = 0
        if b and b[0] == 0x06:
            off = 1
        while off < len(b) and b[off] in (0x1F, 0x20):
            off += 1
            _, off = self.cint(b, off)
        t, _ = self.parse_type(b, off)
        return t

    # -- custom attributes -------------------------------------------------
    def serstring(self, b, off):
        v, off = self.cint(b, off)
        return b[off:off + v].decode("utf8", "replace"), off + v

    def decode_attr(self, idx):
        b = self.blob(idx)
        if len(b) < 4:
            return {}
        off = 2
        cnt = struct.unpack_from("<H", b, off)[0]
        off += 2
        named = {}
        for _ in range(cnt):
            if off + 2 > len(b):
                break
            kind = b[off]
            off += 1
            ftype = b[off]
            off += 1
            name, off = self.serstring(b, off)
            if ftype == 0x0E:
                val, off = self.serstring(b, off)
            elif ftype == 0x02:
                if off >= len(b):
                    break
                val = bool(b[off])
                off += 1
            elif ftype == 0x08:
                if off + 4 > len(b):
                    break
                val = struct.unpack_from("<i", b, off)[0]
                off += 4
            elif ftype == 0x55:
                enum, off = self.serstring(b, off)
                val = struct.unpack_from("<i", b, off)[0]
                off += 4
                name = enum.split(",")[0].split(".")[-1]
                val = (enum.split(",")[0], val)
            else:
                val = f"<0x{ftype:02x}>"
            named[name] = val
        return named

    HCA = [6, 5, 2, 3, 9, 10, 11, 1, 15, 24, 21, 18, 27, 28, 33, 36, 39, 40, 41, 43, 45, 44]

    def attr_typename(self, v):
        # CustomAttributeType coded index: 3 tag bits; tag 2=MethodDef, 3=MemberRef, 0/1/4 reserved
        tag, row = v & 7, v >> 3
        if tag == 3 and 0 < row <= len(self.mr):
            r = self.mr[row - 1]
            cls = r["Class"]
            # MemberRefParent coded index: 3 tag bits (TypeDef, TypeRef, ModuleRef, MethodDef, TypeSpec)
            ct, crow = cls & 7, cls >> 3
            if ct == 1:
                owner = self.tr_names.get(crow, f"?{crow}")
            elif ct == 0:
                owner = self.td_names.get(crow, f"?{crow}")
            elif ct == 2:
                owner = f"ModuleRef#{crow}"
            elif ct == 3:
                owner = f"MethodDef#{crow}"
            else:
                owner = f"TypeSpec#{crow}"
            return f"{owner}::{self.s(r['Name'])}" 
        if tag == 2 and 0 < row <= len(self.methods):
            return f"MethodDef::{self.s(self.methods[row-1]['Name'])}"
        return f"?{tag}:{row}"

    def method_attrs(self):
        HCA = self.HCA
        by_parent = {}
        for r in self.cattrs:
            tag = r["Parent"] & 0x1F
            row = r["Parent"] >> 5
            if tag >= len(HCA):
                continue
            by_parent.setdefault((HCA[tag], row), []).append(
                (self.attr_typename(r["Type"]), self.decode_attr(r["Value"])))
        return by_parent

    # -- owners ------------------------------------------------------------
    def method_owner(self):
        owners = {}
        for i, td in enumerate(self.td):
            start = td["MethodList"]
            end = self.td[i + 1]["MethodList"] if i + 1 < len(self.td) else len(self.methods) + 1
            for rno in range(start, end):
                owners[rno] = i + 1
        return owners

    def param_groups(self):
        pg = {}
        for rno, mrow in enumerate(self.methods, start=1):
            start = mrow["ParamList"]
            nxt = self.methods[rno]["ParamList"] if rno < len(self.methods) else len(self.tabs[9]) + 1
            grp = []
            for x in range(start, nxt):
                if x - 1 < len(self.tabs[9]):
                    grp.append(self.s(self.tabs[9][x - 1]["Name"]))
            pg[rno] = grp
        return pg

    def dump(self, pretty=True):
        attrs = self.method_attrs()
        owners = self.method_owner()
        pgroups = self.param_groups()
        out = {}
        for rno, mrow in enumerate(self.methods, start=1):
            nm = self.s(mrow["Name"])
            own = owners.get(rno)
            if own is None or not self.td_names[own].endswith("ILocalServices"):
                continue
            sig = self.parse_method_sig(mrow["Signature"])
            pnames = pgroups.get(rno, [])
            params = []
            for i, t in enumerate(sig["params"]):
                params.append({"name": pnames[i] if i < len(pnames) else f"p{i}", "type": t})
            web = {}
            for an, av in attrs.get((6, rno), []):
                if "WebInvoke" in an or "WebGet" in an:
                    for k, v in av.items():
                        if isinstance(v, tuple):
                            v = v[1]
                        web[k] = v
            out[nm] = {"ret": sig["ret"], "params": params, "web": web}
        return json.loads(json.dumps(out)) if pretty else out

    def dump_dtos(self):
        out = {}
        for i, td in enumerate(self.td):
            tn = self.td_names[i + 1]
            if not (tn.startswith("AtiranLocalServices.")
                    and not tn.startswith("AtiranLocalServices.DataAccess")
                    and not tn.startswith("AtiranLocalServices.Helper")
                    and not tn.endswith("LocalServices")
                    and not tn.endswith("ILocalServices")):
                continue
            start = td["FieldList"]
            end = self.td[i + 1]["FieldList"] if i + 1 < len(self.td) else len(self.fields) + 1
            props = []
            for f in self.fields[start - 1:end - 1]:
                fname = self.s(f["Name"])
                if fname.startswith("<") and fname.endswith(">k__BackingField"):
                    fname = fname[1:-len(">k__BackingField")]
                props.append({"json": fname, "type": self.parse_field_sig(f["Signature"])})
            if props:
                out[tn] = props
        return out

    def resolve_token(self, tok):
        table, row = (tok >> 24) & 0xFF, tok & 0xFFFFFF
        if table == 2 and 0 < row <= len(self.td):
            return f"TD:{self.td_names[row]}"
        if table == 1 and 0 < row <= len(self.tr):
            return f"TR:{self.tr_names[row]}"
        if table == 6 and 0 < row <= len(self.methods):
            return f"MD:{self.s(self.methods[row-1]['Name'])}"
        if table == 10 and 0 < row <= len(self.mr):
            r = self.mr[row - 1]
            cls = r["Class"]
            ct, crow = cls & 7, cls >> 3
            if ct == 1:
                owner = self.tr_names.get(crow, f"?{crow}")
            elif ct == 0:
                owner = self.td_names.get(crow, f"?{crow}")
            else:
                owner = f"{ct}#{crow}"
            return f"MR:{owner}::{self.s(r['Name'])}" 
        if table == 4 and 0 < row <= len(self.fields):
            return f"FLD:{self.s(self.fields[row-1]['Name'])}"
        if table == 11:
            return f"TS:{row}"
        return f"T(tbl={table},row={row})"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("dll")
    ap.add_argument("--il", action="store_true", help="dump IL for Login/activate methods")
    args = ap.parse_args()
    ex = ApiExtractor(args.dll)
    print(json.dumps(ex.dump(), indent=1, ensure_ascii=False))
    if args.il:
        try:
            from dnfile import dnPE
            from dncil.cil.body.reader import read_method_body_from_bytes
            pe = dnPE(args.dll)
            all_data = open(args.dll, "rb").read()

            def rva2off(rva):
                for s in pe.sections:
                    if s.VirtualAddress <= rva < s.VirtualAddress + s.SizeOfRawData:
                        return s.PointerToRawData + (rva - s.VirtualAddress)
                return None

            want = {"Login", "AcivateTablet", "GetCustomerByLogin", "LoginVisitor", "CheckSetInfo",
                    "RegisterDeviceAppId", "GetPeriods"}
            for td in pe.net.mdtables.TypeDef:
                if str(td.TypeName) != "LocalServices":
                    continue
                for idx in td.MethodList:
                    m = idx.row
                    if str(m.Name) not in want:
                        continue
                    print(f"\n######## {m.Name}")
                    off = rva2off(m.Rva)
                    body = read_method_body_from_bytes(all_data[off:off + 0x2000])
                    for insn in body.instructions:
                        tok = insn.operand.value if hasattr(insn.operand, "value") else insn.operand
                        extra = f" ; {ex.resolve_token(tok)}" if isinstance(tok, int) and tok > 0x02000000 else ""
                        print(insn, extra)
        except ImportError:
            print("dncil not installed; skipping IL dump")


if __name__ == "__main__":
    main()
