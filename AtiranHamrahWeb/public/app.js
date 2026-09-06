const state = {
  dashboard: null,
  activeView: 'overview',
};

const palette = [
  '#00c6ff',
  '#0072ff',
  '#7b61ff',
  '#ff6e7f',
  '#ffb86c',
  '#6fcf97',
  '#4ecdc4',
  '#bb6bd9',
  '#8ef8cd',
  '#ff6b6b',
];

const kindMap = {
  dash: { accent: '#00c6ff', glow: '#9be4ff', soft: '#005bb8', glyph: '◈', label: 'داشبورد کل' },
  customers: { accent: '#0072ff', glow: '#9ccbff', soft: '#003c8f', glyph: '◎', label: 'مشتریان' },
  products: { accent: '#6fcf97', glow: '#c4f7dc', soft: '#157a4d', glyph: '▣', label: 'کالاها و قیمت‌ها' },
  checks: { accent: '#ff6e7f', glow: '#ffc1ca', soft: '#a81632', glyph: '🗎', label: 'چک‌ها' },
  banks: { accent: '#ffb86c', glow: '#ffe1bb', soft: '#9a5e05', glyph: '🏦', label: 'بانک‌ها' },
  invoices: { accent: '#7b61ff', glow: '#d5ccff', soft: '#3d2a9b', glyph: '▤', label: 'فروش و فاکتورها' },
  purchases: { accent: '#4ecdc4', glow: '#c1f7f3', soft: '#0f8578', glyph: '↻', label: 'خرید و سود' },
  inventory: { accent: '#bb6bd9', glow: '#e9d4ff', soft: '#5a2b7b', glyph: '◫', label: 'موجودی انبار' },
  visitors: { accent: '#8ef8cd', glow: '#e3ffef', soft: '#0e7a55', glyph: '➤', label: 'ویزیتورها' },
  sales: { accent: '#ffb86c', glow: '#ffe1bb', soft: '#9a5e05', glyph: '📈', label: 'فروش کل' },
  credit: { accent: '#bb6bd9', glow: '#e9d4ff', soft: '#5a2b7b', glyph: '💳', label: 'اعتبار' },
  tax: { accent: '#6c8cff', glow: '#d0d9ff', soft: '#2c3f9b', glyph: '٪', label: 'مالیات' },
  discount: { accent: '#ff6b6b', glow: '#ffc4c4', soft: '#8e1c1c', glyph: '🏷', label: 'تخفیف' },
  db: { accent: '#3ddcff', glow: '#cff5ff', soft: '#0c5f7c', glyph: '◉', label: 'دیتابیس' },
  profit: { accent: '#8ef8cd', glow: '#e3ffef', soft: '#0e7a55', glyph: '≈', label: 'سود بالقوه' },
  buy: { accent: '#4ecdc4', glow: '#c1f7f3', soft: '#0f8578', glyph: '↺', label: 'ارزش خرید' },
  sell: { accent: '#ffb86c', glow: '#ffe1bb', soft: '#9a5e05', glyph: '↗', label: 'ارزش فروش' },
  lowStock: { accent: '#ff6e7f', glow: '#ffc1ca', soft: '#a81632', glyph: '▼', label: 'کم‌موجود' },
  highStock: { accent: '#6fcf97', glow: '#c4f7dc', soft: '#157a4d', glyph: '▲', label: 'پرموجود' },
  pending: { accent: '#ffb86c', glow: '#ffe1bb', soft: '#9a5e05', glyph: '◷', label: 'چک در جریان' },
  settled: { accent: '#6fcf97', glow: '#c4f7dc', soft: '#157a4d', glyph: '✓', label: 'چک تسویه' },
  creditors: { accent: '#ff6e7f', glow: '#ffc1ca', soft: '#a81632', glyph: '⚠', label: 'بدهکاران' },
};

// ---------------------------------------------------------------- utils
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));

function formatMoney(value) {
  const n = Number(value || 0);
  return Math.abs(n) >= 1 ? n.toLocaleString('en-US', { maximumFractionDigits: 0 }) : '0';
}

function formatLong(value) {
  const n = Number(value || 0);
  return n.toLocaleString('en-US', { maximumFractionDigits: 0 });
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}

function el(tag, attrs = {}, html = '') {
  const node = document.createElement(tag);
  Object.entries(attrs).forEach(([k, v]) => node.setAttribute(k, v));
  if (html) node.innerHTML = html;
  return node;
}

function svg(tag, attrs = {}) {
  const node = document.createElementNS('http://www.w3.org/2000/svg', tag);
  Object.entries(attrs).forEach(([k, v]) => node.setAttribute(k, v));
  return node;
}

function appendSvg(parent, child) {
  parent.appendChild(child);
}

