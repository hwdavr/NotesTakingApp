package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.text
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest : BaseViewModelTest() {
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val folderRepository: FolderRepository = mockk(relaxed = true)
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
        viewModel = NoteEditorViewModel(noteRepository, folderRepository)
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
        viewModel.addParagraphBlock() // Now we have 2 blocks
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
        viewModel.addParagraphBlock()
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
}
