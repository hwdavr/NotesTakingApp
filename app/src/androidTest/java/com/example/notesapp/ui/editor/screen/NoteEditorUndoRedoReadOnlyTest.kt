package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performKeyInput
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
import com.example.notesapp.ui.editor.viewmodel.redo
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.undo
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class NoteEditorUndoRedoReadOnlyTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createReadOnlyViewModel(): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_ro",
                    title = "Shared read only note",
                    content = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Locked text")))
                        )
                    ).toJsonString(),
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
            noteId = "note_ro",
            title = "Shared read only note",
            document = NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Locked text"))))
            ),
            isLoaded = true,
            isEditable = false
        )
        return vm
    }

    @Composable
    private fun EditorTestContent(viewModel: NoteEditorViewModel) {
        NotesTakingAppTheme {
            val state by viewModel.uiState.collectAsState()
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_ro",
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

    private fun pressCtrlShortcut(target: SemanticsNodeInteraction, key: Key, shift: Boolean = false) {
        target.performKeyInput {
            keyDown(Key.CtrlLeft)
            if (shift) keyDown(Key.ShiftLeft)
            keyDown(key)
            keyUp(key)
            if (shift) keyUp(Key.ShiftLeft)
            keyUp(Key.CtrlLeft)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun readOnlyNoteHidesUndoRedoAndIgnoresShortcuts() {
        val viewModel = createReadOnlyViewModel()
        viewModel.setFocusedBlock("b1")
        composeRule.setContent { EditorTestContent(viewModel) }

        // The read-only bottom bar renders no Undo/Redo surface.
        composeRule.onAllNodesWithTag("editor_undo_action").assertCountEquals(0)
        composeRule.onAllNodesWithTag("editor_redo_action").assertCountEquals(0)

        // Ctrl+Z and Ctrl+Shift+Z key events cause no document change on the read-only note.
        val body = composeRule.onAllNodesWithTag("editor_text_block")[0]
        pressCtrlShortcut(body, Key.Z)
        pressCtrlShortcut(body, Key.Z, shift = true)

        assertEquals("Locked text", viewModel.bodyText())
        assertEquals("Shared read only note", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)
    }
}
