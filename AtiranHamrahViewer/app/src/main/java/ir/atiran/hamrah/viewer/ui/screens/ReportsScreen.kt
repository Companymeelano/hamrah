package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.DbCheck
import ir.atiran.hamrah.viewer.data.DbDashboard
import ir.atiran.hamrah.viewer.data.DbFactor
import ir.atiran.hamrah.viewer.data.DbCustomer
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ChartEntry
import ir.atiran.hamrah.viewer.ui.components.DonutChart
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.GradientBars
import ir.atiran.hamrah.viewer.ui.components.KpiCard
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.RadarChart
import ir.atiran.hamrah.viewer.ui.components.RankedList
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: AppViewModel, settings: AtiranSettings) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dashboard by remember { mutableStateOf<DbDashboard?>(null) }
    var connectionInfo by remember { mutableStateOf<String?>(null) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        TopAppBar(title = { Text("گزارشات مدیریتی — مستقیم از دیتابیس") })

        // Luxury branding banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F3D).copy(alpha = 0.97f)),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "پنل مدیریت Atiran2",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "Meelano Studio Design  •  Milad Yaghoobi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8EF8CD),
                )
                connectionInfo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.80f),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { load() },
                enabled = !loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("اتصال و بارگذاری")
            }
            OutlinedButton(
                onClick = { vm.goSettings() },
                enabled = !loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("تنظیمات سرور/دیتابیس")
            }
        }

        ErrorBanner(error)
        if (loading) LoadingBox()

        dashboard?.let { d -> DashboardContent(d) }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DashboardContent(d: DbDashboard) {
    val totalStock = d.inventory.sumOf { it.moj }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            emoji = "👥", title = "مشتریان", value = formatLong(d.customerCount.toLong()),
            subtitle = "مجموع بدهی: ${formatMoney(d.totalDebt)}",
            accent = Color(0xFF0072FF), modifier = Modifier.weight(1f),
        )
        KpiCard(
            emoji = "📦", title = "کالاها", value = formatLong(d.productCount.toLong()),
            subtitle = "مجموع موجودی انبار", accent = Color(0xFF6FCF97), modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            emoji = "🧾", title = "چک‌ها", value = formatMoney(d.totalCheckAmount),
            subtitle = "${d.checkCount} چک", accent = Color(0xFFFF6E7F), modifier = Modifier.weight(1f),
        )
        KpiCard(
            emoji = "🏬", title = "موجودی کل", value = formatMoney(totalStock),
            subtitle = "مجموع واحدهای انبار", accent = Color(0xFF7B61FF), modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            emoji = "💰", title = "فروش کل", value = formatMoney(d.totalSales),
            subtitle = "از فاکتورها", accent = Color(0xFFFFB86C), modifier = Modifier.weight(1f),
        )
        KpiCard(
            emoji = "💳", title = "اعتبار مشتریان", value = formatMoney(d.totalCredit),
            subtitle = "مجموع", accent = Color(0xFF4ECDC4), modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            emoji = "🧮", title = "مالیات", value = formatMoney(d.totalTax),
            subtitle = "مجموع", accent = Color(0xFFBB6BD9), modifier = Modifier.weight(1f),
        )
        KpiCard(
            emoji = "🏷️", title = "تخفیف", value = formatMoney(d.totalTakhfif),
            subtitle = "مجموع", accent = Color(0xFFFF6B6B), modifier = Modifier.weight(1f),
        )
    }

    Spacer(Modifier.height(10.dp))
    RadarChart(
        title = "تحلیل چندبُعدی داده‌ها",
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

    if (d.checks.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
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

    if (d.factors.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        GradientBars(salesByDate(d.factors), unit = "فروش")
    }

    if (d.inventory.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        GradientBars(groupInventoryByWarehouse(d.inventory.map { it.anbarName to it.moj }), unit = "موجودی")
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

private fun formatMoney(v: Double): String {
    val l = v.roundToLong()
    val s = String.format("%,d", l)
    return "$s"
}

private fun formatLong(v: Long?): String = v?.let { String.format("%,d", it) } ?: "—"
