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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.Check
import ir.atiran.hamrah.viewer.data.Customer
import ir.atiran.hamrah.viewer.data.DTOCompany
import ir.atiran.hamrah.viewer.data.VWInventoryAnbars
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ChartEntry
import ir.atiran.hamrah.viewer.ui.components.DonutChart
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.GradientBars
import ir.atiran.hamrah.viewer.ui.components.KpiCard
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.RankedList
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

private data class ReportCounts(
    val customers: Int?,
    val products: Int?,
    val maxShMo: Int?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: AppViewModel, settings: AtiranSettings) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var company by remember { mutableStateOf<DTOCompany?>(null) }
    var counts by remember { mutableStateOf<ReportCounts?>(null) }
    var checks by remember { mutableStateOf<List<Check>?>(null) }
    var inventory by remember { mutableStateOf<List<VWInventoryAnbars>?>(null) }
    var customers by remember { mutableStateOf<List<Customer>?>(null) }
    var periods by remember { mutableStateOf<List<String>?>(null) }

    fun loadDashboard() {
        loading = true
        error = null
        scope.launch {
            try {
                val repo = vm.repository() ?: throw IllegalStateException("تنظیمات سرور ناقص است")
                val repoCustomers = mutableListOf<Customer>()
                var start = 0
                while (start <= 2000) {
                    val page = repo.customers(start, 500)
                    if (page.isEmpty()) break
                    repoCustomers += page
                    start += 500
                }
                counts = ReportCounts(
                    customers = repo.countMo(),
                    products = repo.countKa(),
                    maxShMo = repo.maxShMo(),
                )
                company = repo.companyInfo()
                checks = repo.allChecks().takeIf { it.isNotEmpty() }
                inventory = repo.inventoryAnbars(0, 10000).takeIf { it.isNotEmpty() }
                customers = repoCustomers.takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                error = e.message ?: "خطا در بارگذاری گزارش‌ها"
            } finally {
                loading = false
            }
        }
    }

    fun loadPeriods() {
        loading = true
        error = null
        scope.launch {
            try {
                periods = vm.repository()?.periods() ?: emptyList()
            } catch (e: Exception) {
                error = e.message ?: "خطا در دریافت دوره‌ها"
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
        TopAppBar(title = { Text("گزارشات مدیریتی") })

        // ---------- credit / branding ----------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2A47).copy(alpha = 0.94f)),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "داشبورد مدیریت آتیران",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "Meelano Studio Design",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8EF8CD),
                )
                Text(
                    text = "طراحی و توسعه: Milad Yaghoobi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { loadDashboard() },
                enabled = !loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("بارگذاری داشبورد")
            }
            OutlinedButton(
                onClick = { loadPeriods() },
                enabled = !loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("دوره‌ها")
            }
        }

        ErrorBanner(error)
        if (loading) LoadingBox()

        company?.let { c ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = c.Name ?: "اطلاعات شرکت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = listOfNotNull(c.Tel1, c.VisName?.let { "ویزیتور: $it" }).joinToString(" • ").ifBlank { "آدرس و تلفن از گزارش شرکت" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        counts?.let { c ->
            val totalChecks = checks?.sumOf { it.Mablagh ?: 0.0 } ?: 0.0
            val totalInventory = inventory?.sumOf { (it.Moj ?: 0.0) } ?: 0.0
            val totalDebt = customers?.sumOf { it.Man ?: 0.0 } ?: 0.0
            val totalCredit = customers?.sumOf { it.Credit ?: 0.0 } ?: 0.0

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    emoji = "👥",
                    title = "مشتریان",
                    value = formatLong(c.customers),
                    subtitle = "آخرین ShMo: ${c.maxShMo ?: "—"}",
                    accent = Color(0xFF0072FF),
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    emoji = "📦",
                    title = "کالاها",
                    value = formatLong(c.products),
                    subtitle = "تعداد اقلام پایه",
                    accent = Color(0xFF6FCF97),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    emoji = "🧾",
                    title = "چک‌ها",
                    value = formatMoney(totalChecks),
                    subtitle = "${checks?.size ?: 0} چک",
                    accent = Color(0xFFFF6E7F),
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    emoji = "🏬",
                    title = "موجودی کل",
                    value = formatMoney(totalInventory),
                    subtitle = "جمع واحدهای انبار",
                    accent = Color(0xFF7B61FF),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    emoji = "💰",
                    title = "بدهی مشتریان",
                    value = formatMoney(totalDebt),
                    subtitle = "از داده‌های بارگذاری‌شده",
                    accent = Color(0xFFFFB86C),
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    emoji = "💳",
                    title = "اعتبار مشتریان",
                    value = formatMoney(totalCredit),
                    subtitle = "از داده‌های بارگذاری‌شده",
                    accent = Color(0xFF4ECDC4),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        checks?.let { cs ->
            DonutChart(
                entries = groupChecksByBank(cs),
                centerTitle = "چک‌ها",
                centerValue = formatMoney(cs.sumOf { it.Mablagh ?: 0.0 }),
            )
            Spacer(Modifier.height(10.dp))
            DonutChart(
                entries = groupChecksByStatus(cs),
                centerTitle = "وضعیت",
                centerValue = "${cs.size}",
            )
        }

        inventory?.let { inv ->
            Spacer(Modifier.height(10.dp))
            GradientBars(groupInventoryByWarehouse(inv), unit = "واحد")
        }

        customers?.let { cs ->
            Spacer(Modifier.height(10.dp))
            RankedList("بدهی مشتریان", topCustomersByField(cs, chooseDebt = true))
            Spacer(Modifier.height(10.dp))
            RankedList("اعتبار مشتریان (سال ۱۴۰۴)", topCustomersByField(cs, chooseDebt = false))
        }

        periods?.let { ps ->
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("دوره‌های سیستم", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = ps.joinToString("، ").ifBlank { "دوره‌ای یافت نشد" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------- helpers
private fun groupChecksByBank(checks: List<Check>): List<ChartEntry> =
    checks
        .map { (it.Bank ?: "نامشخص").trim().ifBlank { "نامشخص" } to (it.Mablagh ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (bank, values) -> ChartEntry(bank, values.sum()) }
        .sortedByDescending { it.value }
        .take(8)

private fun groupChecksByStatus(checks: List<Check>): List<ChartEntry> =
    checks
        .map { (it.Status ?: 0).toString() to (it.Mablagh ?: 0.0) }
        .groupBy({ it.first }, { it.second })
        .map { (status, values) -> ChartEntry("وضعیت $status", values.sum()) }
        .sortedByDescending { it.value }
        .take(8)

private fun groupInventoryByWarehouse(items: List<VWInventoryAnbars>): List<ChartEntry> =
    items
        .map {
            val name = (it.AnbName ?: it.name ?: "انبار ${it.rdf_anbars ?: "?"}").trim()
                .ifBlank { "انبار ${it.rdf_anbars ?: "?"}" }
            name to (it.Moj ?: 0.0)
        }
        .groupBy({ it.first }, { it.second })
        .map { (name, values) -> ChartEntry(name, values.sum()) }
        .sortedByDescending { it.value }
        .take(8)

private fun topCustomersByField(customers: List<Customer>, chooseDebt: Boolean): List<ChartEntry> =
    customers
        .mapNotNull { c ->
            val v = if (chooseDebt) c.Man else c.Credit
            if (v == null || v <= 0.0) null else ChartEntry(c.Moname ?: "شماره ${c.Shmo ?: "?"}", v)
        }
        .sortedByDescending { it.value }
        .take(8)

private fun formatMoney(v: Double): String {
    val l = v.roundToLong()
    val s = String.format("%,d", l)
    return "$s تومان"
}

private fun formatLong(v: Int?): String = v?.let { String.format("%,d", it) } ?: "—"
