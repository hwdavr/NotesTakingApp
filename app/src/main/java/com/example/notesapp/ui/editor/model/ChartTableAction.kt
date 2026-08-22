package com.example.notesapp.ui.editor.model

sealed interface ChartTableAction {
    val blockId: String

    data class InsertColumnLeft(
        override val blockId: String,
        val columnIndex: Int
    ) : ChartTableAction

    data class InsertColumnRight(
        override val blockId: String,
        val columnIndex: Int
    ) : ChartTableAction

    data class DeleteColumn(
        override val blockId: String,
        val columnIndex: Int
    ) : ChartTableAction

    data class ClearColumn(
        override val blockId: String,
        val columnIndex: Int
    ) : ChartTableAction

    data class InsertRowAbove(
        override val blockId: String,
        val rowIndex: Int
    ) : ChartTableAction

    data class InsertRowBelow(
        override val blockId: String,
        val rowIndex: Int
    ) : ChartTableAction

    data class DeleteRow(
        override val blockId: String,
        val rowIndex: Int
    ) : ChartTableAction

    data class ClearRow(
        override val blockId: String,
        val rowIndex: Int
    ) : ChartTableAction
}
