package com.example.notesapp.ui.editor.mapper

enum class BasicBlockType(val storageValue: String) {
    PARAGRAPH("paragraph"),
    HEADING_1("heading_1"),
    HEADING_2("heading_2"),
    HEADING_3("heading_3"),
    HEADING_4("heading_4"),
    BULLETED_LIST("bulleted"),
    NUMBERED_LIST("numbered"),
    TODO_LIST("checkbox"),
    TOGGLE_LIST("toggle"),
    CALLOUT("callout"),
    QUOTE("quote"),
    MERMAID("mermaid"),
    CODE("code"),
    BAR_CHART("chart_bar"),
    LINE_CHART("chart_line"),
    PIE_CHART("chart_pie"),
    UNKNOWN("");

    companion object {
        fun fromStorageValue(value: String): BasicBlockType = when (value) {
            "heading" -> HEADING_1
            else -> entries.firstOrNull { it.storageValue == value } ?: UNKNOWN
        }
    }
}

fun BasicBlockType.createEmptyTextBlock(id: String = newBlockId()): EditorBlock.TextBlock = EditorBlock.TextBlock(
    id = id,
    type = if (this == BasicBlockType.UNKNOWN) {
        BasicBlockType.PARAGRAPH.storageValue
    } else {
        storageValue
    },
    isExpanded = this == BasicBlockType.TOGGLE_LIST
)

fun EditorBlock.TextBlock.basicBlockType(): BasicBlockType = BasicBlockType.fromStorageValue(type)

fun EditorBlock.TextBlock.canonicalStorageType(): String = basicBlockType().storageValue.ifBlank { type }

fun BasicBlockType.headingLevel(): Int? = when (this) {
    BasicBlockType.HEADING_1 -> 1
    BasicBlockType.HEADING_2 -> 2
    BasicBlockType.HEADING_3 -> 3
    BasicBlockType.HEADING_4 -> 4
    else -> null
}
