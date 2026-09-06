import express from 'express';
import sql from 'mssql';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

dotenv.config();

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PORT = Number(process.env.PORT || 8080);
const DEMO_MODE = process.env.DEMO_MODE === '1';

/**
 * Direct, read-only connection to Atiran2 SQL Server.
 * This web app never writes to the database; every query is a plain SELECT.
 */
const dbConfig = {
  user: process.env.DB_USER || 'AdminAn',
  password: process.env.DB_PASSWORD || 'St@R2022$',
  server: process.env.DB_HOST || '37.143.147.19',
  port: Number(process.env.DB_PORT || 1433),
  database: process.env.DB_NAME || 'Atiran2',
  pool: {
    max: 5,
    min: 0,
    idleTimeoutMillis: 30000,
  },
  options: {
    encrypt: false,
    trustServerCertificate: true,
    enableArithAbort: true,
    requestTimeout: 30000,
  },
};

let poolPromise = null;

async function getPool() {
  if (poolPromise) return poolPromise;
  poolPromise = sql.connect(dbConfig);
  try {
    await poolPromise;
  } catch (error) {
    poolPromise = null;
    throw error;
  }
  return poolPromise;
}

async function run(sqlText) {
  const pool = await getPool();
  const result = await pool.request().query(sqlText);
  return result.recordset || [];
}