function kpiCard(kindKey, value, subtitle = '') {
  const k = kindMap[kindKey] || kindMap.dash;
  const card = el('div', { class: 'kpi-card' });
  card.style.setProperty('--accent', k.accent);
  card.style.setProperty('--glow', k.glow);
  card.style.setProperty('--soft', k.soft);

  const icon = el('div', { class: 'm3d-icon' }, k.glyph);
  const body = el('div', { class: 'kpi-body' });
  body.appendChild(el('div', { class: 'kpi-title' }, escapeHtml(k.label)));
  body.appendChild(el('div', { class: 'kpi-value' }, escapeHtml(value)));
  if (subtitle) body.appendChild(el('div', { class: 'kpi-sub' }, escapeHtml(subtitle)));

  card.append(icon, body);
  return card;
}

function insightCard(kindKey, title, value) {
  const k = kindMap[kindKey] || kindMap.dash;
  const card = el('div', { class: 'insight-card' });
  card.style.setProperty('--accent', k.accent);
  card.style.setProperty('--glow', k.glow);
  card.style.setProperty('--soft', k.soft);

  const icon = el('div', { class: 'insight-icon' }, k.glyph);
  const body = el('div', { class: 'insight-body' });
  body.appendChild(el('div', { class: 'insight-title' }, escapeHtml(title)));
  body.appendChild(el('div', { class: 'insight-value' }, escapeHtml(value)));

  card.append(icon, body);
  return card;
}

// ---------------------------------------------------------------- charts
function rankedList(title, entries, unit = '') {
  const card = el('div', { class: 'list-card' });
  card.appendChild(el('h3', {}, escapeHtml(title)));

  const max = Math.max(...entries.map((e) => Number(e.value || 0)), 1);
  entries.slice(0, 10).forEach((entry, i) => {
    const color = palette[i % palette.length];
    const row = el('div', { class: 'rank-item' });

    const top = el('div', { class: 'rank-top' });
    top.appendChild(el('div', { class: 'rank-label' }, `${i + 1}. ${escapeHtml(entry.label)}`));
    top.appendChild(el('div', { class: 'rank-value', style: `color:${color}` }, `${formatMoney(entry.value)} ${unit}`.trim()));

    const bar = el('div', { class: 'rank-bar' });
    const fill = el('div', { class: 'rank-fill', style: `width:${Math.max((entry.value / max) * 100, 3)}%` });
    fill.style.setProperty('--fill', color);
    bar.appendChild(fill);

    row.append(top, bar);
    card.appendChild(row);
  });
  return card;
}

function donutChart(title, entries, centerTitle, centerValue) {
  const card = el('div', { class: 'chart-card' });
  card.appendChild(el('h3', {}, escapeHtml(title)));

  const wrap = el('div', { class: 'chart-row' });
  const svgEl = svg('svg', { viewBox: '0 0 220 170', width: '100%' });
  const total = Math.max(entries.reduce((s, e) => s + e.value, 0), 1);
  const cx = 72;
  const cy = 85;
  const r = 56;
  const strokeW = 18;
  let angle = -90;

  entries.forEach((entry, i) => {
    const sweep = (entry.value / total) * 360;
    const path = svg('path', {
      d: arcPath(cx, cy, r, angle, angle + sweep),
      fill: 'none',
      stroke: palette[i % palette.length],
      'stroke-width': strokeW,
      'stroke-linecap': 'round',
      opacity: '0.92',
    });
    appendSvg(svgEl, path);
    angle += sweep;
  });

  const center = svg('text', { x: cx, y: cy - 6, 'text-anchor': 'middle', fill: '#b8c9e8', 'font-size': '11', 'font-family': 'inherit' });
  center.textContent = centerTitle;
  const centerNum = svg('text', { x: cx, y: cy + 15, 'text-anchor': 'middle', fill: '#ffffff', 'font-size': '14', 'font-weight': '800', 'font-family': 'inherit' });
  centerNum.textContent = centerValue;
  appendSvg(svgEl, center);
  appendSvg(svgEl, centerNum);

  const legend = el('div', { class: 'legend' });
  entries.slice(0, 8).forEach((entry, i) => {
    const item = el('span');
    const dot = el('span', { class: 'legend-dot', style: `background:${palette[i % palette.length]};color:${palette[i % palette.length]}` });
    item.append(dot, ` ${escapeHtml(entry.label)} ${((entry.value / total) * 100).toFixed(1)}%`);
    legend.appendChild(item);
  });

  wrap.append(svgEl);
  card.append(wrap);
  if (entries.length) card.appendChild(legend);
  return card;
}

