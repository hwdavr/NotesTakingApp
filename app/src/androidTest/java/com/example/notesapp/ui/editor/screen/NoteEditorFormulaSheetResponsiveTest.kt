package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.FakeFolderRepository
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.R
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

@RunWith(AndroidJUnit4::class)
class NoteEditorFormulaSheetResponsiveTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

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

    @Test
    fun longFormulaPreviewScrollsHorizontallyWithoutWrapping() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Formula Test"))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_action").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()

        val longFormulaSource = "\\alpha + \\beta + \\gamma + \\delta + \\epsilon + \\zeta + " +
            "\\eta + \\theta + \\iota + \\kappa + \\lambda + \\mu + \\nu + \\xi + " +
            "\\pi + \\rho + \\sigma + \\tau + \\upsilon + \\phi + \\chi + \\psi + \\omega"
        composeRule.onNodeWithTag("editor_formula_source_input").performTextInput(longFormulaSource)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_preview").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_preview_text").assertIsDisplayed()

        val previewBounds = composeRule.onNodeWithTag("editor_formula_preview").getUnclippedBoundsInRoot()
        val textBounds = composeRule.onNodeWithTag("editor_formula_preview_text").getUnclippedBoundsInRoot()
        assertTrue(
            "Preview text width (${textBounds.width}) should be wider than preview container (${previewBounds.width})",
            textBounds.width >= previewBounds.width
        )

        composeRule.onNodeWithTag("editor_formula_preview").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_cancel").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_submit").assertIsDisplayed()
    }

    @Test
    fun formulaSheetRemainsOpenAndActionsReachableAboveIme() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Formula IME Test"))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        viewModel.openFormulaSheet()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()

        composeRule.onNodeWithTag("editor_formula_source_input").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_source_input").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_preview").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_cancel").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_submit").assertIsDisplayed()

        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertDoesNotExist()
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertDoesNotExist()
    }

    @Test
    fun formulaSheetUsesSingleTextOnlyInsertAction() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Before "),
                RichText(text = INLINE_FORMULA_PLACEHOLDER, formulaSource = "\\alpha", inlineId = "f1"),
                RichText(" After")
            )
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        viewModel.openFormulaSheet()
        composeRule.waitForIdle()

        val insertButton = composeRule.onNodeWithTag("editor_formula_submit")
        insertButton.assertIsDisplayed()
        insertButton.assertTextEquals(context.getString(R.string.editor_formula_insert))
        composeRule.onNodeWithTag("editor_formula_submit", useUnmergedTree = true)
            .onChildren()
            .assertCountEquals(1)

        val insertBounds = insertButton.getUnclippedBoundsInRoot()
        val cancelBounds = composeRule.onNodeWithTag("editor_formula_cancel").getUnclippedBoundsInRoot()
        assertTrue("Insert button height must be at least 48dp", insertBounds.height >= 48.dp)
        assertTrue("Insert button width must be at least 48dp", insertBounds.width >= 48.dp)
        assertTrue("Cancel button height must be at least 48dp", cancelBounds.height >= 48.dp)
        assertTrue("Cancel button width must be at least 48dp", cancelBounds.width >= 48.dp)

        viewModel.cancelFormula()
        composeRule.waitForIdle()

        viewModel.openFormulaSheetForEdit("block_1", "f1")
        composeRule.waitForIdle()

        val updateButton = composeRule.onNodeWithTag("editor_formula_submit")
        updateButton.assertIsDisplayed()
        updateButton.assertTextEquals(context.getString(R.string.editor_formula_update))
        composeRule.onNodeWithTag("editor_formula_submit", useUnmergedTree = true)
            .onChildren()
            .assertCountEquals(1)

        val updateBounds = updateButton.getUnclippedBoundsInRoot()
        assertTrue("Update button height must be at least 48dp", updateBounds.height >= 48.dp)
        assertTrue("Update button width must be at least 48dp", updateBounds.width >= 48.dp)
    }
}
