package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Each main management report topic owns a unique multi-dimensional 3D icon. */
enum class MetricKind(
    val title: String,
    val accent: Color,
    val glow: Color,
    val soft: Color,
) {
    DASHBOARD(
        title = "داشبورد کل",
        accent = Color(0xFF00C6FF),
        glow = Color(0xFF9BE4FF),
        soft = Color(0xFF005BB8),
    ),
    CUSTOMER(
        title = "مشتریان",
        accent = Color(0xFF0072FF),
        glow = Color(0xFF9CCBFF),
        soft = Color(0xFF003C8F),
    ),
    PRODUCT(
        title = "کالاها و قیمت‌ها",
        accent = Color(0xFF6FCF97),
        glow = Color(0xFFC4F7DC),
        soft = Color(0xFF157A4D),
    ),
    CHECK(
        title = "چک‌ها",
        accent = Color(0xFFFF6E7F),
        glow = Color(0xFFFFC1CA),
        soft = Color(0xFFA81632),
    ),
    INVOICE(
        title = "فاکتورها",
        accent = Color(0xFFFFB86C),
        glow = Color(0xFFFFE1BB),
        soft = Color(0xFF9A5E05),
    ),
    INVENTORY(
        title = "موجودی انبار",
        accent = Color(0xFF7B61FF),
        glow = Color(0xFFD5CCFF),
        soft = Color(0xFF3D2A9B),
    ),
    VISITOR(
        title = "ویزیتورها",
        accent = Color(0xFF4ECDC4),
        glow = Color(0xFFC1F7F3),
        soft = Color(0xFF0F8578),
    ),
    SALES(
        title = "فروش کل",
        accent = Color(0xFF8EF8CD),
        glow = Color(0xFFE3FFEF),
        soft = Color(0xFF0E7A55),
    ),
    CREDIT(
        title = "اعتبار مشتریان",
        accent = Color(0xFFBB6BD9),
        glow = Color(0xFFE9D4FF),
        soft = Color(0xFF5A2B7B),
    ),
    TAX(
        title = "مالیات",
        accent = Color(0xFF6C8CFF),
        glow = Color(0xFFD0D9FF),
        soft = Color(0xFF2C3F9B),
    ),
    DISCOUNT(
        title = "تخفیف",
        accent = Color(0xFFFF6B6B),
        glow = Color(0xFFFFC4C4),
        soft = Color(0xFF8E1C1C),
    ),
    DATABASE(
        title = "دیتابیس",
        accent = Color(0xFF3DDCFF),
        glow = Color(0xFFCFF5FF),
        soft = Color(0xFF0C5F7C),
    ),
}

/**
 * Glossy, isometric 3D tile icon. Every report topic gets its own
 * hand-drawn symbol (people, boxes, checks, invoices, warehouses, visitors ...)
 * so the dashboard feels rich and visually memorable instead of flat emoji.
 */
