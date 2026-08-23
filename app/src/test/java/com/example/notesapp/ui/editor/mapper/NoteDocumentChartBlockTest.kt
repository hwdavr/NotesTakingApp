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
                {"id":"chart-1","type":"chart","chartType":"line","title":"Sales","columnIds":["category","revenue"],"selectedColumnId":"revenue","rows":[[[{"text":"Month"}],[{"text":"Revenue"}]],[[{"text":"Jan"}],[{"text":"10","marks":["bold"]}]]]},
                {"id":"legacy-1","type":"legacy_widget","text":"Readable legacy content"}
              ]
            }
        """.trimIndent()

        val document = NoteDocument.fromContent(content)
        val chart = document.blocks[0] as EditorBlock.ChartBlock
        val serialized = document.toJsonString()
        val restored = NoteDocument.fromContent(serialized)
        val restoredChart = restored.blocks[0] as EditorBlock.ChartBlock

        assertEquals("chart-1", chart.id)
        assertEquals(ChartType.LINE, chart.chartType)
        assertEquals("Sales", chart.title)
        assertEquals("revenue", chart.selectedColumnId)
        assertEquals(listOf("category", "revenue"), chart.columnIds)
        assertTrue(serialized.contains("\"chartType\":\"line\""))
        assertTrue(serialized.contains("\"id\":\"chart-1\""))
        assertTrue(serialized.contains("\"title\":\"Sales\""))
        assertTrue(serialized.contains("\"columnIds\":[\"category\",\"revenue\"]"))
        assertTrue(serialized.contains("\"selectedColumnId\":\"revenue\""))
        assertEquals("chart-1", restoredChart.id)
        assertEquals(ChartType.LINE, restoredChart.chartType)
        assertEquals("Sales", restoredChart.title)
        assertEquals(listOf("category", "revenue"), restoredChart.columnIds)
        assertEquals("revenue", restoredChart.selectedColumnId)
        assertEquals(chart.rows, restoredChart.rows)
        assertEquals("10", restoredChart.rows[1][1].joinToString("") { it.text })
        assertEquals(listOf("bold"), restoredChart.rows[1][1].single().marks)
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
