package com.example.notesapp.ui.notes

import androidx.lifecycle.SavedStateHandle
import com.example.notesapp.MainDispatcherRule
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionNotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)

    private val testFolders = listOf(
        Folder(id = "f1", name = "Folder 1", sortKey = "1", deviceId = "dev", createdAt = 0, updatedAt = 0),
        Folder(id = "f2", name = "Sub Folder", parentFolderId = "f1", sortKey = "2", deviceId = "dev", createdAt = 0, updatedAt = 0)
    )

    private val testNotes = listOf(
        Note(id = "n1", title = "Note 1", content = "Content 1", folderId = "f1", sortKey = "1", deviceId = "dev", createdAt = 0, updatedAt = 0),
        Note(id = "n2", title = "Note 2", content = "Content 2", folderId = "f2", sortKey = "2", deviceId = "dev", createdAt = 0, updatedAt = 0)
    )

    private fun createViewModel(type: String = "all", folderId: String? = null, label: String? = null): CollectionNotesViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            set("type", type)
            set("folderId", folderId)
            set("label", label)
        }
        every { folderRepository.getFolders() } returns flowOf(testFolders)
        every { noteRepository.getActiveNotes() } returns flowOf(testNotes)
        
        return CollectionNotesViewModel(folderRepository, noteRepository, savedStateHandle)
    }

    @Test
    fun `uiState with type all shows all notes`() = runTest {
        val viewModel = createViewModel(type = "all")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.items.filterIsInstance<CollectionItemUiModel.NoteItem>().size)
    }

    @Test
    fun `uiState with type folder shows child folders and notes`() = runTest {
        val viewModel = createViewModel(type = "folder", folderId = "f1")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.items.filterIsInstance<CollectionItemUiModel.FolderItem>().size)
        assertEquals(1, state.items.filterIsInstance<CollectionItemUiModel.NoteItem>().size)
        
        val folderItem = state.items.filterIsInstance<CollectionItemUiModel.FolderItem>().first()
        assertEquals("f2", folderItem.id)
        
        val noteItem = state.items.filterIsInstance<CollectionItemUiModel.NoteItem>().first()
        assertEquals("n1", noteItem.note.id)
    }
}
