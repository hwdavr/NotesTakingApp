package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.data.remote.ApiItem
import com.example.notesapp.data.remote.MutationResultDto
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.domain.note.Note
import com.example.notesapp.util.DeviceIdProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NoteRepositoryImplTest {

    private lateinit var dao: NoteDao
    private lateinit var api: NotesApiService
    private lateinit var syncCoordinator: ItemsSyncCoordinator
    private lateinit var deviceIdProvider: DeviceIdProvider
    private lateinit var repository: NoteRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        api = mockk()
        syncCoordinator = mockk()
        deviceIdProvider = mockk()
        
        every { deviceIdProvider.deviceId } returns "device1"
        repository = NoteRepositoryImpl(dao, api, syncCoordinator, deviceIdProvider)
    }

    @Test
    fun `getActiveNotes returns mapped domain objects`() = runTest {
        val entity = NoteEntity(
            id = "n1",
            title = "Title",
            content = "Content",
            folderId = "f1",
            sortKey = "100",
            version = 1,
            deviceId = "device1",
            lastSyncedVersion = 0,
            isFavorite = false,
            isShared = false,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )
        every { dao.getActiveNotes() } returns flowOf(listOf(entity))

        val result = repository.getActiveNotes().first()

        assertEquals(1, result.size)
        assertEquals("n1", result[0].id)
    }

    @Test
    fun `save creates new note and syncs if api succeeds`() = runTest {
        val note = Note(
            id = "",
            title = "Title",
            content = "Content",
            folderId = "f1",
            sortKey = "100",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val apiItem = ApiItem(
            id = "n1", userId = "u1", type = "note", parentId = "f1",
            name = "Title", content = "Content", sortKey = "100",
            version = 1, deviceId = "device1", lastSyncedVersion = 1,
            deletedAt = null, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"
        )
        coEvery { dao.getNoteById(any()) } returns null
        coEvery { api.createNote(any()) } returns apiItem
        coEvery { syncCoordinator.syncAll() } returns Unit

        repository.save(note)

        coVerify { api.createNote(match { it.name == "Title" && it.content == "Content" }) }
        coVerify { syncCoordinator.syncAll() }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `save updates existing note and syncs if api succeeds`() = runTest {
        val existingEntity = NoteEntity(
            id = "n1", title = "Old", content = "Old", folderId = "f1", sortKey = "100",
            version = 1, deviceId = "device1", lastSyncedVersion = 1,
            isFavorite = false, isShared = false, createdAt = 1000L, updatedAt = 2000L, deletedAt = null
        )
        val note = Note(id = "n1", title = "New", content = "New", folderId = "f2", sortKey = "100", createdAt = 1000L, updatedAt = 1000L)
        
        val apiItem = ApiItem(
            id = "n1", userId = "u1", type = "note", parentId = "f1",
            name = "New", content = "New", sortKey = "100",
            version = 2, deviceId = "device1", lastSyncedVersion = 2,
            deletedAt = null, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"
        )
        val mutationResult = MutationResultDto(status = "success", item = apiItem)
        coEvery { dao.getNoteById("n1") } returns existingEntity
        coEvery { api.renameItem(any(), any()) } returns mutationResult
        coEvery { api.updateNoteContent(any(), any()) } returns mutationResult
        coEvery { api.moveItem(any(), any()) } returns mutationResult
        coEvery { syncCoordinator.syncAll() } returns Unit

        repository.save(note)

        coVerify { api.renameItem("n1", match { it.name == "New" }) }
        coVerify { api.updateNoteContent("n1", match { it.content == "New" }) }
        coVerify { api.moveItem("n1", match { it.parentId == "f2" }) }
        coVerify { syncCoordinator.syncAll() }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `save falls back to local save if api fails`() = runTest {
        val note = Note(id = "n1", title = "Title", content = "Content", folderId = "f1", sortKey = "100", version = 1, createdAt = 1000L, updatedAt = 1000L)
        
        coEvery { dao.getNoteById("n1") } returns null
        coEvery { api.createNote(any()) } throws IOException("Network error")
        coEvery { dao.insert(any()) } returns Unit

        repository.save(note)

        coVerify { api.createNote(any()) }
        coVerify { dao.insert(match { it.id == "n1" && it.version == 1L }) }
    }

    @Test
    fun `getSharedNotes returns mapped shared notes from dao`() = runTest {
        val sharedEntity = NoteEntity(
            id = "sn1", title = "Shared", content = "Content", folderId = "f1", sortKey = "100",
            version = 1, deviceId = "device2", lastSyncedVersion = 1,
            isFavorite = false, isShared = true, createdAt = 1000L, updatedAt = 2000L, deletedAt = null
        )
        every { dao.getSharedNotes() } returns flowOf(listOf(sharedEntity))

        val result = repository.getSharedNotes().first()

        assertEquals(1, result.size)
        assertEquals("sn1", result[0].id)
        assertEquals(true, result[0].isShared)
    }
}
