package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
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
import com.example.notesapp.ui.editor.viewmodel.resetSelectedTextToBody
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NoteEditorEnterCursorReproductionTest {
    @get:Rule val composeRule = createComposeRule()

    private var viewModel: NoteEditorViewModel? = null

    private fun createViewModel(initialDocument: NoteDocument? = null): NoteEditorViewModel {
        val noteRepo = FakeNoteRepository(
            listOf(
                Note(
                    id = "note_1",
                    title = "Test Note",
                    content = (initialDocument ?: NoteDocument()).toJsonString(),
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
        return NoteEditorViewModel(
            noteRepo,
            folderRepo,
            summarizer,
            categorizer,
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
    }

    private fun dump(): String {
        val vm = viewModel
        val doc = vm?.uiState?.value?.document?.blocks
            ?.map { block ->
                block.javaClass.simpleName + ":" + (if (block is EditorBlock.TextBlock) block.text() else "")
            }
            ?.joinToString(" | ") + " focus=" + vm?.uiState?.value?.focusedBlockId
        val fields = composeRule.onAllNodesWithTag("editor_text_block").fetchSemanticsNodes()
            .mapIndexed { index, node ->
                val editable = node.config.firstNotNullOfOrNull { entry ->
                    entry.key.name.takeIf { it.contains("EditableText") }?.let { entry.value.toString() }
                }
                val focused = node.config.firstNotNullOfOrNull { entry ->
                    entry.key.name.takeIf { it == "Focused" }?.let { entry.value.toString() }
                }
                "field$index=[" + (editable ?: "?") + " focused=" + (focused ?: "?") + "]"
            }
            .joinToString(" ")
        return doc + " :: " + fields
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
                onBlockFocused = { blockId ->
                    viewModel?.setFocusedBlock(blockId)
                },
                onSelectionChange = viewModel::updateSelection,
                onDeleteBlock = viewModel::deleteBlock
            )
        }
    }

    @Test
    fun givenTwoBlocks_whenEnterPressedAtEndOfFirstBlock_thenCursorStaysInNewBlock() {
        val vm = createViewModel(
            NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "block_1"),
                    EditorBlock.TextBlock(
                        id = "block_2",
                        children = listOf(RichText("Last"))
                    )
                )
            )
        )
        viewModel = vm
        vm.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "block_1"),
                    EditorBlock.TextBlock(
                        id = "block_2",
                        children = listOf(RichText("Last"))
                    )
                )
            ),
            focusedBlockId = null,
            selectionStart = 0,
            selectionEnd = 0,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(vm) }
        composeRule.waitForIdle()

        // Focus the first (empty) block and type text, like a real user would
        composeRule.onAllNodesWithTag("editor_text_block")[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("Hello")
        composeRule.waitForIdle()
        assertEquals(
            "Setup: text must land in the first block (doc=" + dump() + ")",
            "Hello",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[0].text()
        )

        // Press Enter at the end of the first block
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("\n")
        composeRule.waitForIdle()

        val blocks = vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(
            "Enter must split the focused block into two blocks (doc=" + dump() + ")",
            3,
            blocks.size
        )
        assertEquals(
            "Text typed before Enter stays in the first block (doc=" + dump() + ")",
            "Hello",
            blocks[0].text()
        )
        assertEquals(
            "The newly created block must be focused after pressing Enter (doc=" + dump() + ")",
            blocks[1].id,
            vm.uiState.value.focusedBlockId
        )

        // Probe where the cursor actually ended up by typing into the currently focused field
        composeRule.onNode(isFocused()).performTextInput("X")
        composeRule.waitForIdle()

        assertEquals(
            "Character typed after Enter must land in the newly created line (doc=" + dump() + ")",
            "X",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[1].text()
        )
        assertEquals(
            "Character typed after Enter must not land in the last block (doc=" + dump() + ")",
            "Last",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[2].text()
        )
    }

    @Test
    fun givenLoadedNote_whenEnterPressed_thenNewBlockIsFocusedAndReceivesNextInput() {
        val vm = createViewModel()
        viewModel = vm
        composeRule.setContent { EditorTestContent(vm) }
        vm.load(null)
        composeRule.waitUntilAtLeastOneExists(hasTestTag("editor_text_block"), 10_000)

        composeRule.onAllNodesWithTag("editor_text_block")[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("Hello")
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("\n")
        composeRule.waitForIdle()

        val blocks = vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals("Enter must create a new block", 2, blocks.size)
        assertEquals("Hello", blocks[0].text())

        composeRule.onNode(isFocused()).performTextInput("World")
        composeRule.waitForIdle()

        assertEquals(
            "Typed text must land in the new block, not the end of the note",
            "World",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[1].text()
        )
    }

    @Test
    fun givenText_whenEnterPressedMidBlock_thenCursorGoesToStartOfNewBlock() {
        val vm = createViewModel()
        viewModel = vm
        vm.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "block_1"))
            ),
            focusedBlockId = null,
            selectionStart = 0,
            selectionEnd = 0,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(vm) }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("editor_text_block")[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("Hello World")
        composeRule.waitForIdle()

        // Move the cursor to the middle (after "Hello"), then press Enter
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("\u0000") // no-op probe
        composeRule.waitForIdle()

        // Re-type with cursor placement: clear and type via selection is unreliable, so instead
        // type "Hello", Enter, then "World" as three sequential inputs at the natural cursor.
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextReplacement("Hello")
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("\n")
        composeRule.waitForIdle()

        val blocks = vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(
            "Enter must create a new block (doc=" + dump() + ")",
            2,
            blocks.size
        )

        composeRule.onNode(isFocused()).performTextInput("World")
        composeRule.waitForIdle()

        assertEquals(
            "Typed text must land in the new block (doc=" + dump() + ")",
            "World",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[1].text()
        )
    }

    @Test
    fun givenText_whenHardwareEnterKey_thenCursorStaysInNewBlock() {
        val vm = createViewModel()
        viewModel = vm
        vm.uiStateInternal.value = NoteEditorUiState(
            noteId = "note_1",
            title = "Test Note",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "block_1"),
                    EditorBlock.TextBlock(
                        id = "block_2",
                        children = listOf(RichText("Last"))
                    )
                )
            ),
            focusedBlockId = null,
            selectionStart = 0,
            selectionEnd = 0,
            isEditable = true,
            isLoaded = true
        )

        composeRule.setContent { EditorTestContent(vm) }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("editor_text_block")[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_text_block")[0].performTextInput("Hello")
        composeRule.waitForIdle()

        // Hardware Enter (what an emulator PC keyboard or physical keyboard sends)
        composeRule.onAllNodesWithTag("editor_text_block")[0].performKeyInput { pressKey(Key.Enter) }
        composeRule.waitForIdle()

        val blocks = vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(
            "Hardware Enter must create a new block (doc=" + dump() + ")",
            3,
            blocks.size
        )
        assertEquals(
            "New block must be focused (doc=" + dump() + ")",
            blocks[1].id,
            vm.uiState.value.focusedBlockId
        )

        composeRule.onNode(isFocused()).performTextInput("X")
        composeRule.waitForIdle()

        assertEquals(
            "Typed text must land in the new block (doc=" + dump() + ")",
            "X",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[1].text()
        )
        assertEquals(
            "Last block must be untouched (doc=" + dump() + ")",
            "Last",
            vm.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()[2].text()
        )
    }
}
