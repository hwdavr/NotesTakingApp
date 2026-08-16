package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorBasicBlocksSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun triggerButton_togglesBasicBlocksPanelVisibility() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
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

        // Trigger is visible
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().performClick()

        // Panel and title are displayed
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_panel_title").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_grid").assertIsDisplayed()

        // All 11 tiles exist in grid and can be scrolled to
        val expectedTags = listOf(
            "basic_blocks_text",
            "basic_blocks_heading_1",
            "basic_blocks_heading_2",
            "basic_blocks_heading_3",
            "basic_blocks_heading_4",
            "basic_blocks_bulleted_list",
            "basic_blocks_numbered_list",
            "basic_blocks_todo_list",
            "basic_blocks_toggle_list",
            "basic_blocks_callout",
            "basic_blocks_quote"
        )

        for (tag in expectedTags) {
            composeRule.onNodeWithTag("basic_blocks_grid").performScrollToNode(hasTestTag(tag))
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
        }

        // Second click toggles off
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
    }

    @Test
    fun tileClick_invokesInsertionCallbackAndCollapsesPanel() {
        val insertedTypes = mutableListOf<BasicBlockType>()
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true,
                document = NoteDocument(
                    blocks = listOf(EditorBlock.TextBlock(id = "block-1", children = listOf(RichText("Hello"))))
                )
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onInsertBasicBlock = { type ->
                    insertedTypes += type
                    true
                },
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
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

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_heading_1").performClick()

        assertEquals(listOf(BasicBlockType.HEADING_1), insertedTypes)
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
    }

    @Test
    fun readOnlyNote_disablesTriggerButton() {
        val state = NoteEditorUiState(
            noteId = "read-only-1",
            isLoaded = true,
            isEditable = false
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "read-only-1",
                state = state,
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
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

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsNotEnabled()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
    }
}
