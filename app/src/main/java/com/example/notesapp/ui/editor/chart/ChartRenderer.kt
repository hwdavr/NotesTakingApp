package com.example.notesapp.ui.editor.chart

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.notesapp.ui.editor.mapper.ChartData
import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import kotlin.math.max
import kotlin.math.min

data class ChartBitmapColors(
    val background: Int,
    val primary: Int,
    val text: Int,
    val grid: Int
)

object ChartBitmapRenderer {
    private const val DEFAULT_WIDTH = 720
    private const val DEFAULT_HEIGHT = 420

    fun render(
        block: EditorBlock.ChartBlock,
        width: Int = DEFAULT_WIDTH,
        height: Int = DEFAULT_HEIGHT,
        selectedPointIndex: Int? = null,
        colors: ChartBitmapColors = ChartBitmapColors(
            background = android.graphics.Color.WHITE,
            primary = android.graphics.Color.rgb(124, 108, 242),
            text = android.graphics.Color.rgb(25, 22, 39),
            grid = android.graphics.Color.rgb(231, 235, 240)
        )
    ): Bitmap {
        require(width > 0 && height > 0) { "Chart bitmap dimensions must be positive" }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(
            canvas = Canvas(bitmap),
            data = ChartTableParser.parse(block),
            width = width.toFloat(),
            height = height.toFloat(),
            colors = colors,
            selectedPointIndex = selectedPointIndex
        )
        return bitmap
    }

