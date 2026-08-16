package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BasicBlocksPanelAutoCollapseTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: NoteEditorUiState = NoteEditorUiState(
            noteId = "note-1",
            isLoaded = true,
            isEditable = true
        ),
        onInsertBasicBlock: (BasicBlockType) -> Boolean = { false },
        onAddParagraph: () -> Unit = {},
        onToggleFormattingToolbar: () -> Unit = {}
    ) {
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = state.noteId,
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
                onInsertBasicBlock = onInsertBasicBlock,
                onAddParagraph = onAddParagraph,
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
                onToggleFormattingToolbar = onToggleFormattingToolbar,
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
    }

    @Test
    fun editorContentTapCollapsesPanelWithoutMutation() {
        val insertCalls = mutableListOf<BasicBlockType>()
        var paragraphCalls = 0
        setContent(
            onInsertBasicBlock = { type ->
                insertCalls.add(type)
                false
            },
            onAddParagraph = { paragraphCalls++ }
        )

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        composeRule.onNodeWithTag("editor_content_scrollable", useUnmergedTree = true).performClick()

        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
        assertTrue("No block must be inserted on outside collapse", insertCalls.isEmpty())
        assertEquals("No paragraph must be appended on outside collapse", 0, paragraphCalls)
    }

    @Test
    fun nonTriggerToolbarControlCollapsesPanelWithoutMutation() {
        val insertCalls = mutableListOf<BasicBlockType>()
        var formattingCalls = 0
        setContent(
            onInsertBasicBlock = { type ->
                insertCalls.add(type)
                false
            },
            onToggleFormattingToolbar = { formattingCalls++ }
        )

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()

        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
        assertEquals(
            "Formatting toolbar must not toggle on the first outside tap",
            0,
            formattingCalls
        )
        assertTrue("No block must be inserted on outside collapse", insertCalls.isEmpty())
    }

    @Test
    fun triggerToggleAndTileInsertionStillWorkAfterAutoCollapse() {
        val insertCalls = mutableListOf<BasicBlockType>()
        setContent(
            onInsertBasicBlock = { type ->
                insertCalls.add(type)
                true
            }
        )

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        composeRule.onNodeWithTag("editor_content_scrollable", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_text"))
        composeRule.onNodeWithTag("basic_blocks_text").performClick()

        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
        assertEquals("Exactly one block must be inserted", 1, insertCalls.size)
        assertEquals(BasicBlockType.PARAGRAPH, insertCalls.first())
    }
}
