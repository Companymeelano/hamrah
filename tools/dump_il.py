#!/usr/bin/env python3
"""Disassemble selected methods of AtiranLocalServices.dll with resolved tokens."""
import sys
sys.path.insert(0, __file__.rsplit('/', 1)[0])
from extract_api import ApiExtractor
import dnfile
from dncil.cil.body.reader import read_method_body_from_bytes

def main(dll, wants):
    ex = ApiExtractor(dll)
    pe = dnfile.dnPE(dll)
    all_data = open(dll, 'rb').read()
    def rva2off(rva):
        for s in pe.sections:
            if s.VirtualAddress <= rva < s.VirtualAddress + s.SizeOfRawData:
                return s.PointerToRawData + (rva - s.VirtualAddress)
        return None
    for td in pe.net.mdtables.TypeDef:
        if str(td.TypeName) not in ('LocalServices', 'Util'):
            continue
        for idx in td.MethodList:
            m = idx.row
            if str(m.Name) not in wants:
                continue
            print(f"\n######## {m.Name}")
            off = rva2off(m.Rva)
            body = read_method_body_from_bytes(all_data[off:off + 0x4000])
            for insn in body.instructions:
                tok = getattr(insn.operand, 'value', insn.operand)
                extra = f" ; {ex.resolve_token(tok)}" if isinstance(tok, int) and tok > 0x02000000 else ""
                print(insn, extra)

if __name__ == '__main__':
    dll = sys.argv[1]
    wants = set(sys.argv[2].split(',')) if len(sys.argv) > 2 else None
    main(dll, wants)
