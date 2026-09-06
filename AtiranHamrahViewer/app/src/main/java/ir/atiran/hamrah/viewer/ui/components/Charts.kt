package ir.atiran.hamrah.viewer.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
                    // 3D depth shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.25f),
                        topLeft = Offset(x + barW * 0.18f, size.height - h + barW * 0.20f),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 3f, barW / 3f),
                    )
                    // main glossy pillar
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.20f), color, color.copy(alpha = 0.55f))),
                        topLeft = Offset(x, size.height - h),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 3f, barW / 3f),
                    )
                    // top glass highlight
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                            startY = size.height - h,
                            endY = size.height - h + (h * 0.35f).coerceAtLeast(barW),
                        ),
                        topLeft = Offset(x, size.height - h),
                        size = Size(barW, (h * 0.35f).coerceAtLeast(barW)),
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

/** Multi-dimensional radar chart for comparing report categories. */
@Composable
fun RadarChart(
    title: String,
    axes: List<String>,
    values: List<Double>,
    color: Color = Color(0xFF00C6FF),
) {
    if (axes.isEmpty() || axes.size != values.size) {
        ChartCard(title) { EmptyBox("داده‌های نمودار نامعتبر است") }
        return
    }
    val maxVal = max(values.max(), 1.0)
    ChartCard(title) {
        Canvas(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2.15f
            val radius = size.minDimension * 0.38f
            val n = axes.size

            fun point(i: Int, r: Float): Offset {
                val angle = -90f + (360f / n) * i
                val rad = Math.toRadians(angle.toDouble())
                return Offset(cx + r * kotlin.math.cos(rad).toFloat(), cy + r * kotlin.math.sin(rad).toFloat())
            }

            // concentric rings
            for (ring in 1..4) {
                val rr = radius * ring / 4f
                val p = Path().apply {
                    for (i in 0 until n) {
                        val q = point(i, rr)
                        if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y)
                    }
                    close()
                }
                drawPath(p, color = color.copy(alpha = 0.12f), style = Stroke(width = 1.5f))
            }
            // axes
            for (i in 0 until n) {
                val q = point(i, radius)
                drawLine(color.copy(alpha = 0.18f), start = Offset(cx, cy), end = q, strokeWidth = 1.5f)
            }
            // data polygon
            val dataPath = Path().apply {
                for (i in 0 until n) {
                    val r = (values[i] / maxVal).toFloat() * radius
                    val q = point(i, r)
                    if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y)
                }
                close()
            }
            drawPath(
                dataPath,
                brush = Brush.radialGradient(
                    listOf(color.copy(alpha = 0.50f), color.copy(alpha = 0.10f)),
                    center = Offset(cx, cy),
                    radius = radius,
                ),
            )
            drawPath(dataPath, color = color, style = Stroke(width = 4f))
            for (i in 0 until n) {
                val r = (values[i] / maxVal).toFloat() * radius
                val q = point(i, r)
                drawCircle(Color.White, radius = 7f, center = q)
                drawCircle(color, radius = 4f, center = q)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            axes.forEach { label ->
                Text(
                    text = label,
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

/**
 * Creative, glossy 3D "M" mark inspired by the letter M (Meelano).
 * The M is drawn as three rounded report bars plus a sparkline, so it reads
 * both as the brand letter and as a management/reporting icon.
 */
@Composable
fun MReportLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 170.dp,
) {
    val transition = rememberInfiniteTransition(label = "logoFloat")
    val floatOffset by transition.animateFloat(
        initialValue = -7f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatOffset",
    )

    Box(modifier = modifier.size(logoSize)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = floatOffset },
        ) {
            val w = size.width
            val h = size.height
            val inset = w * 0.035f
            val corner = w * 0.22f
            val shadowOffset = w * 0.055f

            // ---- 3D depth shadow layer
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.30f),
                topLeft = Offset(inset + shadowOffset, inset + shadowOffset),
                size = Size(w - 2 * inset, h - 2 * inset),
                cornerRadius = CornerRadius(corner, corner),
            )

            // ---- main rounded square (glossy blue -> purple)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF003C8F), Color(0xFF0067E0), Color(0xFF7B61FF)),
                    start = Offset(w * 0.1f, 0f),
                    end = Offset(w * 0.9f, h),
                ),
                topLeft = Offset(inset, inset),
                size = Size(w - 2 * inset, h - 2 * inset),
                cornerRadius = CornerRadius(corner, corner),
            )

            // ---- top glass shine
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.42f), Color.White.copy(alpha = 0.0f)),
                    startY = inset,
                    endY = inset + (h - 2 * inset) * 0.5f,
                ),
                topLeft = Offset(inset, inset),
                size = Size(w - 2 * inset, (h - 2 * inset) * 0.5f),
                cornerRadius = CornerRadius(corner, corner),
            )

            // ---- the M-like report mark (three rounded bars + slanted spine)
            val cx = w / 2f
            val markTop = h * 0.28f
            val markBottom = h * 0.74f
            val barW = w * 0.11f
            val gap = w * 0.07f
            val startX = cx - barW - gap
            val endX = cx + barW + gap
            val barColor = Color(0xFFEAF6FF)
            val barColor2 = Color(0xFF9BE4FF)

            // left vertical bar
            drawRoundRect(
                brush = Brush.linearGradient(listOf(barColor, barColor2), start = Offset.Zero, end = Offset(w, h)),
                topLeft = Offset(startX, markTop),
                size = Size(barW, markBottom - markTop),
                cornerRadius = CornerRadius(barW / 3f, barW / 3f),
            )

            // right vertical bar
            drawRoundRect(
                brush = Brush.linearGradient(listOf(barColor2, barColor)),
                topLeft = Offset(endX, markTop),
                size = Size(barW, markBottom - markTop),
                cornerRadius = CornerRadius(barW / 3f, barW / 3f),
            )

            // middle slanted spine (creative M diagonal)
            val spine = Path().apply {
                moveTo(startX, markTop)
                lineTo(cx, markBottom - h * 0.08f)
                lineTo(endX, markTop)
            }
            drawPath(
                path = spine,
                color = Color.White.copy(alpha = 0.90f),
                style = Stroke(width = barW * 0.78f, cap = StrokeCap.Round),
            )

            // small sparkline inside the M
            val sparkColor = Color(0xFFFFB86C)
            val points = listOf(
                Offset(cx - barW * 1.9f, markBottom - h * 0.02f),
                Offset(cx - barW * 0.9f, markBottom - h * 0.06f),
                Offset(cx, markBottom - h * 0.04f),
                Offset(cx + barW * 0.9f, markBottom - h * 0.08f),
                Offset(cx + barW * 1.9f, markBottom - h * 0.03f),
            )
            val sparkPath = Path().apply {
                points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
            }
            drawPath(path = sparkPath, color = sparkColor, style = Stroke(width = barW * 0.32f, cap = StrokeCap.Round))
            points.forEach { p ->
                drawCircle(color = Color.White, radius = barW * 0.26f, center = p)
            }

            // small rising bar cluster (creative 3D pillar)
            val pillarColor = Color(0xFF8EF8CD)
            listOf(0.34f, 0.58f, 0.82f).forEachIndexed { idx, frac ->
                val pw = w * 0.045f
                val px = w * 0.18f + idx * (pw * 1.7f)
                val ph = h * 0.12f * frac
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(pillarColor, pillarColor.copy(alpha = 0.35f))),
                    topLeft = Offset(px, h * 0.78f - ph),
                    size = Size(pw, ph),
                    cornerRadius = CornerRadius(pw / 3f, pw / 3f),
                )
            }
        }

        // brand label inside logo (bold 3D-like)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "M",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.02f),
                )
            }
        }
    }
}
