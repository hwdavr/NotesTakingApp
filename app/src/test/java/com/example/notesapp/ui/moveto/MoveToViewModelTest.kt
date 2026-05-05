package com.example.notesapp.ui.moveto

import androidx.lifecycle.SavedStateHandle
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoveToViewModelTest : BaseViewModelTest() {

    @Test
    fun `search filters folder destinations`() = runTest {
        val viewModel = createViewModel(
            itemType = MoveToViewModel.ITEM_TYPE_NOTE,
            itemId = "note_1",
            folders = listOf(
                folder("folder_1", "Work"),
                folder("folder_2", "Personal")
            ),
            notes = listOf(note("note_1", "Draft", folderId = null))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onSearchChanged("work")

        val state = viewModel.uiState.value
        assertEquals("work", state.searchQuery)
        assertEquals(listOf("Work"), state.folderResults.map { it.name })
    }

    @Test
    fun `folder move hides itself and descendants`() = runTest {
        val viewModel = createViewModel(
            itemType = MoveToViewModel.ITEM_TYPE_FOLDER,
            itemId = "folder_1",
            folders = listOf(
                folder("folder_1", "Work"),
                folder("folder_2", "Child", parentFolderId = "folder_1"),
                folder("folder_3", "Other")
            ),
            notes = emptyList()
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val names = viewModel.uiState.value.folderResults.map { it.name }
        assertFalse("Work" in names)
        assertFalse("Child" in names)
        assertTrue("Other" in names)
    }

    @Test
    fun `move folder calls folder repository move`() = runTest {
        val folderRepository = FakeMoveFolderRepository(
            listOf(folder("folder_1", "Work"), folder("folder_2", "Personal"))
        )
        val viewModel = createViewModel(
            itemType = MoveToViewModel.ITEM_TYPE_FOLDER,
            itemId = "folder_1",
            folderRepository = folderRepository,
            notes = emptyList()
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.moveTo("folder_2") {}

        assertEquals("folder_1" to "folder_2", folderRepository.movedFolder)
    }

    @Test
    fun `move note calls note repository move`() = runTest {
        val noteRepository = FakeMoveNoteRepository(
            listOf(note("note_1", "Draft", folderId = null))
        )
        val viewModel = createViewModel(
            itemType = MoveToViewModel.ITEM_TYPE_NOTE,
            itemId = "note_1",
            folders = listOf(folder("folder_1", "Work")),
            noteRepository = noteRepository
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.moveTo("folder_1") {}

        assertEquals("note_1" to "folder_1", noteRepository.movedNote)
    }

    private fun createViewModel(
        itemType: String,
        itemId: String,
        folders: List<Folder> = emptyList(),
        notes: List<Note> = emptyList(),
        folderRepository: FakeMoveFolderRepository = FakeMoveFolderRepository(folders),
        noteRepository: FakeMoveNoteRepository = FakeMoveNoteRepository(notes)
    ): MoveToViewModel = MoveToViewModel(
        folderRepository = folderRepository,
        noteRepository = noteRepository,
        savedStateHandle = SavedStateHandle(
            mapOf(
                "itemType" to itemType,
                "itemId" to itemId
            )
        )
    )
}

private class FakeMoveFolderRepository(
    initialFolders: List<Folder>
) : FolderRepository {
    private val folders = MutableStateFlow(initialFolders)
    var movedFolder: Pair<String, String?>? = null

    override fun getFolders(): Flow<List<Folder>> = folders
    override fun getArchivedFolders(): Flow<List<Folder>> = flowOf(emptyList())
    override suspend fun getArchivedFolderCount(): Int = 0

    override suspend fun insert(folder: Folder) = Unit

    override suspend fun update(folder: Folder) = Unit

    override suspend fun move(folder: Folder, parentFolderId: String?) {
        movedFolder = folder.id to parentFolderId
    }

    override suspend fun delete(folder: Folder) = Unit
    override suspend fun toggleFavorite(folder: Folder) = Unit
    override suspend fun sync() = Unit
}

private class FakeMoveNoteRepository(
    initialNotes: List<Note>
) : NoteRepository {
    private val notes = MutableStateFlow(initialNotes)
    var movedNote: Pair<String, String?>? = null

    override fun getActiveNotes(): Flow<List<Note>> = notes
    override fun getArchivedNotes(): Flow<List<Note>> = flowOf(emptyList())

    override suspend fun getNoteById(id: String): Note? = notes.value.firstOrNull { it.id == id }

    override suspend fun getActiveNoteCount(): Int = notes.value.size

    override suspend fun getActiveNoteCountForFolder(folderId: String): Int =
        notes.value.count { it.folderId == folderId }

    override suspend fun getFavoriteNoteCount(): Int = notes.value.count { it.isFavorite }
    override suspend fun getArchivedNoteCount(): Int = 0

    override suspend fun save(note: Note) = Unit

    override suspend fun move(note: Note, folderId: String?) {
        movedNote = note.id to folderId
    }

    override suspend fun delete(note: Note) = Unit
    override suspend fun toggleFavorite(note: Note) = Unit
    override suspend fun sync() = Unit
}

private fun folder(id: String, name: String, parentFolderId: String? = null): Folder = Folder(
    id = id,
    name = name,
    parentFolderId = parentFolderId,
    createdAt = 0,
    updatedAt = 0
)

private fun note(id: String, title: String, folderId: String?): Note = Note(
    id = id,
    title = title,
    content = "",
    folderId = folderId,
    createdAt = 0,
    updatedAt = 0
)
