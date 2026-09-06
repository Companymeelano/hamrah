package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.MReportLogo

private val DeepBlue = Color(0xFF071B3D)
private val DeepBlue2 = Color(0xFF0B2A5B)
private val Aqua = Color(0xFF00C6FF)
private val Purple = Color(0xFF7B61FF)
private val Mint = Color(0xFF8EF8CD)
private val White = Color.White

@Composable
fun LandingScreen(vm: AppViewModel) {
    // slow floating glow animation for a "living 3D" feel
    val transition = rememberInfiniteTransition(label = "landing")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepBlue, DeepBlue2, Color(0xFF10284A)))),
    ) {
        // decorative 3D glow blobs / bokeh
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(listOf(Aqua.copy(alpha = 0.34f * pulse), Color.Transparent)),
                radius = w * 0.42f,
                center = Offset(w * 0.08f, h * 0.12f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Purple.copy(alpha = 0.36f * pulse), Color.Transparent)),
                radius = w * 0.48f,
                center = Offset(w * 0.95f, h * 0.24f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Mint.copy(alpha = 0.24f * pulse), Color.Transparent)),
                radius = w * 0.42f,
                center = Offset(w * 0.22f, h * 0.92f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFFFB86C).copy(alpha = 0.18f * pulse), Color.Transparent)),
                radius = w * 0.36f,
                center = Offset(w * 0.88f, h * 0.90f),
            )
            // soft horizontal light band
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, White.copy(alpha = 0.05f), Color.Transparent),
                ),
                topLeft = Offset(0f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.11f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // floating 3D M / report logo
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = 0f
                        scaleX = 1f + (pulse - 0.75f) * 0.06f
                        scaleY = 1f + (pulse - 0.75f) * 0.06f
                    },
            ) {
                MReportLogo(logoSize = 185.dp)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "آتیران همراه",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = White,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xFF000000).copy(alpha = 0.55f),
                    offset = Offset(0f, 4f),
                    blurRadius = 10f,
                ),
            )
            Text(
                text = "گزارشات مدیریتی پیشرفته",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Aqua,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xFF000000).copy(alpha = 0.45f),
                    offset = Offset(0f, 3f),
                    blurRadius = 7f,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مشتریان  •  کالاها  •  چک‌ها  •  مالی  •  موجودی انبار",
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.80f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = White.copy(alpha = 0.08f),
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "اتصال از پیش تنظیم‌شده",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Mint,
                    )
                    Text(
                        text = "37.143.147.19:1433  •  AdminAn",
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "بدون نیاز به تایپ — با لمس دکمه، اتصال و احراز هویت انجام می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ErrorBanner(vm.error)

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { vm.connectWithPreset() },
                enabled = !vm.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Aqua,
                    contentColor = DeepBlue,
                ),
            ) {
                if (vm.busy) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepBlue, strokeWidth = 3.dp)
                } else {
                    Text(
                        text = "ورود و مشاهده گزارشات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            OutlinedButton(
                onClick = { vm.goSettings() },
                enabled = !vm.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = White.copy(alpha = 0.9f),
                ),
            ) {
                Text("تنظیمات اتصال / ورود دستی")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Meelano Studio Design",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Mint,
                )
                Text(
                    text = "  •  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.6f),
                )
                Text(
                    text = "Milad Yaghoobi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Aqua,
                )
            }
        }
    }
}
