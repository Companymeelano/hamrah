package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Unique 3D topic tile used to navigate the management reports.
 * It has its own gradient, glow, glass panel, 3D icon and a soft press state.
 */
@Composable
fun ReportTopicCard(
    kind: MetricKind,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val shape = RoundedCornerShape(22.dp)
    val borderColor by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.18f),
        animationSpec = tween(220),
        label = "topicBorder",
    )
    val panel by animateColorAsState(
        targetValue = if (selected) kind.glow.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.09f),
        animationSpec = tween(220),
        label = "topicPanel",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (selected) 14.dp else 9.dp,
                shape = shape,
                ambientColor = kind.accent.copy(alpha = 0.38f),
                spotColor = kind.accent.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(panel, kind.soft.copy(alpha = 0.22f)),
                    start = Offset.Zero,
                    end = Offset(300f, 300f),
                ),
            )
            .border(BorderStroke(1.dp, borderColor), shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Metric3DIcon(kind = kind, size = 44.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = kind.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(kind.accent.copy(alpha = if (selected) 0.35f else 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (selected) "●" else "›",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
