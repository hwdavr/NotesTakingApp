package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
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
import com.example.notesapp.ui.editor.mapper.INLINE_FORMULA_PLACEHOLDER
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.cancelFormula
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheet
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheetForEdit
import com.example.notesapp.ui.editor.viewmodel.resetSelectedTextToBody
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.submitFormula
import com.example.notesapp.ui.editor.viewmodel.updateFormulaSource
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NoteEditorFormulaAndWordSelectionReproductionTest {
    @get:Rule val composeRule = createComposeRule()

    private fun createViewModel(initialDocument: NoteDocument = NoteDocument()): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Test Note",
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

    @Test
    fun formulaRendersVisibleDisplayText_notObjPlaceholder() {
        val formulaBlock = EditorBlock.TextBlock(
            id = "block_formula",
            children = listOf(
                RichText(
                    text = INLINE_FORMULA_PLACEHOLDER,
                    formulaSource = "E=mc^2",
                    inlineId = "formula_1"
                )
            )
        )
        val rawPlaceholderBlock = EditorBlock.TextBlock(
            id = "block_raw",
            children = listOf(RichText(INLINE_FORMULA_PLACEHOLDER))
        )
        val document = NoteDocument(blocks = listOf(formulaBlock, rawPlaceholderBlock))
        val viewModel = createViewModel(document)
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = document,
            focusedBlockId = null,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        val textBlocks = composeRule.onAllNodesWithTag("editor_text_block")
        val formulaImage = textBlocks[0].captureToImage()
        val rawPlaceholderImage = textBlocks[1].captureToImage()

        // Before fix: Formula renders as \uFFFC (the raw OBJ placeholder), producing 0 differing pixels from rawPlaceholderImage.
        // The test asserts that the rendered formula visibly differs from the raw placeholder [OBJ] glyph.
        assertTrue(
            "Formula block must visually render formatted math text differing from raw OBJ glyph",
            differingPixelCount(formulaImage, rawPlaceholderImage) > 0
        )
    }

    @Test
    fun doubleTapSelectsWordUnderCursor() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Hello world test"))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            selectionStart = 0,
            selectionEnd = 0,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        // Perform double-click/double-tap in the center of the text block
        composeRule.onNodeWithTag("editor_text_block").performTouchInput {
            doubleClick(center)
        }
        composeRule.waitForIdle()

        val selStart = viewModel.uiState.value.selectionStart
        val selEnd = viewModel.uiState.value.selectionEnd

        // Before fix: Double click does not select the word around the cursor (selStart == selEnd).
        assertTrue(
            "Double-tap must select word around cursor (start=$selStart, end=$selEnd)",
            selEnd > selStart
        )
    }
}
