package com.example.notesapp.ui.folders

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoldersViewModelTest : BaseViewModelTest() {

    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private lateinit var viewModel: FoldersViewModel

    private val testFolders = listOf(
        Folder(id = "1", name = "Folder 1", sortKey = "1", deviceId = "dev", createdAt = 0, updatedAt = 0),
        Folder(id = "2", name = "Favorites", sortKey = "2", deviceId = "dev", createdAt = 0, updatedAt = 0)
    )

    private val testNotes = listOf(
        Note(id = "n1", title = "Note 1", content = "Content 1", folderId = "1", sortKey = "1", deviceId = "dev", createdAt = 0, updatedAt = 0)
    )

    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns flowOf(testFolders)
        every { noteRepository.getActiveNotes() } returns flowOf(testNotes)
        coEvery { noteRepository.getActiveNoteCount() } returns 1
        coEvery { noteRepository.getActiveNoteCountForFolder(any()) } returns 1

        viewModel = FoldersViewModel(folderRepository, noteRepository)
    }

    @Test
    fun `uiState initially reflects folders and notes`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        val state = viewModel.uiState.value
        assertEquals(2, state.treeItems.filterIsInstance<FolderTreeItem.FolderItem>().size)
        assertEquals(1, state.treeItems.filterIsInstance<FolderTreeItem.NoteItem>().size)
    }

    @Test
    fun `onSearchChanged updates uiState with filtered items`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        viewModel.onSearchChanged("Note 1")
        
        val state = viewModel.uiState.value
        assertTrue(state.isSearchActive)
        assertEquals(1, state.treeItems.size)
        assertTrue(state.treeItems[0] is FolderTreeItem.NoteItem)
    }

    @Test
    fun `addFolder calls repository insert`() = runTest {
        viewModel.addFolder("New Folder")
        coVerify { folderRepository.insert(any()) }
    }

    @Test
    fun `renameFolder calls repository update`() = runTest {
        val folder = testFolders[0]
        viewModel.renameFolder(folder, "Renamed")
        coVerify { folderRepository.update(match { it.name == "Renamed" && it.id == folder.id }) }
    }

    @Test
    fun `renameNote calls repository save`() = runTest {
        val note = testNotes[0]
        viewModel.renameNote(note, "Renamed Note")
        coVerify { noteRepository.save(match { it.title == "Renamed Note" && it.id == note.id }) }
    }

    @Test
    fun `addNoteToFavorites calls toggleFavorite on repository`() = runTest {
        val note = testNotes[0]
        viewModel.addNoteToFavorites(note)
        coVerify { noteRepository.toggleFavorite(note) }
    }

    @Test
    fun `addFolderToFavorites calls toggleFavorite on repository`() = runTest {
        val folder = testFolders[0]
        viewModel.addFolderToFavorites(folder)
        coVerify { folderRepository.toggleFavorite(folder) }
    }
}
