package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorBottomBarTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun bottomBar_togglesBetweenDefaultAndFormatting() {
        val isFormattingVisible = mutableStateOf(false)
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state =
                NoteEditorUiState(
                    noteId = "note_1",
                    isFormattingToolbarVisible = isFormattingVisible.value,
                    isLoaded = true
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {
                    isFormattingVisible.value = !isFormattingVisible.value
                },
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        // Initially default bottom bar is shown
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("editor_formatting_bottom_bar").fetchSemanticsNodes().isEmpty())
        // Click Aa button
        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()
        // Now formatting bottom bar is shown
        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("editor_default_bottom_bar").fetchSemanticsNodes().isEmpty())
        // Click hide button
        composeRule.onNodeWithTag("editor_hide_formatting").performClick()
        // Back to default bottom bar
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("editor_formatting_bottom_bar").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun formattingBottomBar_underlineButton_togglesUnderlineMark() {
        val toggledMarks = mutableListOf<Pair<String, String>>()
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state =
                NoteEditorUiState(
                    noteId = "note_1",
                    isFormattingToolbarVisible = true,
                    isLoaded = true,
                    document = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(
                                id = "block_1",
                                children = listOf(RichText("Hello"))
                            )
                        )
                    )
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { blockId, mark -> toggledMarks += blockId to mark },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.onNodeWithTag("editor_underline_action").assertIsDisplayed().performClick()
        assertEquals(listOf("block_1" to "underline"), toggledMarks)
    }

    @Test
    fun formattingBottomBar_strikethroughButton_togglesStrikethroughMark() {
        val toggledMarks = mutableListOf<Pair<String, String>>()
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state =
                NoteEditorUiState(
                    noteId = "note_1",
                    isFormattingToolbarVisible = true,
                    isLoaded = true,
                    document = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(
                                id = "block_1",
                                children = listOf(RichText("Hello"))
                            )
                        )
                    )
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { blockId, mark -> toggledMarks += blockId to mark },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.onNodeWithTag("editor_strikethrough_action").assertIsDisplayed().performClick()
        assertEquals(listOf("block_1" to "strikethrough"), toggledMarks)
    }

    @Test
    fun readOnlyNote_hidesBottomBar() {
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state =
                NoteEditorUiState(
                    noteId = "note_1",
                    isLoaded = true,
                    isEditable = false
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        assertTrue(composeRule.onAllNodesWithTag("editor_default_bottom_bar").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("editor_formatting_bottom_bar").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun defaultBottomBar_checkboxButton_triggersToggleCheckbox() {
        val toggledCheckboxBlocks = mutableListOf<String>()
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(
                    noteId = "note_1",
                    isLoaded = true,
                    document = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(id = "block_1", children = listOf(RichText("Hello")))
                        )
                    )
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = { toggledCheckboxBlocks += it },
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.onNodeWithTag("editor_checkbox_action").assertIsDisplayed().performClick()
        assertEquals(listOf("block_1"), toggledCheckboxBlocks)
    }

    @Test
    fun checkboxIcon_taps_triggerToggleCheckboxChecked() {
        val toggledCheckedBlocks = mutableListOf<String>()
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(
                    noteId = "note_1",
                    isLoaded = true,
                    document = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(
                                id = "block_1",
                                type = "checkbox",
                                checked = false,
                                children = listOf(RichText("Hello"))
                            )
                        )
                    )
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = { toggledCheckedBlocks += it },
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.onNodeWithTag("editor_checkbox_icon").assertIsDisplayed().performClick()
        assertEquals(listOf("block_1"), toggledCheckedBlocks)
    }
}