function arcPath(cx, cy, r, a0, a1) {
  const rad0 = (a0 * Math.PI) / 180;
  const rad1 = (a1 * Math.PI) / 180;
  const x0 = cx + r * Math.cos(rad0);
  const y0 = cy + r * Math.sin(rad0);
  const x1 = cx + r * Math.cos(rad1);
  const y1 = cy + r * Math.sin(rad1);
  const large = a1 - a0 > 180 ? 1 : 0;
  return `M ${x0} ${y0} A ${r} ${r} 0 ${large} 1 ${x1} ${y1}`;
}

function radarChart(title, axes, values) {
  const card = el('div', { class: 'chart-card' });
  card.appendChild(el('h3', {}, escapeHtml(title)));

  const svgEl = svg('svg', { viewBox: '0 0 320 300', width: '100%' });
  const cx = 160;
  const cy = 150;
  const radius = 108;
  const max = Math.max(...values, 1);
  const n = axes.length;
  const color = '#00c6ff';

  const pts = (sc) => Array.from({ length: n }, (_, i) => {
    const a = -90 + (360 / n) * i;
    const rad = (a * Math.PI) / 180;
    return [cx + radius * sc * Math.cos(rad), cy + radius * sc * Math.sin(rad)];
  });

  const dpts = pts(1).map((v) => [v[0] + 4, v[1] + 4]);
  appendSvg(svgEl, svg('polygon', { points: dpts.map((p) => p.join(',')).join(' '), fill: 'rgba(0,0,0,0.22)' }));

  [1, 2, 3, 4].forEach((ring) => {
    appendSvg(svgEl, svg('polygon', {
      points: pts(ring / 4).map((p) => p.join(',')).join(' '),
      fill: 'none',
      stroke: color,
      'stroke-opacity': ring % 2 ? '0.16' : '0.28',
      'stroke-width': '1.4',
    }));
  });

  pts(1).forEach((p) => {
    appendSvg(svgEl, svg('line', { x1: cx, y1: cy, x2: p[0], y2: p[1], stroke: color, 'stroke-opacity': '0.20', 'stroke-width': '1.2' }));
  });

  const dataPts = values.map((v, i) => {
    const a = -90 + (360 / n) * i;
    const rad = (a * Math.PI) / 180;
    const sc = Math.max(v / max, 0.04);
    return [cx + radius * sc * Math.cos(rad), cy + radius * sc * Math.sin(rad)];
  });
  appendSvg(svgEl, svg('polygon', {
    points: dataPts.map((p) => p.join(',')).join(' '),
    fill: 'rgba(0,198,255,0.32)',
    stroke: color,
    'stroke-width': '4',
    'stroke-linejoin': 'round',
  }));

  dataPts.forEach((p) => {
    appendSvg(svgEl, svg('circle', { cx: p[0], cy: p[1], r: 7, fill: '#fff' }));
    appendSvg(svgEl, svg('circle', { cx: p[0], cy: p[1], r: 4, fill: color }));
  });

  card.appendChild(svgEl);
  const legend = el('div', { class: 'legend' });
  axes.forEach((axis, i) => {
    const item = el('span');
    const dot = el('span', { class: 'legend-dot', style: `background:${palette[i % palette.length]};color:${palette[i % palette.length]}` });
    item.append(dot, ` ${escapeHtml(axis)}: ${formatMoney(values[i])}`);
    legend.appendChild(item);
  });
  card.appendChild(legend);
  return card;
}

function isometricBars(title, entries, unit = '') {
  const card = el('div', { class: 'chart-card' });
  card.appendChild(el('h3', {}, escapeHtml(title)));

  const W = 420;
  const H = 230;
  const max = Math.max(...entries.map((e) => e.value), 1);
  const n = Math.max(entries.length, 1);
  const slot = W / n;
  const barW = Math.max(slot * 0.42, 16);
  const depthX = 10;
  const depthY = 8;
  const base = H * 0.88;
  const maxH = H * 0.76;

  const svgEl = svg('svg', { viewBox: `0 0 ${W} ${H}`, width: '100%' });

  entries.slice(0, 8).forEach((entry, i) => {
    const color = palette[i % palette.length];
    const x = slot * i + (slot - barW) / 2;
    const h = Math.max((entry.value / max) * maxH, 6);
    const y = base - h;

    appendSvg(svgEl, svg('rect', {
      x: x + barW * 0.16 + depthX,
      y: base + depthY * 0.3,
      width: barW,
      height: Math.max(h * 0.05, 4),
      rx: barW * 0.22,
      fill: 'rgba(0,0,0,0.26)',
    }));

    appendSvg(svgEl, svg('polygon', {
      points: [[x + barW, y], [x + barW + depthX, y - depthY], [x + barW + depthX, base - depthY], [x + barW, base]].map((p) => p.join(',')).join(' '),
      fill: color,
      opacity: '0.42',
    }));

    appendSvg(svgEl, svg('polygon', {
      points: [[x, y], [x + depthX, y - depthY], [x + barW + depthX, y - depthY], [x + barW, y]].map((p) => p.join(',')).join(' '),
      fill: '#ffffff',
      opacity: '0.34',
    }));

    appendSvg(svgEl, svg('rect', { x, y, width: barW, height: h, rx: barW * 0.22, fill: color, opacity: '0.90' }));

    appendSvg(svgEl, svg('rect', {
      x: x + barW * 0.16,
      y: y + 2,
      width: barW * 0.30,
      height: Math.max(h * 0.40, 4),
      rx: barW * 0.12,
      fill: '#ffffff',
      opacity: '0.25',
    }));

    const label = svg('text', {
      x: x + barW / 2,
      y: H - 5,
      'text-anchor': 'middle',
      fill: '#b8c9e8',
      'font-size': '10',
      'font-family': 'inherit',
    });
    label.textContent = entry.label.length > 8 ? `${entry.label.slice(0, 7)}…` : entry.label;
    appendSvg(svgEl, label);
  });

  card.appendChild(svgEl);
  if (unit) card.appendChild(el('div', { class: 'legend' }, `واحد: ${escapeHtml(unit)}`));
  return card;
}

