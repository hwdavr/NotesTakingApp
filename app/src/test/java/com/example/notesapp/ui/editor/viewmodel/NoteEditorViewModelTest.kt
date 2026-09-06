package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.NoteSummaryUnavailableException
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest : BaseViewModelTest() {
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val deleteVoiceNoteAudioUseCase: DeleteVoiceNoteAudioUseCase = mockk(relaxed = true)
    private val deleteVoiceNoteBlockUseCase: DeleteVoiceNoteBlockUseCase = mockk(relaxed = true)
    private lateinit var noteSummarizer: FakeNoteSummarizer
    private lateinit var viewModel: NoteEditorViewModel
    private val testNote = Note(
        id = "n1",
        title = "Title",
        content = "Content",
        folderId = "f1",
        sortKey = "1",
        deviceId = "dev",
        createdAt = 1000L,
        updatedAt = 1000L
    )
    private val readOnlyNote = Note(
        id = "readonly",
        title = "Shared read only",
        content = "Locked content",
        folderId = "f1",
        sortKey = "1",
        deviceId = "dev",
        createdAt = 1000L,
        updatedAt = 1000L,
        accessRole = NoteAccessRole.READ_ONLY
    )

    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns flowOf(emptyList())
        coEvery { noteRepository.getNoteById("n1") } returns testNote
        coEvery { noteRepository.getNoteById("readonly") } returns readOnlyNote
        noteSummarizer = FakeNoteSummarizer()
        val categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true)
        viewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            SummarizeNoteUseCase(noteSummarizer),
            categorizeNoteUseCase,
            deleteVoiceNoteAudioUseCase,
            deleteVoiceNoteBlockUseCase
        )
    }

    @Test
    fun `load with noteId updates uiState`() = runTest {
        viewModel.load("n1")
        val state = viewModel.uiState.value
        assertTrue(state.isLoaded)
        assertEquals("n1", state.noteId)
        assertEquals("Title", state.title)
        assertEquals("Content", state.content)
        assertTrue(state.isEditable)
    }

    @Test
    fun `deleting a voice block removes its audio metadata through the cleanup use case`() = runTest {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.Voice(
                        blockId = "voice-1",
                        audioFilePath = "/data/data/app/files/voice-notes/voice.m4a",
                        audioFormat = AudioFormat.AAC,
                        durationMs = 1_000L,
                        fileSizeBytes = 100L,
                        sampleRateHertz = 44_100,
                        channels = 1,
                        createdAt = 1L,
                        updatedAt = 1L
                    ),
                    EditorBlock.TextBlock(id = "transcript-1")
                )
            ),
            isLoaded = true
        )

        viewModel.deleteBlock("voice-1")
        advanceUntilIdle()

        coVerify { deleteVoiceNoteBlockUseCase("voice-1") }
        assertTrue(viewModel.uiState.value.document.blocks.none { it.id == "voice-1" })
    }

    @Test
    fun `load read only note disables editing`() = runTest {
        viewModel.load("readonly")
        val state = viewModel.uiState.value
        assertTrue(state.isLoaded)
        assertEquals("readonly", state.noteId)
        assertEquals("Shared read only", state.title)
        assertEquals("Locked content", state.content)
        assertTrue(!state.isEditable)
    }

    @Test
    fun `load without noteId generates new id`() = runTest {
        viewModel.load(null)
        val state = viewModel.uiState.value
        assertTrue(state.isLoaded)
        assertTrue(state.noteId?.startsWith("note_") == true)
        assertEquals("", state.title)
        assertEquals(NoteSummaryUiState.Empty, state.summaryState)
    }

    @Test
    fun `load without noteId after loading a note resets state`() = runTest {
        viewModel.load("n1")
        assertEquals("Title", viewModel.uiState.value.title)
        viewModel.load(null)
        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.content)
    }

    @Test
    fun `load non empty note exposes generated summary`() = runTest {
        coEvery { noteRepository.getNoteById("summary") } returns testNote.copy(
            id = "summary",
            content = longNoteText()
        )

        viewModel.load("summary")

        assertEquals(NoteSummaryUiState.Content("Generated local summary."), viewModel.uiState.value.summaryState)
    }

    @Test
    fun `load blank note exposes empty summary state`() = runTest {
        coEvery { noteRepository.getNoteById("blank") } returns testNote.copy(
            id = "blank",
            content = ""
        )

        viewModel.load("blank")

        assertEquals(NoteSummaryUiState.Empty, viewModel.uiState.value.summaryState)
        assertTrue(noteSummarizer.inputs.isEmpty())
    }

    @Test
    fun `load note keeps editor usable when summary fails`() = runTest {
        coEvery { noteRepository.getNoteById("summary_error") } returns testNote.copy(
            id = "summary_error",
            content = longNoteText()
        )
        noteSummarizer.failure = NoteSummaryUnavailableException()

        viewModel.load("summary_error")

        val state = viewModel.uiState.value
        assertEquals(NoteSummaryUiState.Error, state.summaryState)
        assertTrue(state.isEditable)
        assertEquals("summary_error", state.noteId)
    }

    @Test
    fun `onTitleChange updates state and schedules auto-save`() = runTest {
        viewModel.load("n1")
        viewModel.onTitleChange("New Title")
        assertEquals("New Title", viewModel.uiState.value.title)
        // Wait for auto-save (2000ms delay in code)
        advanceTimeBy(2001)
        coVerify { noteRepository.save(match { it.title == "New Title" }) }
    }

    @Test
    fun `save calls repository save`() = runTest {
        viewModel.load("n1")
        viewModel.onContentChange("New Content")
        var called = false
        viewModel.save { called = true }
        coVerify {
            noteRepository.save(
                match {
                    val json = JSONObject(it.content)
                    json.getJSONArray("blocks").getJSONObject(0)
                        .getJSONArray("children").getJSONObject(0).getString("text") == "New Content"
                }
            )
        }
        assertTrue(called)
    }

    @Test
    fun `addImageBlock adds image block and saves structured json`() = runTest {
        viewModel.load("n1")
        viewModel.addImageBlock()
        val imageBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.ImageBlock>().first()
        viewModel.updateImageBlock(imageBlock.id, url = "https://cdn.example.com/image.png", caption = "My image")
        viewModel.save {}
        coVerify {
            noteRepository.save(
                match {
                    val blocks = JSONObject(it.content).getJSONArray("blocks")
                    (0 until blocks.length()).any { index ->
                        val block = blocks.getJSONObject(index)
                        block.getString("type") == "image" &&
                            block.getString("url") == "https://cdn.example.com/image.png" &&
                            block.getString("caption") == "My image"
                    }
                }
            )
        }
    }

    @Test
    fun basicBlockFactoryCreatesExpectedDefaults() {
        val supportedTypes = BasicBlockType.entries.filter { it != BasicBlockType.UNKNOWN }
        val blocks = supportedTypes.map(viewModel::createBasicBlock)

        assertEquals(supportedTypes.map(BasicBlockType::storageValue), blocks.map(EditorBlock.TextBlock::type))
        assertEquals(blocks.size, blocks.map(EditorBlock.TextBlock::id).toSet().size)
        assertTrue(blocks.all { it.children == listOf(RichText("")) })
        assertTrue(!blocks.single { it.type == "checkbox" }.checked)
        assertTrue(blocks.single { it.type == "toggle" }.isExpanded)
    }

    @Test
    fun toggleExpandedStatePersistsAcrossDocumentRoundTrip() {
        val toggleBlock = viewModel.createBasicBlock(BasicBlockType.TOGGLE_LIST)
        setEditorDocument(toggleBlock)

        assertTrue(toggleBlock.isExpanded)
        assertTrue(viewModel.toggleToggleExpanded(toggleBlock.id))

        val collapsedBlock = viewModel.uiState.value.document.blocks.single() as EditorBlock.TextBlock
        val restoredBlock = NoteDocument.fromContent(viewModel.uiState.value.document.toJsonString())
            .blocks
            .single() as EditorBlock.TextBlock

        assertTrue(!collapsedBlock.isExpanded)
        assertTrue(!restoredBlock.isExpanded)
    }

    @Test
    fun `read only toggle expansion is ignored`() {
        val toggleBlock = viewModel.createBasicBlock(BasicBlockType.TOGGLE_LIST)
        setEditorDocument(toggleBlock, editable = false)

        assertTrue(!viewModel.toggleToggleExpanded(toggleBlock.id))
        assertTrue((viewModel.uiState.value.document.blocks.single() as EditorBlock.TextBlock).isExpanded)
    }

    @Test
    fun `onTextBlockChange splits newline into separate text blocks`() = runTest {
        viewModel.load("n1")
        val firstBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        viewModel.onTextBlockChange(firstBlock.id, "First line\nSecond line")
        val textBlocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, textBlocks.size)
        assertEquals(firstBlock.id, textBlocks[0].id)
        assertEquals("First line", textBlocks[0].children.joinToString("") { it.text })
        assertEquals("Second line", textBlocks[1].children.joinToString("") { it.text })
    }

    @Test
    fun `addTableBlock updates table cell and saves structured json`() = runTest {
        viewModel.load("n1")
        viewModel.addTableBlock()
        val tableBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TableBlock>().first()
        viewModel.updateTableCell(tableBlock.id, rowIndex = 1, cellIndex = 0, value = "Alice")
        viewModel.save {}
        coVerify {
            noteRepository.save(
                match {
                    val blocks = JSONObject(it.content).getJSONArray("blocks")
                    (0 until blocks.length()).any { index ->
                        val block = blocks.getJSONObject(index)
                        block.getString("type") == "table" &&
                            block.getJSONArray("rows").getJSONArray(1).getJSONArray(0)
                                .getJSONObject(0).getString("text") == "Alice"
                    }
                }
            )
        }
    }

    @Test
    fun `updateTableCell strips newlines`() = runTest {
        viewModel.load("n1")
        viewModel.addTableBlock()
        val tableBlock = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TableBlock>().first()
        viewModel.updateTableCell(tableBlock.id, rowIndex = 1, cellIndex = 0, value = "Alice\nBob")
        val updatedTableBlock = viewModel.uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TableBlock>()
            .first()
        val cellText = updatedTableBlock.rows[1][0].joinToString("") { it.text }
        assertEquals("Alice Bob", cellText)
    }

    @Test
    fun tableInsertOperations() = runTest {
        val tableId = "table-insert"
        val originalRows = tableRows("A", "B", "C", "D")

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.insertTableColumnLeft(tableId, columnIndex = 0)
        var table = currentTable(tableId)
        assertEquals(2, table.rows.size)
        assertEquals(3, table.rows[0].size)
        assertEquals("", table.rows[0][0].joinToString("") { it.text })
        assertEquals("A", table.rows[0][1].joinToString("") { it.text })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.insertTableColumnRight(tableId, columnIndex = 1)
        table = currentTable(tableId)
        assertEquals(3, table.rows[0].size)
        assertEquals("B", table.rows[0][1].joinToString("") { it.text })
        assertEquals("", table.rows[0][2].joinToString("") { it.text })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.insertTableRowAbove(tableId, rowIndex = 0)
        table = currentTable(tableId)
        assertEquals(3, table.rows.size)
        assertTrue(table.rows[0].all { cell -> cell.joinToString("") { it.text }.isEmpty() })
        assertEquals("A", table.rows[1][0].joinToString("") { it.text })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.insertTableRowBelow(tableId, rowIndex = 1)
        table = currentTable(tableId)
        assertEquals(3, table.rows.size)
        assertEquals("D", table.rows[1][1].joinToString("") { it.text })
        assertTrue(table.rows[2].all { cell -> cell.joinToString("") { it.text }.isEmpty() })

        val unevenRows = listOf(
            listOf(listOf(RichText("A"))),
            listOf(listOf(RichText("B")), listOf(RichText("C")))
        )
        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = unevenRows))
        viewModel.insertTableColumnRight(tableId, columnIndex = 1)
        table = currentTable(tableId)
        assertEquals(3, table.rows[0].size)
        assertTrue(table.rows[0].drop(1).all { cell -> cell.joinToString("") { it.text }.isEmpty() })
    }

    @Test
    fun tableDeleteOperations() = runTest {
        val tableId = "table-delete"
        val originalRows = tableRows("A", "B", "C", "D")

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.deleteTableColumn(tableId, columnIndex = 0)
        var table = currentTable(tableId)
        assertEquals(1, table.rows[0].size)
        assertEquals("B", table.rows[0][0].joinToString("") { it.text })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.deleteTableRow(tableId, rowIndex = 0)
        table = currentTable(tableId)
        assertEquals(1, table.rows.size)
        assertEquals("C", table.rows[0][0].joinToString("") { it.text })

        setEditorDocument(
            EditorBlock.TableBlock(
                id = tableId,
                rows = listOf(listOf(originalRows[0][0]))
            )
        )
        viewModel.deleteTableColumn(tableId, columnIndex = 0)
        assertTrue(viewModel.uiState.value.document.blocks.none { it.id == tableId })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = listOf(originalRows[0])))
        viewModel.deleteTableRow(tableId, rowIndex = 0)
        assertTrue(viewModel.uiState.value.document.blocks.none { it.id == tableId })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.deleteTable(tableId)
        assertTrue(viewModel.uiState.value.document.blocks.none { it.id == tableId })
    }

    @Test
    fun tableClearOperations() = runTest {
        val tableId = "table-clear"
        val originalRows = tableRows("A", "B", "C", "D")
        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))

        viewModel.clearTableColumn(tableId, columnIndex = 0)
        var table = currentTable(tableId)
        assertEquals("", table.rows[0][0].joinToString("") { it.text })
        assertEquals("B", table.rows[0][1].joinToString("") { it.text })
        assertEquals("", table.rows[1][0].joinToString("") { it.text })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.clearTableRow(tableId, rowIndex = 1)
        table = currentTable(tableId)
        assertEquals("A", table.rows[0][0].joinToString("") { it.text })
        assertEquals("", table.rows[1][0].joinToString("") { it.text })
        assertEquals("", table.rows[1][1].joinToString("") { it.text })

        viewModel.clearTable(tableId)
        table = currentTable(tableId)
        assertTrue(table.rows.flatten().all { cell -> cell.joinToString("") { it.text }.isEmpty() })
    }

    @Test
    fun tableActionDispatcherRoutesEveryProductionCommand() = runTest {
        val tableId = "table-dispatch"
        val originalRows = tableRows("A", "B", "C", "D")

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.InsertColumnLeft(tableId, 0))
        assertEquals(3, currentTable(tableId).rows[0].size)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.InsertColumnRight(tableId, 1))
        assertEquals(3, currentTable(tableId).rows[0].size)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.DeleteColumn(tableId, 0))
        assertEquals(1, currentTable(tableId).rows[0].size)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.ClearColumn(tableId, 0))
        assertTrue(currentTable(tableId).rows.all { row -> row[0].joinToString("") { it.text }.isEmpty() })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.InsertRowAbove(tableId, 0))
        assertEquals(3, currentTable(tableId).rows.size)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.InsertRowBelow(tableId, 1))
        assertEquals(3, currentTable(tableId).rows.size)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.DeleteRow(tableId, 0))
        assertEquals(1, currentTable(tableId).rows.size)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.ClearRow(tableId, 0))
        assertTrue(currentTable(tableId).rows[0].all { cell -> cell.joinToString("") { it.text }.isEmpty() })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.ClearTable(tableId))
        assertTrue(currentTable(tableId).rows.flatten().all { cell -> cell.joinToString("") { it.text }.isEmpty() })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.ToggleTableFitToWidth(tableId))
        assertTrue(currentTable(tableId).fitToWidth)

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.DuplicateTable(tableId))
        assertEquals(2, viewModel.uiState.value.document.blocks.count { it is EditorBlock.TableBlock })

        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = originalRows))
        viewModel.onTableAction(TableHandleAction.DeleteTable(tableId))
        assertTrue(viewModel.uiState.value.document.blocks.none { it.id == tableId })
    }

    @Test
    fun tableFocusActionsPersistTargetInViewModelState() = runTest {
        val tableId = "table-focus"
        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = tableRows("A", "B", "C", "D")))

        viewModel.onTableAction(TableHandleAction.FocusCell(tableId, rowIndex = 1, columnIndex = 0))

        assertEquals(
            TableFocusTarget(rowIndex = 1, columnIndex = 0),
            viewModel.uiState.value.focusedTableCells[tableId]
        )

        viewModel.onTableAction(TableHandleAction.ClearFocus(tableId))

        assertTrue(tableId !in viewModel.uiState.value.focusedTableCells)
    }

    @Test
    fun duplicateTableDeepCopies() = runTest {
        val table = EditorBlock.TableBlock(
            id = "table-duplicate",
            rows = tableRows("A", "B", "C", "D"),
            fitToWidth = true
        )
        setEditorDocument(
            EditorBlock.TextBlock(id = "before"),
            table,
            EditorBlock.TextBlock(id = "after")
        )

        viewModel.duplicateTable(table.id)

        val blocks = viewModel.uiState.value.document.blocks
        val duplicate = blocks[2] as EditorBlock.TableBlock
        assertEquals(table.rows, duplicate.rows)
        assertEquals(table.fitToWidth, duplicate.fitToWidth)
        assertEquals(table.id, (blocks[1] as EditorBlock.TableBlock).id)
        assertNotSame(table.id, duplicate.id)
        assertNotSame(table.rows, duplicate.rows)
        assertNotSame(table.rows[0], duplicate.rows[0])
        assertNotSame(table.rows[0][0][0], duplicate.rows[0][0][0])
    }

    @Test
    fun toggleTableFitToWidth() = runTest {
        val tableId = "table-fit"
        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = tableRows("A", "B", "C", "D")))

        viewModel.toggleTableFitToWidth(tableId)
        assertTrue(currentTable(tableId).fitToWidth)
        viewModel.toggleTableFitToWidth(tableId)
        assertTrue(!currentTable(tableId).fitToWidth)
    }

    @Test
    fun readOnlyTableCommandsAreNoOps() = runTest {
        val tableId = "table-read-only"
        val table = EditorBlock.TableBlock(id = tableId, rows = tableRows("A", "B", "C", "D"))
        setEditorDocument(table, editable = false)
        val initialDocument = viewModel.uiState.value.document

        viewModel.insertTableColumnLeft(tableId, 0)
        viewModel.insertTableColumnRight(tableId, 0)
        viewModel.deleteTableColumn(tableId, 0)
        viewModel.clearTableColumn(tableId, 0)
        viewModel.insertTableRowAbove(tableId, 0)
        viewModel.insertTableRowBelow(tableId, 0)
        viewModel.deleteTableRow(tableId, 0)
        viewModel.clearTableRow(tableId, 0)
        viewModel.clearTable(tableId)
        viewModel.duplicateTable(tableId)
        viewModel.deleteTable(tableId)
        viewModel.toggleTableFitToWidth(tableId)
        advanceTimeBy(2_001)

        assertEquals(initialDocument, viewModel.uiState.value.document)
        coVerify(exactly = 0) { noteRepository.save(any()) }
    }

    @Test
    fun tableOperationsAutoSaveUpdatedDocument() = runTest {
        val tableId = "table-save"
        setEditorDocument(EditorBlock.TableBlock(id = tableId, rows = tableRows("A", "B", "C", "D")))

        viewModel.clearTableColumn(tableId, columnIndex = 0)
        advanceTimeBy(2_001)

        coVerify {
            noteRepository.save(
                match { note ->
                    val savedTable = NoteDocument.fromContent(note.content).blocks
                        .filterIsInstance<EditorBlock.TableBlock>()
                        .single()
                    savedTable.rows[0][0].joinToString("") { it.text }.isEmpty() &&
                        savedTable.rows[0][1].joinToString("") { it.text } == "B"
                }
            )
        }
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        viewModel.load("n1")
        var called = false
        viewModel.delete { called = true }
        coVerify { noteRepository.delete(match { it.id == "n1" }) }
        assertTrue(called)
    }

    @Test
    fun `toggleFavorite updates state and saves note`() = runTest {
        viewModel.load("n1")
        viewModel.toggleFavorite()
        assertTrue(viewModel.uiState.value.isFavorite)
        coVerify { noteRepository.save(match { it.id == "n1" && it.isFavorite }) }

        viewModel.toggleFavorite()
        assertTrue(!viewModel.uiState.value.isFavorite)
        coVerify { noteRepository.save(match { it.id == "n1" && !it.isFavorite }) }
    }

    @Test
    fun `onFolderSelected updates state and schedules auto-save`() = runTest {
        viewModel.load("n1")
        viewModel.onFolderSelected("f2")
        assertEquals("f2", viewModel.uiState.value.folderId)
        advanceTimeBy(2001)
        coVerify { noteRepository.save(match { it.folderId == "f2" }) }
    }

    @Test
    fun `toggleFormattingToolbar toggles visibility`() = runTest {
        assertTrue(!viewModel.uiState.value.isFormattingToolbarVisible)
        viewModel.toggleFormattingToolbar()
        assertTrue(viewModel.uiState.value.isFormattingToolbarVisible)
        viewModel.toggleFormattingToolbar()
        assertTrue(!viewModel.uiState.value.isFormattingToolbarVisible)
    }

    @Test
    fun `setFocusedBlock updates focusedBlockId`() = runTest {
        viewModel.setFocusedBlock("block1")
        assertEquals("block1", viewModel.uiState.value.focusedBlockId)
        viewModel.setFocusedBlock(null)
        assertEquals(null, viewModel.uiState.value.focusedBlockId)
    }

    @Test
    fun `updateSelection updates selection indices`() = runTest {
        viewModel.updateSelection(10, 20)
        assertEquals(10, viewModel.uiState.value.selectionStart)
        assertEquals(20, viewModel.uiState.value.selectionEnd)
    }

    @Test
    fun `deleteBlock removes block and updates focus`() = runTest {
        viewModel.load("n1")
        viewModel.insertBasicBlock(BasicBlockType.PARAGRAPH) // Now we have 2 blocks
        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(2, blocks.size)

        val blockToDelete = blocks[1].id
        viewModel.deleteBlock(blockToDelete)

        assertEquals(1, viewModel.uiState.value.document.blocks.size)
        assertEquals(blocks[0].id, viewModel.uiState.value.focusedBlockId)
    }

    @Test
    fun `deleteBlock does nothing if only one block remains`() = runTest {
        viewModel.load("n1")
        val blockId = viewModel.uiState.value.document.blocks[0].id
        viewModel.deleteBlock(blockId)
        assertEquals(1, viewModel.uiState.value.document.blocks.size)
    }

    @Test
    fun `shareCurrentNote saves note and calls callback`() = runTest {
        viewModel.load("n1")
        var readyNoteId: String? = null
        viewModel.shareCurrentNote { readyNoteId = it }
        advanceUntilIdle()
        assertEquals("n1", readyNoteId)
        coVerify { noteRepository.save(any()) }
    }

    @Test
    fun `load with non-existent noteId generates new id`() = runTest {
        coEvery { noteRepository.getNoteById("non_existent") } returns null
        viewModel.load("non_existent")
        val state = viewModel.uiState.value
        assertTrue(state.isLoaded)
        assertTrue(state.noteId?.startsWith("note_") == true)
    }

    @Test
    fun `rename updates title and saves internally`() = runTest {
        viewModel.load("n1")
        viewModel.rename("Renamed Title")
        assertEquals("Renamed Title", viewModel.uiState.value.title)
        advanceUntilIdle()
        coVerify { noteRepository.save(match { it.title == "Renamed Title" }) }
    }

    @Test
    fun `delete with unsaved note does not call repository delete`() = runTest {
        viewModel.load(null) // createdAt will be 0
        var called = false
        viewModel.delete { called = true }
        coVerify(exactly = 0) { noteRepository.delete(any()) }
        assertTrue(called)
    }

    @Test
    fun `read only note mutating actions are ignored`() = runTest {
        viewModel.load("readonly")
        val initialBlocks = viewModel.uiState.value.document.blocks.size

        viewModel.onTitleChange("Changed")
        viewModel.onContentChange("Changed content")
        viewModel.onFolderSelected("f2")
        viewModel.toggleFavorite()
        viewModel.insertBasicBlock(BasicBlockType.PARAGRAPH)
        viewModel.addImageBlock()
        viewModel.addTableBlock()
        val blockId = viewModel.uiState.value.document.blocks.first().id
        viewModel.onTextBlockChange(blockId, "Edited")
        viewModel.toggleBlockMark(blockId, "bold")
        viewModel.deleteBlock(blockId)

        advanceTimeBy(2001)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Shared read only", state.title)
        assertEquals("Locked content", state.content)
        assertEquals("f1", state.folderId)
        assertTrue(!state.isFavorite)
        assertEquals(initialBlocks, state.document.blocks.size)
        coVerify(exactly = 0) { noteRepository.save(any()) }
        coVerify(exactly = 0) { noteRepository.delete(any()) }
    }

    @Test
    fun `delete on read only note does not call repository delete`() = runTest {
        viewModel.load("readonly")
        var called = false

        viewModel.delete { called = true }

        coVerify(exactly = 0) { noteRepository.delete(any()) }
        assertTrue(called)
    }

    @Test
    fun `toggleBlockMark applies bold mark to selection`() = runTest {
        viewModel.load("n1")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        viewModel.setFocusedBlock(block.id)
        viewModel.updateSelection(0, 5) // "Title" is at 0-5? No, "Content" is the text in load("n1")
        // testNote has title "Title" and content "Content"
        // But wait, the mapper fromContent("Content") will have "Content" as text.

        viewModel.toggleBlockMark(block.id, "bold")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertTrue(updatedBlock.children.any { "bold" in it.marks })
    }

    @Test
    fun `toggleBlockMark removes bold mark if already selected with markers`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("**Bold** Text")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()

        viewModel.updateSelection(0, 8) // "**Bold**"
        viewModel.toggleBlockMark(block.id, "bold")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals("Bold Text", updatedBlock.text())
    }

    @Test
    fun `reproduce bug bold button cannot unbold`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("**Bold** Text")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()

        // "Bold" is at index 0 to 4 in the plain text "Bold Text"
        viewModel.updateSelection(0, 4)
        viewModel.toggleBlockMark(block.id, "bold")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        val firstChild = updatedBlock.children.first()
        assertTrue("bold" !in firstChild.marks)
    }

    @Test
    fun `reproduce bug italic button not working`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("*Italic* Text")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()

        // "Italic" is at index 0 to 6 in the plain text "Italic Text"
        viewModel.updateSelection(0, 6)
        viewModel.toggleBlockMark(block.id, "italic")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        val firstChild = updatedBlock.children.first()
        assertTrue("italic" !in firstChild.marks)
    }

    @Test
    fun `toggleBlockMark applies underline mark to selection`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("Underline Text")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()

        viewModel.updateSelection(0, 9)
        viewModel.toggleBlockMark(block.id, "underline")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        val firstChild = updatedBlock.children.first()
        assertTrue("underline" in firstChild.marks)
    }

    @Test
    fun `toggleBlockMark applies strikethrough mark to selection`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("Strikethrough Text")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()

        viewModel.updateSelection(0, 13)
        viewModel.toggleBlockMark(block.id, "strikethrough")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        val firstChild = updatedBlock.children.first()
        assertTrue("strikethrough" in firstChild.marks)
    }

    @Test
    fun `toggleBlockMark with selection bolds ONLY selected portion and leaves rest unbold`() = runTest {
        viewModel.load("n1") // testNote.content = "Content" (length 7)
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        viewModel.setFocusedBlock(block.id)
        viewModel.updateSelection(0, 5) // "Conte"

        viewModel.toggleBlockMark(block.id, "bold")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        val selectedChild = updatedBlock.children.firstOrNull { it.text == "Conte" }
        val unselectedChild = updatedBlock.children.firstOrNull { it.text == "nt" }
        assertNotNull("Selected portion 'Conte' should exist as a child", selectedChild)
        assertNotNull("Unselected portion 'nt' should exist as a child", unselectedChild)
        assertTrue("Selected 'Conte' should be bold", selectedChild!!.marks.contains("bold"))
        assertTrue("Unselected 'nt' should NOT be bold", !unselectedChild!!.marks.contains("bold"))
    }

    @Test
    fun `toggleBlockMark with collapsed cursor (no selection) does NOT bold whole block`() = runTest {
        viewModel.load("n1") // testNote.content = "Content"
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        viewModel.setFocusedBlock(block.id)
        viewModel.updateSelection(3, 3) // cursor at position 3, no selection

        viewModel.toggleBlockMark(block.id, "bold")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertTrue(
            "With no selection, bold should NOT be applied to the whole block. " +
                "Actual children: ${updatedBlock.children.map { it.text to it.marks }}",
            updatedBlock.children.none { "bold" in it.marks }
        )
    }

    @Test
    fun `toggleCheckbox converts paragraph to checkbox block`() = runTest {
        viewModel.load("n1")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals("paragraph", block.type)

        viewModel.toggleCheckbox(block.id)

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals("checkbox", updatedBlock.type)
        assertTrue(!updatedBlock.checked)
    }

    @Test
    fun `toggleCheckbox converts checkbox block back to paragraph`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("- [ ] Task")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals("checkbox", block.type)

        viewModel.toggleCheckbox(block.id)

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals("paragraph", updatedBlock.type)
    }

    @Test
    fun `toggleCheckboxChecked flips checked state`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("- [ ] Task")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals("checkbox", block.type)
        assertTrue(!block.checked)

        viewModel.toggleCheckboxChecked(block.id)

        val updatedBlock1 = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertTrue(updatedBlock1.checked)

        viewModel.toggleCheckboxChecked(block.id)

        val updatedBlock2 = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertTrue(!updatedBlock2.checked)
    }

    @Test
    fun `splitTextBlock on non-empty checkbox propagates unchecked checkbox`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("- [x] Task Text")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals("checkbox", block.type)
        assertTrue(block.checked)

        viewModel.onTextBlockChange(block.id, "Task Text\nNew Task")

        val blocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, blocks.size)
        assertEquals("checkbox", blocks[0].type)
        assertTrue(blocks[0].checked) // remains checked
        assertEquals("checkbox", blocks[1].type)
        assertTrue(!blocks[1].checked) // propagated is unchecked
    }

    @Test
    fun `splitTextBlock on empty checkbox converts both to paragraph`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("- [ ] ")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals("checkbox", block.type)

        viewModel.onTextBlockChange(block.id, "\n")

        val blocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, blocks.size)
        assertEquals("paragraph", blocks[0].type)
        assertEquals("paragraph", blocks[1].type)
    }

    @Test
    fun `splitTextBlock moves focus and cursor to the newly created block`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("Hello")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        viewModel.setFocusedBlock(block.id)
        viewModel.updateSelection(block.text().length, block.text().length)

        viewModel.onTextBlockChange(block.id, "Hello\n")

        val blocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, blocks.size)
        assertEquals("Hello", blocks[0].text())
        assertEquals("", blocks[1].text())
        assertEquals(
            "New block must be focused so the cursor continues on the new line",
            blocks[1].id,
            viewModel.uiState.value.focusedBlockId
        )
        assertEquals(0, viewModel.uiState.value.selectionStart)
        assertEquals(0, viewModel.uiState.value.selectionEnd)
    }

    @Test
    fun `onTextBlockChange on existing checkbox preserves checkbox type`() = runTest {
        viewModel.load(null)
        viewModel.onContentChange("- [ ] ")
        val block = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals("checkbox", block.type)

        viewModel.onTextBlockChange(block.id, "Buy milk")

        val updatedBlock = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals("checkbox", updatedBlock.type)
        assertEquals("Buy milk", updatedBlock.text())
    }

    @Test
    fun `splitTextBlock preserves toggle type and state`() = runTest {
        val toggleBlock = viewModel.createBasicBlock(BasicBlockType.TOGGLE_LIST).copy(
            children = listOf(RichText("First")),
            isExpanded = false
        )
        setEditorDocument(toggleBlock)

        viewModel.onTextBlockChange(toggleBlock.id, "First\nSecond")

        val blocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(listOf("toggle", "toggle"), blocks.map(EditorBlock.TextBlock::type))
        assertTrue(!blocks.first().isExpanded)
        assertTrue(blocks.last().isExpanded)
    }

    @Test
    fun `insertBasicBlock inserts new block after focused block`() = runTest {
        viewModel.load("n1")
        val initialBlocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(1, initialBlocks.size)
        val firstBlockId = initialBlocks.first().id
        viewModel.setFocusedBlock(firstBlockId)

        val inserted = viewModel.insertBasicBlock(BasicBlockType.HEADING_1)

        assertTrue(inserted)
        val updatedBlocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, updatedBlocks.size)
        assertEquals(firstBlockId, updatedBlocks[0].id)
        assertEquals("heading_1", updatedBlocks[1].type)
        assertEquals(updatedBlocks[1].id, viewModel.uiState.value.focusedBlockId)
        assertEquals(0, viewModel.uiState.value.selectionStart)
        assertEquals(0, viewModel.uiState.value.selectionEnd)
    }

    @Test
    fun `insertBasicBlock appends new block to end when no block is focused`() = runTest {
        viewModel.load("n1")
        viewModel.setFocusedBlock(null)

        val inserted = viewModel.insertBasicBlock(BasicBlockType.QUOTE)

        assertTrue(inserted)
        val updatedBlocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(2, updatedBlocks.size)
        assertEquals("quote", updatedBlocks.last().type)
        assertEquals(updatedBlocks.last().id, viewModel.uiState.value.focusedBlockId)
    }

    @Test
    fun `insertBasicBlock on read only note returns false and mutates nothing`() = runTest {
        viewModel.load("readonly")
        val initialBlocksCount = viewModel.uiState.value.document.blocks.size

        val inserted = viewModel.insertBasicBlock(BasicBlockType.CALLOUT)

        assertTrue(!inserted)
        assertEquals(initialBlocksCount, viewModel.uiState.value.document.blocks.size)
    }

    @Test
    fun bodyResetWithNoOrCrossBlockSelectionLeavesDocumentUnchanged() = runTest {
        val block1 = EditorBlock.TextBlock(
            id = "b1",
            type = "heading_1",
            children = listOf(RichText("Title", listOf("bold")))
        )
        val block2 = EditorBlock.TextBlock(
            id = "b2",
            type = "paragraph",
            children = listOf(RichText("Paragraph", listOf("italic")))
        )
        val initialDoc = NoteDocument(blocks = listOf(block1, block2))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = initialDoc,
            focusedBlockId = "b1",
            selectionStart = 2,
            selectionEnd = 2,
            pendingTypingMarks = setOf("underline"),
            isLoaded = true,
            isEditable = true
        )

        // 1. Collapsed selection (2, 2) on b1 leaves document unchanged
        viewModel.resetSelectedTextToBody("b1")
        assertEquals(initialDoc, viewModel.uiState.value.document)
        assertEquals(2, viewModel.uiState.value.selectionStart)
        assertEquals(2, viewModel.uiState.value.selectionEnd)
        assertEquals(setOf("underline"), viewModel.uiState.value.pendingTypingMarks)

        // 2. Unfocused block target ("b2" while focusedBlockId is "b1") leaves document unchanged
        viewModel.updateSelection(0, 5)
        viewModel.resetSelectedTextToBody("b2")
        assertEquals(initialDoc, viewModel.uiState.value.document)

        // 3. Out of bounds selection leaves document unchanged
        viewModel.updateSelection(0, 100)
        viewModel.resetSelectedTextToBody("b1")
        assertEquals(initialDoc, viewModel.uiState.value.document)

        // 4. Negative selection leaves document unchanged
        viewModel.updateSelection(-1, -1)
        viewModel.resetSelectedTextToBody("b1")
        assertEquals(initialDoc, viewModel.uiState.value.document)

        advanceUntilIdle()
        coVerify(exactly = 0) { noteRepository.save(any()) }
    }

    @Test
    fun `resetSelectedTextToBody on heading splits into prefix, body paragraph, and suffix`() = runTest {
        val block = EditorBlock.TextBlock(
            id = "b1",
            type = "heading_1",
            children = listOf(RichText("Prefix Target Suffix", listOf("bold")))
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "b1",
            selectionStart = 7,
            selectionEnd = 13,
            isLoaded = true,
            isEditable = true
        )

        viewModel.resetSelectedTextToBody("b1")

        val blocks = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        assertEquals(3, blocks.size)
        assertEquals("Prefix ", blocks[0].text())
        assertEquals("heading_1", blocks[0].type)
        assertEquals("Target", blocks[1].text())
        assertEquals("paragraph", blocks[1].type)
        assertTrue(blocks[1].children[0].marks.isEmpty())
        assertEquals(" Suffix", blocks[2].text())
        assertEquals("heading_1", blocks[2].type)
    }

    @Test
    fun `toggleBlockMark at collapsed cursor toggles pending typing marks`() = runTest {
        val block = EditorBlock.TextBlock(id = "b1", children = listOf(RichText("Text")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "b1",
            selectionStart = 4,
            selectionEnd = 4,
            isLoaded = true,
            isEditable = true
        )

        viewModel.toggleBlockMark("b1", "bold")
        assertEquals(setOf("bold"), viewModel.uiState.value.pendingTypingMarks)

        viewModel.toggleBlockMark("b1", "italic")
        assertEquals(setOf("bold", "italic"), viewModel.uiState.value.pendingTypingMarks)

        viewModel.toggleBlockMark("b1", "bold")
        assertEquals(setOf("italic"), viewModel.uiState.value.pendingTypingMarks)
    }

    @Test
    fun `onTextBlockChange applies pending typing marks to typed text`() = runTest {
        val block = EditorBlock.TextBlock(id = "b1", children = listOf(RichText("Hello")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = "b1",
            selectionStart = 5,
            selectionEnd = 5,
            pendingTypingMarks = setOf("code"),
            isLoaded = true,
            isEditable = true
        )

        viewModel.onTextBlockChange("b1", "Hello world")

        val updated = viewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().first()
        assertEquals(2, updated.children.size)
        assertEquals("Hello", updated.children[0].text)
        assertEquals(emptyList<String>(), updated.children[0].marks)
        assertEquals(" world", updated.children[1].text)
        assertEquals(listOf("code"), updated.children[1].marks)
    }

    private fun setEditorDocument(vararg blocks: EditorBlock, editable: Boolean = true) {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "table-note",
            title = "Table note",
            document = NoteDocument(blocks = blocks.toList()),
            isEditable = editable,
            isLoaded = true
        )
    }

    private fun currentTable(tableId: String): EditorBlock.TableBlock =
        viewModel.uiState.value.document.blocks.first { it.id == tableId } as EditorBlock.TableBlock

    private fun tableRows(
        firstCell: String,
        secondCell: String,
        thirdCell: String,
        fourthCell: String
    ): List<List<List<RichText>>> = listOf(
        listOf(listOf(RichText(firstCell)), listOf(RichText(secondCell))),
        listOf(listOf(RichText(thirdCell)), listOf(RichText(fourthCell)))
    )

    private class FakeNoteSummarizer : NoteSummarizer {
        val inputs = mutableListOf<Pair<String, String>>()
        var failure: Throwable? = null

        override suspend fun summarize(title: String, noteText: String): NoteSummary {
            inputs += title to noteText
            failure?.let { throw it }
            return NoteSummary("Generated local summary.")
        }
    }

    private fun longNoteText(): String = List(70) { index ->
        "Editor paragraph $index contains enough detail for Gemini Nano summarization and local AI testing."
    }.joinToString(separator = " ")
}
