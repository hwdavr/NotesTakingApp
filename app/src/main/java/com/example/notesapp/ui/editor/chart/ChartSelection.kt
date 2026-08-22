package com.example.notesapp.ui.editor.chart

enum class ChartRenderState {
    CONTENT,
    EMPTY,
    ERROR
}

enum class ChartSheet {
    NONE,
    VIEW,
    OPTIONS,
    DATA_COLUMN
}

data class ChartInteractionState(
    val renderState: ChartRenderState = ChartRenderState.EMPTY,
    val selectedPointIndex: Int? = null,
    val openSheet: ChartSheet = ChartSheet.NONE
)

sealed interface ChartSelectionEvent {
    data class Rendered(val pointCount: Int) : ChartSelectionEvent

    data object RenderFailed : ChartSelectionEvent

    data class DatumTapped(val pointIndex: Int) : ChartSelectionEvent

    data object TooltipDismissed : ChartSelectionEvent

    data class SheetOpened(val sheet: ChartSheet) : ChartSelectionEvent

    data object SheetDismissed : ChartSelectionEvent
}

object ChartSelectionReducer {
    fun reduce(state: ChartInteractionState, event: ChartSelectionEvent): ChartInteractionState = when (event) {
        is ChartSelectionEvent.Rendered -> state.copy(
            renderState = if (event.pointCount > 0) {
                ChartRenderState.CONTENT
            } else {
                ChartRenderState.EMPTY
            },
            selectedPointIndex = state.selectedPointIndex
                ?.takeIf { it in 0 until event.pointCount }
        )

        ChartSelectionEvent.RenderFailed -> state.copy(
            renderState = ChartRenderState.ERROR,
            selectedPointIndex = null
        )

        is ChartSelectionEvent.DatumTapped -> state.copy(
            selectedPointIndex = event.pointIndex.takeIf { it >= 0 },
            openSheet = ChartSheet.NONE
        )

        ChartSelectionEvent.TooltipDismissed -> state.copy(selectedPointIndex = null)

        is ChartSelectionEvent.SheetOpened -> state.copy(
            selectedPointIndex = null,
            openSheet = event.sheet
        )

        ChartSelectionEvent.SheetDismissed -> state.copy(
            selectedPointIndex = null,
            openSheet = ChartSheet.NONE
        )
    }
}