// ---------------------------------------------------------------- grouping helpers
function mapToChart(pairs, build) {
  const groups = new Map();
  pairs.forEach(([key, value]) => {
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(value);
  });
  return Array.from(groups.entries()).map(([key, values]) => build(key, values));
}

function salesByDate(factors) {
  return mapToChart(
    factors.map((f) => [f.date || '?', f.allFel || 0]),
    (date, values) => ({ label: date.slice(-5), value: values.reduce((s, v) => s + v, 0) }),
  ).sort((a, b) => b.label.localeCompare(a.label)).slice(0, 10);
}

function checksByBank(checks) {
  return mapToChart(
    checks.map((c) => [(c.bank || 'نامشخص').trim() || 'نامشخص', c.amount || 0]),
    (bank, values) => ({ label: bank, value: values.reduce((s, v) => s + v, 0) }),
  ).sort((a, b) => b.value - a.value);
}

function checksByStatus(checks) {
  return mapToChart(
    checks.map((c) => [`وضعیت ${c.status || 0}`, c.amount || 0]),
    (status, values) => ({ label: status, value: values.reduce((s, v) => s + v, 0) }),
  ).sort((a, b) => b.value - a.value);
}

function inventoryByWarehouse(inventory) {
  return mapToChart(
    inventory.map((r) => [r.anbarName || 'انبار', r.moj || 0]),
    (name, values) => ({ label: name, value: values.reduce((s, v) => s + v, 0) }),
  ).sort((a, b) => b.value - a.value);
}

function customersPerVisitor(d) {
  return d.visitors
    .map((v) => ({ label: v.name || `ویزیتور ${v.rdf}`, value: Number(d.customers.filter((c) => c.visRdf === v.rdf).length) }))
    .filter((e) => e.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 10);
}

function debtByVisitor(d) {
  return d.visitors
    .map((v) => ({ label: v.name || `ویزیتور ${v.rdf}`, value: d.customers.filter((c) => c.visRdf === v.rdf).reduce((s, c) => s + (c.man || 0), 0) }))
    .filter((e) => e.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 10);
}

function salesByCustomer(factors) {
  return mapToChart(
    factors.map((f) => [f.customerName || `فاکتور ${f.shFactor}`, f.allFel || 0]),
    (name, values) => ({ label: name, value: values.reduce((s, v) => s + v, 0) }),
  ).sort((a, b) => b.value - a.value);
}

function topBanksFrom(d) {
  const banks = d.summary?.topBanks;
  if (Array.isArray(banks) && banks.length) return banks.map((x) => ({ label: x.bank, value: x.value }));
  return checksByBank(d.checks);
}

function buyValueByProduct(products) {
  return products
    .map((p) => ({
      label: p.name || `کالا ${p.shka}`,
      value: (p.buyPrice || 0) * (p.mojkavah || 0),
      moj: p.mojkavah || 0,
    }))
    .filter((e) => e.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 10);
}

function profitByProduct(products) {
  return products
    .map((p) => ({
      label: p.name || `کالا ${p.shka}`,
      value: Math.max(((p.finalPrice || 0) - (p.buyPrice || 0)) * (p.mojkavah || 0), 0),
    }))
    .filter((e) => e.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 10);
}

function summaryNumber(d, key, fallback = 0) {
  return d.summary && Number.isFinite(d.summary[key]) ? d.summary[key] : fallback;
}

