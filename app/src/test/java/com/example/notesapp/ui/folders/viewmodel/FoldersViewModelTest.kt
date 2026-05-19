package com.example.notesapp.ui.folders.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    private val folder1 = Folder(id = "f1", name = "Work", createdAt = 1000L, updatedAt = 1000L)
    private val folder2 =
        Folder(id = "f2", name = "Personal", parentFolderId = "f1", createdAt = 1000L, updatedAt = 1000L)
    private val note1 =
        Note(id = "n1", title = "Note 1", content = "Content", folderId = "f1", createdAt = 1000L, updatedAt = 1000L)

    private val foldersFlow = MutableStateFlow(listOf(folder1, folder2))
    private val notesFlow = MutableStateFlow(listOf(note1))
    private val sharedNotesFlow = MutableStateFlow<List<Note>>(emptyList())

    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns foldersFlow
        every { noteRepository.getActiveNotes() } returns notesFlow
        coEvery { noteRepository.getActiveNoteCount() } returns 1
        coEvery { noteRepository.getFavoriteNoteCount() } returns 0
        coEvery { folderRepository.getArchivedFolderCount() } returns 0
        coEvery { noteRepository.getArchivedNoteCount() } returns 0
        coEvery { noteRepository.getActiveNoteCountForFolder(any()) } returns 0
        every { noteRepository.getSharedNotes() } returns sharedNotesFlow

        viewModel = FoldersViewModel(folderRepository, noteRepository)
    }

    @Test
    fun `uiState contains correct tree structure`() = runTest {
        viewModel.onSearchChanged("") // Trigger emission if needed
        val state = viewModel.uiState.first { it.treeItems.isNotEmpty() }
        val items = state.treeItems
        // Root: f1
        // Children of f1: n1 (note), f2 (folder)
        assertEquals(3, items.size)

        val f1Item = items[0] as FolderTreeItem.FolderItem
        assertEquals("f1", f1Item.folder.id)
        val n1Item = items[1] as FolderTreeItem.NoteItem
        assertEquals("n1", n1Item.note.id)
        assertEquals(1, n1Item.depth)

        val f2Item = items[2] as FolderTreeItem.FolderItem
        assertEquals("f2", f2Item.folder.id)
        assertEquals(1, f2Item.depth)
    }

    @Test
    fun `onSearchChanged filters items`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        viewModel.onSearchChanged("Personal")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(1, state.treeItems.size)
        val item = state.treeItems[0] as FolderTreeItem.FolderItem
        assertEquals("Personal", item.folder.name)
    }

    @Test
    fun `addFolder calls repository`() = runTest {
        viewModel.addFolder("New Folder", "f1")
        advanceUntilIdle()
        coVerify { folderRepository.insert(match { it.name == "New Folder" && it.parentFolderId == "f1" }) }
    }

    @Test
    fun `renameFolder calls repository`() = runTest {
        viewModel.renameFolder(folder1, "Updated Work")
        advanceUntilIdle()
        coVerify { folderRepository.update(match { it.id == "f1" && it.name == "Updated Work" }) }
    }

    @Test
    fun `deleteFolder archives folder and its descendants`() = runTest {
        // f1 contains f2 and n1
        viewModel.deleteFolder(folder1)
        advanceUntilIdle()

        coVerify { noteRepository.delete(match { it.id == "n1" }) }
        coVerify { folderRepository.delete(match { it.id == "f2" }) }
        coVerify { folderRepository.delete(match { it.id == "f1" }) }
    }

    @Test
    fun `toggleFavorite for note calls repository`() = runTest {
        viewModel.addNoteToFavorites(note1)
        advanceUntilIdle()
        coVerify { noteRepository.toggleFavorite(note1) }
    }

    @Test
    fun `uiState contains shared items when provided`() = runTest {
        val sharedNote =
            Note(
                id = "sn1",
                title = "Shared Note",
                content = "Shared Content",
                isShared = true,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        sharedNotesFlow.value = listOf(sharedNote)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.sharedTreeItems.size)
        val item = state.sharedTreeItems[0] as FolderTreeItem.NoteItem
        assertEquals("sn1", item.note.id)
        assertTrue(item.note.isShared)
    }

    @Test
    fun `search filters both owned and shared notes`() = runTest {
        val sharedNote =
            Note(
                id = "sn1",
                title = "Shared Note",
                content = "Shared Content",
                isShared = true,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        sharedNotesFlow.value = listOf(sharedNote)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchChanged("Shared")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Note: In search mode, treeItems should contain everything matching
        assertEquals(1, state.treeItems.size)
        val item = state.treeItems[0] as FolderTreeItem.NoteItem
        assertEquals("sn1", item.note.id)
    }
}
