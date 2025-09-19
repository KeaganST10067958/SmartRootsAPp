package com.keagan.smartroots.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun LineChart(
    values: List<Float>,
    stroke: Color,
    grid: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // grid
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
        for (i in 1..4) {
            val y = h * i / 5f
            drawLine(grid, start = Offset(8f, y), end = Offset(w - 8f, y), strokeWidth = 1f, pathEffect = dash)
        }

        if (values.isEmpty()) return@Canvas
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 1f
        val xStep = (w - 24f) / (values.size - 1).coerceAtLeast(1)

        val path = Path()
        values.forEachIndexed { i, v ->
            val x = 12f + i * xStep
            val y = (h - 12f) - (if (max - min == 0f) 0f else ((v - min) / (max - min))) * (h - 24f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = stroke, style = Stroke(width = 6f, cap = StrokeCap.Round))
    }
}