@Composable
fun Metric3DIcon(
    kind: MetricKind,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    rounded: Boolean = true,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.width
        val h = size.height
        val inset = w * 0.035f
        val corner = w * 0.24f
        val depth = w * 0.055f

        // ---- 3D depth shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.30f),
            topLeft = Offset(inset + depth, inset + depth * 0.9f),
            size = Size(w - 2 * inset, h - 2 * inset),
            cornerRadius = CornerRadius(corner, corner),
        )

        // ---- glass tile
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(kind.glow.copy(alpha = 0.98f), kind.accent, kind.soft),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            ),
            topLeft = Offset(inset, inset),
            size = Size(w - 2 * inset, h - 2 * inset),
            cornerRadius = CornerRadius(corner, corner),
        )

        // ---- top gloss reflection
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.03f)),
                startY = inset,
                endY = inset + h * 0.42f,
            ),
            topLeft = Offset(inset, inset),
            size = Size(w - 2 * inset, h * 0.42f),
            cornerRadius = CornerRadius(corner, corner),
        )

        // ---- radial inner glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(w * 0.38f, h * 0.30f),
                radius = w * 0.42f,
            ),
            radius = w * 0.42f,
            center = Offset(w * 0.38f, h * 0.30f),
        )

        when (kind) {
            MetricKind.DASHBOARD -> drawDashboardGlyph(w, h, kind)
            MetricKind.CUSTOMER -> drawCustomerGlyph(w, h, kind)
            MetricKind.PRODUCT -> drawProductGlyph(w, h, kind)
            MetricKind.CHECK -> drawCheckGlyph(w, h, kind)
            MetricKind.INVOICE -> drawInvoiceGlyph(w, h, kind)
            MetricKind.INVENTORY -> drawInventoryGlyph(w, h, kind)
            MetricKind.VISITOR -> drawVisitorGlyph(w, h, kind)
            MetricKind.SALES -> drawSalesGlyph(w, h, kind)
            MetricKind.CREDIT -> drawCreditGlyph(w, h, kind)
            MetricKind.TAX -> drawTaxGlyph(w, h, kind)
            MetricKind.DISCOUNT -> drawDiscountGlyph(w, h, kind)
            MetricKind.DATABASE -> drawDatabaseGlyph(w, h, kind)
        }
    }
}

