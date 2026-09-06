package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.screens.browse.ChecksScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.CustomersScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.GoodsScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.InventoryScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.InvoicesScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.MessagesScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.MiscScreen
import ir.atiran.hamrah.viewer.ui.screens.ReportsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel, settings: AtiranSettings) {
    val current by vm.settings.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(vm = vm, initial = current)
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("آتیران همراه — مشاهده داده‌ها") },
            actions = {
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                }
            },
        )

        Text(
            "سرور: ${settings.normalizedServer()}  •  CPUID: ${settings.cpuId}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionItem("گزارشات مدیریتی", Icons.Filled.Insights) { ReportsScreen(vm, settings) }
            SectionItem("مشتریان", Icons.Filled.Group) { CustomersScreen(vm) }
            SectionItem("کالاها و قیمت‌ها", Icons.Filled.Category) { GoodsScreen(vm) }
            SectionItem("موجودی انبارها", Icons.Filled.Inventory2) { InventoryScreen(vm) }
            SectionItem("فاکتورها", Icons.Filled.Receipt) { InvoicesScreen(vm, settings) }
            SectionItem("چک‌ها", Icons.Filled.Payments) { ChecksScreen(vm, settings) }
            SectionItem("پیام‌ها", Icons.Filled.Email) { MessagesScreen(vm) }
            SectionItem("گزارش‌ها و اطلاعات", Icons.Filled.Description) { MiscScreen(vm) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.SectionItem(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Card(
        onClick = { open = !open },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Column {
            ListItem(
                headlineContent = { Text(title) },
                leadingContent = { Icon(icon, contentDescription = null) },
                trailingContent = {
                    Icon(
                        if (open) Icons.Filled.ChevronRight else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
            )
            if (open) {
                HorizontalDivider()
                content()
            }
        }
    }
}
