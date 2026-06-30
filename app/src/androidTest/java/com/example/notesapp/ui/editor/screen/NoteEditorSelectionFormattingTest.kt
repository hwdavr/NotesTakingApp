package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
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

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NoteEditorSelectionFormattingTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun givenTextSelected_whenBoldTapped_thenSelectionAtToggleTimeIsNonCollapsed() {
        var lastSelection: Pair<Int, Int> = Pair(0, 0)
        var selectionAtToggle: Pair<Int, Int>? = null
        var toggledBlock: String? = null
        var toggledMark: String? = null

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state =
                NoteEditorUiState(
                    noteId = "note_1",
                    isFormattingToolbarVisible = true,
                    isLoaded = true,
                    focusedBlockId = "block_1",
                    document =
                    NoteDocument(
                        blocks =
                        listOf(
                            EditorBlock.TextBlock(
                                id = "block_1",
                                children = listOf(RichText("Hello World"))
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
                onToggleMark = { blockId, mark ->
                    toggledBlock = blockId
                    toggledMark = mark
                    selectionAtToggle = lastSelection
                },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { start, end -> lastSelection = start to end },
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(0, 5))
        composeRule.waitForIdle()
        assertEquals(
            "Precondition: selecting 'Hello' should report selection (0, 5)",
            Pair(0, 5),
            lastSelection
        )

        composeRule.onNodeWithTag("editor_bold_action").performClick()
        composeRule.waitForIdle()

        assertEquals("block_1", toggledBlock)
        assertEquals("bold", toggledMark)
        val (start, end) = selectionAtToggle ?: Pair(-1, -1)
        assertTrue(
            "Selection at toggle time should stay (0, 5) but was ($start, $end) — " +
                "tapping the button stole focus and collapsed the selection"
        )
    }
}