// ---------------------------------------------------------------- views
function renderOverview(d) {
  const hero = $('#overview-hero');
  hero.innerHTML = '';
  hero.appendChild(el('div', { class: 'm3d-badge' }, 'M'));
  const heroBody = el('div', {});
  heroBody.appendChild(el('h2', {}, 'پنل مدیریت کل Atiran2'));
  heroBody.appendChild(el('p', {}, 'مشتریان • کالاها • چک‌ها • بانک‌ها • فروش و خرید • انبار • ویزیتور'));
  heroBody.appendChild(el('p', { class: 'meta' }, escapeHtml(d.connection || '37.143.147.19:1433')));
  const tags = el('div', { class: 'hero-tags' });
  [
    `بدهی: ${formatMoney(summaryNumber(d, 'totalDebt'))}`,
    `فروش: ${formatMoney(summaryNumber(d, 'totalSales'))}`,
    `سود بالقوه: ${formatMoney(summaryNumber(d, 'potentialProfit'))}`,
  ].forEach((t) => tags.appendChild(el('span', { class: 'hero-tag' }, t)));
  heroBody.appendChild(tags);
  hero.appendChild(heroBody);

  const insights = $('#overview-insights');
  insights.innerHTML = '';
  [
    insightCard('buy', 'ارزش خرید موجودی', formatMoney(summaryNumber(d, 'buyValue'))),
    insightCard('sell', 'ارزش فروش موجودی', formatMoney(summaryNumber(d, 'sellValue'))),
    insightCard('profit', 'سود بالقوه', formatMoney(summaryNumber(d, 'potentialProfit'))),
    insightCard('lowStock', 'کالاهای کم‌موجود', formatLong(summaryNumber(d, 'lowStock'))),
    insightCard('highStock', 'کالاهای پرموجود', formatLong(summaryNumber(d, 'highStock'))),
    insightCard('creditors', 'مشتریان بدهکار', formatLong(summaryNumber(d, 'debtors'))),
  ].forEach((card) => insights.appendChild(card));

  const kpis = $('#overview-kpis');
  kpis.innerHTML = '';
  [
    kpiCard('customers', formatLong(d.summary.customerCount), `مجموع بدهی: ${formatMoney(d.summary.totalDebt)}`),
    kpiCard('products', formatLong(d.summary.productCount), 'کالا در دیتابیس'),
    kpiCard('checks', formatMoney(d.summary.totalCheckAmount), `${formatLong(d.summary.checkCount)} چک`),
    kpiCard('banks', formatLong(topBanksFrom(d).length), 'گروه بانکی'),
    kpiCard('inventory', formatMoney(d.summary.totalStock), 'جمع موجودی انبار'),
    kpiCard('sales', formatMoney(d.summary.totalSales), 'از فاکتورها'),
    kpiCard('credit', formatMoney(d.summary.totalCredit), 'مجموع اعتبار'),
    kpiCard('tax', formatMoney(d.summary.totalTax), 'مجموع مالیات'),
    kpiCard('discount', formatMoney(d.summary.totalTakhfif), 'مجموع تخفیف'),
    kpiCard('buy', formatMoney(summaryNumber(d, 'buyValue')), 'ارزش خرید'),
    kpiCard('sell', formatMoney(summaryNumber(d, 'sellValue')), 'ارزش فروش'),
    kpiCard('profit', formatMoney(summaryNumber(d, 'potentialProfit')), 'سود بالقوه'),
  ].forEach((card) => kpis.appendChild(card));

  const radar = $('#overview-radar');
  radar.innerHTML = '';
  radar.appendChild(radarChart('شبکه هشت‌بُعدی مدیریت', ['مشتری', 'کالا', 'چک', 'فاکتور', 'انبار', 'ویزیتور', 'بانک', 'سود'], [
    Math.max(d.summary.customerCount, 1),
    Math.max(d.summary.productCount, 1),
    Math.max(d.summary.checkCount, 1),
    Math.max(d.summary.factorCount, 1),
    Math.max(d.inventory.length, 1),
    Math.max(d.summary.visitorCount, 1),
    Math.max(topBanksFrom(d).length, 1),
    Math.max(summaryNumber(d, 'potentialProfit'), 1),
  ]));

  const bars = $('#overview-bars');
  bars.innerHTML = '';
  const sales = salesByDate(d.factors);
  const inv = inventoryByWarehouse(d.inventory);
  if (sales.length) bars.appendChild(isometricBars('ستون‌های سه‌بعدی فروش', sales, 'فروش'));
  if (inv.length) bars.appendChild(isometricBars('ستون‌های سه‌بعدی موجودی انبار', inv, 'موجودی'));

  const donuts = $('#overview-donuts');
  donuts.innerHTML = '';
  if (d.checks.length) {
    donuts.appendChild(donutChart('چک‌ها بر اساس بانک', checksByBank(d.checks), 'چک‌ها', formatMoney(d.summary.totalCheckAmount)));
    donuts.appendChild(donutChart('چک‌ها بر اساس وضعیت', checksByStatus(d.checks), 'وضعیت', formatLong(d.summary.checkCount)));
  }

  const ranked = $('#overview-ranked');
  ranked.innerHTML = '';
  const products = d.products.filter((p) => p.finalPrice > 0).sort((a, b) => b.finalPrice - a.finalPrice).slice(0, 8).map((p) => ({ label: p.name || `کالا ${p.shka}`, value: p.finalPrice }));
  if (products.length) ranked.appendChild(rankedList('کالاهای با ارزش بیشتر', products, 'ریال'));
  const topDebt = d.customers.filter((c) => c.man > 0).sort((a, b) => b.man - a.man).slice(0, 8).map((c) => ({ label: c.name || `شماره ${c.shmo || '؟'}`, value: c.man }));
  if (topDebt.length) ranked.appendChild(rankedList('مشتریان با بیشترین بدهی', topDebt, 'تومان'));
  const topBanks = topBanksFrom(d);
  if (topBanks.length) ranked.appendChild(rankedList('گروه‌های بانکی بر اساس چک', topBanks, 'تومان'));
}

