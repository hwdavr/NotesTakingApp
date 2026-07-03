package com.example.notesapp.data.sync

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.data.remote.ApiItem
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.util.DeviceIdProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsSyncCoordinatorTest {
    private val api: NotesApiService = mockk()
    private val folderDao: FolderDao = mockk(relaxed = true)
    private val noteDao: NoteDao = mockk(relaxed = true)
    private val deviceIdProvider: DeviceIdProvider = mockk()
    private lateinit var coordinator: ItemsSyncCoordinator

    @Before
    fun setup() {
        every { deviceIdProvider.deviceId } returns "test-device"
        coordinator = ItemsSyncCoordinator(api, folderDao, noteDao, deviceIdProvider)
    }

    @Test
    fun `syncAll pulls and saves items`() = runTest {
        val apiItems = listOf(
            ApiItem(
                id = "n1",
                userId = "u1",
                type = "note",
                parentId = "f1",
                name = "Title",
                content = "Content",
                sortKey = "s1",
                version = 1L,
                deviceId = "d1",
                lastSyncedVersion = 0L,
                deletedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            ApiItem(
                id = "f1",
                userId = "u1",
                type = "folder",
                parentId = null,
                name = "Work",
                content = "",
                sortKey = "s2",
                version = 1L,
                deviceId = "d1",
                lastSyncedVersion = 0L,
                deletedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
        )
        coEvery { api.listItems(any()) } returns apiItems
        coEvery { noteDao.getNoteById("n1") } returns null

        coordinator.syncAll()

        coVerify { folderDao.clearAll() }
        coVerify { noteDao.clearAll() }
        coVerify { folderDao.insertAll(any()) }
        coVerify { noteDao.insertAll(any()) }
    }

    @Test
    fun `syncAll pushes local updates if version is higher`() = runTest {
        val remoteItem =
            ApiItem(
                id = "n1",
                userId = "u1",
                type = "note",
                parentId = "f1",
                name = "Old",
                content = "Old",
                sortKey = "s1",
                version = 1L,
                deviceId = "d1",
                lastSyncedVersion = 0L,
                deletedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
        val localNote = NoteEntity(
            id = "n1",
            title = "New",
            content = "New",
            sortKey = "sort",
            version = 2L,
            deviceId = "test-device",
            lastSyncedVersion = 1L,
            createdAt = 0L,
            updatedAt = 0L
        )

        coEvery {
            api.listItems(any())
        } returns listOf(remoteItem) andThen listOf(remoteItem.copy(version = 2, content = "New"))
        coEvery { noteDao.getNoteById("n1") } returns localNote
        coEvery { api.updateNoteContent(any(), any()) } returns mockk()

        coordinator.syncAll()

        coVerify { api.updateNoteContent("n1", match { it.content == "New" && it.lastSyncedVersion == 1L }) }
    }

    @Test
    fun `syncAll serializes concurrent calls`() = runTest {
        val apiItems = listOf(
            ApiItem(
                id = "n1",
                userId = "u1",
                type = "note",
                parentId = "f1",
                name = "Title",
                content = "Content",
                sortKey = "s1",
                version = 1L,
                deviceId = "d1",
                lastSyncedVersion = 0L,
                deletedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            ApiItem(
                id = "f1",
                userId = "u1",
                type = "folder",
                parentId = null,
                name = "Work",
                content = "",
                sortKey = "s2",
                version = 1L,
                deviceId = "d1",
                lastSyncedVersion = 0L,
                deletedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
        )
        val gate = CompletableDeferred<Unit>()
        var activeCalls = 0
        var maxActiveCalls = 0
        coEvery { api.listItems(any()) } coAnswers {
            activeCalls++
            maxActiveCalls = maxOf(maxActiveCalls, activeCalls)
            gate.await()
            activeCalls--
            apiItems
        }
        coEvery { noteDao.getNoteById(any()) } returns null

        val job1 = launch { coordinator.syncAll() }
        val job2 = launch { coordinator.syncAll() }

        advanceUntilIdle()
        assertEquals(1, maxActiveCalls)

        gate.complete(Unit)
        job1.join()
        job2.join()

        coVerify(exactly = 2) { api.listItems(any()) }
        coVerify(exactly = 2) { folderDao.clearAll() }
        coVerify(exactly = 2) { noteDao.clearAll() }
        coVerify(exactly = 2) { folderDao.insertAll(any()) }
        coVerify(exactly = 2) { noteDao.insertAll(any()) }
    }
}
