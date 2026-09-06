package com.example.notesapp.ui.editor.screen

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
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
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorUndoRedoVisualFlowTest {

    @get:Rule
    val activityRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun makeActivityEdgeToEdge() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            WindowCompat.setDecorFitsSystemWindows(activityRule.activity.window, false)
        }
        activityRule.waitForIdle()
    }

    private fun createViewModel(document: NoteDocument, editable: Boolean = true): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Undo & Redo Visual Note",
                    content = document.toJsonString(),
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
            title = "Undo & Redo Visual Note",
            document = document,
            isLoaded = true,
            isEditable = editable
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
                onBlockFocused = viewModel::setFocusedBlock,
                onSelectionChange = viewModel::updateSelection,
                onDeleteBlock = viewModel::deleteBlock,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo
            )
        }
    }

    private fun revealUndoRedo() {
        // Scroll the default toolbar so both controls sit in the visible rail, mirroring the mockups.
        activityRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_redo_action"))
        activityRule.waitForIdle()
    }

    private fun settleImeInsetsIfVisible() {
        // Typing opens the IME; its inset animates in asynchronously and can race geometry
        // assertions. Wait briefly so the layout is final before measuring or capturing.
        if (isImeVisible()) {
            SystemClock.sleep(900L)
            activityRule.waitForIdle()
        }
    }

    private fun assertToolbarRailGeometry() {
        // The bar is a fixed 56dp rail; Undo and Redo are 48dp-tall buttons vertically centered in it.
        // boundsInRoot is in pixels; convert with the display density for dp assertions.
        val pxPerDp = activityRule.density.density
        val barBounds = activityRule.onNodeWithTag("editor_default_bottom_bar")
            .fetchSemanticsNode().boundsInRoot
        val undoBounds = activityRule.onNodeWithTag("editor_undo_action")
            .fetchSemanticsNode().boundsInRoot
        val redoBounds = activityRule.onNodeWithTag("editor_redo_action")
            .fetchSemanticsNode().boundsInRoot
        val barHeightDp = barBounds.height / pxPerDp
        val undoHeightDp = undoBounds.height / pxPerDp
        val redoHeightDp = redoBounds.height / pxPerDp
        assertTrue("bar must be >= 48dp tall (was ${barHeightDp}dp)", barHeightDp >= 48f)
        assertTrue("undo button must be >= 48dp tall (was ${undoHeightDp}dp)", undoHeightDp >= 48f)
        assertTrue("redo button must be >= 48dp tall (was ${redoHeightDp}dp)", redoHeightDp >= 48f)
        assertTrue("undo and redo must share the same rail row", kotlin.math.abs(undoBounds.top - redoBounds.top) < 1f)
        // Buttons stay inside the vertical extent of the bar rail.
        assertTrue(
            "undo must sit inside the bar",
            undoBounds.top >= barBounds.top - 1f && undoBounds.bottom <= barBounds.bottom + 1f
        )
        assertTrue(
            "redo must sit inside the bar",
            redoBounds.top >= barBounds.top - 1f && redoBounds.bottom <= barBounds.bottom + 1f
        )
    }

    private fun imeBottomInsetPx(): Int {
        val decorView = activityRule.activity.window.decorView
        val insets = ViewCompat.getRootWindowInsets(decorView) ?: return 0
        return insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
    }

    private fun assertBarAboveImeInset() {
        val bottomBar = activityRule.onNodeWithTag("editor_default_bottom_bar")
        bottomBar.assertIsDisplayed()
        // boundsInRoot is in pixels, as are the decor height and the IME inset.
        val barBottomPx = bottomBar.fetchSemanticsNode().boundsInRoot.bottom
        val decorHeight = activityRule.activity.window.decorView.height
        val imeInset = imeBottomInsetPx()
        assertTrue(
            "Toolbar (bottom=$barBottomPx px) must sit above the IME inset " +
                "(ime=$imeInset px on $decorHeight px screen)",
            barBottomPx <= (decorHeight - imeInset + 4).toFloat()
        )
    }

    private fun captureVisualEvidence(fileName: String) {
        activityRule.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Could not capture visual evidence: $fileName")
        val directory = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir("visual_evidence")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        val screenshot = File(directory, "$fileName.png")
        screenshot.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write visual evidence: $fileName"
            }
        }
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("cp ${screenshot.absolutePath} /sdcard/Download/$fileName.png")
            .use { }
    }

    private fun isImeVisible(): Boolean {
        val decorView = activityRule.activity.window.decorView
        val insets = ViewCompat.getRootWindowInsets(decorView) ?: return false
        return insets.isVisible(WindowInsetsCompat.Type.ime())
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
        val deadline = SystemClock.uptimeMillis() + 3_000L
        while (SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(100)
            if (isImeVisible()) return true
        }
        return isImeVisible()
    }

    @Test
    fun captureUndoRedoDisabledAtBaseline() {
        val document = NoteDocument(
            blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Baseline content"))))
        )
        val viewModel = createViewModel(document)
        activityRule.setContent { EditorTestContent(viewModel) }

        revealUndoRedo()
        assertToolbarRailGeometry()
        activityRule.onNodeWithTag("editor_undo_action").assertIsNotEnabled()
        activityRule.onNodeWithTag("editor_redo_action").assertIsNotEnabled()
        captureVisualEvidence("undo_redo_disabled_baseline")
    }

    @Test
    fun captureUndoEnabledRedoDisabled() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Written content"))),
                EditorBlock.TextBlock(id = "b2", children = listOf(RichText(text = "")))
            )
        )
        val viewModel = createViewModel(document)
        viewModel.setFocusedBlock("b2")
        activityRule.setContent { EditorTestContent(viewModel) }

        activityRule.onAllNodesWithTag("editor_text_block")[1].performTextInput(" more")
        activityRule.waitForIdle()
        settleImeInsetsIfVisible()

        revealUndoRedo()
        assertToolbarRailGeometry()
        activityRule.onNodeWithTag("editor_undo_action").assertIsEnabled()
        activityRule.onNodeWithTag("editor_redo_action").assertIsNotEnabled()
        captureVisualEvidence("undo_redo_undo_enabled")
    }

    @Test
    fun captureRedoEnabledAfterUndo() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Written content"))),
                EditorBlock.TextBlock(id = "b2", children = listOf(RichText(text = "")))
            )
        )
        val viewModel = createViewModel(document)
        viewModel.setFocusedBlock("b2")
        activityRule.setContent { EditorTestContent(viewModel) }

        activityRule.onAllNodesWithTag("editor_text_block")[1].performTextInput(" more")
        activityRule.waitForIdle()
        activityRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_undo_action"))
        activityRule.onNodeWithTag("editor_undo_action").performClick()
        activityRule.waitForIdle()
        settleImeInsetsIfVisible()

        revealUndoRedo()
        assertToolbarRailGeometry()
        activityRule.onNodeWithTag("editor_undo_action").assertIsNotEnabled()
        activityRule.onNodeWithTag("editor_redo_action").assertIsEnabled()
        captureVisualEvidence("undo_redo_redo_enabled")
    }

    @Test
    fun captureUndoRedoKeyboardVisible() {
        val document = NoteDocument(
            blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Keyboard note"))))
        )
        val viewModel = createViewModel(document)
        viewModel.setFocusedBlock("b1")
        activityRule.setContent { EditorTestContent(viewModel) }

        // Focus a body block so the IME opens, then confirm the toolbar still sits above it.
        val body = activityRule.onAllNodesWithTag("editor_text_block")[0]
        body.performTextInput("typed")
        activityRule.waitForIdle()
        check(ensureImeVisible()) { "Soft keyboard must be visible for the keyboard capture" }
        // Let the IME slide-in animation settle so the inset and layout are final.
        SystemClock.sleep(1_000L)
        activityRule.waitForIdle()

        revealUndoRedo()
        assertToolbarRailGeometry()
        assertBarAboveImeInset()
        captureVisualEvidence("undo_redo_keyboard_visible")
    }

    @Test
    fun captureReadOnlyWithoutUndoRedo() {
        val document = NoteDocument(
            blocks = listOf(EditorBlock.TextBlock(id = "b1", children = listOf(RichText(text = "Locked content"))))
        )
        val viewModel = createViewModel(document, editable = false)
        activityRule.setContent { EditorTestContent(viewModel) }

        activityRule.waitForIdle()
        activityRule.onAllNodesWithTag("editor_undo_action").assertCountEquals(0)
        activityRule.onAllNodesWithTag("editor_redo_action").assertCountEquals(0)
        val readOnlyBarBounds = activityRule.onNodeWithTag("editor_read_only_bottom_bar")
            .fetchSemanticsNode().boundsInRoot
        val pxPerDp = activityRule.density.density
        assertTrue(
            "read-only bar must be >= 48dp tall (was ${readOnlyBarBounds.height / pxPerDp}dp)",
            readOnlyBarBounds.height / pxPerDp >= 48f
        )
        captureVisualEvidence("undo_redo_read_only_absent")
    }
}
