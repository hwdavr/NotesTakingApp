package com.example.notesapp.ui.editor

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
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
        
        coVerify { noteRepository.save(match { it.content == "New Content" }) }
        assertTrue(called)
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
