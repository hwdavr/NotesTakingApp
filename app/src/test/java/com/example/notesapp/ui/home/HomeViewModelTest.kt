package com.example.notesapp.ui.home

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
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
class HomeViewModelTest : BaseViewModelTest() {

    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private lateinit var viewModel: HomeViewModel

    private val testFolders = listOf(
        Folder(id = "f1", name = "Folder 1", sortKey = "1", deviceId = "dev", createdAt = 0, updatedAt = 0),
        Folder(id = "f2", name = "Favorites", sortKey = "2", deviceId = "dev", createdAt = 0, updatedAt = 0)
    )

    private val testNotes = listOf(
        Note(
            id = "n1",
            title = "Note 1",
            content = "Content 1",
            folderId = "f1",
            sortKey = "1",
            deviceId = "dev",
            createdAt = 0,
            updatedAt = 0
        ),
        Note(
            id = "n2",
            title = "Fav Note",
            content = "Fav Content",
            folderId = "f2",
            sortKey = "2",
            deviceId = "dev",
            createdAt = 0,
            updatedAt = 0
        )
    )

    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns flowOf(testFolders)
        every { noteRepository.getActiveNotes() } returns flowOf(testNotes)

        viewModel = HomeViewModel(noteRepository, folderRepository)
    }

    @Test
    fun `uiState initially shows all notes`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.recentNotes.size)
        assertEquals(2, state.recentFolders.size)
        assertEquals("all_notes", state.selectedFolderId)
    }

    @Test
    fun `selectFolder updates uiState with filtered notes`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.selectFolder("f1")

        val state = viewModel.uiState.value
        assertEquals("f1", state.selectedFolderId)
        assertEquals(1, state.recentNotes.size)
        assertEquals("n1", state.recentNotes[0].id)
    }

    @Test
    fun `selectFolder favorites filters correctly`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.selectFolder("favorites")

        val state = viewModel.uiState.value
        assertEquals("favorites", state.selectedFolderId)
        assertEquals(1, state.recentNotes.size)
        assertEquals("n2", state.recentNotes[0].id)
    }
}
