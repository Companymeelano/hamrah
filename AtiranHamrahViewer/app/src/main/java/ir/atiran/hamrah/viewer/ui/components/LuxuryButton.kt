package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Luxurious gradient button used on the landing / login screens
 * so the app feels premium and "3D" without needing a heavy UI toolkit.
 */
@Composable
fun LuxuryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: List<Color> = listOf(Color(0xFF00C6FF), Color(0xFF0072FF), Color(0xFF7B61FF)),
    height: Int = 58,
    fontSize: Int = 17,
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .height(height.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 2.dp,
                shape = shape,
                ambientColor = colors.first().copy(alpha = 0.40f),
                spotColor = colors.first().copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(colors)
                } else {
                    Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.45f), Color.Gray.copy(alpha = 0.32f)))
                },
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), shape)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize.sp),
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.55f),
        )
    }
}
