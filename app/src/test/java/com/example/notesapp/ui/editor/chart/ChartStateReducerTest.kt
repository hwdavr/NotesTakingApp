package com.example.notesapp.ui.editor.chart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChartStateReducerTest {
    @Test
    fun testEmptyAndRenderErrorStatesKeepRecoveryControls() {
        val initial = ChartInteractionState(
            renderState = ChartRenderState.CONTENT,
            selectedPointIndex = 0,
            openSheet = ChartSheet.VIEW
        )

        val empty = ChartSelectionReducer.reduce(initial, ChartSelectionEvent.Rendered(0))
        val error = ChartSelectionReducer.reduce(empty, ChartSelectionEvent.RenderFailed)

        assertEquals(ChartRenderState.EMPTY, empty.renderState)
        assertNull(empty.selectedPointIndex)
        assertEquals(ChartSheet.VIEW, empty.openSheet)
        assertEquals(ChartRenderState.ERROR, error.renderState)
        assertEquals(ChartSheet.VIEW, error.openSheet)
    }

    @Test
    fun testTransientSelectionAndSheetDismissalDoesNotMutateBlock() {
        val initial = ChartInteractionState(renderState = ChartRenderState.CONTENT)

        val selected = ChartSelectionReducer.reduce(initial, ChartSelectionEvent.DatumTapped(1))
        val dismissed = ChartSelectionReducer.reduce(
            selected,
            ChartSelectionEvent.TooltipDismissed
        )

        assertEquals(1, selected.selectedPointIndex)
        assertNull(dismissed.selectedPointIndex)
        assertEquals(ChartSheet.NONE, dismissed.openSheet)
    }
}
