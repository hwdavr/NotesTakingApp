package com.example.notesapp.ui.editor.chart

import com.example.notesapp.ui.editor.mapper.ChartPoint
import kotlin.math.max
import kotlin.math.min

data class ChartRenderRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

data class ChartRenderPoint(val x: Float, val y: Float)

data class ChartNumericDomain(val min: Float, val max: Float) {
    val range: Float get() = max - min
}

object ChartRenderGeometry {
    fun chartBounds(width: Float, height: Float): ChartRenderRect = ChartRenderRect(
        left = 64f,
        top = 24f,
        right = max(65f, width - 24f),
        bottom = max(66f, height - 64f)
    )

    fun valueDomain(points: List<ChartPoint>): ChartNumericDomain {
        if (points.isEmpty()) return ChartNumericDomain(-1f, 1f)
        val minimum = points.minOf { it.value }
        val maximum = points.maxOf { it.value }
        val lower = min(0f, minimum)
        val upper = max(0f, maximum)
        if (lower < upper) return ChartNumericDomain(lower, upper)
        val padding = max(1f, kotlin.math.abs(lower) * 0.2f)
        return ChartNumericDomain(lower - padding, upper + padding)
    }

    fun yForValue(value: Float, bounds: ChartRenderRect, domain: ChartNumericDomain): Float {
        if (domain.range <= 0f) return bounds.centerY
        val fraction = ((value - domain.min) / domain.range).coerceIn(0f, 1f)
        return bounds.bottom - bounds.height * fraction
    }

    fun barRect(
        point: ChartPoint,
        pointIndex: Int,
        pointCount: Int,
        width: Float,
        height: Float,
        domain: ChartNumericDomain = valueDomain(listOf(point))
    ): ChartRenderRect {
        val bounds = chartBounds(width, height)
        val slot = bounds.width / pointCount.coerceAtLeast(1)
        val barWidth = slot * 0.58f
        val left = bounds.left + slot * pointIndex + (slot - barWidth) / 2f
        val zeroY = yForValue(0f, bounds, domain)
        val valueY = yForValue(point.value, bounds, domain)
        return ChartRenderRect(
            left = left,
            top = min(zeroY, valueY),
            right = left + barWidth,
            bottom = max(zeroY, valueY)
        )
    }

    fun linePoint(
        point: ChartPoint,
        pointIndex: Int,
        pointCount: Int,
        width: Float,
        height: Float,
        domain: ChartNumericDomain = valueDomain(listOf(point))
    ): ChartRenderPoint {
        val bounds = chartBounds(width, height)
        val xStep = bounds.width / (pointCount - 1).coerceAtLeast(1)
        return ChartRenderPoint(
            x = if (pointCount == 1) bounds.centerX else bounds.left + xStep * pointIndex,
            y = yForValue(point.value, bounds, domain)
        )
    }

    fun piePoint(pointIndex: Int, points: List<ChartPoint>, width: Float, height: Float): ChartRenderPoint {
        val diameter = min(width, height) * 0.66f
        val centerX = width / 2f
        val centerY = height * 0.46f
        val total = points.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
        val angleBefore = points.take(pointIndex).sumOf { it.value.toDouble() }.toFloat() / total * 360f
        val sweep = points.getOrNull(pointIndex)?.value?.div(total)?.times(360f) ?: 0f
        val angleRadians = Math.toRadians((-90f + angleBefore + sweep / 2f).toDouble())
        val radius = diameter * 0.36f
        return ChartRenderPoint(
            x = centerX + kotlin.math.cos(angleRadians).toFloat() * radius,
            y = centerY + kotlin.math.sin(angleRadians).toFloat() * radius
        )
    }
}
