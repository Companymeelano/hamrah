package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranDbRepository
import ir.atiran.hamrah.viewer.data.AtiranDbSettings
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.LuxuryButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, initial: AtiranSettings) {
    var s by remember { mutableStateOf(initial) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("تنظیمات اتصال مستقیم دیتابیس") })

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "برنامه مستقیماً و فقط با SELECT به دیتابیس Atiran2 متصل می‌شود. هیچ سرویس HTTP یا پورت اضافه‌ای استفاده نمی‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text("اتصال مستقیم دیتابیس (Atiran2)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = s.dbHost,
                    onValueChange = { s = s.copy(dbHost = it) },
                    label = { Text("آدرس سرور") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = s.dbPort,
                    onValueChange = { s = s.copy(dbPort = it) },
                    label = { Text("پورت") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = s.dbName,
                onValueChange = { s = s.copy(dbName = it) },
                label = { Text("نام دیتابیس") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = s.dbUser,
                    onValueChange = { s = s.copy(dbUser = it) },
                    label = { Text("کاربر دیتابیس") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = s.dbPassword,
                    onValueChange = { s = s.copy(dbPassword = it) },
                    label = { Text("رمز دیتابیس") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                "پیش‌فرض: 37.143.147.19  •  پورت 1433  •  دیتابیس Atiran2",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ErrorBanner(testError)
            if (testing) LoadingBox()

            LuxuryButton(
                text = "ذخیره و رفتن به ورود",
                onClick = { vm.saveSettings(s) },
                enabled = !testing,
                modifier = Modifier.fillMaxWidth(),
                height = 54,
            )

            LuxuryButton(
                text = "تست اتصال مستقیم",
                onClick = {
                    testing = true
                    testError = null
                    testResult = null
                    scope.launch {
                        try {
                            val repo = AtiranDbRepository(
                                AtiranDbSettings(
                                    host = s.dbHost.trim(),
                                    port = s.dbPort.trim(),
                                    dbName = s.dbName.trim(),
                                    user = s.dbUser.trim(),
                                    password = s.dbPassword,
                                ),
                            )
                            testResult = repo.testConnection()
                            testError = null
                        } catch (e: Exception) {
                            testError = "تست اتصال مستقیم ناموفق: ${e.message}"
                            testResult = null
                        } finally {
                            testing = false
                        }
                    }
                },
                enabled = !testing,
                modifier = Modifier.fillMaxWidth(),
                height = 52,
                colors = listOf(androidx.compose.ui.graphics.Color(0xFF8EF8CD), androidx.compose.ui.graphics.Color(0xFF00C6FF)),
            )

            if (!testResult.isNullOrBlank()) {
                Text(
                    text = "✅ $testResult",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
