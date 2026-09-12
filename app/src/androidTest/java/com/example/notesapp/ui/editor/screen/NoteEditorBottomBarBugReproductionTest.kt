package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.DiscussionUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorBottomBarBugReproductionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenBasicBlocksPanelOpen_whenAddImageTapped_thenImageActionExecutes() {
        var imageActionExecuted = false

        renderEditor(onAddImage = { imageActionExecuted = true })

        openBasicBlocksPanel()
        composeRule.onNodeWithTag("editor_add_image").performClick()

        assertTrue(
            "The image action must execute on the first tap after opening the Basic Blocks panel",
            imageActionExecuted
        )
    }

    @Test
    fun givenBasicBlocksPanelOpen_whenMicrophoneTapped_thenVoiceActionExecutes() {
        var voiceActionExecuted = false

        renderEditor(onOpenVoiceRecorder = { voiceActionExecuted = true })

        openBasicBlocksPanel()
        composeRule.onNodeWithTag("editor_mic_btn").performClick()

        assertTrue(
            "The microphone action must execute on the first tap after opening the Basic Blocks panel",
            voiceActionExecuted
        )
    }

    @Test
    fun givenBasicBlocksPanelOpen_whenTableTapped_thenTableActionExecutes() {
        var tableActionExecuted = false

        renderEditor(onAddTable = { tableActionExecuted = true })

        openBasicBlocksPanel()
        composeRule.onNodeWithTag("editor_add_table").performClick()

        assertTrue(
            "The table action must execute on the first tap after opening the Basic Blocks panel",
            tableActionExecuted
        )
    }

    @Test
    fun givenBasicBlocksPanelOpen_whenMentionTapped_thenDiscussionSheetOpens() {
        var discussionSheetOpened = false

        renderEditor(onOpenDiscussion = { discussionSheetOpened = true })

        openBasicBlocksPanel()
        composeRule.onNodeWithTag("editor_mention_action").performClick()

        assertTrue(
            "The @ action must open the discussion mention flow on the first tap",
            discussionSheetOpened
        )
    }

    @Test
    fun givenEditableEditor_whenMentionTapped_thenExistingDiscussionSheetOpensWithMentionPrompt() {
        val discussionState = mutableStateOf(DiscussionUiState())

        renderEditor(
            onOpenDiscussion = {
                discussionState.value = DiscussionUiState(
                    isVisible = true,
                    noteId = "note-1",
                    blockId = "block-1",
                    focusedBlockText = "Editable text",
                    commentText = "@",
                    selectionStart = 1,
                    selectionEnd = 1
                )
            },
            discussionStateProvider = { discussionState.value }
        )

        composeRule.onNodeWithTag("editor_mention_action").performClick()

        composeRule.onNodeWithTag("discussion_sheet_title").assertIsDisplayed()
        composeRule.onNodeWithTag("discussion_focused_block_text").assertIsDisplayed()
        composeRule.onNodeWithTag("discussion_comment_input").assertIsDisplayed()
    }

    private fun openBasicBlocksPanel() {
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("editor_default_bottom_bar").performTouchInput { swipeLeft() }
    }

    private fun renderEditor(
        onAddImage: () -> Unit = {},
        onAddTable: () -> Unit = {},
        onOpenVoiceRecorder: () -> Unit = {},
        onOpenDiscussion: () -> Unit = {},
        discussionStateProvider: () -> DiscussionUiState = { DiscussionUiState() }
    ) {
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = NoteEditorUiState(
                    noteId = "note-1",
                    isLoaded = true,
                    isEditable = true,
                    document = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(
                                id = "block-1",
                                children = listOf(RichText("Editable text"))
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
                onOpenVoiceRecorder = { _, _ -> onOpenVoiceRecorder() },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddImage = onAddImage,
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = { _, _, _ -> },
                onAddTable = onAddTable,
                onTableCellChange = { _, _, _, _ -> },
                onOpenDiscussion = onOpenDiscussion,
                discussionState = discussionStateProvider(),
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.waitForIdle()
    }
}
