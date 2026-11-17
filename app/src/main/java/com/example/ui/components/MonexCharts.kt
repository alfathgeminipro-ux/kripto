package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextMuted
import com.example.ui.theme.MonexTextPrimary
import com.example.util.formatCompactRupiah
import kotlin.math.roundToInt

@Composable
fun MonexAreaChart(
    points: List<Long>,
    labels: List<String>,
    accentColor: Color = MonexNeonLime,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return
    val progress = remember { Animatable(0f) }
    var selectedIdx by remember { mutableIntStateOf(-1) }

    LaunchedEffect(points) {
        selectedIdx = -1
        progress.snapTo(0f)
        progress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedIdx in points.indices) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MonexSurfaceElevated)
                    .border(1.dp, MonexGlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${labels.getOrNull(selectedIdx).orEmpty()} • ${formatCompactRupiah(points[selectedIdx])}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val step = size.width.toFloat() / (points.size - 1).coerceAtLeast(1)
                        selectedIdx = (offset.x / step).roundToInt().coerceIn(0, points.lastIndex)
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val minVal = points.minOrNull() ?: 0L
            val maxVal = points.maxOrNull() ?: 1L
            val span = (maxVal - minVal).coerceAtLeast(1L).toFloat()
            val topPad = 14.dp.toPx()
            val bottomPad = 14.dp.toPx()
            val usableH = (h - topPad - bottomPad).coerceAtLeast(1f)

            val coords = points.mapIndexed { idx, value ->
                val x = (idx.toFloat() / (points.size - 1)) * w
                val norm = (value - minVal).toFloat() / span
                val y = topPad + usableH * (1f - norm)
                Offset(x, y)
            }

            // Dotted baseline guide
            drawLine(
                color = Color(0x28FFFFFF),
                start = Offset(0f, h * 0.55f),
                end = Offset(w, h * 0.55f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            val strokePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                for (i in 1 until coords.size) {
                    val prev = coords[i - 1]
                    val curr = coords[i]
                    val ctrlX = (prev.x + curr.x) / 2f
                    cubicTo(ctrlX, prev.y, ctrlX, curr.y, curr.x, curr.y)
                }
            }

            val measure = PathMeasure()
            measure.setPath(strokePath, false)
            val animatedStroke = Path()
            measure.getSegment(0f, measure.length * progress.value, animatedStroke, true)

            val fillPath = Path().apply {
                addPath(animatedStroke)
                val currentEndX = coords.first().x + (coords.last().x - coords.first().x) * progress.value
                lineTo(currentEndX, h)
                lineTo(coords.first().x, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.34f), Color.Transparent)
                )
            )

            drawPath(
                path = animatedStroke,
                color = accentColor,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val highlightIdx = if (selectedIdx in coords.indices) selectedIdx else (coords.size * 0.7f).toInt().coerceIn(coords.indices)
            if (progress.value > 0.75f) {
                val pt = coords[highlightIdx]
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = pt
                )
            }
        }

        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { lbl ->
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MonexTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun MonexBarChart(
    bars: List<Float>,
    highlightIndex: Int,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
    ) {
        if (bars.isEmpty()) return@Canvas
        val count = bars.size
        val gap = 10.dp.toPx()
        val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(8.dp.toPx())
        val maxVal = bars.maxOrNull()?.coerceAtLeast(0.1f) ?: 1f

        bars.forEachIndexed { idx, value ->
            val ratio = (value / maxVal).coerceIn(0.12f, 1f) * progress.value
            val barH = size.height * ratio
            val x = idx * (barWidth + gap)
            val y = size.height - barH
            val isHighlighted = idx == highlightIndex
            drawRoundRect(
                color = if (isHighlighted) Color(0x66FFFFFF) else Color(0x24FFFFFF),
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            if (isHighlighted) {
                drawRoundRect(
                    color = MonexTextPrimary.copy(alpha = 0.65f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, 4.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun MonexSparkline(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        val w = size.width
        val h = size.height
        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val span = (maxVal - minVal).coerceAtLeast(0.01f)

        val coords = points.mapIndexed { idx, v ->
            val x = (idx.toFloat() / (points.size - 1)) * w
            val y = 6.dp.toPx() + (h - 12.dp.toPx()) * (1f - (v - minVal) / span)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val p = coords[i - 1]
                val c = coords[i]
                val cx = (p.x + c.x) / 2f
                cubicTo(cx, p.y, cx, c.y, c.x, c.y)
            }
        }

        val measure = PathMeasure()
        measure.setPath(strokePath, false)
        val animatedStroke = Path()
        measure.getSegment(0f, measure.length * progress.value, animatedStroke, true)

        val fillPath = Path().apply {
            addPath(animatedStroke)
            val endX = coords.first().x + (coords.last().x - coords.first().x) * progress.value
            lineTo(endX, h)
            lineTo(coords.first().x, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.30f), Color.Transparent)
            )
        )
        drawPath(
            path = animatedStroke,
            color = lineColor,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
