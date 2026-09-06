package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorUndoRedoViewModelTest : BaseViewModelTest() {

    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val deleteVoiceNoteAudioUseCase: DeleteVoiceNoteAudioUseCase = mockk(relaxed = true)
    private val deleteVoiceNoteBlockUseCase: DeleteVoiceNoteBlockUseCase = mockk(relaxed = true)
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        val summarizeNoteUseCase = SummarizeNoteUseCase(mockk<NoteSummarizer>(relaxed = true))
        val categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true)
        viewModel = NoteEditorViewModel(
            mockk(relaxed = true),
            folderRepository,
            summarizeNoteUseCase,
            categorizeNoteUseCase,
            deleteVoiceNoteAudioUseCase,
            deleteVoiceNoteBlockUseCase
        )
    }

    private fun textBlock(id: String, text: String): EditorBlock.TextBlock =
        EditorBlock.TextBlock(id = id, children = listOf(RichText(text)))

    private fun NoteDocument.textOf(): String =
        blocks.filterIsInstance<EditorBlock.TextBlock>().joinToString("") { it.text() }

    private fun seedEditable(
        vararg blocks: EditorBlock,
        focus: String? = null,
        selectionStart: Int = 0,
        selectionEnd: Int = 0
    ) {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = blocks.toList()),
            focusedBlockId = focus,
            selectionStart = selectionStart,
            selectionEnd = selectionEnd,
            isLoaded = true
        )
    }

    private fun seedReadOnly() {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "ro",
            document = NoteDocument(blocks = listOf(textBlock("b1", "Locked"))),
            isEditable = false,
            isLoaded = true
        )
    }

    @Test
    fun `baseline seed exposes no undo and typing enables undo`() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")

        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        viewModel.onTextBlockChange("b1", "h")
        assertTrue(viewModel.uiState.value.canUndo)

        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun `continuous typing coalesces into one undo step and redo replays it`() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1", selectionStart = 0, selectionEnd = 0)

        viewModel.onTextBlockChange("b1", "h")
        viewModel.onTextBlockChange("b1", "he")
        viewModel.onTextBlockChange("b1", "hel")
        assertEquals("hel", viewModel.uiState.value.document.textOf())

        // The whole run is one step: a single undo returns to the baseline.
        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)

        viewModel.redo()
        assertEquals("hel", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canRedo)
    }

    @Test
    fun `undo restores caret context recorded for the state`() = runTest {
        seedEditable(textBlock("b1", "abc"), textBlock("b2", ""), focus = "b2", selectionStart = 1, selectionEnd = 1)

        viewModel.onTextBlockChange("b2", "x")
        viewModel.onTextBlockChange("b2", "xy")

        viewModel.undo()
        val undone = viewModel.uiState.value
        assertEquals("abc", undone.document.textOf())
        assertEquals("b2", undone.focusedBlockId)
        assertEquals(1, undone.selectionStart)
        assertEquals(1, undone.selectionEnd)
    }

    @Test
    fun pausedTypingSplitsIntoNewUndoStep() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")
        var clock = 0L
        viewModel.uiStateInternal.nowMs = { clock }

        viewModel.onTextBlockChange("b1", "a") // run 1 at t=0
        clock = 2_000L
        viewModel.onTextBlockChange("b1", "ab") // t=2000 -> new run
        clock = 2_050L
        viewModel.onTextBlockChange("b1", "abc") // merges into run 2

        viewModel.undo()
        assertEquals("a", viewModel.uiState.value.document.textOf())
        assertTrue(viewModel.uiState.value.canUndo)

        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
    }

    @Test
    fun `title edits never affect undo history`() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")

        viewModel.onTitleChange("A title")
        viewModel.onTitleChange("Another title")

        assertEquals("Another title", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        // Undo leaves the (title-only changed) note untouched.
        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertEquals("Another title", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.canUndo)
    }

    @Test
    fun `a new edit after undo truncates the redo tail`() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")

        viewModel.onTextBlockChange("b1", "first")
        viewModel.undo()
        assertTrue(viewModel.uiState.value.canRedo)

        viewModel.onTextBlockChange("b1", "second")
        assertFalse(viewModel.uiState.value.canRedo)

        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun `discrete emoji insertion is its own undo step even mid typing`() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1", selectionStart = 0, selectionEnd = 0)

        viewModel.onTextBlockChange("b1", "h")
        viewModel.onTextBlockChange("b1", "hi")
        val afterRun = viewModel.uiState.value.document

        viewModel.insertEmoji("\uD83D\uDE00")
        val afterEmoji = viewModel.uiState.value.document

        // Undo removes only the emoji step.
        viewModel.undo()
        assertEquals(afterRun, viewModel.uiState.value.document)

        // Second undo removes the typed run.
        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertTrue(viewModel.uiState.value.canRedo)

        viewModel.redo()
        assertEquals(afterRun, viewModel.uiState.value.document)
        viewModel.redo()
        assertEquals(afterEmoji, viewModel.uiState.value.document)
    }

    @Test
    fun `read-only note ignores undo and redo`() = runTest {
        seedReadOnly()

        viewModel.undo()
        viewModel.redo()
        viewModel.onTextBlockChange("b1", "changed")

        assertEquals("Locked", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)
    }

    @Test
    fun `deleting a block is undone restoring the previous document and focus`() = runTest {
        seedEditable(textBlock("b1", "one"), textBlock("b2", "two"), focus = "b2")

        viewModel.deleteBlock("b2")
        assertEquals("one", viewModel.uiState.value.document.textOf())

        viewModel.undo()
        val restored = viewModel.uiState.value
        assertEquals("onetwo", restored.document.textOf())
        assertEquals("b2", restored.focusedBlockId)
        assertTrue(viewModel.uiState.value.canRedo)

        viewModel.redo()
        assertEquals("one", viewModel.uiState.value.document.textOf())
    }

    @Test
    fun `inserting a basic block is a discrete undoable step`() = runTest {
        seedEditable(textBlock("b1", "text"), focus = "b1")

        assertTrue(viewModel.insertBasicBlock(BasicBlockType.PARAGRAPH))
        val withBlock = viewModel.uiState.value.document

        // Undo removes the inserted paragraph block entirely.
        viewModel.undo()
        assertEquals("text", viewModel.uiState.value.document.textOf())
        assertEquals(1, viewModel.uiState.value.document.blocks.size)
        assertFalse(viewModel.uiState.value.canUndo)

        viewModel.redo()
        assertEquals(withBlock, viewModel.uiState.value.document)
    }

    @Test
    fun `undo clears pending typing marks`() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")

        viewModel.toggleBlockMark("b1", "bold") // collapsed cursor -> pending mark
        assertTrue("bold" in viewModel.uiState.value.pendingTypingMarks)

        viewModel.onTextBlockChange("b1", "x")
        viewModel.undo()

        assertTrue(viewModel.uiState.value.pendingTypingMarks.isEmpty())
        assertEquals("", viewModel.uiState.value.document.textOf())
    }
}
