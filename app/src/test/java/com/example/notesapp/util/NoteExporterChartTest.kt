package com.example.notesapp.util

import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.chartAssetPath
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteExporterChartTest {
    private val exporter = NoteExporter(ApplicationProvider.getApplicationContext())

    @Test
    fun testMarkdownChartExportProducesZipPackage() {
        val note = chartNote(
            rows = listOf(
                row("Category", "Revenue", "Cost"),
                row("January", "120", "80"),
                row("February", "180", "110")
            ),
            columnIds = listOf("category", "revenue", "cost"),
            selectedColumnId = "revenue"
        )
        val output = ByteArrayOutputStream()

        exporter.exportToMarkdown(note, output)

        val entries = unzip(output.toByteArray())
        val markdown = entries["note.md"]?.toString(Charsets.UTF_8)
        assertNotNull(markdown)
        assertTrue(markdown!!.contains("# Sales report"))
        assertTrue(markdown.contains("![Sales chart](assets/chart_chart-export-1.png)"))
        assertTrue(markdown.contains("| January | 120 | 80 |"))
        assertTrue(entries[chartAssetPath("chart-export-1")]?.isNotEmpty() == true)
    }

    @Test
    fun testPdfChartExportUsesBitmapAndTableFallback() {
        val validBitmap = exporter.chartBitmapForExport(
            chartBlock(
                rows = listOf(row("Category", "Value"), row("January", "120")),
                columnIds = listOf("category", "value"),
                selectedColumnId = "value"
            ),
            width = 495,
            height = 240
        )
        val fallbackBitmap = exporter.chartBitmapForExport(
            chartBlock(
                rows = listOf(row("Category", "Value"), row("January", "invalid")),
                columnIds = listOf("category", "value"),
                selectedColumnId = "value"
            ),
            width = 495,
            height = 240
        )

        assertTrue(validBitmap != null && validBitmap.width > 0 && validBitmap.height > 0)
        assertTrue(fallbackBitmap == null)
    }

    @Test
    fun testChartExportFailurePreservesDataAndReportsLocalizedFallback() {
        val note = chartNote(
            rows = listOf(
                row("Category", "Value"),
                row("January", "not-a-number")
            ),
            columnIds = listOf("category", "value"),
            selectedColumnId = "value"
        )
        val output = ByteArrayOutputStream()

        exporter.exportToMarkdown(note, output)

        val entries = unzip(output.toByteArray())
        val markdown = entries.getValue("note.md").toString(Charsets.UTF_8)
        val reloaded = NoteDocument.fromContent(note.content)
        val chart = reloaded.blocks.filterIsInstance<EditorBlock.ChartBlock>().single()

        assertFalse(markdown.contains("!["))
        assertTrue(markdown.contains("Chart image unavailable; table data exported instead."))
        assertTrue(markdown.contains("| January | not-a-number |"))
        assertTrue(chart.rows.last().last().first().text == "not-a-number")
        assertTrue(entries.keys.none { it.endsWith(".png") })
    }

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { archive ->
            while (true) {
                val entry = archive.nextEntry ?: break
                entries[entry.name] = archive.readBytes()
                archive.closeEntry()
            }
        }
        return entries
    }

    private fun chartNote(rows: List<List<List<RichText>>>, columnIds: List<String>, selectedColumnId: String): Note =
        Note(
            id = "note-chart-export",
            title = "Sales report",
            content = NoteDocument(
                blocks = listOf(chartBlock(rows, columnIds, selectedColumnId))
            ).toJsonString(),
            createdAt = 1L,
            updatedAt = 2L
        )

    private fun chartBlock(
        rows: List<List<List<RichText>>>,
        columnIds: List<String>,
        selectedColumnId: String
    ): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "chart-export-1",
        chartType = ChartType.BAR,
        title = "Sales chart",
        rows = rows,
        columnIds = columnIds,
        selectedColumnId = selectedColumnId
    )

    private fun row(vararg values: String): List<List<RichText>> = values.map { value ->
        listOf(RichText(value))
    }
}
