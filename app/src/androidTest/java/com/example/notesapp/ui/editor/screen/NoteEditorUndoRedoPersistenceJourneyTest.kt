package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorUndoRedoPersistenceJourneyTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val noteId = "journey-note"
    private val baseDocument = NoteDocument(
        blocks = listOf(
            EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Base text"))),
            EditorBlock.TextBlock(id = "b2", children = listOf(RichText(text = "")))
        )
    )

    private fun noteRepository(): FakeNoteRepository = FakeNoteRepository(
        listOf(
            Note(
                id = noteId,
                title = "Journey Note",
                content = baseDocument.toJsonString(),
                createdAt = 1_000L,
                updatedAt = 1_000L
            )
        )
    )

    private fun createViewModel(noteRepo: FakeNoteRepository): NoteEditorViewModel {
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
        return NoteEditorViewModel(
            noteRepo,
            folderRepo,
            summarizer,
            categorizer,
            deleteVoiceAudio,
            deleteVoiceBlock
        )
    }

    @Composable
    private fun EditorTestContent(viewModel: NoteEditorViewModel, onBack: () -> Unit) {
        NotesTakingAppTheme {
            val state by viewModel.uiState.collectAsState()
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = noteId,
                state = state,
                onBack = onBack,
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

    @Test
    fun undoStatePersistsAcrossExitAndReopenWithFreshBaseline() {
        val noteRepo = noteRepository()

        // Session 1: open the note through the load path (as the Editor destination does).
        val editorViewModel = createViewModel(noteRepo)
        editorViewModel.load(noteId)
        var leftEditor = false
        composeRule.setContent {
            EditorTestContent(editorViewModel, onBack = {
                editorViewModel.handleBackPress { leftEditor = true }
            })
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { editorViewModel.uiState.value.isLoaded }
        composeRule.waitForIdle()

        // Type into the trailing body block and undo it through the toolbar.
        val trailingBlockId = editorViewModel.uiState.value.document.blocks.last().id
        editorViewModel.setFocusedBlock(trailingBlockId)
        val bodyBlocks = composeRule.onAllNodesWithTag("editor_text_block")
        bodyBlocks[bodyBlocks.fetchSemanticsNodes().size - 1].performTextInput("typed tail")
        composeRule.waitForIdle()
        assertEquals("Base texttyped tail", editorViewModel.bodyText())

        composeRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_undo_action"))
        composeRule.onNodeWithTag("editor_undo_action").performClick()
        composeRule.waitForIdle()
        assertEquals("Base text", editorViewModel.bodyText())
        assertFalse(editorViewModel.uiState.value.canUndo)
        assertTrue(editorViewModel.uiState.value.canRedo)

        // Leave the note with the top-bar back action (the production save-on-back path).
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { leftEditor }
        composeRule.waitForIdle()

        // The repository now holds the undone document as the note's current content.
        val persisted = runBlockingLookup(noteRepo)
        assertEquals("Base text", persistedContentText(persisted))

        // Session 2: reopen the same note from the list into a fresh Editor destination.
        val reopenedViewModel = createViewModel(noteRepo)
        reopenedViewModel.load(noteId)
        composeRule.waitUntil(timeoutMillis = 5_000) { reopenedViewModel.uiState.value.isLoaded }
        composeRule.waitForIdle()

        assertEquals("Base text", reopenedViewModel.bodyText())
        // Fresh baseline: no earlier history is reachable in either direction.
        assertFalse(reopenedViewModel.uiState.value.canUndo)
        assertFalse(reopenedViewModel.uiState.value.canRedo)
    }

    private fun runBlockingLookup(repo: FakeNoteRepository): Note? {
        val note = try {
            kotlinx.coroutines.runBlocking { repo.getNoteById(noteId) }
        } catch (_: Throwable) {
            null
        }
        return note
    }

    private fun persistedContentText(note: Note?): String? {
        if (note == null) return null
        return NoteDocument.fromContent(note.content)
            .blocks.filterIsInstance<EditorBlock.TextBlock>()
            .joinToString("") { it.text() }
    }
}
