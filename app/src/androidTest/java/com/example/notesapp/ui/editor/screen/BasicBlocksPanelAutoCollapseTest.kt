package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BasicBlocksPanelAutoCollapseTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editorContentTapCollapsesPanelWithoutMutation() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )
        var insertCalled = false
        var imageAddCalled = false

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
                onInsertBasicBlock = {
                    insertCalled = true
                    true
                },
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddImage = { imageAddCalled = true },
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

        // Open the panel
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        // Tap inside the editor content area
        composeRule.onNodeWithTag("editor_content_scrollable").assertIsDisplayed().performClick()

        // Verify basic_blocks_panel disappears without block insertion or image addition
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
        assertFalse("No block should be inserted on outside tap collapse", insertCalled)
        assertFalse("No add image action should be called", imageAddCalled)
    }

    @Test
    fun nonTriggerToolbarControlCollapsesPanelWithoutMutation() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )
        var imageAddCalled = false
        var insertCalled = false

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
                onInsertBasicBlock = {
                    insertCalled = true
                    true
                },
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddImage = { imageAddCalled = true },
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

        // Open the panel
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        // Tap non-trigger toolbar control (editor_add_image)
        composeRule.onNodeWithTag("editor_add_image").assertIsDisplayed().performClick()

        // Verify basic_blocks_panel disappears, no image added, no block inserted
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
        assertFalse("The first tap on non-trigger toolbar control only collapses panel", imageAddCalled)
        assertFalse("No block should be inserted", insertCalled)
    }

    @Test
    fun triggerToggleAndTileInsertionStillWorkAfterAutoCollapse() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )
        var insertedType: BasicBlockType? = null

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
                onInsertBasicBlock = { type ->
                    insertedType = type
                    true
                },
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

        // 1. Open and auto-collapse via content tap
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_content_scrollable").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()

        // 2. Reopen via trigger
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        // 3. Select Heading 2 tile
        composeRule.onNodeWithTag("basic_blocks_heading_2").assertIsDisplayed().performClick()

        // 4. Assert Heading 2 inserted and panel collapsed
        assertEquals(BasicBlockType.HEADING_2, insertedType)
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
    }
}
