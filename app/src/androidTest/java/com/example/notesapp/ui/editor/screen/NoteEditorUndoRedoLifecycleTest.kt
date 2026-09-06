package com.example.notesapp.ui.editor.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
class NoteEditorUndoRedoLifecycleTest {

    @get:Rule
    val activityRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: NoteEditorViewModel

    private fun viewModelFactory(): ViewModelProvider.Factory {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Lifecycle Test Note",
                    content = NoteDocument(
                        blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = ""))))
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
        return viewModelFactory {
            initializer {
                NoteEditorViewModel(
                    noteRepo,
                    folderRepo,
                    summarizer,
                    categorizer,
                    deleteVoiceAudio,
                    deleteVoiceBlock
                )
            }
        }
    }

    @Composable
    private fun EditorTestContent() {
        val vm: NoteEditorViewModel = viewModel(factory = viewModelFactory())
        viewModel = vm
        val state by vm.uiState.collectAsState()
        NotesTakingAppTheme {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = state,
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = vm::onTitleChange,
                onRename = vm::rename,
                onToggleFavorite = vm::toggleFavorite,
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = vm::onTextBlockChange,
                onToggleCheckbox = vm::toggleCheckbox,
                onToggleCheckboxChecked = vm::toggleCheckboxChecked,
                onToggleMark = vm::toggleBlockMark,
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = vm::updateImageBlock,
                onAddTable = {},
                onTableCellChange = vm::updateTableCell,
                onFolderSelected = vm::onFolderSelected,
                onToggleFormattingToolbar = vm::toggleFormattingToolbar,
                onBlockFocused = vm::setFocusedBlock,
                onSelectionChange = vm::updateSelection,
                onDeleteBlock = vm::deleteBlock,
                onUndo = vm::undo,
                onRedo = vm::redo
            )
        }
    }

    private fun NoteEditorViewModel.bodyText(): String {
        return uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TextBlock>()
            .joinToString("") { it.text() }
    }

    @Test
    fun undoHistorySurvivesActivityRecreation() {
        val restorationTester = StateRestorationTester(activityRule)
        restorationTester.setContent {
            EditorTestContent()
        }
        // NoteEditorScreenContent does not load the note itself (the top-level screen does), so
        // bind the retained ViewModel to the fake note before interacting.
        viewModel.load("note_1")
        activityRule.waitForIdle()

        // Type a body run and undo it once: the editor shows the undone text with Redo enabled.
        activityRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("recreated")
        activityRule.waitForIdle()
        assertEquals("recreated", viewModel.bodyText())

        activityRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_undo_action"))
        activityRule.onNodeWithTag("editor_undo_action").performClick()
        activityRule.waitForIdle()
        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)

        // Recreate the activity: the retained ViewModel keeps the undone state and the redo tail.
        restorationTester.emulateSavedInstanceStateRestore()
        activityRule.waitForIdle()

        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)

        // The re-rendered editor exposes Redo enabled over the undone content.
        activityRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_redo_action"))
        activityRule.onNodeWithTag("editor_redo_action").assertIsEnabled()

        // A further Redo restores the typed text.
        activityRule.onNodeWithTag("editor_redo_action").performClick()
        activityRule.waitForIdle()
        assertEquals("recreated", viewModel.bodyText())
        assertTrue(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)
    }
}
