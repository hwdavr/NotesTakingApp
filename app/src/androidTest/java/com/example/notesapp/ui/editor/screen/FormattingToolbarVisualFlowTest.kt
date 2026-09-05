package com.example.notesapp.ui.editor.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
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
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerItem
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerUiState
import com.example.notesapp.ui.editor.viewmodel.cancelFormula
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheet
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheetForEdit
import com.example.notesapp.ui.editor.viewmodel.resetSelectedTextToBody
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.submitFormula
import com.example.notesapp.ui.editor.viewmodel.updateFormulaSource
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormattingToolbarVisualFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createViewModel(initialDocument: NoteDocument = NoteDocument()): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Visual Flow Test Note",
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
            title = "Visual Flow Test Note",
            document = initialDocument,
            isLoaded = true,
            isEditable = true,
            isFormattingToolbarVisible = true
        )
        return vm
    }

    private fun captureVisualEvidence(fileName: String) {
        composeRule.waitForIdle()
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

    private fun differingPixelCount(first: ImageBitmap, second: ImageBitmap): Int {
        val bmp1 = first.asAndroidBitmap()
        val bmp2 = second.asAndroidBitmap()
        val width = minOf(bmp1.width, bmp2.width)
        val height = minOf(bmp1.height, bmp2.height)
        var diff = 0
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (bmp1.getPixel(x, y) != bmp2.getPixel(x, y)) {
                    diff++
                }
            }
        }
        return diff
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
                onFormulaClick = viewModel::openFormulaSheetForEdit
            )
        }
    }

    @Test
    fun captureToolbarSelection() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(
                        id = "b1",
                        children = listOf(
                            RichText(text = "The quick brown fox jumps over the lazy dog.")
                        )
                    )
                )
            )
        )
        viewModel.setFocusedBlock("b1")
        viewModel.updateSelection(4, 15) // select "quick brown"

        composeRule.setContent {
            EditorTestContent(viewModel)
        }

        composeRule.waitForIdle()
        captureVisualEvidence("formatting_toolbar_selection")
    }

    @Test
    fun captureEditorKeyboard() {
        val viewModel = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(
                        id = "b1",
                        children = listOf(
                            RichText(text = "NotesTakingApp Formatting ")
                        )
                    )
                )
            )
        )
        viewModel.setFocusedBlock("b1")
        val len = "NotesTakingApp Formatting ".length
        viewModel.updateSelection(len, len)
        viewModel.toggleBlockMark("b1", "bold")

        composeRule.setContent {
            EditorTestContent(viewModel)
        }

        composeRule.waitForIdle()
        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        viewModel.onTextBlockChange("b1", "NotesTakingApp Formatting BOLD")
        composeRule.waitForIdle()
        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(differingPixelCount(beforeImage, afterImage) > 0)

        captureVisualEvidence("formatting_toolbar_keyboard")
    }

    @Test
    fun captureLinkPicker() {
        val items = listOf(
            NoteLinkPickerItem(id = "n1", title = "Project Architecture", folderName = "Engineering"),
            NoteLinkPickerItem(id = "n2", title = "Q3 Objectives & Key Results", folderName = "Planning"),
            NoteLinkPickerItem(id = "n3", title = "Weekly Sync Notes", folderName = null),
            NoteLinkPickerItem(id = "n4", title = "Design System Reference", folderName = "Design")
        )
        val uiState = NoteLinkPickerUiState.Content(
            searchQuery = "",
            notes = items,
            hasExistingLink = true
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                NoteLinkPickerScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onSearchQueryChanged = {},
                    onRetry = {},
                    onSelectNote = { _, _ -> },
                    onRemoveLink = {}
                )
            }
        }

        composeRule.waitForIdle()
        captureVisualEvidence("note_link_picker")
    }

    @Test
    fun captureFormulaDefault() {
        val viewModel = createViewModel()
        viewModel.openFormulaSheet()

        composeRule.setContent {
            EditorTestContent(viewModel)
        }

        composeRule.waitForIdle()
        captureVisualEvidence("formula_sheet_default")
    }

    @Test
    fun captureFormulaInvalid() {
        val viewModel = createViewModel()
        viewModel.openFormulaSheet()
        viewModel.updateFormulaSource("\\frac{1}{")
        viewModel.submitFormula()

        composeRule.setContent {
            EditorTestContent(viewModel)
        }

        composeRule.waitForIdle()
        captureVisualEvidence("formula_sheet_invalid")
    }

    @Test
    fun captureFormulaSheetKeyboard() {
        val viewModel = createViewModel()
        viewModel.openFormulaSheet()
        viewModel.updateFormulaSource("\\int_{-\\infty}^{\\infty} e^{-x^2} dx = \\sqrt{\\pi}")

        composeRule.setContent {
            EditorTestContent(viewModel)
        }

        composeRule.waitForIdle()
        captureVisualEvidence("formula_sheet_keyboard")
    }

    @Test
    fun captureFormulaSheetDarkTheme() {
        val viewModel = createViewModel()
        viewModel.openFormulaSheet()
        viewModel.updateFormulaSource("f(x) = \\sum_{n=0}^{\\infty} \\frac{f^{(n)}(a)}{n!} (x-a)^n")

        composeRule.setContent {
            NotesTakingAppTheme(darkTheme = true) {
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
                    onFormulaClick = viewModel::openFormulaSheetForEdit
                )
            }
        }

        composeRule.waitForIdle()
        captureVisualEvidence("formula_sheet_dark_theme")
    }
}
