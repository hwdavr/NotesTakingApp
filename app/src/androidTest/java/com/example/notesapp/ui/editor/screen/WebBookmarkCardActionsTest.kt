package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebBookmarkCardActionsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun opensEditWithExistingUrl() {
        var editRequest: List<String>? = null
        val bookmark = bookmark()
        var state by mutableStateOf(stateWith(bookmark))

        setContent(
            state = { state },
            onEdit = { id, url, title, description ->
                editRequest = listOf(id, url, title, description)
            },
            onDeleteBlock = { blockId ->
                state = state.copy(
                    document = state.document.copy(
                        blocks = state.document.blocks.filterNot { it.id == blockId }
                    )
                )
            }
        )

        composeRule.onNodeWithTag("editor_web_bookmark_actions_${bookmark.id}").performClick()
        composeRule.onNodeWithTag("web_bookmark_actions_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_actions_edit").performClick()

        assertEquals(
            listOf(bookmark.id, bookmark.url, bookmark.title, bookmark.description),
            editRequest
        )
        composeRule.onNodeWithTag("web_bookmark_actions_sheet").assertIsNotDisplayed()
    }

    @Test
    fun showsEditDeleteSheetAndRequiresDeleteConfirmation() {
        val bookmark = bookmark()
        var state by mutableStateOf(stateWith(bookmark))

        setContent(
            state = { state },
            onDeleteBlock = { blockId ->
                state = state.copy(
                    document = state.document.copy(
                        blocks = state.document.blocks.filterNot { it.id == blockId }
                    )
                )
            }
        )

        composeRule.onNodeWithTag("editor_web_bookmark_actions_${bookmark.id}").performClick()
        composeRule.onNodeWithTag("web_bookmark_actions_sheet_title").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_actions_edit").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_actions_delete").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_actions_close").assertDoesNotExist()
        composeRule.onNodeWithTag("web_bookmark_actions_delete").performClick()
        composeRule.onNodeWithTag("web_bookmark_delete_confirmation").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_delete_cancel").performClick()
        composeRule.onNodeWithTag("editor_web_bookmark_block_${bookmark.id}").assertIsDisplayed()

        composeRule.onNodeWithTag("editor_web_bookmark_actions_${bookmark.id}").performClick()
        composeRule.onNodeWithTag("web_bookmark_actions_delete").performClick()
        composeRule.onNodeWithTag("web_bookmark_delete_confirm").performClick()
        composeRule.onNodeWithTag("editor_web_bookmark_block_${bookmark.id}").assertIsNotDisplayed()
    }

    @Test
    fun deletingLastBookmarkLeavesEditableTextBlock() {
        val bookmark = bookmark()
        var state by mutableStateOf(stateWith(bookmark))

        setContent(
            state = { state },
            onDeleteBlock = { blockId ->
                val remaining = state.document.blocks.filterNot { it.id == blockId }
                state = state.copy(document = NoteDocument(blocks = remaining).ensureEditableTextBlock())
            }
        )

        composeRule.onNodeWithTag("editor_web_bookmark_actions_${bookmark.id}").performClick()
        composeRule.onNodeWithTag("web_bookmark_actions_delete").performClick()
        composeRule.onNodeWithTag("web_bookmark_delete_confirm").performClick()

        composeRule.onNodeWithTag("editor_web_bookmark_block_${bookmark.id}").assertIsNotDisplayed()
        composeRule.onNodeWithTag("editor_text_block").assertIsDisplayed()
    }

    private fun setContent(
        state: () -> NoteEditorUiState,
        onEdit: (String, String, String, String) -> Unit = { _, _, _, _ -> },
        onDeleteBlock: (String) -> Unit
    ) {
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state(),
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
                onDeleteBlock = onDeleteBlock,
                onEditWebBookmarkEditor = onEdit
            )
        }
    }

    private fun stateWith(bookmark: EditorBlock.WebBookmarkBlock) = NoteEditorUiState(
        noteId = "note-1",
        title = "Bookmark note",
        document = NoteDocument(blocks = listOf(bookmark)),
        isLoaded = true,
        isEditable = true
    )

    private fun bookmark() = EditorBlock.WebBookmarkBlock(
        id = "bookmark-actions-1",
        url = "https://example.com/article",
        title = "Example article",
        description = "A persisted bookmark description"
    )
}
