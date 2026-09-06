package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.FakeFolderRepository
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.cancelFormula
import com.example.notesapp.ui.editor.viewmodel.onTableAction
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheet
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheetForEdit
import com.example.notesapp.ui.editor.viewmodel.redo
import com.example.notesapp.ui.editor.viewmodel.resetSelectedTextToBody
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.submitFormula
import com.example.notesapp.ui.editor.viewmodel.undo
import com.example.notesapp.ui.editor.viewmodel.updateFormulaSource
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NoteEditorUndoRedoContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createViewModel(initialDocument: NoteDocument = NoteDocument()): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Undo Redo Content Test",
                    content = initialDocument.toJsonString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
        val folderRepo = FakeFolderRepository()
        val summarizer = SummarizeNoteUseCase(
            object : NoteSummarizer {
                override suspend fun summarize(title: String, noteText: String): NoteSummary = NoteSummary("Summary")
            }
        )
        val categorizer = CategorizeNoteUseCase(
            object : FolderCategorizer {
                override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? = null
            }
        )
        val vm = NoteEditorViewModel(
            noteRepo,
            folderRepo,
            summarizer,
            categorizer,
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        vm.uiStateInternal.value = vm.uiStateInternal.value.copy(
            noteId = "note_1",
            title = "Undo Redo Content Test",
            document = initialDocument,
            isLoaded = true,
            isEditable = true
        )
        return vm
    }

    @Composable
    private fun EditorTestContent(viewModel: NoteEditorViewModel) {
        NotesTakingAppTheme {
            val state by viewModel.uiState.collectAsState()
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = state,
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = viewModel::onTitleChange,
                onRename = viewModel::rename,
                onToggleFavorite = viewModel::toggleFavorite,
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = viewModel::onTextBlockChange,
                onToggleCheckbox = viewModel::toggleCheckbox,
                onToggleCheckboxChecked = viewModel::toggleCheckboxChecked,
                onToggleMark = viewModel::toggleBlockMark,
                onResetBody = viewModel::resetSelectedTextToBody,
                onInsertBasicBlock = viewModel::insertBasicBlock,
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = viewModel::updateImageBlock,
                onAddTable = {},
                onTableCellChange = viewModel::updateTableCell,
                onFolderSelected = viewModel::onFolderSelected,
                onToggleFormattingToolbar = viewModel::toggleFormattingToolbar,
                onBlockFocused = { viewModel.setFocusedBlock(it) },
                onSelectionChange = viewModel::updateSelection,
                onDeleteBlock = viewModel::deleteBlock,
                onTableAction = viewModel::onTableAction,
                onOpenFormula = viewModel::openFormulaSheet,
                onFormulaSourceChange = viewModel::updateFormulaSource,
                onSubmitFormula = { viewModel.submitFormula() },
                onCancelFormula = viewModel::cancelFormula,
                onFormulaClick = viewModel::openFormulaSheetForEdit,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo
            )
        }
    }

    private fun NoteEditorViewModel.bodyText(): String {
        return uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TextBlock>()
            .joinToString("") { block -> block.children.joinToString("") { it.text } }
    }

    private fun bottomBar(): SemanticsNodeInteraction = composeRule.onNodeWithTag("editor_default_bottom_bar")

    private fun clickUndo() {
        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun discreteActionsUnwindAsSeparateSteps() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(""))))
            )
        )
        viewModel.setFocusedBlock("b1")
        composeRule.setContent { EditorTestContent(viewModel) }

        val body = composeRule.onAllNodesWithTag("editor_text_block")[0]
        body.performTextInput("Hello world")
        composeRule.waitForIdle()
        val afterTyping = viewModel.uiState.value.document

        // Apply Bold over the selected word through the formatting bar.
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(0, 5))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_bold_action").performClick()
        composeRule.waitForIdle()
        val afterBold = viewModel.uiState.value.document

        // Emoji insertion (the funnel path the emoji surface uses).
        viewModel.updateSelection(5, 5)
        assert(viewModel.insertEmoji("\uD83D\uDE00"))
        composeRule.waitForIdle()
        val afterEmoji = viewModel.uiState.value.document

        // Return to the default bottom bar before using the basic blocks panel.
        composeRule.onNodeWithTag("editor_hide_formatting").performClick()
        composeRule.waitForIdle()

        // Paragraph insertion through the basic blocks panel.
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("basic_blocks_text").performClick()
        composeRule.waitForIdle()
        val afterParagraph = viewModel.uiState.value.document

        clickUndo()
        assertEquals(afterEmoji, viewModel.uiState.value.document)
        clickUndo()
        assertEquals(afterBold, viewModel.uiState.value.document)
        clickUndo()
        assertEquals(afterTyping, viewModel.uiState.value.document)
        clickUndo()
        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
    }

    @Test
    fun undoOfBlockInsertionFallsFocusBackToPreviousBlock() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "p1", children = listOf(RichText("Paragraph"))))
            )
        )
        viewModel.setFocusedBlock("p1")
        composeRule.setContent { EditorTestContent(viewModel) }

        // Insert a heading below the focused paragraph through the basic blocks panel.
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("basic_blocks_heading_1").performClick()
        composeRule.waitForIdle()

        val headingBlock = viewModel.uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TextBlock>().last { it.type == "heading_1" }
        assertEquals(
            "p1",
            viewModel.uiState.value.document.blocks
                .takeWhile { it.id != headingBlock.id }.last().id
        )
        val insertedId = headingBlock.id

        clickUndo()
        val undone = viewModel.uiState.value
        assertEquals("p1", undone.focusedBlockId)
        assertFalse(undone.document.blocks.any { it.id == insertedId })
        assertEquals("Paragraph", viewModel.bodyText())
    }

    @Test
    fun tableCellTypingUndoRestoresCellAndCaret() {
        val table = EditorBlock.TableBlock(id = "tbl1")
        val viewModel = createViewModel(NoteDocument(blocks = listOf(table)))
        viewModel.uiStateInternal.value = viewModel.uiStateInternal.value.copy(
            focusedBlockId = "tbl1",
            focusedTableCells = mapOf("tbl1" to com.example.notesapp.ui.editor.model.TableFocusTarget(0, 0))
        )
        composeRule.setContent { EditorTestContent(viewModel) }
        val baselineDoc = viewModel.uiState.value.document

        val firstCell = composeRule.onAllNodesWithTag("editor_table_cell")[0]
        firstCell.performClick()
        composeRule.waitForIdle()
        firstCell.performTextInput("X")
        composeRule.waitForIdle()
        firstCell.performTextInput("Y")
        composeRule.waitForIdle()

        clickUndo()
        val undone = viewModel.uiState.value
        assertEquals(baselineDoc, undone.document)
        assertEquals(
            com.example.notesapp.ui.editor.model.TableFocusTarget(0, 0),
            undone.focusedTableCells["tbl1"]
        )
        composeRule.onNodeWithTag("editor_redo_action").let { redo ->
            bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
            redo.assertIsEnabled()
        }
    }
}