function heroRow(viewEl, a, b) {
  const div = el('div', { class: 'kpi-grid' });
  div.appendChild(kpiCard(a.view, a.value, a.subtitle || ''));
  div.appendChild(kpiCard(b.view, b.value, b.subtitle || ''));
  viewEl.appendChild(div);
}

function renderCustomers(d) {
  const view = $('#view-customers');
  view.innerHTML = '';
  heroRow(view,
    { view: 'customers', value: formatLong(d.summary.customerCount), subtitle: 'تعداد کل' },
    { view: 'credit', value: formatMoney(d.summary.totalCredit), subtitle: 'اعتبار مشتریان' },
  );
  const debt = d.customers.filter((c) => c.man > 0).sort((a, b) => b.man - a.man).slice(0, 10).map((c) => ({ label: c.name || `شماره ${c.shmo || '؟'}`, value: c.man }));
  const credit = d.customers.filter((c) => c.cred > 0).sort((a, b) => b.cred - a.cred).slice(0, 10).map((c) => ({ label: c.name || `شماره ${c.shmo || '؟'}`, value: c.cred }));
  if (debt.length) view.appendChild(rankedList('مشتریان با بیشترین بدهی', debt, 'تومان'));
  if (credit.length) view.appendChild(rankedList('مشتریان با بیشترین اعتبار', credit, 'تومان'));
  if (debt.length) view.appendChild(isometricBars('سه‌بعدی بدهی مشتریان', debt.slice(0, 8), 'تومان'));
  const visitor = customersPerVisitor(d);
  if (visitor.length) view.appendChild(rankedList('پوشش مشتری توسط ویزیتورها', visitor, 'مشتری'));
}

function renderProducts(d) {
  const view = $('#view-products');
  view.innerHTML = '';
  heroRow(view,
    { view: 'products', value: formatLong(d.summary.productCount), subtitle: 'تعداد کالا' },
    { view: 'inventory', value: formatMoney(d.summary.totalStock), subtitle: 'جمع موجودی' },
  );
  const price = d.products.filter((p) => p.finalPrice > 0).sort((a, b) => b.finalPrice - a.finalPrice).slice(0, 10).map((p) => ({ label: p.name || `کالا ${p.shka}`, value: p.finalPrice }));
  const stock = d.products.filter((p) => p.mojkavah > 0).sort((a, b) => b.mojkavah - a.mojkavah).slice(0, 10).map((p) => ({ label: p.name || `کالا ${p.shka}`, value: p.mojkavah }));
  if (price.length) view.appendChild(rankedList('کالاهای با بیشترین قیمت فروش', price, 'ریال'));
  if (stock.length) view.appendChild(rankedList('کالاهای با بیشترین موجودی', stock, 'واحد'));
}

function renderChecks(d) {
  const view = $('#view-checks');
  view.innerHTML = '';
  heroRow(view,
    { view: 'checks', value: formatMoney(d.summary.totalCheckAmount), subtitle: 'جمع چک‌ها' },
    { view: 'db', value: formatLong(d.summary.checkCount), subtitle: 'تعداد چک' },
  );
  if (!d.checks.length) return;
  view.appendChild(isometricBars('ستون‌های سه‌بعدی مبلغ چک بر اساس بانک', checksByBank(d.checks).slice(0, 8), 'تومان'));
  view.appendChild(donutChart('توزیع چک‌ها بر اساس بانک', checksByBank(d.checks), 'بانک', formatMoney(d.summary.totalCheckAmount)));
  view.appendChild(donutChart('توزیع چک‌ها بر اساس وضعیت', checksByStatus(d.checks), 'وضعیت', formatLong(d.summary.checkCount)));
}

