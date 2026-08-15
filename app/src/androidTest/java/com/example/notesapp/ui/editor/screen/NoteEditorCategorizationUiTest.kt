@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorCategorizationUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun smartCategorizationDialog_rendersExpectedElements_andTriggersCallbacks() {
        var okClicked = false
        var cancelClicked = false

        val testFolder = Folder(id = "f1", name = "Work", createdAt = 0L, updatedAt = 0L)
        val state = NoteEditorUiState(
            noteId = "note_1",
            isLoaded = true,
            showCategorizationDialog = true,
            recommendedFolder = testFolder
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
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
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {},
                onConfirmCategorization = { okClicked = true },
                onCancelCategorization = { cancelClicked = true }
            )
        }

        // Verify dialog components are displayed
        composeRule.onNodeWithTag("smart_categorization_dialog").assertIsDisplayed()

        // Verify clicks
        composeRule.onNodeWithTag("smart_categorization_ok").performClick()
        assertTrue(okClicked)

        composeRule.onNodeWithTag("smart_categorization_cancel").performClick()
        assertTrue(cancelClicked)
    }

    @Test
    fun smartCategorizationNoMatchDialog_rendersExpectedElements_andTriggersCallbacks() {
        var yesClicked = false
        var noClicked = false
        val state = NoteEditorUiState(
            noteId = "note_1",
            isLoaded = true,
            showCategorizationNoMatchDialog = true
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
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
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {},
                onConfirmManualMove = { yesClicked = true },
                onCancelManualMove = { noClicked = true }
            )
        }

        composeRule.onNodeWithTag("smart_categorization_no_match_dialog").assertIsDisplayed()

        composeRule.onNodeWithTag("smart_categorization_no_match_yes").performClick()
        assertTrue(yesClicked)

        composeRule.onNodeWithTag("smart_categorization_no_match_no").performClick()
        assertTrue(noClicked)
    }

    @Test
    fun smartCategorizationProgress_displaysWhenCategorizing() {
        val state = NoteEditorUiState(
            noteId = "note_1",
            isLoaded = true,
            isCategorizing = true
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
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
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {},
                onConfirmCategorization = {},
                onCancelCategorization = {}
            )
        }

        // Verify progress indicator is displayed
        composeRule.onNodeWithTag("smart_categorization_progress").assertIsDisplayed()
    }

    @Test
    fun backSyncProgress_displaysWhenBackSyncing() {
        val state = NoteEditorUiState(
            noteId = "note_1",
            isLoaded = true,
            isBackSyncing = true
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
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
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {},
                onConfirmCategorization = {},
                onCancelCategorization = {}
            )
        }

        composeRule.onNodeWithTag("editor_back_sync_progress").assertIsDisplayed()
    }
}
