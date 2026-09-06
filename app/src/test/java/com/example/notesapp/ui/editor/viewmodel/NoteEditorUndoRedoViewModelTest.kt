package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.model.TableFocusTarget
import com.example.notesapp.ui.editor.model.TableHandleAction
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorUndoRedoViewModelTest : BaseViewModelTest() {

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val deleteVoiceNoteAudioUseCase: DeleteVoiceNoteAudioUseCase = mockk(relaxed = true)
    private val deleteVoiceNoteBlockUseCase: DeleteVoiceNoteBlockUseCase = mockk(relaxed = true)
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        val summarizeNoteUseCase = SummarizeNoteUseCase(mockk<NoteSummarizer>(relaxed = true))
        val categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true)
        viewModel = NoteEditorViewModel(
            noteRepository,
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
        selectionEnd: Int = 0,
        focusedTableCells: Map<String, TableFocusTarget> = emptyMap()
    ) {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = blocks.toList()),
            focusedBlockId = focus,
            selectionStart = selectionStart,
            selectionEnd = selectionEnd,
            focusedTableCells = focusedTableCells,
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
    fun `baselineDoc seed exposes no undo and typing enables undo`() = runTest {
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

        // The whole run is one step: a single undo returns to the baselineDoc.
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
    fun undoRedoClearsPendingTypingMarks() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")

        viewModel.toggleBlockMark("b1", "bold") // collapsed cursor -> pending mark
        assertTrue("bold" in viewModel.uiState.value.pendingTypingMarks)

        viewModel.onTextBlockChange("b1", "x")
        viewModel.undo()

        assertTrue(viewModel.uiState.value.pendingTypingMarks.isEmpty())
        assertEquals("", viewModel.uiState.value.document.textOf())

        // Redo replays the typed run without resurrecting the pending mark.
        viewModel.redo()
        assertTrue(viewModel.uiState.value.pendingTypingMarks.isEmpty())
        assertEquals("x", viewModel.uiState.value.document.textOf())

        // Subsequent typing carries no stale Bold mark.
        viewModel.onTextBlockChange("b1", "xy")
        val updated = viewModel.uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TextBlock>()
            .single()
        assertTrue(updated.children.filter { it.text == "y" }.all { "bold" !in it.marks })
    }

    // ---------------------------------------------------------------------
    // US-2: discrete rich-content actions unwind as exact steps (TC-US-2-*)
    // ---------------------------------------------------------------------

    private fun editorStateSnapshot(): NoteEditorUiState = viewModel.uiState.value

    private fun undoToState(expected: NoteEditorUiState) {
        viewModel.undo()
        assertEquals(expected.document, viewModel.uiState.value.document)
    }

    @Test
    fun discreteActionsProduceSeparateStepsAndExactUnwind() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1", selectionStart = 0, selectionEnd = 0)
        val baselineDoc = editorStateSnapshot()

        // 1) A coalesced typing run (two commits, one step).
        viewModel.onTextBlockChange("b1", "H")
        viewModel.onTextBlockChange("b1", "Hi")
        val afterTyping = editorStateSnapshot()

        // 2) Bold applied over the whole run -> discrete mark step.
        viewModel.updateSelection(0, 2)
        viewModel.toggleBlockMark("b1", "bold")
        val afterBold = editorStateSnapshot()

        // 3) Emoji insertion -> discrete step.
        viewModel.updateSelection(2, 2)
        viewModel.insertEmoji("\uD83D\uDE00")
        val afterEmoji = editorStateSnapshot()

        // 4) Block type conversion (paragraph -> checkbox) -> discrete step.
        viewModel.toggleCheckbox("b1")
        val afterCheckbox = editorStateSnapshot()

        // 5) A structural rich-content action: append a table, then add a row below.
        viewModel.addTableBlock()
        val tableBlock = viewModel.uiState.value.document.blocks.last() as EditorBlock.TableBlock
        val afterTableAdd = editorStateSnapshot()
        viewModel.onTableAction(TableHandleAction.InsertRowBelow(tableBlock.id, 0))
        val afterRowAdd = editorStateSnapshot()

        // Undo unwinds in reverse: each step restores the state that preceded it.
        val expectedReverse = listOf(
            afterTableAdd,
            afterCheckbox,
            afterEmoji,
            afterBold,
            afterTyping,
            baselineDoc
        )
        expectedReverse.forEach { expected -> undoToState(expected) }
        assertFalse(viewModel.uiState.value.canUndo)

        // Redo replays every step forward, byte-exact.
        val expectedForward = listOf(
            afterTyping,
            afterBold,
            afterEmoji,
            afterCheckbox,
            afterTableAdd,
            afterRowAdd
        )
        expectedForward.forEach { expected ->
            viewModel.redo()
            assertEquals(expected.document, viewModel.uiState.value.document)
        }
        assertEquals(afterRowAdd.document, viewModel.uiState.value.document)
        assertFalse(viewModel.uiState.value.canRedo)
    }

    @Test
    fun redoAcrossRichContentAndTruncationOnNewEdit() = runTest {
        seedEditable(textBlock("b1", "text"), focus = "b1")
        val baselineDoc = editorStateSnapshot()

        assertTrue(viewModel.insertBasicBlock(BasicBlockType.PARAGRAPH))
        val afterBlockInsert = editorStateSnapshot()
        viewModel.addTableBlock()
        val afterTableAdd = editorStateSnapshot()
        val tableBlock = viewModel.uiState.value.document.blocks.last() as EditorBlock.TableBlock
        viewModel.onTableAction(TableHandleAction.InsertRowBelow(tableBlock.id, 0))
        val afterTableEdit = editorStateSnapshot()

        // Undo unwinds the row add, table add, and block insertion one step at a time.
        viewModel.undo()
        assertEquals(afterTableAdd.document, viewModel.uiState.value.document)
        viewModel.undo()
        assertEquals(afterBlockInsert.document, viewModel.uiState.value.document)
        viewModel.undo()
        assertEquals(baselineDoc.document, viewModel.uiState.value.document)
        assertFalse(viewModel.uiState.value.canUndo)

        // Redo replays the block insertion, table add, and row add exactly in order.
        viewModel.redo()
        assertEquals(afterBlockInsert.document, viewModel.uiState.value.document)
        assertTrue(viewModel.uiState.value.canRedo)
        viewModel.redo()
        assertEquals(afterTableAdd.document, viewModel.uiState.value.document)
        viewModel.redo()
        assertEquals(afterTableEdit.document, viewModel.uiState.value.document)
        assertFalse(viewModel.uiState.value.canRedo)

        // A new mark toggle on the focused body block truncates the redo tail and
        // becomes the next undo step.
        viewModel.setFocusedBlock("b1")
        viewModel.updateSelection(0, 1)
        viewModel.toggleBlockMark("b1", "italic")
        assertFalse(viewModel.uiState.value.canRedo)
        viewModel.undo()
        assertEquals(afterTableEdit.document, viewModel.uiState.value.document)
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun undoRemovedBlockFallsBackToPrecedingBlock() = runTest {
        seedEditable(textBlock("a1", "A text"), textBlock("a2", "B text"), focus = "a1")

        assertTrue(viewModel.insertBasicBlock(BasicBlockType.HEADING_1))
        val insertedId = viewModel.uiState.value.focusedBlockId
        assertNotNull(insertedId)

        viewModel.undo()
        // Focus falls back to the preceding paragraph that still exists.
        assertEquals("a1", viewModel.uiState.value.focusedBlockId)
        assertEquals(2, viewModel.uiState.value.document.blocks.size)
    }

    @Test
    fun undoToEmptyDocumentClearsFocusWithoutCrash() = runTest {
        // Baseline is an editable empty document with no focus.
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = emptyList()),
            isLoaded = true,
            isEditable = true
        )
        assertTrue(viewModel.insertBasicBlock(BasicBlockType.PARAGRAPH))
        assertEquals(1, viewModel.uiState.value.document.blocks.size)

        viewModel.undo()
        assertEquals(0, viewModel.uiState.value.document.blocks.size)
        assertEquals(null, viewModel.uiState.value.focusedBlockId)
        assertFalse(viewModel.uiState.value.canUndo)
    }

    @Test
    fun cellTypingUndoRestoresSelectionContext() = runTest {
        val tableId = "tbl1"
        val table = EditorBlock.TableBlock(id = tableId)
        seedEditable(
            table,
            focus = tableId,
            focusedTableCells = mapOf(tableId to TableFocusTarget(0, 0)),
            selectionStart = 0,
            selectionEnd = 0
        )
        val context = mapOf(tableId to TableFocusTarget(0, 0))
        val baselineDoc = viewModel.uiState.value.document

        // Same-cell typing commits coalesce into one step.
        viewModel.updateTableCell(tableId, 0, 0, "ab")
        viewModel.updateTableCell(tableId, 0, 0, "abc")
        viewModel.updateTableCell(tableId, 0, 0, "abcd")

        viewModel.undo()
        assertEquals(baselineDoc, viewModel.uiState.value.document)
        // Undo restored the recorded cell-focus context.
        assertEquals(context, viewModel.uiState.value.focusedTableCells)

        // Chart data cell typing through the same funnel.
        val chart = EditorBlock.ChartBlock(id = "chart1")
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(chart)),
            focusedBlockId = "chart1",
            focusedTableCells = mapOf("chart1" to TableFocusTarget(0, 1)),
            isLoaded = true
        )
        val chartContext = mapOf("chart1" to TableFocusTarget(0, 1))
        val chartBaseline = viewModel.uiState.value.document
        viewModel.updateChartCell("chart1", 0, 1, "5")
        viewModel.updateChartCell("chart1", 0, 1, "55")

        viewModel.undo()
        assertEquals(chartBaseline, viewModel.uiState.value.document)
        assertEquals(chartContext, viewModel.uiState.value.focusedTableCells)
    }

    @Test
    fun undoVoiceOrImageInsertionKeepsFileAndRedoRestoresBlock() = runTest {
        seedEditable(textBlock("b1", ""))
        val baselineDoc = viewModel.uiState.value.document
        val image = EditorBlock.ImageBlock(url = "/files/img.png", caption = "Shot")
        val voice = EditorBlock.Voice(
            blockId = "voice-1",
            audioFilePath = "/data/data/app/files/voice-notes/v.m4a",
            audioFormat = AudioFormat.AAC,
            durationMs = 1_000L,
            fileSizeBytes = 100L,
            sampleRateHertz = 44_100,
            channels = 1,
            createdAt = 1L,
            updatedAt = 1L
        )
        viewModel.appendBlock(voice)
        val afterVoice = viewModel.uiState.value.document
        viewModel.appendBlock(image)
        val afterImage = viewModel.uiState.value.document

        viewModel.undo()
        assertEquals(afterVoice, viewModel.uiState.value.document)
        viewModel.undo()
        assertEquals(baselineDoc, viewModel.uiState.value.document)
        coVerify(exactly = 0) { deleteVoiceNoteBlockUseCase(any()) }
        coVerify(exactly = 0) { deleteVoiceNoteAudioUseCase(any(), any()) }

        viewModel.redo()
        assertEquals(afterVoice, viewModel.uiState.value.document)
        viewModel.redo()
        assertEquals(afterImage, viewModel.uiState.value.document)
        val restoredImage = viewModel.uiState.value.document.blocks
            .filterIsInstance<EditorBlock.ImageBlock>().single()
        assertEquals(image.url, restoredImage.url)
    }

    @Test
    fun overlayCommittedEditsBecomeNextUndoStep() = runTest {
        seedEditable(textBlock("b1", "abc"), focus = "b1", selectionStart = 0, selectionEnd = 0)
        val baselineDoc = viewModel.uiState.value.document

        // Link picker overlay commit path.
        viewModel.onTargetNoteSelected("target-1", "Linked note")
        val afterLink = viewModel.uiState.value.document

        // Formula sheet overlay commit path (insert a valid inline formula at the cursor).
        viewModel.openFormulaSheet()
        viewModel.updateFormulaSource("E = mc^2")
        assertTrue(viewModel.submitFormula())
        val afterFormula = viewModel.uiState.value.document

        viewModel.undo()
        assertEquals(afterLink, viewModel.uiState.value.document)
        viewModel.undo()
        assertEquals(baselineDoc, viewModel.uiState.value.document)
        assertFalse(viewModel.uiState.value.canUndo)

        viewModel.redo()
        assertEquals(afterLink, viewModel.uiState.value.document)
        viewModel.redo()
        assertEquals(afterFormula, viewModel.uiState.value.document)
        assertFalse(viewModel.uiState.value.canRedo)
    }

    // ---------------------------------------------------------------------
    // US-3: access-change guardrails and autosave persistence (TC-US-3-*)
    // ---------------------------------------------------------------------

    @Test
    fun undoRedoNoopWhenNotEditableAfterAccessChange() = runTest {
        seedEditable(textBlock("b1", ""), focus = "b1")
        viewModel.onTextBlockChange("b1", "typed")
        assertEquals("typed", viewModel.uiState.value.document.textOf())
        assertTrue(viewModel.uiState.value.canUndo)

        // Mid-session access change (e.g. share role flips to READ_ONLY): undo surface hides.
        viewModel.uiStateInternal.value = viewModel.uiStateInternal.value.copy(isEditable = false)
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        viewModel.undo()
        viewModel.redo()
        assertEquals("typed", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)

        // Restoring edit access resurfaces the original single step: the access flips
        // themselves recorded no history entries.
        viewModel.uiStateInternal.value = viewModel.uiStateInternal.value.copy(isEditable = true)
        assertTrue(viewModel.uiState.value.canUndo)
        assertFalse(viewModel.uiState.value.canRedo)
        viewModel.undo()
        assertEquals("", viewModel.uiState.value.document.textOf())
        assertFalse(viewModel.uiState.value.canUndo)
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun autosavePersistsUndoneDocument() = runTest {
        seedEditable(textBlock("b1", "base"), focus = "b1")

        viewModel.onTextBlockChange("b1", "baseX")
        assertEquals("baseX", viewModel.uiState.value.document.textOf())

        viewModel.undo()
        assertEquals("base", viewModel.uiState.value.document.textOf())
        assertTrue(viewModel.uiState.value.canRedo)

        // The undo schedules the existing autosave path; wait for the 2s debounce.
        advanceTimeBy(2_001)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            noteRepository.save(
                match { saved ->
                    NoteDocument.fromContent(saved.content).textOf() == "base"
                }
            )
        }
        assertEquals("base", viewModel.uiState.value.document.textOf())
    }
}
