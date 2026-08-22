package com.example.notesapp.ui.editor.chart

import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.columnOptions
import com.example.notesapp.ui.editor.mapper.normalized
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartColumnSelectionTest {
    @Test
    fun testFallbackAndLabelsUseStableIds() {
        val chart = EditorBlock.ChartBlock(
            id = "chart-columns",
            columnIds = listOf("category", "", "", "tax"),
            selectedColumnId = "deleted",
            rows = listOf(
                row("Category", "", "", "Tax"),
                row("Jan", "10", "20", "3")
            )
        )

        val normalized = chart.normalized()
        val options = chart.columnOptions()
        val parsed = ChartTableParser.parse(chart)

        assertEquals(listOf("category", "c_column_2", "c_column_3", "tax"), normalized.columnIds)
        assertEquals(listOf("c_column_2", "c_column_3", "tax"), options.map { it.id })
        assertEquals(listOf(2, 3, 4), options.map { it.fallbackPosition })
        assertEquals("c_column_2", parsed.selectedColumnId)
        assertEquals(null, parsed.selectedColumnHeader)
    }

    private fun row(vararg values: String): List<List<RichText>> = values.map { value -> listOf(RichText(value)) }
}
