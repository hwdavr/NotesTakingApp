package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.NoteEditorScreenContent
import com.example.notesapp.ui.editor.NoteEditorUiState
import com.example.notesapp.ui.editor.document.EditorBlock
import com.example.notesapp.ui.editor.document.NoteDocument
import com.example.notesapp.ui.editor.document.RichText
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
                onSave = {},
                onDelete = {},
                onTitleChange = {},
                onTextBlockChange = { _, _ -> },
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

        composeRule.onNodeWithTag("rich_document_blocks").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_text_block_text_1").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_image_block_image_1").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_table_block_table_1").assertIsDisplayed()
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
                isFormattingToolbarVisible = isFormattingVisible.value
            )

            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = state,
                onBack = {},
                onSave = {},
                onDelete = {},
                onTitleChange = {},
                onTextBlockChange = { _, _ -> },
                onToggleMark = { blockId, mark -> toggledBold = blockId == "text_1" && mark == "bold" },
                onAddParagraph = {},
                onAddImage = { addedImage = true },
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
        composeRule.onNodeWithTag("editor_add_table").performClick()
        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()
        composeRule.onNodeWithTag("editor_bold_action").performClick()

        assertTrue(toggledBold)
        assertTrue(addedImage)
        assertTrue(addedTable)
    }
}
