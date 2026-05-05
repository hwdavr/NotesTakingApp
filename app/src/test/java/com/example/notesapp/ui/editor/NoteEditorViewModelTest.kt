package com.example.notesapp.ui.editor

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.document.EditorBlock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
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

    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns flowOf(emptyList())
        coEvery { noteRepository.getNoteById("n1") } returns testNote

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
}