// ---------------------------------------------------------------- glyph helpers
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDashboardGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val barColor = Color.White
    val spark = kind.glow
    val bw = w * 0.085f
    val gap = w * 0.055f
    val base = h * 0.71f
    val leftX = w * 0.24f
    val heights = listOf(h * 0.22f, h * 0.34f, h * 0.28f)
    heights.forEachIndexed { i, bh ->
        val x = leftX + i * (bw + gap)
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(barColor, Color.White.copy(alpha = 0.55f))),
            topLeft = Offset(x, base - bh),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(bw / 3f, bw / 3f),
        )
    }
    val points = listOf(
        Offset(leftX, base + h * 0.04f),
        Offset(leftX + bw, base - h * 0.01f),
        Offset(leftX + 2 * (bw + gap), base + h * 0.06f),
        Offset(leftX + 3 * (bw + gap) - gap, base - h * 0.05f),
    )
    val p = Path().apply {
        points.forEachIndexed { i, pt -> if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y) }
    }
    drawPath(p, color = spark, style = Stroke(width = bw * 0.42f, cap = StrokeCap.Round))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCustomerGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val skin = Color.White
    val dark = kind.soft.copy(alpha = 0.55f)

    fun person(cx: Float, cy: Float, scale: Float, shadow: Boolean) {
        val headR = w * 0.075f * scale
        val bodyW = w * 0.22f * scale
        val bodyH = w * 0.18f * scale
        val y = cy - bodyH * 0.55f
        val ox = if (shadow) w * 0.025f else 0f
        val c = if (shadow) dark else skin
        drawCircle(
            color = c,
            radius = headR,
            center = Offset(cx + ox, y - headR * 0.9f),
        )
        drawRoundRect(
            color = c,
            topLeft = Offset(cx - bodyW / 2f + ox, y),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(bodyW / 3f, bodyW / 3f),
        )
    }

    person(w * 0.34f, h * 0.46f, 1f, shadow = true)
    person(w * 0.66f, h * 0.52f, 0.82f, shadow = true)
    person(w * 0.34f, h * 0.46f, 1f, shadow = false)
    person(w * 0.66f, h * 0.52f, 0.82f, shadow = false)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.82f),
        topLeft = Offset(w * 0.28f, h * 0.72f),
        size = Size(w * 0.44f, h * 0.055f),
        cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProductGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val cw = w * 0.34f
    val ch = cw
    val dx = w * 0.10f
    val dy = ch * 0.18f
    val x = (w - cw - dx) / 2f
    val y = (h - ch - dy) / 2f + dy * 0.5f

    // front
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.82f))),
        topLeft = Offset(x, y + dy),
        size = Size(cw, ch),
        cornerRadius = CornerRadius(w * 0.045f, w * 0.045f),
    )
    // top face
    val top = Path().apply {
        moveTo(x, y + dy)
        lineTo(x + dx, y)
        lineTo(x + cw + dx, y)
        lineTo(x + cw, y + dy)
        close()
    }
    drawPath(top, color = kind.glow.copy(alpha = 0.95f))
    // side face
    val side = Path().apply {
        moveTo(x + cw, y + dy)
        lineTo(x + cw + dx, y)
        lineTo(x + cw + dx, y + ch)
        lineTo(x + cw, y + dy + ch)
        close()
    }
    drawPath(side, color = kind.soft.copy(alpha = 0.72f))
    // band
    drawRoundRect(
        color = kind.accent.copy(alpha = 0.62f),
        topLeft = Offset(x + cw * 0.15f, y + dy + ch * 0.42f),
        size = Size(cw * 0.70f, ch * 0.16f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCheckGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val x = w * 0.20f
    val y = h * 0.22f
    val pw = w * 0.60f
    val ph = h * 0.56f
    val corner = w * 0.06f

    drawRoundRect(
        brush = Brush.linearGradient(listOf(Color.White, kind.glow.copy(alpha = 0.96f))),
        topLeft = Offset(x, y),
        size = Size(pw, ph),
        cornerRadius = CornerRadius(corner, corner),
    )
    // folded corner
    val fold = Path().apply {
        moveTo(x + pw * 0.72f, y)
        lineTo(x + pw, y + ph * 0.30f)
        lineTo(x + pw * 0.72f, y + ph * 0.30f)
        close()
    }
    drawPath(fold, color = kind.soft.copy(alpha = 0.80f))
    // check mark
    val check = Path().apply {
        moveTo(x + pw * 0.22f, y + ph * 0.52f)
        lineTo(x + pw * 0.40f, y + ph * 0.68f)
        lineTo(x + pw * 0.78f, y + ph * 0.30f)
    }
    drawPath(check, color = kind.soft, style = Stroke(width = w * 0.065f, cap = StrokeCap.Round))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInvoiceGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val cw = w * 0.26f
    val ch = h * 0.48f
    val gap = w * 0.035f
    val x0 = w * 0.25f
    val y0 = h * 0.25f

    listOf(0.4f, 0.58f).forEachIndexed { i, alpha ->
        val x = x0 + i * (cw + gap)
        drawRoundRect(
            color = Color.White.copy(alpha = (1f - i * 0.18f)),
            topLeft = Offset(x + w * 0.035f, y0 + ch * 0.05f),
            size = Size(cw, ch),
            cornerRadius = CornerRadius(w * 0.045f, w * 0.045f),
        )
    }
    val x = x0
    val y = y0
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color.White, kind.glow.copy(alpha = 0.90f))),
        topLeft = Offset(x, y),
        size = Size(cw, ch),
        cornerRadius = CornerRadius(w * 0.045f, w * 0.045f),
    )
    repeat(3) { idx ->
        drawRoundRect(
            color = kind.soft.copy(alpha = 0.72f),
            topLeft = Offset(x + cw * 0.18f, y + ch * (0.22f + idx * 0.20f)),
            size = Size(cw * 0.64f, ch * 0.08f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInventoryGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val shelfY = h * 0.48f
    drawLine(
        color = Color.White.copy(alpha = 0.88f),
        start = Offset(w * 0.18f, shelfY),
        end = Offset(w * 0.82f, shelfY),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.88f),
        start = Offset(w * 0.18f, h * 0.70f),
        end = Offset(w * 0.82f, h * 0.70f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.88f),
        start = Offset(w * 0.18f, h * 0.34f),
        end = Offset(w * 0.82f, h * 0.34f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round,
    )

    fun box(cx: Float, cy: Float, sx: Float) {
        val bw = w * 0.18f * sx
        val bh = h * 0.11f * sx
        drawRoundRect(
            color = Color.White.copy(alpha = 0.92f),
            topLeft = Offset(cx - bw / 2f, cy - bh / 2f),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(bw * 0.12f, bw * 0.12f),
        )
        drawRoundRect(
            color = kind.accent.copy(alpha = 0.75f),
            topLeft = Offset(cx - bw * 0.24f, cy - bh * 0.12f),
            size = Size(bw * 0.48f, bh * 0.24f),
            cornerRadius = CornerRadius(bw * 0.06f, bw * 0.06f),
        )
    }

    box(w * 0.34f, h * 0.43f, 1f)
    box(w * 0.62f, h * 0.43f, 0.88f)
    box(w * 0.34f, h * 0.59f, 1.12f)
    box(w * 0.66f, h * 0.59f, 0.86f)
    box(w * 0.46f, h * 0.78f, 1.05f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVisitorGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val cx = w * 0.50f
    val cy = h * 0.34f
    val r = w * 0.27f

    drawCircle(
        color = Color.White.copy(alpha = 0.92f),
        radius = r,
        center = Offset(cx, cy),
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(kind.glow, kind.accent),
            center = Offset(cx, cy),
            radius = r,
        ),
        radius = r * 0.58f,
        center = Offset(cx, cy),
    )
    drawCircle(
        color = kind.soft,
        radius = r * 0.16f,
        center = Offset(cx, cy),
    )
    // route line
    val route = Path().apply {
        moveTo(w * 0.26f, h * 0.76f)
        cubicTo(w * 0.40f, h * 0.62f, w * 0.58f, h * 0.88f, w * 0.74f, h * 0.66f)
    }
    drawPath(
        route,
        color = Color.White.copy(alpha = 0.88f),
        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round),
    )
    drawCircle(color = Color.White, radius = w * 0.045f, center = Offset(w * 0.26f, h * 0.76f))
    drawCircle(color = kind.glow, radius = w * 0.055f, center = Offset(w * 0.74f, h * 0.66f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSalesGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val bw = w * 0.15f
    val dx = w * 0.065f
    val dy = w * 0.055f
    val base = h * 0.73f
    val x0 = w * 0.24f
    val heights = listOf(h * 0.22f, h * 0.38f, h * 0.30f)
    heights.forEachIndexed { i, bh ->
        val x = x0 + i * (bw + w * 0.055f)
        val y = base - bh
        // side
        val side = Path().apply {
            moveTo(x + bw, y)
            lineTo(x + bw + dx, y - dy)
            lineTo(x + bw + dx, base - dy)
            lineTo(x + bw, base)
            close()
        }
        drawPath(side, color = Color.White.copy(alpha = 0.60f))
        // front
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.85f))),
            topLeft = Offset(x, y),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(bw / 3f, bw / 3f),
        )
        // top
        val top = Path().apply {
            moveTo(x, y)
            lineTo(x + dx, y - dy)
            lineTo(x + bw + dx, y - dy)
            lineTo(x + bw, y)
            close()
        }
        drawPath(top, color = kind.glow)
    }
    drawRoundRect(
        color = kind.soft.copy(alpha = 0.55f),
        topLeft = Offset(x0 - w * 0.045f, base),
        size = Size(w * 0.58f, h * 0.025f),
        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCreditGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val x = w * 0.18f
    val y = h * 0.26f
    val pw = w * 0.64f
    val ph = h * 0.46f
    val corner = w * 0.07f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.22f),
        topLeft = Offset(x + w * 0.035f, y + h * 0.035f),
        size = Size(pw, ph),
        cornerRadius = CornerRadius(corner, corner),
    )
    drawRoundRect(
        brush = Brush.linearGradient(listOf(Color.White, kind.glow.copy(alpha = 0.96f))),
        topLeft = Offset(x, y),
        size = Size(pw, ph),
        cornerRadius = CornerRadius(corner, corner),
    )
    // stripe
    drawRoundRect(
        color = kind.soft.copy(alpha = 0.78f),
        topLeft = Offset(x, y + ph * 0.18f),
        size = Size(pw, ph * 0.22f),
        cornerRadius = CornerRadius(corner, corner),
    )
    // chips
    drawRoundRect(
        color = kind.accent.copy(alpha = 0.92f),
        topLeft = Offset(x + pw * 0.10f, y + ph * 0.52f),
        size = Size(pw * 0.26f, ph * 0.20f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
    )
    repeat(4) { idx ->
        drawRoundRect(
            color = Color.White.copy(alpha = 0.86f),
            topLeft = Offset(x + pw * 0.46f + idx * pw * 0.12f, y + ph * 0.58f),
            size = Size(pw * 0.055f, ph * 0.10f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTaxGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val cx = w * 0.50f
    val cy = h * 0.50f
    val r = w * 0.30f
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, kind.glow.copy(alpha = 0.95f)), center = Offset(cx - r * 0.3f, cy - r * 0.3f), radius = r * 2f),
        radius = r,
        center = Offset(cx, cy),
    )
    drawCircle(
        color = kind.soft,
        radius = r * 0.14f,
        center = Offset(cx - r * 0.36f, cy - r * 0.36f),
    )
    drawCircle(
        color = kind.soft,
        radius = r * 0.14f,
        center = Offset(cx + r * 0.36f, cy + r * 0.36f),
    )
    drawLine(
        color = kind.soft,
        start = Offset(cx - r * 0.40f, cy + r * 0.40f),
        end = Offset(cx + r * 0.40f, cy - r * 0.40f),
        strokeWidth = w * 0.055f,
        cap = StrokeCap.Round,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDiscountGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val x = w * 0.22f
    val y = h * 0.30f
    val pw = w * 0.50f
    val ph = h * 0.40f
    val holeR = w * 0.055f
    val tag = Path().apply {
        moveTo(x, y)
        lineTo(x + pw, y)
        lineTo(x + pw, y + ph)
        lineTo(x, y + ph)
        lineTo(x - w * 0.10f, y + ph / 2f)
        close()
    }
    drawPath(
        path = tag,
        brush = Brush.linearGradient(
            listOf(Color.White, kind.glow.copy(alpha = 0.95f)),
            start = Offset(x, y),
            end = Offset(x + pw, y + ph),
        ),
    )
    drawCircle(color = kind.soft, radius = holeR, center = Offset(x + w * 0.08f, y + ph / 2f))
    drawRoundRect(
        color = kind.soft.copy(alpha = 0.80f),
        topLeft = Offset(x + pw * 0.20f, y + ph * 0.26f),
        size = Size(pw * 0.42f, ph * 0.13f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
    )
    drawRoundRect(
        color = kind.soft.copy(alpha = 0.55f),
        topLeft = Offset(x + pw * 0.20f, y + ph * 0.58f),
        size = Size(pw * 0.30f, ph * 0.11f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDatabaseGlyph(
    w: Float,
    h: Float,
    kind: MetricKind,
) {
    val cx = w * 0.50f
    val rx = w * 0.24f
    val ry = w * 0.085f
    val topY = h * 0.25f
    val bottomY = h * 0.72f

    drawRect(
        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.88f), Color.White.copy(alpha = 0.60f))),
        topLeft = Offset(cx - rx, topY),
        size = Size(rx * 2f, bottomY - topY),
    )
    drawOval(
        color = Color.White.copy(alpha = 0.88f),
        topLeft = Offset(cx - rx, bottomY - ry),
        size = Size(rx * 2f, ry * 2f),
    )
    drawOval(
        brush = Brush.linearGradient(listOf(kind.glow, kind.accent), start = Offset(cx - rx, topY - ry), end = Offset(cx + rx, topY + ry)),
        topLeft = Offset(cx - rx, topY - ry),
        size = Size(rx * 2f, ry * 2f),
    )
    repeat(2) { i ->
        drawLine(
            color = kind.soft.copy(alpha = 0.58f),
            start = Offset(cx - rx * 0.72f, topY + (bottomY - topY) * (0.38f + i * 0.26f)),
            end = Offset(cx + rx * 0.72f, topY + (bottomY - topY) * (0.38f + i * 0.26f)),
            strokeWidth = w * 0.025f,
            cap = StrokeCap.Round,
        )
    }
}
