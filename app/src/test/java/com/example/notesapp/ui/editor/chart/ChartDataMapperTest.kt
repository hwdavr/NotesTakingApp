package com.example.notesapp.ui.editor.chart

import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartDataMapperTest {
    @Test
    fun testSelectedColumnMappingSkipsInvalidPairs() {
        val chart = EditorBlock.ChartBlock(
            id = "chart-2",
            chartType = ChartType.BAR,
            columnIds = listOf("category", "revenue", "cost"),
            selectedColumnId = "cost",
            rows = listOf(
                row("Category", "Revenue", "Cost"),
                row("Jan", "10", "8"),
                row("Feb", "invalid", "9"),
                row("", "20", "7"),
                row("Mar", "12", "NaN"),
                row("Apr", "15", "-5")
            )
        )

        val data = ChartTableParser.parse(chart)

        assertEquals(listOf("Jan", "Feb", "Apr"), data.points.map { it.category })
        assertEquals(listOf(8f, 9f, -5f), data.points.map { it.value })
        assertEquals("cost", data.selectedColumnId)
    }

    @Test
    fun selectedColumnMappingSkipsInvalidPairs() {
        val chart = EditorBlock.ChartBlock(
            id = "chart-1",
            chartType = ChartType.BAR,
            columnIds = listOf("category", "value"),
            selectedColumnId = "value",
            rows = listOf(
                row("Category", "Value"),
                row("Jan", "10"),
                row("Feb", "invalid"),
                row("", "20"),
                row("Mar", "-5")
            )
        )

        assertEquals(listOf("Jan", "Mar"), ChartTableParser.parse(chart).points.map { it.category })
        assertEquals(listOf(10f, -5f), ChartTableParser.parse(chart).points.map { it.value })
    }

    @Test
    fun pieMappingSkipsNonPositiveValues() {
        val chart = EditorBlock.ChartBlock(
            chartType = ChartType.PIE,
            columnIds = listOf("category", "value"),
            selectedColumnId = "value",
            rows = listOf(row("Category", "Value"), row("A", "0"), row("B", "-1"), row("C", "2"))
        )

        assertEquals(listOf("C"), ChartTableParser.parse(chart).points.map { it.category })
    }

    private fun row(vararg values: String): List<List<RichText>> = values.map { value -> listOf(RichText(value)) }
}
