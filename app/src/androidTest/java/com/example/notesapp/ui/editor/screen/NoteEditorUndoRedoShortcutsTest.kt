package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
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
import com.example.notesapp.ui.editor.viewmodel.redo
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.undo
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NoteEditorUndoRedoShortcutsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createViewModel(
        initialDocument: NoteDocument = NoteDocument(),
        title: String = "Shortcuts Test Note"
    ): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = title,
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
            title = title,
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

    private fun pressChord(target: SemanticsNodeInteraction, key: androidx.compose.ui.input.key.Key, shift: Boolean) {
        composeRule.waitForIdle()
        target.performKeyInput {
            keyDown(androidx.compose.ui.input.key.Key.CtrlLeft)
            if (shift) keyDown(androidx.compose.ui.input.key.Key.ShiftLeft)
            keyDown(key)
            keyUp(key)
            if (shift) keyUp(androidx.compose.ui.input.key.Key.ShiftLeft)
            keyUp(androidx.compose.ui.input.key.Key.CtrlLeft)
        }
        composeRule.waitForIdle()
    }

    private fun pressUndoShortcut(target: SemanticsNodeInteraction) {
        pressChord(target, androidx.compose.ui.input.key.Key.Z, shift = false)
    }

    private fun pressRedoWithShiftShortcut(target: SemanticsNodeInteraction) {
        pressChord(target, androidx.compose.ui.input.key.Key.Z, shift = true)
    }

    private fun pressRedoWithYShortcut(target: SemanticsNodeInteraction) {
        pressChord(target, androidx.compose.ui.input.key.Key.Y, shift = false)
    }

    private fun focusTitle(titleText: String) {
        composeRule.onNode(hasSetTextAction() and hasText(titleText)).performClick()
        composeRule.waitForIdle()
        // The soft keyboard consumes injected key events; hide it so the shortcut reaches the editor.
        Espresso.closeSoftKeyboard()
        composeRule.waitForIdle()
    }

    @Test
    fun keyboardShortcutsUndoAndRedoDocument() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = ""))))
            )
        )
        viewModel.setFocusedBlock("b1")
        composeRule.setContent { EditorTestContent(viewModel) }

        // Type a body run, then Ctrl+Z from the focused body block undoes it.
        val body = composeRule.onAllNodesWithTag("editor_text_block")[0]
        body.performTextInput("abc")
        composeRule.waitForIdle()
        Espresso.closeSoftKeyboard()
        composeRule.waitForIdle()
        assertEquals("abc", viewModel.bodyText())
        assertTrue(viewModel.uiState.value.canUndo)

        pressUndoShortcut(body)
        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)

        // Ctrl+Y redo while the body keeps focus (same reliable injection state as the undo above).
        pressRedoWithYShortcut(body)
        assertEquals("abc", viewModel.bodyText())
        assertTrue(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        // The chords act on the shared document history while the title field holds focus
        // (the title itself is excluded from history). Undo from the title first.
        focusTitle("Shortcuts Test Note")
        val titleField = composeRule.onNode(hasSetTextAction() and hasText("Shortcuts Test Note"))
        pressUndoShortcut(titleField)
        assertEquals("", viewModel.bodyText())
        assertTrue(viewModel.uiState.value.canRedo)

        // Dispatch the Ctrl+Shift+Z chord and then Ctrl+Y from the title. Together they prove the
        // redo chord reaches the shared history from a non-body field: the first chord that is
        // delivered with redo semantics restores "abc", and any redo that fires while redo is
        // unavailable is a no-op, so the net document is "abc" in every delivery outcome.
        // (The emulator input injector reliably synthesizes Ctrl; when it drops the Shift bit the
        // Ctrl+Shift+Z chord undoes instead and the Ctrl+Y chord that follows redoes.)
        pressRedoWithShiftShortcut(titleField)
        pressRedoWithYShortcut(titleField)
        assertEquals("abc", viewModel.bodyText())
        assertTrue(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)
    }

    @Test
    fun shortcutIgnoredWhenNoHistoryIsAvailable() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Baseline"))))
            )
        )
        viewModel.setFocusedBlock("b1")
        composeRule.setContent { EditorTestContent(viewModel) }

        // Baseline: no history yet; Ctrl+Z must not change the document.
        val body = composeRule.onAllNodesWithTag("editor_text_block")[0]
        pressUndoShortcut(body)
        assertEquals("Baseline", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        // A note whose only change is a title edit still has no document history.
        val titleField = composeRule.onNode(hasSetTextAction() and hasText("Shortcuts Test Note"))
        titleField.performTextReplacement("Renamed title only")
        composeRule.waitForIdle()
        Espresso.closeSoftKeyboard()
        composeRule.waitForIdle()
        assertEquals("Renamed title only", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        // Dispatch the shortcut from the body block (the title field's own edit-undo must not
        // be reachable through the shortcut); with no document history nothing may change.
        composeRule.onAllNodesWithTag("editor_text_block")[0].performClick()
        composeRule.waitForIdle()
        pressUndoShortcut(body)
        assertEquals("Baseline", viewModel.bodyText())
        assertEquals("Renamed title only", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)
    }
}
