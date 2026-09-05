package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteLinkPickerViewModelTest : BaseViewModelTest() {

    private val noteRepository: NoteRepository = mockk()
    private val folderRepository: FolderRepository = mockk()

    private fun createViewModel(
        callerNoteId: String = "current_note_id",
        hasExistingLink: Boolean = false
    ): NoteLinkPickerViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "callerNoteId" to callerNoteId,
                "hasExistingLink" to hasExistingLink
            )
        )
        return NoteLinkPickerViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            savedStateHandle = savedStateHandle
        )
    }

    private fun createNote(id: String, title: String, folderId: String? = null): Note {
        return Note(
            id = id,
            title = title,
            content = "content",
            folderId = folderId,
            createdAt = 1000L,
            updatedAt = 1000L
        )
    }

    private fun createFolder(id: String, name: String): Folder {
        return Folder(
            id = id,
            name = name,
            createdAt = 1000L,
            updatedAt = 1000L
        )
    }

    @Test
    fun initialLoadingState() = runTest {
        val activeNotesFlow = MutableSharedFlow<List<Note>>()
        val foldersFlow = MutableSharedFlow<List<Folder>>()
        every { noteRepository.getActiveNotes() } returns activeNotesFlow
        every { folderRepository.getFolders() } returns foldersFlow

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }

        assertEquals(NoteLinkPickerUiState.Loading, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun candidateNotesExcludeCallerAndMapFolders() = runTest {
        val notes = listOf(
            createNote(id = "current_note_id", title = "Caller Note"),
            createNote(id = "note_1", title = "Design System", folderId = "f1"),
            createNote(id = "note_2", title = "General Ideas", folderId = null)
        )
        val folders = listOf(
            createFolder(id = "f1", name = "Architecture")
        )

        every { noteRepository.getActiveNotes() } returns flowOf(notes)
        every { folderRepository.getFolders() } returns flowOf(folders)

        val viewModel = createViewModel(callerNoteId = "current_note_id", hasExistingLink = true)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value as NoteLinkPickerUiState.Content
        assertEquals(2, state.notes.size)
        assertTrue(state.hasExistingLink)

        assertEquals("note_1", state.notes[0].id)
        assertEquals("Design System", state.notes[0].title)
        assertEquals("Architecture", state.notes[0].folderName)

        assertEquals("note_2", state.notes[1].id)
        assertEquals("General Ideas", state.notes[1].title)
        assertEquals(null, state.notes[1].folderName)

        job.cancel()
    }

    @Test
    fun searchQueryFiltersNotesByTitleAndFolder() = runTest {
        val notes = listOf(
            createNote(id = "note_1", title = "Design System", folderId = "f1"),
            createNote(id = "note_2", title = "Recipe Ideas", folderId = "f2"),
            createNote(id = "note_3", title = "Random Thoughts", folderId = null)
        )
        val folders = listOf(
            createFolder(id = "f1", name = "Engineering"),
            createFolder(id = "f2", name = "Personal")
        )

        every { noteRepository.getActiveNotes() } returns flowOf(notes)
        every { folderRepository.getFolders() } returns flowOf(folders)

        val viewModel = createViewModel(callerNoteId = "caller")
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Filter by title
        viewModel.onSearchQueryChanged("design")
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as NoteLinkPickerUiState.Content
        assertEquals(1, state1.notes.size)
        assertEquals("note_1", state1.notes.first().id)

        // Filter by folder name
        viewModel.onSearchQueryChanged("personal")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as NoteLinkPickerUiState.Content
        assertEquals(1, state2.notes.size)
        assertEquals("note_2", state2.notes.first().id)

        job.cancel()
    }

    @Test
    fun emptySearchQueryYieldsEmptyState() = runTest {
        val notes = listOf(
            createNote(id = "note_1", title = "Design System")
        )
        every { noteRepository.getActiveNotes() } returns flowOf(notes)
        every { folderRepository.getFolders() } returns flowOf(emptyList())

        val viewModel = createViewModel(callerNoteId = "caller", hasExistingLink = false)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("non_existent_term")
        advanceUntilIdle()

        val state = viewModel.uiState.value as NoteLinkPickerUiState.Empty
        assertEquals("non_existent_term", state.searchQuery)
        assertEquals(false, state.hasExistingLink)

        job.cancel()
    }

    @Test
    fun errorStateOnRepositoryFailure() = runTest {
        every { noteRepository.getActiveNotes() } returns flow { throw java.io.IOException("DB error") }
        every { folderRepository.getFolders() } returns flowOf(emptyList())

        val viewModel = createViewModel(hasExistingLink = true)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value as NoteLinkPickerUiState.Error
        assertEquals("DB error", state.message)
        assertTrue(state.hasExistingLink)

        viewModel.retry()
        advanceUntilIdle()

        job.cancel()
    }
}
