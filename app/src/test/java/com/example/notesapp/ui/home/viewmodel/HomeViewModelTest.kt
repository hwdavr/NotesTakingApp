package com.example.notesapp.ui.home.viewmodel

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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    private val testSharedNotes = listOf(
        Note(
            id = "sn1",
            title = "Shared Note",
            content = "Shared Content",
            isShared = true,
            folderId = "f1",
            sortKey = "3",
            deviceId = "other",
            createdAt = 0,
            updatedAt = 0
        )
    )
    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns flowOf(testFolders)
        every { noteRepository.getActiveNotes() } returns flowOf(testNotes)
        every { noteRepository.getSharedNotes() } returns flowOf(testSharedNotes)
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
        // 2 virtual folders (All Notes, Shared) + 2 test folders = 4
        assertEquals(4, state.recentFolders.size)
        assertEquals("all_notes", state.selectedFolderId)
    }
    @Test
    fun `uiState calculates note counts correctly`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        val state = viewModel.uiState.value
        val f1Model = state.recentFolders.find { it.id == "f1" }
        val favModel = state.recentFolders.find { it.id == "f2" }
        // f1 contains n1 (owned) and sn1 (shared)
        assertEquals(2, f1Model?.noteCount)
        assertEquals(1, favModel?.noteCount)
    }

    @Test
    fun `renameNote calls repository save`() = runTest {
        val note = testNotes[0]
        viewModel.renameNote(note, "New Title")
        advanceUntilIdle()
        io.mockk.coVerify { 
            noteRepository.save(match { it.id == note.id && it.title == "New Title" }) 
        }
    }

    @Test
    fun `deleteNote calls repository delete`() = runTest {
        val note = testNotes[0]
        viewModel.deleteNote(note)
        advanceUntilIdle()
        io.mockk.coVerify { noteRepository.delete(note) }
    }

    @Test
    fun `addNoteToFavorites calls repository toggleFavorite`() = runTest {
        val note = testNotes[0]
        viewModel.addNoteToFavorites(note)
        advanceUntilIdle()
        io.mockk.coVerify { noteRepository.toggleFavorite(note) }
    }

    @Test
    fun `selectFolder favorites handles missing Favorites folder`() = runTest {
        every { folderRepository.getFolders() } returns flowOf(listOf(testFolders[0])) // Only f1, no Favorites
        viewModel = HomeViewModel(noteRepository, folderRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        viewModel.selectFolder("favorites")
        val state = viewModel.uiState.value
        assertEquals("favorites", state.selectedFolderId)
        assertEquals(0, state.recentNotes.size)
    }

    @Test
    fun `selectFolder shared shows only shared notes`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        viewModel.selectFolder("shared")
        val state = viewModel.uiState.value
        assertEquals("shared", state.selectedFolderId)
        assertEquals(1, state.recentNotes.size)
        assertEquals("sn1", state.recentNotes[0].id)
        assertTrue(state.recentNotes[0].isShared)
    }
}
