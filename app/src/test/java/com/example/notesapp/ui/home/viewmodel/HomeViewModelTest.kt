package com.example.notesapp.ui.home.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
            updatedAt = 0,
            isFavorite = true
        ),
        Note(
            id = "n3",
            title = "Another Fav Note",
            content = "Another Fav Content",
            folderId = "f1",
            sortKey = "4",
            deviceId = "dev",
            createdAt = 0,
            updatedAt = 0,
            isFavorite = true
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
    fun `uiState initially shows loading until first sync completes`() = runTest {
        val syncGate = CompletableDeferred<Unit>()
        coEvery { folderRepository.sync() } coAnswers { syncGate.await() }
        coEvery { noteRepository.sync() } coAnswers { }
        val loadingViewModel = HomeViewModel(noteRepository, folderRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            loadingViewModel.uiState.collect()
        }
        val loadingState = loadingViewModel.uiState.value
        assertTrue(loadingState.isLoading)
        syncGate.complete(Unit)
        advanceUntilIdle()
        val loadedState = loadingViewModel.uiState.value
        assertFalse(loadedState.isLoading)
    }

    @Test
    fun `uiState initially shows all notes`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // All notes should include both active and shared notes
        assertEquals(4, state.recentNotes.size)
        assertTrue(state.recentNotes.any { it.id == "sn1" })
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
        // f1 contains n1 (owned), n3 (owned), and sn1 (shared)
        assertEquals(3, f1Model?.noteCount)
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
    fun `selectFolder favorites filters notes by isFavorite even if Favorites folder is missing`() = runTest {
        every { folderRepository.getFolders() } returns flowOf(listOf(testFolders[0])) // Only f1, no Favorites
        viewModel = HomeViewModel(noteRepository, folderRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.selectFolder("favorites")
        val state = viewModel.uiState.value
        assertEquals("favorites", state.selectedFolderId)
        // Should show both favorite notes n2 and n3 even if there's no folder named Favorites
        assertEquals(2, state.recentNotes.size)
        assertTrue(state.recentNotes.any { it.id == "n2" })
        assertTrue(state.recentNotes.any { it.id == "n3" })
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

    @Test
    fun `refresh triggers repository sync`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        viewModel.refresh()
        advanceUntilIdle()
        io.mockk.coVerify { folderRepository.sync() }
        io.mockk.coVerify { noteRepository.sync() }
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `init syncs note repository`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        io.mockk.coVerify { noteRepository.sync() }
    }

    @Test
    fun `refresh syncs note repository`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        viewModel.refresh()
        advanceUntilIdle()
        io.mockk.coVerify { noteRepository.sync() }
    }
}
