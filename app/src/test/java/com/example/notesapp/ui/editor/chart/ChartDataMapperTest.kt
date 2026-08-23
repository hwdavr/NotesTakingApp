package com.example.notesapp.ui.editor.chart

import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.normalized
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

    @Test
    fun headerOnlyAndBlankRowsRemainEmptyWithoutDiscardingTheTableShape() {
        val chart = EditorBlock.ChartBlock(
            chartType = ChartType.LINE,
            columnIds = listOf("category", "value"),
            selectedColumnId = "value",
            rows = listOf(
                row("Category", "Value"),
                row("", ""),
                row(" ", "not-a-number")
            )
        )

        val parsed = ChartTableParser.parse(chart)

        assertEquals(emptyList<Any>(), parsed.points)
        assertEquals(3, chart.normalized().rows.size)
        assertEquals(listOf("category", "value"), chart.normalized().columnIds)
    }

    @Test
    fun allZeroBarAndLineValuesKeepTheirRowsWhilePieBecomesEmpty() {
        val rows = listOf(row("Category", "Value"), row("A", "0"), row("B", "0"))
        val bar = EditorBlock.ChartBlock(
            chartType = ChartType.BAR,
            columnIds = listOf("category", "value"),
            selectedColumnId = "value",
            rows = rows
        )
        val line = bar.copy(chartType = ChartType.LINE)
        val pie = bar.copy(chartType = ChartType.PIE)

        assertEquals(listOf(0f, 0f), ChartTableParser.parse(bar).points.map { it.value })
        assertEquals(listOf(0f, 0f), ChartTableParser.parse(line).points.map { it.value })
        assertEquals(emptyList<Any>(), ChartTableParser.parse(pie).points)
    }

    @Test
    fun duplicateColumnIdsBecomeStableAndLargeTablesKeepAllValidPoints() {
        val rows = buildList {
            add(row("Category", "Revenue", "Cost"))
            repeat(200) { index -> add(row("Row $index", "${index + 1}", "${index + 2}")) }
        }
        val chart = EditorBlock.ChartBlock(
            chartType = ChartType.BAR,
            columnIds = listOf("category", "value", "value"),
            selectedColumnId = "value",
            rows = rows
        )

        val normalized = chart.normalized()
        val parsed = ChartTableParser.parse(chart)

        assertEquals(listOf("category", "value", "c_column_4"), normalized.columnIds)
        assertEquals(200, parsed.points.size)
        assertEquals("Row 199", parsed.points.last().category)
    }

    private fun row(vararg values: String): List<List<RichText>> = values.map { value -> listOf(RichText(value)) }
}
