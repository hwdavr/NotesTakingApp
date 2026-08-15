@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorRichDocumentScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun richDocumentBlocks_renderImageAndTable() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "text_1", children = listOf(RichText("Hello"))),
                EditorBlock.ImageBlock(id = "image_1", url = "https://cdn.example.com/image.png", caption = "My image"),
                EditorBlock.TableBlock(id = "table_1")
            )
        )
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(noteId = "note_1", title = "Title", document = document, isLoaded = true),
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
        composeRule.waitForIdle()
        assertEditorNodeExists("rich_document_blocks")
        assertEditorNodeExists("editor_text_block")
        assertEditorNodeExists("editor_image_block")
        assertEditorNodeExists("editor_table_block")
    }

    private fun assertEditorNodeExists(testTag: String) {
        assertTrue(
            composeRule.onAllNodesWithTag(testTag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
    }

    @Test
    fun imageBlock_deleteButton_triggersCallback() {
        var deletedBlockId: String? = null
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.ImageBlock(id = "image_1", url = "https://example.com/image.png")
            )
        )
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(noteId = "note_1", title = "Title", document = document, isLoaded = true),
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
                onDeleteBlock = { deletedBlockId = it }
            )
        }
        composeRule.onNodeWithTag("editor_image_block_delete").performClick()
        assertTrue(deletedBlockId == "image_1")
    }

    @Test
    fun editorToolbarActions_triggerCallbacks() {
        var addedImage = false
        var addedTable = false
        var toggledBold = false
        val isFormattingVisible = mutableStateOf(false)
        composeRule.setContent {
            val state = NoteEditorUiState(
                noteId = "note_1",
                title = "Title",
                document = NoteDocument(blocks = listOf(EditorBlock.TextBlock(id = "text_1"))),
                isLoaded = true,
                isFormattingToolbarVisible = isFormattingVisible.value,
                focusedBlockId = "text_1"
            )
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
                onToggleMark = { blockId, mark -> toggledBold = blockId == "text_1" && mark == "bold" },
                onAddParagraph = {},
                onAddImage = { addedImage = true },
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = { _, _, _ -> },
                onAddTable = { addedTable = true },
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = { isFormattingVisible.value = !isFormattingVisible.value },
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.onNodeWithTag("editor_add_image").performClick()
        // Scroll to the table button and click it
        composeRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_add_table"))
        composeRule.onNodeWithTag("editor_add_table").performClick()
        // Scroll back to the toggle button
        composeRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_toggle_formatting"))
        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()
        // Wait for formatting toolbar to be visible
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag("editor_bold_action").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("editor_bold_action").performClick()
        assertTrue("addedImage failed", addedImage)
        assertTrue("addedTable failed", addedTable)
        assertTrue("toggledBold failed", toggledBold)
    }

    @Test
    fun emptyTextBlock_doesNotShowPlaceholder() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "text_1", children = emptyList())
            )
        )
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(noteId = "note_1", title = "Title", document = document, isLoaded = true),
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
        composeRule.onNodeWithTag("editor_text_block").assertIsDisplayed()
        composeRule.onNodeWithText("Start writing…").assertDoesNotExist()
    }

    @Test
    fun emptySpaceClick_focusesLastTextBlock() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "text_1", children = listOf(RichText("Hello"))),
                EditorBlock.TextBlock(id = "text_2", children = listOf(RichText("World")))
            )
        )
        var focusedBlockId: String? = null
        val state = NoteEditorUiState(
            noteId = "note_1",
            title = "Title",
            document = document,
            isLoaded = true,
            isEditable = true
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
                onBlockFocused = { focusedBlockId = it },
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }

        // Tapping the empty space (editor_content_scrollable) should trigger focus request on text_2
        composeRule.onNodeWithTag("editor_content_scrollable").performClick()
        composeRule.waitForIdle()

        // Verify that the callback was triggered with the last block's ID
        assertTrue(focusedBlockId == "text_2")
    }

    @Test
    fun emptySpaceClick_withImageAtEnd_focusesLastTextBlock() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "text_1", children = listOf(RichText("Hello"))),
                EditorBlock.TextBlock(id = "text_2", children = listOf(RichText("World"))),
                EditorBlock.ImageBlock(id = "image_1", url = "https://example.com/image.png")
            )
        )
        var focusedBlockId: String? = null
        val state = NoteEditorUiState(
            noteId = "note_1",
            title = "Title",
            document = document,
            isLoaded = true,
            isEditable = true
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
                onBlockFocused = { focusedBlockId = it },
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }

        // Tapping the empty space (editor_content_scrollable) should focus text_2, which is the last TextBlock
        composeRule.onNodeWithTag("editor_content_scrollable").performClick()
        composeRule.waitForIdle()

        assertTrue(focusedBlockId == "text_2")
    }
}