function renderBanks(d) {
  const view = $('#view-banks');
  view.innerHTML = '';
  const banks = topBanksFrom(d);
  heroRow(view,
    { view: 'banks', value: formatLong(banks.length), subtitle: 'گروه بانکی' },
    { view: 'checks', value: formatMoney(d.summary.totalCheckAmount), subtitle: 'جمع چک‌ها' },
  );
  view.appendChild(isometricBars('ستون‌های سه‌بعدی مبلغ چک بر اساس بانک', banks.slice(0, 8), 'تومان'));
  if (banks.length) view.appendChild(donutChart('توزیع چک‌ها بر اساس بانک', banks, 'بانک', formatMoney(d.summary.totalCheckAmount)));
  if (banks.length) view.appendChild(rankedList('اولویت بانک‌ها بر اساس مبلغ چک', banks, 'تومان'));
}

function renderInvoices(d) {
  const view = $('#view-invoices');
  view.innerHTML = '';
  heroRow(view,
    { view: 'sales', value: formatMoney(d.summary.totalSales), subtitle: 'فروش کل' },
    { view: 'invoices', value: formatLong(d.summary.factorCount), subtitle: 'تعداد فاکتور' },
  );
  const sales = salesByDate(d.factors);
  if (sales.length) {
    view.appendChild(isometricBars('ستون‌های سه‌بعدی فروش', sales, 'فروش'));
    const trend = [...sales].reverse();
    const svgCard = el('div', { class: 'chart-card' });
    svgCard.appendChild(el('h3', {}, 'روند فروش'));
    const svgEl = svg('svg', { viewBox: '0 0 420 220', width: '100%' });
    const max = Math.max(...trend.map((e) => e.value), 1);
    const pts = trend.map((e, i) => [30 + (360 / Math.max(trend.length - 1, 1)) * i, 180 - (e.value / max) * 140]);
    appendSvg(svgEl, svg('polygon', {
      points: [[pts[0][0], 190], ...pts, [pts[pts.length - 1][0], 190]].map((p) => p.join(',')).join(' '),
      fill: 'rgba(0,198,255,0.18)',
    }));
    appendSvg(svgEl, svg('polyline', {
      points: pts.map((p) => p.join(',')).join(' '),
      fill: 'none',
      stroke: '#00c6ff',
      'stroke-width': '4',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
    }));
    pts.forEach((p) => {
      appendSvg(svgEl, svg('circle', { cx: p[0], cy: p[1], r: 7, fill: '#fff' }));
      appendSvg(svgEl, svg('circle', { cx: p[0], cy: p[1], r: 4, fill: '#00c6ff' }));
    });
    svgCard.appendChild(svgEl);
    view.appendChild(svgCard);
  }
  const byCustomer = salesByCustomer(d.factors);
  if (byCustomer.length) view.appendChild(rankedList('مشتریان بر اساس مجموع فاکتور', byCustomer, 'تومان'));
}

function renderPurchases(d) {
  const view = $('#view-purchases');
  view.innerHTML = '';
  heroRow(view,
    { view: 'buy', value: formatMoney(summaryNumber(d, 'buyValue')), subtitle: 'ارزش خرید موجودی' },
    { view: 'profit', value: formatMoney(summaryNumber(d, 'potentialProfit')), subtitle: 'سود بالقوه' },
  );
  const buys = buyValueByProduct(d.products);
  if (buys.length) view.appendChild(isometricBars('ستون‌های سه‌بعدی ارزش خرید کالا', buys.slice(0, 8), 'ریال'));
  if (buys.length) view.appendChild(rankedList('کالاها بر اساس ارزش خرید', buys, 'ریال'));
  const profit = profitByProduct(d.products);
  if (profit.length) view.appendChild(rankedList('سود بالقوه کالاها', profit, 'ریال'));
  if (d.products.length) view.appendChild(isometricBars('ستون‌های سه‌بعدی ارزش فروش', d.products.filter((p) => p.finalPrice > 0).sort((a, b) => (b.finalPrice * b.mojkavah) - (a.finalPrice * a.mojkavah)).slice(0, 8).map((p) => ({ label: p.name || `کالا ${p.shka}`, value: (p.finalPrice || 0) * (p.mojkavah || 0) })), 'ریال'));
}

