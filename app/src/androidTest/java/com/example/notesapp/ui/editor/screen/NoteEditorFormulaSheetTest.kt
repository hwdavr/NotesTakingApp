package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
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
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.cancelFormula
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheet
import com.example.notesapp.ui.editor.viewmodel.openFormulaSheetForEdit
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.submitFormula
import com.example.notesapp.ui.editor.viewmodel.updateFormulaSource
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorFormulaSheetTest {

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
    fun formulaActionReplacesSelectionWithRenderedFormula() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Prefix Target Suffix"))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            selectionStart = 7,
            selectionEnd = 13,
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_action").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_source_input").performTextInput("\\frac{1}{2}")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_submit").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertDoesNotExist()

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals(3, updatedBlock.children.size)
        assertEquals("Prefix ", updatedBlock.children[0].text)
        assertTrue("Second child should be a formula", updatedBlock.children[1].isFormula)
        assertEquals("\\frac{1}{2}", updatedBlock.children[1].formulaSource)
        assertEquals(INLINE_FORMULA_PLACEHOLDER, updatedBlock.children[1].text)
        assertEquals(" Suffix", updatedBlock.children[2].text)

        val inlineId = updatedBlock.children[1].inlineId
        assertNotNull(inlineId)
        composeRule.onNodeWithTag("editor_inline_formula_$inlineId").assertExists()
    }

    @Test
    fun formulaActionInsertsAtCollapsedCursor() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Hello World"))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            selectionStart = 5,
            selectionEnd = 5,
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_action").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_source_input").performTextInput("\\alpha")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_submit").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertDoesNotExist()

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals(3, updatedBlock.children.size)
        assertEquals("Hello", updatedBlock.children[0].text)
        assertTrue(updatedBlock.children[1].isFormula)
        assertEquals("\\alpha", updatedBlock.children[1].formulaSource)
        assertEquals(" World", updatedBlock.children[2].text)
    }

    @Test
    fun formulaActionAppendsAndFocusesWhenNoTextBlockIsFocused() {
        val viewModel = createViewModel(NoteDocument(blocks = emptyList()))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = emptyList()),
            focusedBlockId = null,
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_action").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_source_input").performTextInput("E = mc^2")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_submit").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertDoesNotExist()

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(1, blocks.size)
        val appendedBlock = blocks.first() as EditorBlock.TextBlock
        assertEquals(appendedBlock.id, viewModel.uiState.value.focusedBlockId)
        assertEquals(1, appendedBlock.children.size)
        assertTrue(appendedBlock.children.first().isFormula)
        assertEquals("E = mc^2", appendedBlock.children.first().formulaSource)
    }

    @Test
    fun invalidFormulaStaysEditableWithoutChangingDocument() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Unchanged text"))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            selectionStart = 0,
            selectionEnd = 0,
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_action").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_source_input").performTextInput("\\frac{a}{b")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_submit").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formula_validation_error").assertExists()

        val currentBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals(1, currentBlock.children.size)
        assertEquals("Unchanged text", currentBlock.children.first().text)

        composeRule.onNodeWithTag("editor_formula_cancel").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertDoesNotExist()
    }

    @Test
    fun tappingFormulaReopensSourceAndValidUpdatePersists() {
        val formulaChild = RichText(
            text = INLINE_FORMULA_PLACEHOLDER,
            formulaSource = "E = mc^2",
            inlineId = "test_formula_atom"
        )
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(formulaChild)
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

        composeRule.onNodeWithTag("editor_inline_formula_test_formula_atom").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.editor_formula_sheet_edit_title)).assertIsDisplayed()

        composeRule.onNodeWithTag("editor_formula_source_input").performTextReplacement("\\sqrt{x}")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_submit").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formula_sheet").assertDoesNotExist()

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        val updatedChild = updatedBlock.children.first()
        assertEquals("test_formula_atom", updatedChild.inlineId)
        assertEquals("\\sqrt{x}", updatedChild.formulaSource)
        assertTrue(updatedChild.isFormula)
    }

    @Test
    fun deletingInlineFormulaRemovesWholeFormulaAtom() {
        val formulaChild = RichText(
            text = INLINE_FORMULA_PLACEHOLDER,
            formulaSource = "x + y = z",
            inlineId = "formula_to_delete"
        )
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Before "),
                formulaChild,
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

        // User deletes the placeholder character from text
        viewModel.onTextBlockChange("block_1", "Before  After")
        composeRule.waitForIdle()

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertTrue("No formula atom should remain", updatedBlock.children.none { it.isFormula })
        assertTrue(
            "Placeholder should be gone",
            updatedBlock.children.none { it.text.contains(INLINE_FORMULA_PLACEHOLDER) }
        )
        assertTrue(
            "Formula source should not leak into text",
            updatedBlock.children.none { it.text.contains("x + y = z") }
        )
        assertEquals("Before  After", updatedBlock.text())
    }
}
