package com.example.notesapp.ui.notes.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest : BaseViewModelTest() {
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private lateinit var viewModel: NotesViewModel
    private val testNotes = listOf(
        Note(
            id = "n1",
            title = "Alpha",
            content = "Content 1",
            folderId = null,
            sortKey = "1",
            deviceId = "dev",
            createdAt = 0,
            updatedAt = 0
        ),
        Note(
            id = "n2",
            title = "Beta",
            content = "Content 2",
            folderId = null,
            sortKey = "2",
            deviceId = "dev",
            createdAt = 0,
            updatedAt = 0
        )
    )
    @Before
    fun setup() {
        every { noteRepository.getActiveNotes() } returns flowOf(testNotes)
        viewModel = NotesViewModel(noteRepository)
    }
    @Test
    fun `uiState initially shows all notes`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.notes.size)
    }
    @Test
    fun `onSearchChanged filters notes by title`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        viewModel.onSearchChanged("Alpha")
        val state = viewModel.uiState.value
        assertEquals(1, state.notes.size)
        assertEquals("Alpha", state.notes[0].title)
    }
    @Test
    fun `onSearchChanged filters notes by content`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        viewModel.onSearchChanged("Content 2")
        val state = viewModel.uiState.value
        assertEquals(1, state.notes.size)
        assertEquals("Beta", state.notes[0].title)
    }
}
