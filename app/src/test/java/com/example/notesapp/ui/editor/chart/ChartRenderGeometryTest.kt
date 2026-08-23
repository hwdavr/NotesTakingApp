package com.example.notesapp.ui.editor.chart

import com.example.notesapp.ui.editor.mapper.ChartPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartRenderGeometryTest {
    @Test
    fun mixedValuesUseZeroInclusiveDomainAndBaseline() {
        val points = listOf(
            ChartPoint(category = "Positive", value = 20f, rowIndex = 1),
            ChartPoint(category = "Negative", value = -10f, rowIndex = 2)
        )

        val domain = ChartRenderGeometry.valueDomain(points)
        val bounds = ChartRenderGeometry.chartBounds(720f, 420f)
        val zeroY = ChartRenderGeometry.yForValue(0f, bounds, domain)
        val positive = ChartRenderGeometry.barRect(points[0], 0, points.size, 720f, 420f, domain)
        val negative = ChartRenderGeometry.barRect(points[1], 1, points.size, 720f, 420f, domain)

        assertEquals(-10f, domain.min, 0f)
        assertEquals(20f, domain.max, 0f)
        assertEquals(zeroY, positive.bottom, 0.001f)
        assertEquals(zeroY, negative.top, 0.001f)
        assertTrue(positive.top < positive.bottom)
        assertTrue(negative.top < negative.bottom)
    }

    @Test
    fun allNegativeValuesKeepBarsInsideChartBounds() {
        val points = listOf(
            ChartPoint(category = "A", value = -40f, rowIndex = 1),
            ChartPoint(category = "B", value = -5f, rowIndex = 2)
        )

        val domain = ChartRenderGeometry.valueDomain(points)
        val bounds = ChartRenderGeometry.chartBounds(640f, 360f)
        val rectangles = points.mapIndexed { index, point ->
            ChartRenderGeometry.barRect(point, index, points.size, 640f, 360f, domain)
        }

        assertEquals(0f, domain.max, 0f)
        rectangles.forEach { rectangle ->
            assertTrue(rectangle.top >= bounds.top)
            assertTrue(rectangle.bottom <= bounds.bottom)
        }
    }

    @Test
    fun lineAndPieGeometryProducesPointTargetsForEveryDatum() {
        val points = listOf(
            ChartPoint(category = "A", value = 10f, rowIndex = 1),
            ChartPoint(category = "B", value = 20f, rowIndex = 2),
            ChartPoint(category = "C", value = 15f, rowIndex = 3)
        )
        val domain = ChartRenderGeometry.valueDomain(points)
        val linePoints = points.mapIndexed { index, point ->
            ChartRenderGeometry.linePoint(point, index, points.size, 720f, 420f, domain)
        }
        val piePoints = points.indices.map { index ->
            ChartRenderGeometry.piePoint(index, points, 720f, 420f)
        }

        assertEquals(3, linePoints.size)
        assertEquals(3, piePoints.size)
        assertTrue(linePoints.zipWithNext().all { (first, second) -> first.x < second.x })
        assertTrue(piePoints.all { point -> point.x in 0f..720f && point.y in 0f..420f })
    }

    @Test
    fun allZeroDomainPlacesZeroAtTheCenterAndKeepsBarsVisible() {
        val points = listOf(
            ChartPoint(category = "A", value = 0f, rowIndex = 1),
            ChartPoint(category = "B", value = 0f, rowIndex = 2)
        )
        val domain = ChartRenderGeometry.valueDomain(points)
        val bounds = ChartRenderGeometry.chartBounds(720f, 420f)
        val zeroY = ChartRenderGeometry.yForValue(0f, bounds, domain)
        val bars = points.mapIndexed { index, point ->
            ChartRenderGeometry.barRect(point, index, points.size, 720f, 420f, domain)
        }

        assertTrue(domain.min < 0f)
        assertTrue(domain.max > 0f)
        assertEquals(bounds.centerY, zeroY, 0.001f)
        bars.forEach { bar ->
            assertEquals(zeroY, bar.top, 0.001f)
            assertEquals(zeroY, bar.bottom, 0.001f)
        }
    }
}