    fun draw(
        canvas: Canvas,
        data: ChartData,
        width: Float,
        height: Float,
        colors: ChartBitmapColors,
        selectedPointIndex: Int? = null
    ) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.text
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        fillPaint.color = colors.background
        canvas.drawRect(0f, 0f, width, height, fillPaint)
        if (data.points.isEmpty()) return
        when (data.chartType) {
            ChartType.BAR -> drawBar(
                canvas,
                data,
                width,
                height,
                colors,
                fillPaint,
                strokePaint,
                textPaint,
                selectedPointIndex
            )
            ChartType.LINE -> drawLine(
                canvas,
                data,
                width,
                height,
                colors,
                fillPaint,
                strokePaint,
                textPaint,
                selectedPointIndex
            )
            ChartType.PIE -> drawPie(
                canvas,
                data,
                width,
                height,
                colors,
                fillPaint,
                textPaint,
                selectedPointIndex
            )
        }
    }

    private fun drawBar(
        canvas: Canvas,
        data: ChartData,
        width: Float,
        height: Float,
        colors: ChartBitmapColors,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        selectedPointIndex: Int?
    ) {
        val chart = chartBounds(width, height)
        val maxValue = max(1f, data.points.maxOf { it.value })
        drawGrid(canvas, chart, maxValue, colors, strokePaint, textPaint)
        val slot = chart.width() / data.points.size
        val barWidth = slot * 0.58f
        data.points.forEachIndexed { index, point ->
            val left = chart.left + slot * index + (slot - barWidth) / 2f
            val top = chart.bottom - chart.height() * (point.value / maxValue)
            fillPaint.color = colors.primary
            canvas.drawRoundRect(RectF(left, top, left + barWidth, chart.bottom), 8f, 8f, fillPaint)
            if (index == selectedPointIndex) {
                strokePaint.color = colors.text
                strokePaint.strokeWidth = 3f
                canvas.drawRoundRect(RectF(left, top, left + barWidth, chart.bottom), 8f, 8f, strokePaint)
            }
            textPaint.color = colors.text
            canvas.drawText(point.value.cleanNumber(), left + barWidth / 2f, top - 8f, textPaint)
            canvas.drawText(point.category, left + barWidth / 2f, chart.bottom + 28f, textPaint)
        }
    }

    private fun drawLine(
        canvas: Canvas,
        data: ChartData,
        width: Float,
        height: Float,
        colors: ChartBitmapColors,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        selectedPointIndex: Int?
    ) {
        val chart = chartBounds(width, height)
        val maxValue = max(1f, data.points.maxOf { it.value })
        drawGrid(canvas, chart, maxValue, colors, strokePaint, textPaint)
        val path = Path()
        val xStep = chart.width() / max(1, data.points.lastIndex)
        data.points.forEachIndexed { index, point ->
            val x = if (data.points.size == 1) chart.centerX() else chart.left + xStep * index
            val y = chart.bottom - chart.height() * (point.value / maxValue)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        strokePaint.color = colors.primary
        strokePaint.strokeWidth = 5f
        canvas.drawPath(path, strokePaint)
        data.points.forEachIndexed { index, point ->
            val x = if (data.points.size == 1) chart.centerX() else chart.left + xStep * index
            val y = chart.bottom - chart.height() * (point.value / maxValue)
            fillPaint.color = colors.primary
            canvas.drawCircle(x, y, 8f, fillPaint)
            if (index == selectedPointIndex) {
                strokePaint.color = colors.text
                strokePaint.strokeWidth = 3f
                canvas.drawCircle(x, y, 13f, strokePaint)
            }
            textPaint.color = colors.text
            canvas.drawText(point.category, x, chart.bottom + 28f, textPaint)
        }
    }

    private fun drawPie(
        canvas: Canvas,
        data: ChartData,
        width: Float,
        height: Float,
        colors: ChartBitmapColors,
        fillPaint: Paint,
        textPaint: Paint,
        selectedPointIndex: Int?
    ) {
        val total = data.points.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) return
        val diameter = min(width, height) * 0.66f
        val centerX = width / 2f
        val centerY = height * 0.46f
        val bounds = RectF(
            centerX - diameter / 2f,
            centerY - diameter / 2f,
            centerX + diameter / 2f,
            centerY + diameter / 2f
        )
        var startAngle = -90f
        data.points.forEachIndexed { index, point ->
            val sweep = point.value / total * 360f
            fillPaint.color = if (index % 2 == 0) colors.primary else lighten(colors.primary)
            canvas.drawArc(bounds, startAngle, sweep, true, fillPaint)
            if (index == selectedPointIndex) {
                textPaint.color = colors.text
                strokePaintForSelection(canvas, bounds, startAngle, sweep, colors.text)
            }
            startAngle += sweep
        }
        textPaint.color = colors.text
        canvas.drawText(data.title, centerX, height - 24f, textPaint)
    }

    private fun strokePaintForSelection(canvas: Canvas, bounds: RectF, startAngle: Float, sweep: Float, color: Int) {
        val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawArc(bounds, startAngle, sweep, true, selectionPaint)
    }

    private fun drawGrid(
        canvas: Canvas,
        chart: RectF,
        maxValue: Float,
        colors: ChartBitmapColors,
        strokePaint: Paint,
        textPaint: Paint
    ) {
        strokePaint.color = colors.grid
        strokePaint.strokeWidth = 1f
        textPaint.color = colors.text
        for (step in 0..4) {
            val fraction = step / 4f
            val y = chart.bottom - chart.height() * fraction
            canvas.drawLine(chart.left, y, chart.right, y, strokePaint)
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText((maxValue * fraction).cleanNumber(), chart.left - 8f, y + 6f, textPaint)
        }
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun chartBounds(width: Float, height: Float): RectF = RectF(
        64f,
        24f,
        max(65f, width - 24f),
        max(66f, height - 64f)
    )

    private fun lighten(color: Int): Int {
        val red = (android.graphics.Color.red(color) + 255) / 2
        val green = (android.graphics.Color.green(color) + 255) / 2
        val blue = (android.graphics.Color.blue(color) + 255) / 2
        return android.graphics.Color.rgb(red, green, blue)
    }

    private fun Float.cleanNumber(): String = if (this % 1f == 0f) toInt().toString() else "%.2f".format(this)
}
