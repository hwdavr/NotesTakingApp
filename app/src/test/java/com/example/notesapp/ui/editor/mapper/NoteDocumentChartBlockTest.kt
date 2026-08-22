package com.example.notesapp.ui.editor.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDocumentChartBlockTest {
    @Test
    fun testChartBlockRoundTripsAndPreservesUnknownBlocks() {
        val content = """
            {
              "version": 1,
              "blocks": [
                {"id":"chart-1","type":"chart","chartType":"line","title":"Sales","columnIds":["category","revenue"],"selectedColumnId":"revenue","rows":[[[{"text":"Month"}],[{"text":"Revenue"}]],[[{"text":"Jan"}],[{"text":"10"}]]]},
                {"id":"legacy-1","type":"legacy_widget","text":"Readable legacy content"}
              ]
            }
        """.trimIndent()

        val document = NoteDocument.fromContent(content)
        val chart = document.blocks[0] as EditorBlock.ChartBlock
        val restored = NoteDocument.fromContent(document.toJsonString())

        assertEquals(ChartType.LINE, chart.chartType)
        assertEquals("revenue", chart.selectedColumnId)
        assertEquals(listOf("category", "revenue"), chart.columnIds)
        assertEquals("10", (restored.blocks[0] as EditorBlock.ChartBlock).rows[1][1].joinToString("") { it.text })
        assertTrue(restored.toPlainText().contains("Readable legacy content"))
    }

    @Test
    fun testUnknownChartTypeFallsBackToBar() {
        val document = NoteDocument.fromContent(
            """
                {"blocks":[{"id":"chart-1","type":"chart","chartType":"radar","columnIds":["category","value"],"rows":[[[{"text":"Category"}],[{"text":"Value"}]],[[{"text":"A"}],[{"text":"4"}]]]}]}
            """.trimIndent()
        )

        val chart = document.blocks.single() as EditorBlock.ChartBlock

        assertEquals(ChartType.BAR, chart.chartType)
        assertEquals("4", chart.rows[1][1].joinToString("") { it.text })
    }

    @Test
    fun testMissingOrInvalidSelectedColumnFallsBackToFirstDataColumn() {
        val missing = chartJson(selectedColumn = null)
        val invalid = chartJson(selectedColumn = "deleted")

        assertEquals(
            "value",
            (NoteDocument.fromContent(missing).blocks.single() as EditorBlock.ChartBlock).selectedColumnId
        )
        assertEquals(
            "value",
            (NoteDocument.fromContent(invalid).blocks.single() as EditorBlock.ChartBlock).selectedColumnId
        )
    }

    @Test
    fun testLegacyTableBlockRemainsReadableAndUnconverted() {
        val document = NoteDocument.fromContent(
            """
                {"blocks":[{"id":"table-1","type":"table","rows":[[[{"text":"Category"}],[{"text":"Value"}]],[[{"text":"A"}],[{"text":"3"}]]]}]}
            """.trimIndent()
        )

        assertTrue(document.blocks.single() is EditorBlock.TableBlock)
        assertEquals("Category\tValue\nA\t3", document.toPlainText())
    }

    private fun chartJson(selectedColumn: String?): String {
        val selectedColumnJson = selectedColumn?.let { "\"$it\"" } ?: "null"
        return """
            {"blocks":[{"id":"chart-1","type":"chart","chartType":"bar",
            "selectedColumnId":$selectedColumnJson,"columnIds":["category","value"],
            "rows":[[[{"text":"Category"}],[{"text":"Value"}]]]}]}
        """.trimIndent().replace("\n", "")
    }
}
