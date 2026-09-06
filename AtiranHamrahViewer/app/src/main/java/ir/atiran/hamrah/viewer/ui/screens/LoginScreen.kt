package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LuxuryButton
import ir.atiran.hamrah.viewer.ui.components.MReportLogo

private val DeepBlue = Color(0xFF071B3D)
private val DeepBlue2 = Color(0xFF0B2A5B)
private val Aqua = Color(0xFF00C6FF)
private val Purple = Color(0xFF7B61FF)
private val Mint = Color(0xFF8EF8CD)
private val White = Color.White

/**
 * Luxury dark login screen for direct connection to the Atiran2 database.
 * Credentials are prefilled, so most managers only tap one gradient button.
 */
@Composable
fun LoginScreen(vm: AppViewModel, settings: AtiranSettings) {
    var dbUser by remember { mutableStateOf(settings.dbUser.ifBlank { settings.username }) }
    var dbPass by remember { mutableStateOf(settings.dbPassword.ifBlank { settings.password }) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepBlue, DeepBlue2, Color(0xFF10284A)))),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(listOf(Aqua.copy(alpha = 0.30f), Color.Transparent)),
                radius = w * 0.45f,
                center = Offset(w * 0.08f, h * 0.12f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Purple.copy(alpha = 0.32f), Color.Transparent)),
                radius = w * 0.45f,
                center = Offset(w * 0.95f, h * 0.25f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Mint.copy(alpha = 0.22f), Color.Transparent)),
                radius = w * 0.42f,
                center = Offset(w * 0.18f, h * 0.94f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MReportLogo(logoSize = 140.dp)
            Spacer(Modifier.height(14.dp))

            Text(
                text = "ورود امن به دیتابیس",
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = Shadow(Color.Black.copy(alpha = 0.55f), Offset(0f, 3f), 8f),
                ),
                fontWeight = FontWeight.ExtraBold,
                color = White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Atiran2  •  ${settings.dbHost}:${settings.dbPort}",
                style = MaterialTheme.typography.bodyMedium,
                color = Aqua,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(18.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.09f)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = Aqua,
                        focusedBorderColor = Aqua,
                        unfocusedBorderColor = White.copy(alpha = 0.55f),
                        focusedLabelColor = Mint,
                        unfocusedLabelColor = White.copy(alpha = 0.72f),
                    )
                    OutlinedTextField(
                        value = dbUser,
                        onValueChange = { dbUser = it },
                        label = { Text("کاربر دیتابیس") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = dbPass,
                        onValueChange = { dbPass = it },
                        label = { Text("رمز دیتابیس") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    ErrorBanner(error)
                    if (loading) CircularProgressIndicator(color = Aqua)

                    LuxuryButton(
                        text = "ورود به گزارشات",
                        onClick = { vm.connectWithDbUser(dbUser, dbPass) },
                        enabled = !loading && !vm.busy,
                        modifier = Modifier.fillMaxWidth(),
                        height = 56,
                        fontSize = 17,
                    )

                    LuxuryButton(
                        text = "ورود سریع با حساب پیشفرض AdminAn",
                        onClick = { vm.connectWithPreset() },
                        enabled = !loading && !vm.busy,
                        modifier = Modifier.fillMaxWidth(),
                        height = 52,
                        fontSize = 15,
                        colors = listOf(Color(0xFF8EF8CD), Color(0xFF00C6FF)),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { vm.goSettings() },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = White.copy(alpha = 0.90f)),
            ) {
                Text("تنظیمات کامل اتصال")
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = "Meelano Studio Design  •  Milad Yaghoobi",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Mint.copy(alpha = 0.9f),
            )
        }
    }
}
