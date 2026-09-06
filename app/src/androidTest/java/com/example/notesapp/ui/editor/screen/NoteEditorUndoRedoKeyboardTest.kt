package com.example.notesapp.ui.editor.screen

import android.content.Context
import android.os.SystemClock
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorUndoRedoKeyboardTest {

    @get:Rule
    val activityRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun makeActivityEdgeToEdge() {
        // Production MainActivity is edge-to-edge (WindowCompat.setDecorFitsSystemWindows false);
        // mirror that so IME insets dispatch into the composition and imePadding lifts the
        // toolbar above the keyboard exactly as it does in the shipped app.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activityRule.activity.window, false)
        }
        activityRule.waitForIdle()
    }
    private fun createViewModel(): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Keyboard Test Note",
                    content = NoteDocument(
                        blocks = listOf(
                            EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "")))
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
            noteId = "note_1",
            title = "Keyboard Test Note",
            document = NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = ""))))
            ),
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

    private fun isImeVisible(): Boolean {
        val decorView = activityRule.activity.window.decorView
        val insets = ViewCompat.getRootWindowInsets(decorView) ?: return false
        return insets.isVisible(WindowInsetsCompat.Type.ime())
    }

    private fun imeBottomInsetPx(): Int {
        val decorView = activityRule.activity.window.decorView
        val insets = ViewCompat.getRootWindowInsets(decorView) ?: return 0
        return insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
    }

    private fun ensureImeVisible(): Boolean {
        activityRule.waitForIdle()
        if (isImeVisible()) return true
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val activity = activityRule.activity
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val focused = activity.currentFocus
            if (focused != null) {
                imm.showSoftInput(focused, InputMethodManager.SHOW_IMPLICIT)
            } else {
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            }
        }
        // The IME window animates in; poll briefly for the inset to appear.
        val deadline = SystemClock.uptimeMillis() + 3_000L
        while (SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(100)
            if (isImeVisible()) return true
        }
        return isImeVisible()
    }

    @Test
    fun undoAppliesWhileImeVisibleAboveToolbar() {
        val viewModel = createViewModel()
        viewModel.setFocusedBlock("b1")
        activityRule.setContent { EditorTestContent(viewModel) }

        // Focus a body text block so the IME opens, then type.
        val body = activityRule.onAllNodesWithTag("editor_text_block")[0]
        body.performTextInput("keyboard typed")
        activityRule.waitForIdle()
        assertEquals("keyboard typed", viewModel.bodyText())

        assertTrue("The soft keyboard must be visible for this IME test", ensureImeVisible())
        // Let the IME slide-in animation settle so the inset and layout are final.
        SystemClock.sleep(1_200L)
        activityRule.waitForIdle()

        // The 56dp bottom toolbar remains reachable above the keyboard with Undo/Redo on it.
        val bottomBar = activityRule.onNodeWithTag("editor_default_bottom_bar")
        bottomBar.assertIsDisplayed()
        bottomBar.performScrollToNode(hasTestTag("editor_undo_action"))
        activityRule.onNodeWithTag("editor_undo_action").assertIsDisplayed()

        // The toolbar does not sit inside the IME inset region.
        val barBounds = bottomBar.getUnclippedBoundsInRoot()
        val decorHeight = activityRule.activity.window.decorView.height
        val imeInset = imeBottomInsetPx()
        val barBottomPx = with(activityRule.density) { barBounds.bottom.toPx() }
        assertTrue(
            "Toolbar (bottom=$barBottomPx px) must sit above the IME inset " +
                "(ime=$imeInset px on $decorHeight px screen)",
            barBottomPx <= (decorHeight - imeInset + 4).toFloat()
        )

        // Undo applies while the keyboard is up.
        activityRule.onNodeWithTag("editor_undo_action").performClick()
        activityRule.waitForIdle()
        assertEquals("", viewModel.bodyText())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)
    }
}
