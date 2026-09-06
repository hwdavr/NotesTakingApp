package com.example.notesapp.ui.editor.screen

import android.os.SystemClock
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
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
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.cancelFormula
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorUndoRedoTextTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createViewModel(
        initialDocument: NoteDocument = NoteDocument(),
        isEditable: Boolean = true
    ): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Undo Redo Test Note",
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
        val deleteVoiceAudio = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true)
        val deleteVoiceBlock = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)

        val vm = NoteEditorViewModel(
            noteRepo,
            folderRepo,
            summarizer,
            categorizer,
            deleteVoiceAudio,
            deleteVoiceBlock
        )
        vm.uiStateInternal.value = vm.uiStateInternal.value.copy(
            noteId = "note_1",
            title = "Undo Redo Test Note",
            document = initialDocument,
            isLoaded = true,
            isEditable = isEditable
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
            .joinToString("") { it.text() }
    }

    private fun bottomBar(): SemanticsNodeInteraction = composeRule.onNodeWithTag("editor_default_bottom_bar")

    private fun clickToolbarAction(tag: String) {
        bottomBar().performScrollToNode(hasTestTag(tag))
        composeRule.onNodeWithTag(tag).performClick()
    }

    private fun typeIntoSecondBodyBlock(text: String) {
        composeRule.onAllNodesWithTag("editor_text_block")[1].performTextInput(text)
        composeRule.waitForIdle()
    }

    @Test
    fun typingThenUndoRestoresPreviousText() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Earlier text."))),
                    EditorBlock.TextBlock(id = "b2", children = listOf(RichText(text = "")))
                )
            )
        )
        viewModel.setFocusedBlock("b2")
        composeRule.setContent { EditorTestContent(viewModel) }

        typeIntoSecondBodyBlock("New sentence")
        assertEquals("Earlier text.New sentence", viewModel.bodyText())

        // One undo removes the whole typing run, keeping the earlier text in order.
        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").performClick()
        composeRule.waitForIdle()

        assertEquals("Earlier text.", viewModel.bodyText())
        val undone = viewModel.uiState.value
        assertEquals("b2", undone.focusedBlockId)
        assertEquals(0, undone.selectionStart)
        assertTrue(undone.canRedo)

        // Undo stays available for the next edit.
        typeIntoSecondBodyBlock("More")
        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").assertIsEnabled()
    }

    @Test
    fun undoRedoDisabledAtBaselineMakeNoChange() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Baseline text")))
                )
            )
        )
        composeRule.setContent { EditorTestContent(viewModel) }

        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        val undo = composeRule.onNodeWithTag("editor_undo_action")
        undo.assertIsNotEnabled()
        undo.performClick()
        composeRule.waitForIdle()
        assertEquals("Baseline text", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)

        bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
        val redo = composeRule.onNodeWithTag("editor_redo_action")
        redo.assertIsNotEnabled()
        redo.performClick()
        composeRule.waitForIdle()
        assertEquals("Baseline text", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canRedo)
    }

    @Test
    fun redoReplaysUndoneText() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Start"))),
                    EditorBlock.TextBlock(id = "b2", children = listOf(RichText(text = "")))
                )
            )
        )
        viewModel.setFocusedBlock("b2")
        composeRule.setContent { EditorTestContent(viewModel) }

        typeIntoSecondBodyBlock("typed")
        assertEquals("Starttyped", viewModel.bodyText())

        clickToolbarAction("editor_undo_action")
        composeRule.waitForIdle()
        assertEquals("Start", viewModel.bodyText())

        clickToolbarAction("editor_redo_action")
        composeRule.waitForIdle()
        assertEquals("Starttyped", viewModel.bodyText())

        // At the head, Redo is disabled and a tap makes no change.
        bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
        val redo = composeRule.onNodeWithTag("editor_redo_action")
        redo.assertIsNotEnabled()
        redo.performClick()
        composeRule.waitForIdle()
        assertEquals("Starttyped", viewModel.bodyText())
    }

    @Test
    fun newEditAfterUndoClearsRedo() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "")))
                )
            )
        )
        viewModel.setFocusedBlock("b1")
        composeRule.setContent { EditorTestContent(viewModel) }

        // Type A, pause past the coalescing window, type B -> two undo steps.
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("A")
        composeRule.waitForIdle()
        SystemClock.sleep(1_100L)
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("B")
        composeRule.waitForIdle()
        assertEquals("AB", viewModel.bodyText())

        clickToolbarAction("editor_undo_action")
        composeRule.waitForIdle()
        assertEquals("A", viewModel.bodyText())
        clickToolbarAction("editor_undo_action")
        composeRule.waitForIdle()
        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)

        // A fresh edit after the undos discards the redo tail.
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("C")
        composeRule.waitForIdle()
        assertEquals("C", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canRedo)
        bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
        composeRule.onNodeWithTag("editor_redo_action").assertIsNotEnabled()

        clickToolbarAction("editor_undo_action")
        composeRule.waitForIdle()
        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        // C became redoable again after its undo.
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun undoEnabledAfterTypingRedoDisabledUntilUndo() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "")))
                )
            )
        )
        viewModel.setFocusedBlock("b1")
        composeRule.setContent { EditorTestContent(viewModel) }

        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("abc")
        composeRule.waitForIdle()

        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").assertIsEnabled()
        bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
        composeRule.onNodeWithTag("editor_redo_action").assertIsNotEnabled()

        clickToolbarAction("editor_undo_action")
        composeRule.waitForIdle()

        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").assertIsNotEnabled()
        bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
        composeRule.onNodeWithTag("editor_redo_action").assertIsEnabled()
    }

    @Test
    fun titleOnlyTypingKeepsUndoRedoDisabled() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Doc")))
                )
            )
        )
        composeRule.setContent { EditorTestContent(viewModel) }

        val titleField = composeRule.onNode(hasSetTextAction() and hasText("Undo Redo Test Note"))
        titleField.performTextReplacement("Edited title")
        composeRule.waitForIdle()

        assertEquals("Edited title", viewModel.uiState.value.title)
        assertEquals("Doc", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        bottomBar().performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").assertIsNotEnabled()
        bottomBar().performScrollToNode(hasTestTag("editor_redo_action"))
        composeRule.onNodeWithTag("editor_redo_action").assertIsNotEnabled()
    }

    @Test
    fun readOnlyNoteDoesNotRenderUndoOrRedo() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Locked")))
                )
            ),
            isEditable = false
        )
        composeRule.setContent { EditorTestContent(viewModel) }

        composeRule.onAllNodesWithTag("editor_undo_action").assertCountEquals(0)
        composeRule.onAllNodesWithTag("editor_redo_action").assertCountEquals(0)
    }
}
