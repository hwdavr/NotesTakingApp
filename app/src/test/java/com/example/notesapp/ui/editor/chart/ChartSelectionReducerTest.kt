package com.example.notesapp.ui.editor.chart

import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChartSelectionReducerTest {
    @Test
    fun testTransientSelectionAndSheetDismissalDoesNotMutateBlock() {
        val block = chartBlock()
        var state = ChartInteractionState(renderState = ChartRenderState.CONTENT)

        state = ChartSelectionReducer.reduce(state, ChartSelectionEvent.DatumTapped(1))

        assertEquals(1, state.selectedPointIndex)
        assertEquals(block, chartBlock())

        state = ChartSelectionReducer.reduce(state, ChartSelectionEvent.TooltipDismissed)

        assertNull(state.selectedPointIndex)
        assertEquals(ChartSheet.NONE, state.openSheet)
    }

    @Test
    fun testEmptyAndRenderErrorStatesKeepRecoveryControls() {
        val initial = ChartInteractionState(
            renderState = ChartRenderState.CONTENT,
            selectedPointIndex = 0,
            openSheet = ChartSheet.VIEW
        )

        val empty = ChartSelectionReducer.reduce(initial, ChartSelectionEvent.Rendered(0))
        assertEquals(ChartRenderState.EMPTY, empty.renderState)
        assertNull(empty.selectedPointIndex)
        assertEquals(ChartSheet.VIEW, empty.openSheet)

        val error = ChartSelectionReducer.reduce(empty, ChartSelectionEvent.RenderFailed)
        assertEquals(ChartRenderState.ERROR, error.renderState)
        assertEquals(ChartSheet.VIEW, error.openSheet)
    }

    @Test
    fun testOpeningOrDismissingSheetClearsSelectedDatum() {
        val selected = ChartInteractionState(
            renderState = ChartRenderState.CONTENT,
            selectedPointIndex = 2
        )

        val opened = ChartSelectionReducer.reduce(
            selected,
            ChartSelectionEvent.SheetOpened(ChartSheet.OPTIONS)
        )
        assertNull(opened.selectedPointIndex)
        assertEquals(ChartSheet.OPTIONS, opened.openSheet)

        val dismissed = ChartSelectionReducer.reduce(opened, ChartSelectionEvent.SheetDismissed)
        assertNull(dismissed.selectedPointIndex)
        assertEquals(ChartSheet.NONE, dismissed.openSheet)
    }

    @Test
    fun testRenderedDataRemovesSelectionWhenTheSelectedPointNoLongerExists() {
        val selected = ChartInteractionState(
            renderState = ChartRenderState.CONTENT,
            selectedPointIndex = 3
        )

        val reduced = ChartSelectionReducer.reduce(selected, ChartSelectionEvent.Rendered(2))

        assertEquals(ChartRenderState.CONTENT, reduced.renderState)
        assertNull(reduced.selectedPointIndex)
    }

    private fun chartBlock(): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "chart-selection",
        chartType = ChartType.BAR,
        columnIds = listOf("category", "value"),
        selectedColumnId = "value",
        rows = listOf(
            row("Category", "Value"),
            row("Jan", "10")
        )
    )

    private fun row(vararg values: String): List<List<RichText>> {
        return values.map { value -> listOf(RichText(value)) }
    }
}
