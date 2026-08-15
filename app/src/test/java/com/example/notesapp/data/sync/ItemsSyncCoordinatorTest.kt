package com.example.notesapp.data.sync

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.data.remote.ApiItem
import com.example.notesapp.data.remote.MutationResultDto
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.util.DeviceIdProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
        every { noteDao.getActiveNotes() } returns flowOf(emptyList())
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
        every { noteDao.getActiveNotes() } returns flowOf(listOf(localNote))
        coEvery { noteDao.getNoteById("n1") } returns localNote
        coEvery { api.updateNoteContent(any(), any()) } returns MutationResultDto(
            status = "success",
            item = remoteItem.copy(version = 2L, content = "New")
        )

        coordinator.syncAll()

        coVerify { api.updateNoteContent("n1", match { it.content == "New" && it.lastSyncedVersion == 1L }) }
    }

    @Test
    fun `syncAll retains acknowledged local voice document when server list is stale`() = runTest {
        val staleRemoteItem = ApiItem(
            id = "voice-placeholder",
            userId = "u1",
            type = "note",
            parentId = null,
            name = "",
            content = "",
            sortKey = "100",
            version = 1L,
            deviceId = "test-device",
            lastSyncedVersion = 1L,
            deletedAt = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        val voiceDocument = """{"blocks":[{"id":"voice_1","type":"voice"}]}"""
        val acknowledgedLocalNote = NoteEntity(
            id = "voice-placeholder",
            title = "",
            content = voiceDocument,
            sortKey = "100",
            version = 2L,
            deviceId = "test-device",
            lastSyncedVersion = 2L,
            createdAt = 0L,
            updatedAt = 2000L
        )
        val savedNotes = slot<List<NoteEntity>>()
        coEvery { api.listItems(any()) } returns listOf(staleRemoteItem)
        every { noteDao.getActiveNotes() } returns flowOf(listOf(acknowledgedLocalNote))
        coEvery { noteDao.getNoteById("voice-placeholder") } returns acknowledgedLocalNote
        coEvery { api.updateNoteContent(any(), any()) } returns MutationResultDto(
            status = "success",
            item = staleRemoteItem.copy(version = 2L, content = voiceDocument)
        )
        coEvery { noteDao.insertAll(capture(savedNotes)) } returns Unit

        coordinator.syncAll()

        assertEquals(voiceDocument, savedNotes.captured.single().content)
    }

    @Test
    fun `syncAll retains local voice document when server list temporarily omits it`() = runTest {
        val voiceDocument = """{"blocks":[{"id":"voice_1","type":"voice"}]}"""
        val localVoiceNote = NoteEntity(
            id = "voice-placeholder",
            title = "",
            content = voiceDocument,
            sortKey = "100",
            version = 2L,
            deviceId = "test-device",
            lastSyncedVersion = 1L,
            createdAt = 0L,
            updatedAt = 2000L
        )
        val savedNotes = slot<List<NoteEntity>>()
        coEvery { api.listItems(any()) } returns emptyList()
        every { noteDao.getActiveNotes() } returns flowOf(listOf(localVoiceNote))
        coEvery { noteDao.insertAll(capture(savedNotes)) } returns Unit

        coordinator.syncAll()

        assertEquals(listOf(localVoiceNote), savedNotes.captured)
    }

    @Test
    fun `syncAll lets newer remote tombstone replace local voice document`() = runTest {
        val localVoiceNote = NoteEntity(
            id = "voice-placeholder",
            title = "",
            content = "local voice document",
            sortKey = "100",
            version = 2L,
            deviceId = "test-device",
            lastSyncedVersion = 1L,
            createdAt = 0L,
            updatedAt = 2000L
        )
        val remoteTombstone = ApiItem(
            id = "voice-placeholder",
            userId = "u1",
            type = "note",
            parentId = null,
            name = "",
            content = "remote deletion",
            sortKey = "100",
            version = 3L,
            deviceId = "test-device",
            lastSyncedVersion = 2L,
            deletedAt = "2024-01-01T00:00:03Z",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:03Z"
        )
        val savedNotes = slot<List<NoteEntity>>()
        coEvery { api.listItems(any()) } returns listOf(remoteTombstone)
        every { noteDao.getActiveNotes() } returns flowOf(listOf(localVoiceNote))
        coEvery { noteDao.getNoteById("voice-placeholder") } returns localVoiceNote
        coEvery { noteDao.insertAll(capture(savedNotes)) } returns Unit

        coordinator.syncAll()

        assertEquals("remote deletion", savedNotes.captured.single().content)
    }

    @Test
    fun `syncAll retains upload acknowledgement when refreshed server list is stale`() = runTest {
        val staleRemoteItem = ApiItem(
            id = "voice-placeholder",
            userId = "u1",
            type = "note",
            parentId = null,
            name = "",
            content = "",
            sortKey = "100",
            version = 1L,
            deviceId = "test-device",
            lastSyncedVersion = 1L,
            deletedAt = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        val localVoiceNote = NoteEntity(
            id = "voice-placeholder",
            title = "",
            content = "offline voice document",
            sortKey = "100",
            version = 2L,
            deviceId = "test-device",
            lastSyncedVersion = 1L,
            createdAt = 0L,
            updatedAt = 2000L
        )
        val savedNotes = slot<List<NoteEntity>>()
        coEvery { api.listItems(any()) } returns listOf(staleRemoteItem)
        every { noteDao.getActiveNotes() } returns flowOf(listOf(localVoiceNote))
        coEvery { noteDao.getNoteById("voice-placeholder") } returns localVoiceNote
        coEvery { api.updateNoteContent(any(), any()) } returns MutationResultDto(
            status = "success",
            item = staleRemoteItem.copy(version = 2L, content = "offline voice document")
        )
        coEvery { noteDao.insertAll(capture(savedNotes)) } returns Unit

        coordinator.syncAll()

        assertEquals("offline voice document", savedNotes.captured.single().content)
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