function renderInventory(d) {
  const view = $('#view-inventory');
  view.innerHTML = '';
  const whCount = new Set(d.inventory.map((r) => r.anbarName)).size;
  heroRow(view,
    { view: 'inventory', value: formatMoney(d.summary.totalStock), subtitle: 'جمع موجودی' },
    { view: 'db', value: formatLong(whCount), subtitle: 'تعداد انبار' },
  );
  const inv = inventoryByWarehouse(d.inventory);
  if (inv.length) view.appendChild(isometricBars('ستون‌های سه‌بعدی موجودی انبارها', inv.slice(0, 8), 'موجودی'));
  const top = d.inventory.filter((r) => r.moj > 0).sort((a, b) => b.moj - a.moj).slice(0, 10).map((r) => ({ label: r.name || `کالا ${r.shka}`, value: r.moj }));
  if (top.length) view.appendChild(rankedList('کالاهای پرموجودی', top, 'واحد'));
}

function renderVisitors(d) {
  const view = $('#view-visitors');
  view.innerHTML = '';
  const routeCount = new Set(d.customers.map((c) => c.visRdf).filter(Boolean)).size;
  heroRow(view,
    { view: 'visitors', value: formatLong(d.summary.visitorCount), subtitle: 'تعداد ویزیتور' },
    { view: 'customers', value: formatLong(routeCount), subtitle: 'مسیرهای فعال' },
  );
  const counts = customersPerVisitor(d);
  const debts = debtByVisitor(d);
  if (counts.length) view.appendChild(rankedList('ویزیتورها بر اساس تعداد مشتری', counts, 'مشتری'));
  if (debts.length) view.appendChild(rankedList('ویزیتورها بر اساس مجموع بدهی مشتری', debts, 'تومان'));
}

function renderCurrentView() {
  const d = state.dashboard;
  if (!d) return;
  switch (state.activeView) {
    case 'overview': renderOverview(d); break;
    case 'customers': renderCustomers(d); break;
    case 'products': renderProducts(d); break;
    case 'checks': renderChecks(d); break;
    case 'banks': renderBanks(d); break;
    case 'invoices': renderInvoices(d); break;
    case 'purchases': renderPurchases(d); break;
    case 'inventory': renderInventory(d); break;
    case 'visitors': renderVisitors(d); break;
    default: renderOverview(d);
  }
}

// ---------------------------------------------------------------- navigation / drawer
function closeDrawer() {
  const sidebar = $('#sidebar');
  const overlay = $('.drawer-overlay');
  if (sidebar) sidebar.classList.remove('open');
  if (overlay) overlay.classList.remove('open');
}

function setupNavigation() {
  const sidebar = $('#sidebar');
  const overlay = el('div', { class: 'drawer-overlay' });
  sidebar.insertAdjacentElement('afterend', overlay);

  const menuBtn = $('#menuBtn');
  if (menuBtn) {
    menuBtn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
      overlay.classList.toggle('open');
    });
  }

  overlay.addEventListener('click', closeDrawer);

  $$('.nav-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.activeView = btn.dataset.view;
      $$('.nav-btn').forEach((b) => b.classList.toggle('active', b === btn));
      $$('.view').forEach((v) => v.classList.remove('active'));
      const target = document.getElementById(`view-${state.activeView}`);
      if (target) target.classList.add('active');
      renderCurrentView();
      closeDrawer();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });

  const brandHome = $('#brandHome');
  if (brandHome) {
    brandHome.addEventListener('click', () => {
      const overviewBtn = document.querySelector('.nav-btn[data-view="overview"]');
      if (overviewBtn) overviewBtn.click();
    });
  }
}

// ---------------------------------------------------------------- load
async function loadDashboard({ silent = false } = {}) {
  const loading = $('#loading');
  const errorBox = $('#errorBox');
  const connectionText = $('#connectionText');
  const refreshBtn = $('#refreshBtn');

  if (!silent) {
    loading.classList.remove('hidden');
  } else {
    loading.classList.add('hidden');
  }
  errorBox.classList.add('hidden');
  refreshBtn?.classList.add('loading');

  try {
    const res = await fetch('/api/dashboard');
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'خطای ناشناخته در دریافت گزارش');

    state.dashboard = data;
    connectionText.textContent = `متصل شد: ${data.connection || 'Atiran2'}`;
    errorBox.classList.add('hidden');
    renderCurrentView();
  } catch (error) {
    errorBox.classList.remove('hidden');
    errorBox.innerHTML = `
      <strong>خطا در اتصال مستقیم به دیتابیس</strong><br>
      <span>${escapeHtml(error.message || error)}</span><br>
      <small>بررسی کنید: پورت 1433 روی SQL Server فعال باشد، IP مجاز باشد و دیتابیس Atiran2 در دسترس باشد.</small>
    `;
    connectionText.textContent = 'اتصال ناموفق — دیتابیس در دسترس نیست';
  } finally {
    loading.classList.add('hidden');
    refreshBtn?.classList.remove('loading');
  }
}

setupNavigation();

const refreshBtn = $('#refreshBtn');
refreshBtn?.addEventListener('click', () => loadDashboard({ silent: true }));

loadDashboard();
