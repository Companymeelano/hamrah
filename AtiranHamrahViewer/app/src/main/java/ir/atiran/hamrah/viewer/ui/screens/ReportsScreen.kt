package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.DbCheck
import ir.atiran.hamrah.viewer.data.DbDashboard
import ir.atiran.hamrah.viewer.data.DbFactor
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.AreaLineChart
import ir.atiran.hamrah.viewer.ui.components.ChartEntry
import ir.atiran.hamrah.viewer.ui.components.DonutChart
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.GradientBars
import ir.atiran.hamrah.viewer.ui.components.Isometric3DBarChart
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.MReportLogo
import ir.atiran.hamrah.viewer.ui.components.Metric3DIcon
import ir.atiran.hamrah.viewer.ui.components.MetricKind
import ir.atiran.hamrah.viewer.ui.components.RadarChart
import ir.atiran.hamrah.viewer.ui.components.RankedList
import ir.atiran.hamrah.viewer.ui.components.ReportTopicCard
import kotlinx.coroutines.launch
import kotlin.math.roundToLong
import java.util.Locale

private enum class ReportView {
    DASHBOARD,
    CUSTOMERS,
    PRODUCTS,
    CHECKS,
    INVOICES,
    INVENTORY,
    VISITORS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: AppViewModel, settings: AtiranSettings) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dashboard by remember { mutableStateOf<DbDashboard?>(null) }
    var connectionInfo by remember { mutableStateOf<String?>(null) }
    var view by remember { mutableStateOf(ReportView.DASHBOARD) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                connectionInfo = vm.dbRepository().testConnection()
                dashboard = vm.dbRepository().dashboard()
                error = null
            } catch (e: Exception) {
                error = "خطا در اتصال مستقیم به دیتابیس: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
    ) {
        TopAppBar(title = { Text("گزارشات مدیریتی — مستقیم از دیتابیس") })

        HeroPanel(connectionInfo)

        SectionNavigator(
            current = view,
            onSelect = { view = it },
        )

        Spacer(Modifier.height(8.dp))

        ErrorBanner(error)
        if (loading) LoadingBox()

        dashboard?.let { d ->
            when (view) {
                ReportView.DASHBOARD -> DashboardOverview(d)
                ReportView.CUSTOMERS -> CustomersOverview(d)
                ReportView.PRODUCTS -> ProductsOverview(d)
                ReportView.CHECKS -> ChecksOverview(d)
                ReportView.INVOICES -> InvoicesOverview(d)
                ReportView.INVENTORY -> InventoryOverview(d)
                ReportView.VISITORS -> VisitorsOverview(d)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------- hero
@Composable
private fun HeroPanel(connectionInfo: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F3D).copy(alpha = 0.98f)),
        shape = RoundedCornerShape(26.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MReportLogo(logoSize = 62.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "پنل مدیریت Atiran2",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Text(
                    text = "Meelano Studio Design  •  Milad Yaghoobi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8EF8CD),
                )
                Text(
                    text = "37.143.147.19:1433  •  اتصال مستقیم SQL Server",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f),
                )
                if (!connectionInfo.isNullOrBlank()) {
                    Text(
                        text = connectionInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7FE9FF).copy(alpha = 0.92f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- navigation
@Composable
private fun SectionNavigator(
    current: ReportView,
    onSelect: (ReportView) -> Unit,
) {
    val topics = listOf(
        ReportView.DASHBOARD to (MetricKind.DASHBOARD to "نمای کلی همه داده‌ها"),
        ReportView.CUSTOMERS to (MetricKind.CUSTOMER to "بدهی، اعتبار، مسیرها"),
        ReportView.PRODUCTS to (MetricKind.PRODUCT to "قیمت و ارزش کالاها"),
        ReportView.CHECKS to (MetricKind.CHECK to "چک‌ها، بانک، وضعیت"),
        ReportView.INVOICES to (MetricKind.INVOICE to "فروش، مالیات، تخفیف"),
        ReportView.INVENTORY to (MetricKind.INVENTORY to "موجودی انبارها"),
        ReportView.VISITORS to (MetricKind.VISITOR to "ویزیتورها و پوشش مشتری"),
    )

    Text(
        text = "موضوعات گزارش",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
    )

    val rows = topics.chunked(2)
    rows.forEachIndexed { rowIndex, rowTopics ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowTopics.forEach { (target, meta) ->
                ReportTopicCard(
                    kind = meta.first,
                    subtitle = meta.second,
                    selected = current == target,
                    onClick = { onSelect(target) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (rowTopics.size == 1) Spacer(Modifier.weight(1f))
        }
        if (rowIndex != rows.lastIndex) Spacer(Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------- overview content
@Composable
private fun DashboardOverview(d: DbDashboard) {
    val totalStock = d.inventory.sumOf { it.moj }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricKpiCard(
            kind = MetricKind.CUSTOMER,
            value = formatLong(d.customerCount.toLong()),
            subtitle = "مجموع بدهی: ${formatMoney(d.totalDebt)}",
            modifier = Modifier.weight(1f),
        )
        MetricKpiCard(
            kind = MetricKind.PRODUCT,
            value = formatLong(d.productCount.toLong()),
            subtitle = "کالا در دیتابیس",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricKpiCard(
            kind = MetricKind.CHECK,
            value = formatMoney(d.totalCheckAmount),
            subtitle = "${d.checkCount} چک",
            modifier = Modifier.weight(1f),
        )
        MetricKpiCard(
            kind = MetricKind.INVENTORY,
            value = formatMoney(totalStock),
            subtitle = "مجموع موجودی انبار",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricKpiCard(
            kind = MetricKind.SALES,
            value = formatMoney(d.totalSales),
            subtitle = "از فاکتورها",
            modifier = Modifier.weight(1f),
        )
        MetricKpiCard(
            kind = MetricKind.CREDIT,
            value = formatMoney(d.totalCredit),
            subtitle = "مجموع اعتبار",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricKpiCard(
            kind = MetricKind.TAX,
            value = formatMoney(d.totalTax),
            subtitle = "مجموع مالیات",
            modifier = Modifier.weight(1f),
        )
        MetricKpiCard(
            kind = MetricKind.DISCOUNT,
            value = formatMoney(d.totalTakhfif),
            subtitle = "مجموع تخفیف",
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(Modifier.height(10.dp))
    SectionTitle("تحلیل چندبُعدی داده‌ها")
    RadarChart(
        title = "شبکه شش‌بُعدی آتیران",
        axes = listOf("مشتری", "کالا", "چک", "فاکتور", "انبار", "ویزیتور"),
        values = listOf(
            d.customerCount.coerceAtLeast(1).toDouble(),
            d.productCount.coerceAtLeast(1).toDouble(),
            d.checkCount.coerceAtLeast(1).toDouble(),
            d.factorCount.coerceAtLeast(1).toDouble(),
            d.inventory.size.coerceAtLeast(1).toDouble(),
            d.visitorCount.coerceAtLeast(1).toDouble(),
        ),
    )

    if (d.factors.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        SectionTitle("ستون‌های سه‌بعدی فروش")
        Isometric3DBarChart(salesByDate(d.factors), unit = "فروش")
    }

    if (d.inventory.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        SectionTitle("ستون‌های سه‌بعدی موجودی")
        Isometric3DBarChart(groupInventoryByWarehouse(d.inventory.map { it.anbarName to it.moj }), unit = "موجودی")
    }

    if (d.checks.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        SectionTitle("توزیع چک‌ها")
        DonutChart(
            entries = groupChecksByBank(d.checks),
            centerTitle = "چک‌ها",
            centerValue = formatMoney(d.totalCheckAmount),
        )
        Spacer(Modifier.height(10.dp))
        DonutChart(
            entries = groupChecksByStatus(d.checks),
            centerTitle = "وضعیت",
            centerValue = "${d.checkCount}",
        )
    }

    if (d.products.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        RankedList(
            "کالاهای با ارزش بیشتر",
            d.products
                .filter { it.finalPrice > 0 }
                .sortedByDescending { it.finalPrice }
                .take(8)
                .map { ChartEntry(it.name ?: "کالا ${it.shka}", it.finalPrice) },
            unit = "ریال",
        )
    }

    if (d.customers.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        RankedList(
            "مشتریان با بیشترین بدهی",
            d.customers
                .filter { it.man != null && it.man > 0 }
                .sortedByDescending { it.man }
                .take(8)
                .map { ChartEntry(it.name ?: "شماره ${it.shmo ?: "?"}", it.man ?: 0.0) },
            unit = "تومان",
        )
        Spacer(Modifier.height(10.dp))
        RankedList(
            "مشتریان با بیشترین اعتبار",
            d.customers
                .filter { it.cred != null && it.cred > 0 }
                .sortedByDescending { it.cred }
                .take(8)
                .map { ChartEntry(it.name ?: "شماره ${it.shmo ?: "?"}", it.cred ?: 0.0) },
            unit = "تومان",
        )
    }
}

@Composable
private fun CustomersOverview(d: DbDashboard) {
    HeroMetricCard(
        kind = MetricKind.CUSTOMER,
        title = "تحلیل مشتریان",
        value = formatLong(d.customerCount.toLong()),
        subtitle = "بدهی ${formatMoney(d.totalDebt)}  •  اعتبار ${formatMoney(d.totalCredit)}",
    )

    if (d.customers.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        RankedList(
            "مشتریان با بیشترین بدهی",
            d.customers
                .filter { it.man != null && it.man > 0 }
                .sortedByDescending { it.man }
                .take(10)
                .map { ChartEntry(it.name ?: "شماره ${it.shmo ?: "?"}", it.man ?: 0.0) },
            unit = "تومان",
        )
        Spacer(Modifier.height(10.dp))
        RankedList(
            "مشتریان با بیشترین اعتبار",
            d.customers
                .filter { it.cred != null && it.cred > 0 }
                .sortedByDescending { it.cred }
                .take(10)
                .map { ChartEntry(it.name ?: "شماره ${it.shmo ?: "?"}", it.cred ?: 0.0) },
            unit = "تومان",
        )
        Spacer(Modifier.height(10.dp))
        val topDebt = d.customers
            .filter { it.man != null && it.man > 0 }
            .sortedByDescending { it.man }
            .take(8)
            .map { ChartEntry(it.name ?: "مشتری ${it.shmo ?: "?"}", it.man ?: 0.0) }
        Isometric3DBarChart(topDebt, unit = "تومان")
    }

    if (d.visitors.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        RankedList(
            "پوشش مشتری توسط ویزیتورها",
            customersPerVisitor(d),
            unit = "مشتری",
        )
    }
}

@Composable
private fun ProductsOverview(d: DbDashboard) {
    heroRow(
        MetricKind.PRODUCT to formatLong(d.productCount.toLong()),
        MetricKind.INVENTORY to formatMoney(d.inventory.sumOf { it.moj }),
    )

    if (d.products.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        RankedList(
            "کالاهای با بیشترین قیمت فروش",
            d.products
                .filter { it.finalPrice > 0 }
                .sortedByDescending { it.finalPrice }
                .take(10)
                .map { ChartEntry(it.name ?: "کالا ${it.shka}", it.finalPrice) },
            unit = "ریال",
        )
        Spacer(Modifier.height(10.dp))
        RankedList(
            "کالاهای با بیشترین موجودی",
            d.products
                .filter { it.mojkavah > 0 }
                .sortedByDescending { it.mojkavah }
                .take(10)
                .map { ChartEntry(it.name ?: "کالا ${it.shka}", it.mojkavah) },
            unit = "واحد",
        )
    }
}

@Composable
private fun ChecksOverview(d: DbDashboard) {
    heroRow(
        MetricKind.CHECK to formatMoney(d.totalCheckAmount),
        MetricKind.DATABASE to "${d.checkCount} چک",
    )

    if (d.checks.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Isometric3DBarChart(groupChecksByBank(d.checks).take(8), unit = "تومان")
        Spacer(Modifier.height(10.dp))
        DonutChart(
            entries = groupChecksByBank(d.checks),
            centerTitle = "بانک",
            centerValue = formatMoney(d.totalCheckAmount),
        )
        Spacer(Modifier.height(10.dp))
        DonutChart(
            entries = groupChecksByStatus(d.checks),
            centerTitle = "وضعیت",
            centerValue = "${d.checkCount}",
        )
    }
}

@Composable
private fun InvoicesOverview(d: DbDashboard) {
    heroRow(
        MetricKind.SALES to formatMoney(d.totalSales),
        MetricKind.INVOICE to formatLong(d.factorCount.toLong()),
    )

    if (d.factors.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        SectionTitle("روند فروش")
        AreaLineChart(salesByDate(d.factors).reversed(), color = Color(0xFF00C6FF))
        Spacer(Modifier.height(10.dp))
        SectionTitle("ستون‌های سه‌بعدی فروش")
        Isometric3DBarChart(salesByDate(d.factors), unit = "فروش")
        Spacer(Modifier.height(10.dp))
        RankedList(
            "مشتریان بر اساس مجموع فاکتور",
            salesByCustomer(d.factors),
            unit = "تومان",
        )
    }
}

@Composable
private fun InventoryOverview(d: DbDashboard) {
    heroRow(
        MetricKind.INVENTORY to formatMoney(d.inventory.sumOf { it.moj }),
        MetricKind.PRODUCT to "${d.inventory.map { it.anbarName }.distinct().count()} انبار",
    )

    if (d.inventory.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        SectionTitle("ستون‌های سه‌بعدی موجودی انبارها")
        Isometric3DBarChart(groupInventoryByWarehouse(d.inventory.map { it.anbarName to it.moj }), unit = "موجودی")
        Spacer(Modifier.height(10.dp))
        RankedList(
            "کالاهای پرموجودی",
            d.inventory
                .filter { it.moj > 0 }
                .sortedByDescending { it.moj }
                .take(10)
                .map { ChartEntry(it.name ?: "کالا ${it.shka}", it.moj) },
            unit = "واحد",
        )
    }
}

@Composable
private fun VisitorsOverview(d: DbDashboard) {
    heroRow(
        MetricKind.VISITOR to formatLong(d.visitorCount.toLong()),
        MetricKind.CUSTOMER to "${d.customers.map { it.visRdf }.distinct().count()} مسیر",
    )

    if (d.visitors.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        RankedList(
            "ویزیتورها بر اساس تعداد مشتری",
            customersPerVisitor(d),
            unit = "مشتری",
        )
        Spacer(Modifier.height(10.dp))
        RankedList(
            "ویزیتورها بر اساس مجموع بدهی مشتری",
            debtByVisitor(d),
            unit = "تومان",
        )
    }
}

// ---------------------------------------------------------------- shared small components
@Composable
private fun heroRow(
    first: Pair<MetricKind, String>,
    second: Pair<MetricKind, String>,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricKpiCard(kind = first.first, value = first.second, modifier = Modifier.weight(1f))
        MetricKpiCard(kind = second.first, value = second.second, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HeroMetricCard(
    kind: MetricKind,
    title: String,
    value: String,
    subtitle: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = kind.soft.copy(alpha = 0.20f)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Metric3DIcon(kind = kind, size = 56.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = kind.accent,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricKpiCard(
    kind: MetricKind,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = kind.soft.copy(alpha = 0.18f)),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Metric3DIcon(kind = kind, size = 46.dp)
            Text(
                text = kind.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = kind.accent,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF0072FF).copy(alpha = 0.16f), Color.Transparent)),
                    RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

// ---------------------------------------------------------------- helpers
private fun salesByDate(factors: List<DbFactor>): List<ChartEntry> =
    factors
        .map { (it.date ?: "?") to (it.allFel ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (date, values) -> ChartEntry(date.takeLast(5), values.sum()) }
        .sortedByDescending { it.label }
        .take(10)

private fun groupChecksByBank(checks: List<DbCheck>): List<ChartEntry> =
    checks
        .map { (it.bank ?: "نامشخص").trim().ifBlank { "نامشخص" } to (it.amount ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (bank, values) -> ChartEntry(bank, values.sum()) }
        .sortedByDescending { it.value }
        .take(8)

private fun groupChecksByStatus(checks: List<DbCheck>): List<ChartEntry> =
    checks
        .map { (it.status ?: 0).toString() to (it.amount ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (status, values) -> ChartEntry("وضعیت $status", values.sum()) }
        .sortedByDescending { it.value }
        .take(8)

private fun groupInventoryByWarehouse(items: List<Pair<String?, Double>>): List<ChartEntry> =
    items
        .map { (it.first ?: "انبار") to (it.second ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (name, values) -> ChartEntry(name, values.sum()) }
        .sortedByDescending { it.value }
        .take(8)

private fun customersPerVisitor(d: DbDashboard): List<ChartEntry> =
    d.visitors
        .map { v ->
            ChartEntry(
                v.name ?: "ویزیتور ${v.rdf}",
                d.customers.count { it.visRdf == v.rdf }.toDouble(),
            )
        }
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .take(10)

private fun debtByVisitor(d: DbDashboard): List<ChartEntry> =
    d.visitors
        .map { v ->
            ChartEntry(
                v.name ?: "ویزیتور ${v.rdf}",
                d.customers.filter { it.visRdf == v.rdf }.sumOf { it.man ?: 0.0 },
            )
        }
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .take(10)

private fun salesByCustomer(factors: List<DbFactor>): List<ChartEntry> =
    factors
        .map { (it.customerName ?: "فاکتور ${it.shFactor}") to (it.allFel ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (customer, values) -> ChartEntry(customer, values.sum()) }
        .sortedByDescending { it.value }
        .take(10)

private fun formatMoney(v: Double): String {
    val l = v.roundToLong()
    return String.format(Locale.US, "%,d", l)
}

private fun formatLong(v: Long?): String = v?.let { String.format(Locale.US, "%,d", it) } ?: "—"
