package com.example.notesapp.ui.editor.model

import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartBlockCardModelTest {
    @Test
    fun fromPreparesPresentationDataWithoutMutatingTheStoredBlock() {
        val source = EditorBlock.ChartBlock(
            id = "chart-model",
            chartType = ChartType.LINE,
            title = "",
            columnIds = listOf("category", "value"),
            selectedColumnId = "deleted",
            rows = listOf(row("Category", "Value"), row("January", "12"))
        )

        val model = ChartBlockCardModel.from(source)

        assertEquals("deleted", source.selectedColumnId)
        assertEquals("", source.title)
        assertEquals(listOf("category", "value"), source.columnIds)
        assertEquals("value", model.block.selectedColumnId)
        assertEquals("January", model.chartData.points.single().category)
        assertEquals(12f, model.chartData.points.single().value)
        assertEquals(listOf("value"), model.columnOptions.map { it.id })
    }

    @Test
    fun fromUsesStableIdsForDuplicateAndBlankDataColumns() {
        val source = EditorBlock.ChartBlock(
            id = "chart-options",
            columnIds = listOf("category", "", "value", "value"),
            selectedColumnId = "value",
            rows = listOf(row("Category", "Revenue", "Cost", "Tax"), row("January", "10", "8", "2"))
        )

        val model = ChartBlockCardModel.from(source)

        assertEquals(
            listOf("c_column_2", "value", "c_column_5"),
            model.columnOptions.map { it.id }
        )
        assertEquals("value", model.chartData.selectedColumnId)
    }

    private fun row(vararg values: String): List<List<RichText>> = values.map { value ->
        listOf(RichText(value))
    }
}