function maybeInt(value) {
  if (value === null || value === undefined) return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function maybeNumber(value) {
  if (value === null || value === undefined) return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function str(value) {
  return value === null || value === undefined ? null : String(value);
}

// ------------------------------------------------------------------ queries
async function testConnection() {
  const rows = await run('SELECT DB_NAME() AS db, @@VERSION AS version');
  const row = rows[0] || {};
  return `${row.db || 'Atiran2'} | ${String(row.version || '').slice(0, 70)}`;
}

async function queryCustomers(limit) {
  return run(`
    SELECT TOP ${limit} SHMO, MONAME, tell1, tell2, cell, addre, sharh,
           cred, man, active, group_rdf, vis_rdf, shomare_masir, lat, lng
    FROM CUSTOMER
    ORDER BY man DESC
  `).then((rows) =>
    rows.map((r) => ({
      shmo: maybeInt(r.SHMO),
      name: str(r.MONAME),
      tell1: str(r.tell1),
      tell2: str(r.tell2),
      cell: str(r.cell),
      address: str(r.addre),
      sharh: str(r.sharh),
      cred: maybeNumber(r.cred),
      man: maybeNumber(r.man),
      active: str(r.active),
      groupRdf: maybeInt(r.group_rdf),
      visRdf: maybeInt(r.vis_rdf),
      masir: maybeInt(r.shomare_masir),
      lat: maybeNumber(r.lat),
      lng: maybeNumber(r.lng),
    })),
  );
}

async function queryProducts(limit) {
  return run(`
    SELECT TOP ${limit} shka, naka, coka, vahsanj, mohvah, mojkavah, mojkajoz,
           bastebandi, tedbastebandi, buy_price, inventory_price, FinalSalePrice,
           pavarez, ptax, group_rdf, active
    FROM inventory
    ORDER BY shka
  `).then((rows) =>
    rows.map((r) => ({
      shka: maybeInt(r.shka) || 0,
      name: str(r.naka),
      code: str(r.coka),
      unit: str(r.vahsanj),
      mohvah: maybeInt(r.mohvah),
      mojkavah: maybeNumber(r.mojkavah) || 0,
      mojkajoz: maybeInt(r.mojkajoz) || 0,
      bast: str(r.bastebandi),
      tedBast: maybeNumber(r.tedbastebandi) || 0,
      buyPrice: maybeNumber(r.buy_price) || 0,
      invPrice: maybeNumber(r.inventory_price) || 0,
      finalPrice: maybeNumber(r.FinalSalePrice) || 0,
      pTax: maybeNumber(r.ptax) || 0,
      pAvarez: maybeNumber(r.pavarez) || 0,
      groupRdf: maybeInt(r.group_rdf) || 0,
      active: str(r.active),
    })),
  );
}

async function queryInventory(limit) {
  return run(`
    SELECT TOP ${limit} ia.shka, i.naka, ia.rdf_anbars, a.name, ia.mojkavah, ia.mojkajoz
    FROM inventory_anbars ia
    LEFT JOIN inventory i ON i.shka = ia.shka
    LEFT JOIN anbar a ON a.rdf_anbar = ia.rdf_anbars
    ORDER BY ia.mojkavah DESC
  `).then((rows) =>
    rows.map((r) => ({
      shka: maybeInt(r.shka) || 0,
      name: str(r.naka),
      anbar: maybeInt(r.rdf_anbars) || 0,
      anbarName: str(r.name),
      moj: maybeNumber(r.mojkavah) || 0,
      mojkajoz: maybeInt(r.mojkajoz) || 0,
    })),
  );
}

async function queryChecks(limit) {
  return run(`
    SELECT TOP ${limit} rdf, getdate, sardate, getchkshhes, getchbank, getchkshobe,
           shgetchk, getchkmab, shmo, chk_satus, getchkdis
    FROM getchk
    ORDER BY rdf DESC
  `).then((rows) =>
    rows.map((r) => ({
      rdf: maybeInt(r.rdf) || 0,
      date: str(r.getdate),
      sarDate: str(r.sardate),
      issuer: str(r.getchkshhes),
      bank: str(r.getchbank),
      branch: str(r.getchkshobe),
      serial: str(r.shgetchk),
      amount: maybeNumber(r.getchkmab) || 0,
      shmo: maybeInt(r.shmo),
      status: maybeInt(r.chk_satus) || 0,
      description: str(r.getchkdis),
    })),
  );
}

async function queryFactors(limit) {
  return run(`
    SELECT TOP ${limit} r.rdf__, r.shfacfo, r.date, r.shmo, r.moname,
           r.[all] AS all_fel, r.tafif, r.tax, r.avarez,
           r.sumlineall, r.MabDaryaftFactor, r.tdf, r.Status, r.done_date, r.ismodify
    FROM sailfact r
    ORDER BY r.rdf__ DESC
  `).then((rows) =>
    rows.map((r) => ({
      rdf: maybeInt(r.rdf__) || 0,
      shFactor: maybeInt(r.shfacfo) || 0,
      date: str(r.date),
      shmo: maybeInt(r.shmo),
      customerName: str(r.moname),
      allFel: maybeNumber(r.all_fel) || 0,
      tafif: maybeNumber(r.tafif) || 0,
      tax: maybeNumber(r.tax) || 0,
      avarez: maybeNumber(r.avarez) || 0,
      sumLineAll: maybeNumber(r.sumlineall) || 0,
      mabDaryaft: maybeNumber(r.MabDaryaftFactor) || 0,
      tdf: maybeNumber(r.tdf) || 0,
      status: maybeInt(r.Status) || 0,
      doneDate: str(r.done_date),
      isModify: str(r.ismodify),
    })),
  );
}

async function queryVisitors() {
  return run(`
    SELECT vis_rdf, vis_name, vis_tell1, vis_tell2, vis_cell, active
    FROM visitor
    ORDER BY vis_name
  `).then((rows) =>
    rows.map((r) => ({
      rdf: maybeInt(r.vis_rdf) || 0,
      name: str(r.vis_name),
      tell1: str(r.vis_tell1),
      tell2: str(r.vis_tell2),
      cell: str(r.vis_cell),
      active: str(r.active),
    })),
  );
}

async function dashboard() {
  const [customers, products, inventory, checks, factors, visitors, info] = await Promise.all([
    queryCustomers(3000),
    queryProducts(5000),
    queryInventory(8000),
    queryChecks(4000),
    queryFactors(5000),
    queryVisitors(),
    testConnection().catch(() => 'Atiran2'),
  ]);

  const totalDebt = customers.reduce((s, x) => s + (x.man || 0), 0);
  const totalCredit = customers.reduce((s, x) => s + (x.cred || 0), 0);
  const totalCheckAmount = checks.reduce((s, x) => s + (x.amount || 0), 0);
  const totalSales = factors.reduce((s, x) => s + (x.allFel || x.sumLineAll || 0), 0);
  const totalTax = factors.reduce((s, x) => s + (x.tax || 0), 0);
  const totalTakhfif = factors.reduce((s, x) => s + (x.tafif || 0), 0);
  const totalStock = inventory.reduce((s, x) => s + (x.moj || 0), 0);

  return {
    connection: info,
    summary: {
      customerCount: customers.length,
      productCount: products.length,
      checkCount: checks.length,
      factorCount: factors.length,
      visitorCount: visitors.length,
      totalDebt,
      totalCredit,
      totalCheckAmount,
      totalSales,
      totalTax,
      totalTakhfif,
      totalStock,
    },
    customers,
    products,
    inventory,
    checks,
    factors,
    visitors,
  };
}

// ------------------------------------------------------------------ demo data
function buildDemoDashboard() {
  const customers = [
    ['علی رضایی', 85000000, 12000000, 1],
    ['مهدی احمدی', 72000000, 3000000, 2],
    ['رضا کریمی', 64000000, 15000000, 1],
    ['امیر حسینی', 51000000, 8000000, 3],
    ['سمیرا مهدوی', 43000000, 22000000, 2],
    ['حمید صادقی', 39000000, 5000000, 3],
    ['مریم نادری', 26000000, 9000000, 1],
    ['پویا قاسمی', 18000000, 4000000, 2],
  ].map(([name, man, cred, visRdf], i) => ({
    shmo: i + 1, name, cred, man, active: '1', visRdf,
  }));

  const products = [
    ['کابل برق ۲×۱/۵', 950000, 120],
    ['پریز دوپول', 750000, 340],
    ['لامپ کم‌مصرف ۱۲ وات', 680000, 210],
    ['کلید تک‌پل', 620000, 180],
    ['ترانس ۲۴ ولت', 1200000, 60],
    ['سیم افشان ۱/۵', 490000, 510],
    ['دوربین مداربسته', 3800000, 35],
    ['آداپتور ۱۲ ولت', 980000, 90],
  ].map(([name, finalPrice, mojkavah], i) => ({
    shka: i + 1, name, finalPrice, mojkavah, unit: 'عدد',
  }));

  const inventory = [
    ['انبار مرکزی', 5200], ['انبار تهران', 4300], ['انبار مشهد', 3600], ['انبار کرج', 1900],
  ].flatMap(([name, moj], i) =>
    products.slice(0, 4).map((p, j) => ({
      shka: p.shka, name: p.name, anbarName: name, anbar: i + 1,
      moj: Math.round(moj / (j + 2)) || 1, mojkajoz: 0,
    })),
  );

  const checks = [
    ['بانک ملت', 28000000, 0], ['بانک ملی', 19000000, 1], ['بانک صادرات', 12500000, 0],
    ['بانک تجارت', 9800000, 1], ['بانک پاسارگاد', 7300000, 2], ['بانک سپه', 4200000, 0],
  ].map(([bank, amount, status], i) => ({
    rdf: i + 1, bank, amount, status, date: `1403/0${i + 1}/10`,
  }));

  const factors = [
    [15000000, '1403/08', 'علی رضایی'],
    [22000000, '1403/09', 'مهدی احمدی'],
    [18000000, '1403/12', 'رضا کریمی'],
    [26500000, '1404/01', 'امیر حسینی'],
    [14500000, '1404/02', 'سمیرا مهدوی'],
    [32000000, '1404/04', 'پویا قاسمی'],
    [27000000, '1404/05', 'حمید صادقی'],
    [21000000, '1404/06', 'مریم نادری'],
  ].map(([allFel, date, name], i) => ({
    rdf: i + 1, date, customerName: name,
    allFel, sumLineAll: allFel, tax: Math.round(allFel * 0.09), tafif: Math.round(allFel * 0.05),
    status: i % 2, shFactor: i + 100,
  }));

  const visitors = [
    [1, 'میلاد یعقوبی'], [2, 'رضا نادری'], [3, 'احسان کریمی'],
  ].map(([rdf, name]) => ({ rdf, name }));

  const customerCount = customers.length;
  const productCount = products.length;
  const checkCount = checks.length;
  const factorCount = factors.length;
  const visitorCount = visitors.length;
  const totalDebt = customers.reduce((s, x) => s + x.man, 0);
  const totalCredit = customers.reduce((s, x) => s + x.cred, 0);
  const totalCheckAmount = checks.reduce((s, x) => s + x.amount, 0);
  const totalSales = factors.reduce((s, x) => s + x.allFel, 0);
  const totalTax = factors.reduce((s, x) => s + x.tax, 0);
  const totalTakhfif = factors.reduce((s, x) => s + x.tafif, 0);
  const totalStock = inventory.reduce((s, x) => s + x.moj, 0);

  return {
    demo: true,
    connection: 'حالت نمایشی (DEMO) — Atiran2 • 37.143.147.19:1433',
    summary: {
      customerCount, productCount, checkCount, factorCount, visitorCount,
      totalDebt, totalCredit, totalCheckAmount, totalSales, totalTax, totalTakhfif, totalStock,
    },
    customers,
    products,
    inventory,
    checks,
    factors,
    visitors,
  };
}

// ------------------------------------------------------------------ app
const app = express();
app.use(express.json());

app.get('/api/health', async (_req, res) => {
  if (DEMO_MODE) {
    return res.json({ ok: true, connection: 'حالت نمایشی (DEMO) — Atiran2' });
  }
  try {
    return res.json({ ok: true, connection: await testConnection() });
  } catch (error) {
    return res.status(503).json({ ok: false, error: String(error.message || error) });
  }
});

app.get('/api/dashboard', async (_req, res) => {
  if (DEMO_MODE) {
    return res.json(buildDemoDashboard());
  }
  try {
    return res.json(await dashboard());
  } catch (error) {
    console.error('[dashboard]', error);
    return res.status(500).json({
      error: String(error.message || error),
      hint: 'سرور دیتابیس از این IP مجاز نیست، یا TCP/IP و پورت 1433 فعال نیست.',
    });
  }
});

app.use(express.static(path.join(__dirname, 'public')));

app.get('*', (_req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`AtiranHamrahWeb running on http://0.0.0.0:${PORT}`);
  console.log(`Direct DB: ${dbConfig.server}:${dbConfig.port} / ${dbConfig.database} / ${dbConfig.user}`);
});
