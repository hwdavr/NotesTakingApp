package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NoteEditorSelectionFormattingTest {
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
    fun givenTextSelected_whenBoldTapped_thenSelectionAtToggleTimeIsNonCollapsed() {
        var lastSelection: Pair<Int, Int> = Pair(0, 0)
        var selectionAtToggle: Pair<Int, Int>? = null
        var toggledBlock: String? = null
        var toggledMark: String? = null

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state =
                NoteEditorUiState(
                    noteId = "note_1",
                    isFormattingToolbarVisible = true,
                    isLoaded = true,
                    focusedBlockId = "block_1",
                    document =
                    NoteDocument(
                        blocks =
                        listOf(
                            EditorBlock.TextBlock(
                                id = "block_1",
                                children = listOf(RichText("Hello World"))
                            )
                        )
                    )
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { blockId, mark ->
                    toggledBlock = blockId
                    toggledMark = mark
                    selectionAtToggle = lastSelection
                },
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { start, end -> lastSelection = start to end },
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(0, 5))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            lastSelection == Pair(0, 5)
        }
        assertEquals(
            "Precondition: selecting 'Hello' should report selection (0, 5)",
            Pair(0, 5),
            lastSelection
        )

        composeRule.onNodeWithTag("editor_bold_action").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            selectionAtToggle != null
        }

        assertEquals("block_1", toggledBlock)
        assertEquals("bold", toggledMark)
        val (start, end) = selectionAtToggle ?: Pair(-1, -1)
        assertTrue(
            "Selection at toggle time should stay (0, 5) but was ($start, $end) — " +
                "tapping the button stole focus and collapsed the selection",
            start == 0 && end == 5
        )
    }

    @Test
    fun bodyActionRemovesAllFormattingFromSelectedText() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Hello ", marks = listOf("bold")),
                RichText("formatted", marks = listOf("italic", "underline")),
                RichText(" world", marks = listOf("code"))
            )
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            selectionStart = 6,
            selectionEnd = 15,
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(6, 15))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        val beforeSelStart = viewModel.uiState.value.selectionStart
        val beforeSelEnd = viewModel.uiState.value.selectionEnd
        val beforeDoc = viewModel.uiState.value.document.toJsonString()

        composeRule.onNodeWithTag("editor_body_action").performClick()
        composeRule.waitForIdle()

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Selected text should visually differ after removing formatting",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        val updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        assertEquals("Hello formatted world", updatedBlock.text())
        val formattedChildren = updatedBlock.children.filter { it.text == "formatted" }
        assertTrue("Selected range should be present", formattedChildren.isNotEmpty())
        assertTrue("Selected range must have no inline marks", formattedChildren.all { it.marks.isEmpty() })
        val prefixChild = updatedBlock.children.first { it.text == "Hello " }
        assertEquals(listOf("bold"), prefixChild.marks)
        val suffixChild = updatedBlock.children.first { it.text == " world" }
        assertEquals(listOf("code"), suffixChild.marks)
    }

    @Test
    fun codeActionChangesOnlyTheSelectedRange() {
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(7, 13))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        composeRule.onNodeWithTag("editor_code_action").performClick()
        composeRule.waitForIdle()

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Selected code must visually render differently (monospace styling)",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        val updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val targetChild = updatedBlock.children.first { it.text == "Target" }
        assertTrue(targetChild.marks.contains("code"))
        val prefixChild = updatedBlock.children.first { it.text == "Prefix " }
        assertTrue(!prefixChild.marks.contains("code"))
        val suffixChild = updatedBlock.children.first { it.text == " Suffix" }
        assertTrue(!suffixChild.marks.contains("code"))

        val md = viewModel.uiState.value.document.toMarkdown()
        assertTrue("Markdown must contain backticks around code", md.contains("`Target`"))

        viewModel.updateSelection(updatedBlock.text().length, updatedBlock.text().length)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_code_action").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_code_action").assertIsSelected()

        viewModel.onTextBlockChange("block_1", updatedBlock.text() + " more")
        composeRule.waitForIdle()
        val finalBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val typedChild = finalBlock.children.last()
        assertTrue("Newly typed text after code toggle must inherit code mark", typedChild.marks.contains("code"))
    }

    @Test
    fun boldActionChangesOnlyTheSelectedRange() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Prefix ", marks = listOf("italic")),
                RichText("Target"),
                RichText(" Suffix", marks = listOf("underline"))
            )
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(7, 13))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        composeRule.onNodeWithTag("editor_bold_action").performClick()
        composeRule.waitForIdle()

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Selected text must visually render bold",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        val updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val targetChild = updatedBlock.children.first { it.text == "Target" }
        assertEquals(listOf("bold"), targetChild.marks)
        val prefixChild = updatedBlock.children.first { it.text == "Prefix " }
        assertEquals(listOf("italic"), prefixChild.marks)
        val suffixChild = updatedBlock.children.first { it.text == " Suffix" }
        assertEquals(listOf("underline"), suffixChild.marks)
    }

    @Test
    fun italicActionChangesOnlyTheSelectedRange() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Prefix ", marks = listOf("bold")),
                RichText("Target"),
                RichText(" Suffix", marks = listOf("code"))
            )
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(7, 13))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        composeRule.onNodeWithTag("editor_italic_action").performClick()
        composeRule.waitForIdle()

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Selected text must visually render italic",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        val updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val targetChild = updatedBlock.children.first { it.text == "Target" }
        assertEquals(listOf("italic"), targetChild.marks)
        val prefixChild = updatedBlock.children.first { it.text == "Prefix " }
        assertEquals(listOf("bold"), prefixChild.marks)
        val suffixChild = updatedBlock.children.first { it.text == " Suffix" }
        assertEquals(listOf("code"), suffixChild.marks)
    }

    @Test
    fun underlineActionChangesOnlyTheSelectedRange() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Prefix ", marks = listOf("bold")),
                RichText("Target"),
                RichText(" Suffix", marks = listOf("italic"))
            )
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(7, 13))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        composeRule.onNodeWithTag("editor_underline_action").performClick()
        composeRule.waitForIdle()

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Selected text must visually render underline",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        val updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val targetChild = updatedBlock.children.first { it.text == "Target" }
        assertEquals(listOf("underline"), targetChild.marks)
        val prefixChild = updatedBlock.children.first { it.text == "Prefix " }
        assertEquals(listOf("bold"), prefixChild.marks)
        val suffixChild = updatedBlock.children.first { it.text == " Suffix" }
        assertEquals(listOf("italic"), suffixChild.marks)
    }

    @Test
    fun strikethroughActionChangesOnlyTheSelectedRange() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(
                RichText("Prefix ", marks = listOf("bold")),
                RichText("Target"),
                RichText(" Suffix", marks = listOf("italic"))
            )
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(7, 13))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        composeRule.onNodeWithTag("editor_strikethrough_action").performClick()
        composeRule.waitForIdle()

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Selected text must visually render strikethrough",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        val updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val targetChild = updatedBlock.children.first { it.text == "Target" }
        assertEquals(listOf("strikethrough"), targetChild.marks)
        val prefixChild = updatedBlock.children.first { it.text == "Prefix " }
        assertEquals(listOf("bold"), prefixChild.marks)
        val suffixChild = updatedBlock.children.first { it.text == " Suffix" }
        assertEquals(listOf("italic"), suffixChild.marks)
    }

    @Test
    fun inlineMarksApplyToFollowingTypedTextAtCollapsedCursor() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Hello"))
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_text_block").performTextInputSelection(TextRange(5, 5))
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        composeRule.onNodeWithTag("editor_bold_action").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_bold_action").assertIsSelected()
        assertEquals(setOf("bold"), viewModel.uiState.value.pendingTypingMarks)

        viewModel.onTextBlockChange("block_1", "Hello B")
        composeRule.waitForIdle()
        var updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        assertTrue(updatedBlock.children.last().marks.contains("bold"))

        val afterImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()
        assertTrue(
            "Typed text with bold mark must visually differ from original",
            differingPixelCount(beforeImage, afterImage) > 0
        )

        composeRule.onNodeWithTag("editor_italic_action").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_italic_action").assertIsSelected()
        composeRule.onNodeWithTag("editor_bold_action").assertIsSelected()
        assertEquals(setOf("bold", "italic"), viewModel.uiState.value.pendingTypingMarks)

        viewModel.onTextBlockChange("block_1", "Hello B I")
        composeRule.waitForIdle()
        updatedBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first {
            it.id == "block_1"
        }
        val multiMarkChild = updatedBlock.children.last()
        assertTrue(multiMarkChild.marks.contains("bold") && multiMarkChild.marks.contains("italic"))

        composeRule.onNodeWithTag("editor_bold_action").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_bold_action").assertIsNotSelected()
        assertEquals(setOf("italic"), viewModel.uiState.value.pendingTypingMarks)

        composeRule.onNodeWithTag("editor_body_action").performClick()
        composeRule.waitForIdle()
        assertTrue(!viewModel.uiState.value.pendingTypingMarks.contains("body"))

        composeRule.onNodeWithTag("editor_formula_action").performClick()
        composeRule.waitForIdle()
        assertTrue(!viewModel.uiState.value.pendingTypingMarks.contains("formula"))
    }

    @Test
    fun formattingToolbarRemainsVisibleAboveIme() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            children = listOf(RichText("Test content for keyboard test"))
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

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertIsDisplayed()
        val toolbarBounds = composeRule.onNodeWithTag("editor_formatting_bottom_bar").getUnclippedBoundsInRoot()
        val toolbarHeight = toolbarBounds.bottom - toolbarBounds.top
        assertEquals(56.dp.value, toolbarHeight.value, 1f)

        val textBlockBounds = composeRule.onNodeWithTag("editor_text_block").getUnclippedBoundsInRoot()
        assertTrue(
            "Toolbar top must be at or below the text block (no overlapping)",
            toolbarBounds.top >= textBlockBounds.top
        )

        composeRule.onNodeWithTag("editor_body_action").assertExists()
        composeRule.onNodeWithTag("editor_bold_action").assertExists()
        composeRule.onNodeWithTag("editor_italic_action").assertExists()
        composeRule.onNodeWithTag("editor_underline_action").assertExists()
        composeRule.onNodeWithTag("editor_strikethrough_action").assertExists()
        composeRule.onNodeWithTag("editor_link_action").assertExists()
        composeRule.onNodeWithTag("editor_code_action").assertExists()
        composeRule.onNodeWithTag("editor_formula_action").assertExists()
    }

    @Test
    fun newLinePreservesCurrentFormatting() {
        val block = EditorBlock.TextBlock(
            id = "block_1",
            type = "bulleted",
            children = listOf(RichText("First Line", marks = listOf("bold", "italic")))
        )
        val viewModel = createViewModel(NoteDocument(blocks = listOf(block)))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "block_1",
            selectionStart = 10,
            selectionEnd = 10,
            isFormattingToolbarVisible = true,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent {
            EditorTestContent(viewModel)
        }
        composeRule.waitForIdle()

        val beforeImage = composeRule.onNodeWithTag("editor_text_block").captureToImage()

        viewModel.onTextBlockChange("block_1", "First Line\n")
        composeRule.waitForIdle()

        val blocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, blocks.size)
        val firstLine = blocks[0]
        val secondLine = blocks[1]

        assertEquals("First Line", firstLine.text())
        assertEquals("bulleted", firstLine.type)
        assertEquals(listOf("bold", "italic"), firstLine.children.first().marks)

        assertEquals("bulleted", secondLine.type)

        viewModel.onTextBlockChange(secondLine.id, "Second Line")
        composeRule.waitForIdle()

        val updatedSecondLine = viewModel.uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TextBlock>()
            .first { it.id == secondLine.id }
        assertEquals("Second Line", updatedSecondLine.text())
        assertTrue("Second line must retain bold mark", updatedSecondLine.children.first().marks.contains("bold"))
        assertTrue("Second line must retain italic mark", updatedSecondLine.children.first().marks.contains("italic"))

        val afterImage = composeRule.onAllNodesWithTag("editor_text_block")[1].captureToImage()
        assertTrue(
            "New line with retained formatting must visually render formatted text",
            differingPixelCount(beforeImage, afterImage) > 0
        )
    }
}
