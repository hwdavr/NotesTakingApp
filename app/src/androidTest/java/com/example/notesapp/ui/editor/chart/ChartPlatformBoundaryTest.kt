package com.example.notesapp.ui.editor.chart

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.util.NoteExporter
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChartPlatformBoundaryTest {
    @Test
    fun testProductionCanvasBitmapAndPdfDocumentBoundary() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val block = chartBlock(
            rows = listOf(
                row("Category", "Value"),
                row("January", "120"),
                row("February", "-40"),
                row("March", "invalid")
            )
        )
        val bitmap = ChartBitmapRenderer.render(
            block = block,
            width = 640,
            height = 360,
            colors = ChartBitmapColors(
                background = Color.WHITE,
                primary = Color.rgb(124, 108, 242),
                text = Color.rgb(25, 22, 39),
                grid = Color.rgb(231, 235, 240)
            )
        )
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
        assertTrue(bitmap.byteCount > 0)
        assertTrue(bitmap.hasRenderedPixels(Color.WHITE))

        listOf(ChartType.BAR, ChartType.LINE, ChartType.PIE).forEach { chartType ->
            val typeBitmap = ChartBitmapRenderer.render(
                block = block.copy(id = "platform-$chartType", chartType = chartType),
                width = 640,
                height = 360,
                colors = platformColors()
            )
            assertTrue("$chartType should render visible pixels", typeBitmap.hasRenderedPixels(Color.WHITE))
            typeBitmap.recycle()
        }

        val invalidBitmap = ChartBitmapRenderer.render(
            block = block.copy(rows = listOf(row("Category", "Value"), row("January", "invalid"))),
            width = 640,
            height = 360,
            colors = platformColors()
        )
        assertFalse(invalidBitmap.hasRenderedPixels(Color.WHITE))
        invalidBitmap.recycle()

        val domain = ChartRenderGeometry.valueDomain(ChartTableParser.parse(block).points)
        val bounds = ChartRenderGeometry.chartBounds(640f, 360f)
        val zeroY = ChartRenderGeometry.yForValue(0f, bounds, domain).toInt()
        assertTrue((-1..1).any { offset -> bitmap.getPixel(100, zeroY + offset) != Color.WHITE })

        val outputFile = File(context.cacheDir, "chart-platform-boundary.pdf")
        val note = Note(
            id = "chart-platform-note",
            title = "Chart platform boundary",
            content = NoteDocument(blocks = listOf(block)).toJsonString(),
            createdAt = 1L,
            updatedAt = 2L
        )
        FileOutputStream(outputFile).use { output -> NoteExporter(context).exportToPdf(note, output) }
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0L)
        assertPdfRegions(outputFile, expectFallback = false)

        val fallbackFile = File(context.cacheDir, "chart-platform-boundary-fallback.pdf")
        val fallbackNote = note.copy(
            id = "chart-platform-fallback-note",
            title = "Chart fallback boundary",
            content = NoteDocument(
                blocks = listOf(
                    block.copy(rows = listOf(row("Category", "Value"), row("January", "invalid")))
                )
            ).toJsonString()
        )
        FileOutputStream(fallbackFile).use { output -> NoteExporter(context).exportToPdf(fallbackNote, output) }
        assertTrue(fallbackFile.length() > 0L)
        assertPdfRegions(fallbackFile, expectFallback = true)

        outputFile.delete()
        fallbackFile.delete()
        bitmap.recycle()
    }

    private fun assertPdfRegions(file: File, expectFallback: Boolean) {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { pdf ->
                assertTrue(pdf.pageCount > 0)
                pdf.openPage(0).use { page ->
                    val pageBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    assertTrue(pageBitmap.hasRenderedPixels(Color.WHITE))
                    assertTrue(countInk(pageBitmap, 0, 0, pageBitmap.width, 120) > 20)
                    assertTrue(countInk(pageBitmap, 40, 100, pageBitmap.width - 40, 430) > 100)
                    if (expectFallback) {
                        assertTrue(countInk(pageBitmap, 40, 140, pageBitmap.width - 40, 430) > 150)
                    }
                    pageBitmap.recycle()
                }
            }
        }
    }

    private fun countInk(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Int {
        val clampedLeft = left.coerceIn(0, bitmap.width)
        val clampedTop = top.coerceIn(0, bitmap.height)
        val clampedRight = right.coerceIn(clampedLeft, bitmap.width)
        val clampedBottom = bottom.coerceIn(clampedTop, bitmap.height)
        var count = 0
        for (y in clampedTop until clampedBottom) {
            for (x in clampedLeft until clampedRight) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.alpha(pixel) > 0 &&
                    (Color.red(pixel) < 245 || Color.green(pixel) < 245 || Color.blue(pixel) < 245)
                ) {
                    count++
                }
            }
        }
        return count
    }

    private fun platformColors(): ChartBitmapColors = ChartBitmapColors(
        background = Color.WHITE,
        primary = Color.rgb(124, 108, 242),
        text = Color.rgb(25, 22, 39),
        grid = Color.rgb(231, 235, 240)
    )

    private fun chartBlock(rows: List<List<List<RichText>>>): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "platform-chart-1",
        chartType = ChartType.BAR,
        title = "Platform chart",
        rows = rows,
        columnIds = listOf("category", "value"),
        selectedColumnId = "value"
    )

    private fun row(vararg values: String): List<List<RichText>> = values.map { value ->
        listOf(RichText(value))
    }

    private fun Bitmap.hasRenderedPixels(background: Int): Boolean {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        return pixels.any { pixel -> pixel != background && Color.alpha(pixel) > 0 }
    }
}
