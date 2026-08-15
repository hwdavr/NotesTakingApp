package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorEmojiPickerTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun editableToolbarOpensPickerWithRecentSelected() {
        val title = "Emoji note"
        val screenState = mutableStateOf(editableState(title = title))
        composeRule.setContent {
            EmojiPickerTestContent(state = screenState, onEmojiSelected = {})
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_category_recent").assertIsSelected()
        assertEquals(title, screenState.value.title)
    }

    @Test
    fun readOnlyToolbarIsDisabledAndDoesNotOpenPicker() {
        val screenState = mutableStateOf(editableState(title = "Shared note").copy(isEditable = false))
        composeRule.setContent {
            EmojiPickerTestContent(state = screenState, onEmojiSelected = {})
        }

        composeRule.onNodeWithTag("editor_insert_emoji")
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertContentDescriptionEquals("Emoji insertion is unavailable in a read-only note.")
        assertTrue(composeRule.onAllNodesWithTag("emoji_picker_sheet").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun selectionInsertsEmojiAndKeepsPickerOpen() {
        val body = EditorBlock.TextBlock(id = "body", children = listOf(RichText("Plan today")))
        val screenState = mutableStateOf(
            editableState(title = "Title", block = body).copy(
                focusedBlockId = body.id,
                selectionStart = 5,
                selectionEnd = 10
            )
        )
        composeRule.setContent {
            EmojiPickerTestContent(
                state = screenState,
                onEmojiSelected = { emoji ->
                    val selectedBlock = screenState.value.document.blocks.single() as EditorBlock.TextBlock
                    screenState.value = screenState.value.copy(
                        document = NoteDocument(
                            blocks = listOf(
                                selectedBlock.copy(children = listOf(RichText("Plan $emoji")))
                            )
                        ),
                        selectionStart = 5 + emoji.length,
                        selectionEnd = 5 + emoji.length
                    )
                }
            )
        }

        openEmojiPicker()
        composeRule.onNodeWithTag("emoji_picker_item_grinning_face").performClick()
        composeRule.waitForIdle()

        val updatedBlock = screenState.value.document.blocks.single() as EditorBlock.TextBlock
        assertEquals("Plan 😀", updatedBlock.text())
        assertEquals(7, screenState.value.selectionStart)
        assertEquals("Title", screenState.value.title)
        composeRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
    }

    private fun openEmojiPicker() {
        composeRule.onNodeWithTag("editor_default_bottom_bar").performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag("editor_insert_emoji").performClick()
        composeRule.waitForIdle()
    }
}

@Composable
private fun EmojiPickerTestContent(state: MutableState<NoteEditorUiState>, onEmojiSelected: (String) -> Unit) {
    NoteEditorScreenContent(
        parentPadding = PaddingValues(0.dp),
        noteId = state.value.noteId,
        state = state.value,
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
        onEmojiSelected = onEmojiSelected,
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

private fun editableState(
    title: String,
    block: EditorBlock.TextBlock = EditorBlock.TextBlock(id = "body", children = listOf(RichText("Body")))
): NoteEditorUiState = NoteEditorUiState(
    noteId = "note-1",
    title = title,
    document = NoteDocument(blocks = listOf(block)),
    isLoaded = true,
    focusedBlockId = block.id,
    selectionStart = block.text().length,
    selectionEnd = block.text().length
)
