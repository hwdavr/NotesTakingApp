package com.example.notesapp.ui.editor.model

data class TableFocusTarget(
    val rowIndex: Int,
    val columnIndex: Int
)

sealed interface TableHandleAction {
    val blockId: String

    data class FocusCell(
        override val blockId: String,
        val rowIndex: Int,
        val columnIndex: Int
    ) : TableHandleAction

    data class ClearFocus(
        override val blockId: String
    ) : TableHandleAction

    data class InsertColumnLeft(
        override val blockId: String,
        val columnIndex: Int
    ) : TableHandleAction

    data class InsertColumnRight(
        override val blockId: String,
        val columnIndex: Int
    ) : TableHandleAction

    data class DeleteColumn(
        override val blockId: String,
        val columnIndex: Int
    ) : TableHandleAction

    data class ClearColumn(
        override val blockId: String,
        val columnIndex: Int
    ) : TableHandleAction

    data class InsertRowAbove(
        override val blockId: String,
        val rowIndex: Int
    ) : TableHandleAction

    data class InsertRowBelow(
        override val blockId: String,
        val rowIndex: Int
    ) : TableHandleAction

    data class DeleteRow(
        override val blockId: String,
        val rowIndex: Int
    ) : TableHandleAction

    data class ClearRow(
        override val blockId: String,
        val rowIndex: Int
    ) : TableHandleAction

    data class ClearTable(
        override val blockId: String
    ) : TableHandleAction

    data class DuplicateTable(
        override val blockId: String
    ) : TableHandleAction

    data class DeleteTable(
        override val blockId: String
    ) : TableHandleAction

    data class ToggleTableFitToWidth(
        override val blockId: String
    ) : TableHandleAction
}
