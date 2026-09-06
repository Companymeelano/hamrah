package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/** Simple value + optional secondary value entry used by all charts. */
data class ChartEntry(
    val label: String,
    val value: Double,
    val secondary: Double = 0.0,
)

val ChartGradient: List<Color> = listOf(
    Color(0xFF00C6FF),
    Color(0xFF0072FF),
    Color(0xFFFF6E7F),
    Color(0xFFBFE9FF),
    Color(0xFF7B61FF),
    Color(0xFFFFB86C),
    Color(0xFF6FCF97),
    Color(0xFFBB6BD9),
    Color(0xFF4ECDC4),
    Color(0xFFFF6B6B),
)

@Composable
fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
fun KpiCard(
    emoji: String,
    title: String,
    value: String,
    subtitle: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.10f),
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Pretty rounded bar chart with vertical gradients.
 * Labels are drawn below the chart for clean RTL/Persian text.
 */
@Composable
fun GradientBars(
    entries: List<ChartEntry>,
    height: Dp = 220.dp,
    unit: String = "",
    colors: List<Color> = ChartGradient,
) {
    if (entries.isEmpty()) {
        ChartCard("نمودار") { EmptyBox("داده‌ای برای نمودار نیست") }
        return
    }
    val maxVal = max(entries.maxOf { it.value }, 1.0)
    ChartCard("مقایسه") {
        Box(modifier = Modifier.fillMaxWidth().height(height)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val n = entries.size
                val slot = size.width / n
                val barW = (slot * 0.48f).coerceAtLeast(6f)
                val maxH = size.height * 0.86f
                entries.forEachIndexed { i, e ->
                    val h = ((e.value / maxVal).toFloat() * maxH).coerceAtLeast(2f)
                    val x = slot * i + (slot - barW) / 2f
                    val color = colors[i % colors.size]
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(color, color.copy(alpha = 0.35f))),
                        topLeft = Offset(x, size.height - h),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 3f, barW / 3f),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            entries.take(8).forEach { e ->
                Text(
                    text = e.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (unit.isNotBlank()) {
            Text(
                text = "واحد: $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Donut/ring chart with legend. Great for distribution reports
 * (checks by bank, customer status, inventory share, etc.).
 */
@Composable
fun DonutChart(
    entries: List<ChartEntry>,
    centerTitle: String,
    centerValue: String,
    colors: List<Color> = ChartGradient,
) {
    if (entries.isEmpty()) {
        ChartCard("توزیع — $centerTitle") { EmptyBox("داده‌ای برای نمودار نیست") }
        return
    }
    val total = entries.sumOf { it.value }.coerceAtLeast(1.0)
    ChartCard("توزیع — $centerTitle") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = size.minDimension * 0.16f
                    var start = -90f
                    entries.forEachIndexed { i, e ->
                        val sweep = ((e.value / total) * 360.0).toFloat()
                        drawArc(
                            color = colors[i % colors.size],
                            startAngle = start,
                            sweepAngle = sweep.coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = Offset(strokeW / 2f, strokeW / 2f),
                            size = Size(size.width - strokeW, size.height - strokeW),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = centerTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                    Text(
                        text = centerValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entries.take(8).forEachIndexed { i, e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(colors[i % colors.size]),
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = e.label,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = String.format("%.1f%%", e.value / total * 100),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Smooth line/area chart with gradient fill. */
@Composable
fun AreaLineChart(
    entries: List<ChartEntry>,
    height: Dp = 210.dp,
    color: Color = Color(0xFF00C6FF),
) {
    if (entries.size < 2) {
        ChartCard("روند") { EmptyBox("برای نمودار روند حداقل دو نقطه لازم است") }
        return
    }
    val maxVal = max(entries.maxOf { it.value }, 1.0)
    ChartCard("روند") {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val pts = entries.mapIndexed { i, e ->
                Offset(
                    x = size.width * i / (entries.size - 1).coerceAtLeast(1),
                    y = size.height - ((e.value / maxVal).toFloat() * size.height * 0.86f),
                )
            }
            val strokeW = 4f
            val linePath = Path().apply {
                pts.forEachIndexed { i, p ->
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
            }
            val fillPath = Path().apply {
                moveTo(pts.first().x, size.height)
                pts.forEach { p -> lineTo(p.x, p.y) }
                lineTo(pts.last().x, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.30f), Color.Transparent),
                ),
            )
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )
            pts.forEach { p ->
                drawCircle(color = Color.White, radius = strokeW * 0.9f, center = p)
                drawCircle(color = color, radius = strokeW * 0.55f, center = p)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            entries.forEach { e ->
                Text(
                    text = e.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Ranked list with gradient progress bars. */
@Composable
fun RankedList(
    title: String,
    entries: List<ChartEntry>,
    unit: String = "",
    colors: List<Color> = ChartGradient,
) {
    if (entries.isEmpty()) {
        ChartCard(title) { EmptyBox("داده‌ای برای نمایش نیست") }
        return
    }
    val maxVal = max(entries.maxOf { it.value }, 1.0)
    ChartCard(title) {
        entries.take(8).forEachIndexed { i, e ->
            val color = colors[i % colors.size]
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${i + 1}. ${e.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (unit.isBlank()) {
                            String.format("%,.0f", e.value)
                        } else {
                            "${String.format("%,.0f", e.value)} $unit"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                    )
                }
                val fraction = (e.value / maxVal).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.55f)))),
                    )
                }
            }
            if (i != entries.lastIndex) Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
