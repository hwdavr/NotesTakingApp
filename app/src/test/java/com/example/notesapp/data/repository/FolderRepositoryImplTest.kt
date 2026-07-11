package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.remote.ApiItem
import com.example.notesapp.data.remote.MutationResultDto
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.util.DeviceIdProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FolderRepositoryImplTest {

    private lateinit var dao: FolderDao
    private lateinit var api: NotesApiService
    private lateinit var syncCoordinator: ItemsSyncCoordinator
    private lateinit var deviceIdProvider: DeviceIdProvider
    private lateinit var repository: FolderRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        api = mockk()
        syncCoordinator = mockk()
        deviceIdProvider = mockk()

        every { deviceIdProvider.deviceId } returns "device1"
        repository = FolderRepositoryImpl(dao, api, syncCoordinator, deviceIdProvider)
    }

    @Test
    fun `getFolders returns mapped domain objects`() = runTest {
        val entity = FolderEntity(
            id = "f1",
            name = "Folder",
            parentFolderId = "p1",
            sortKey = "100",
            version = 1,
            deviceId = "device1",
            lastSyncedVersion = 0,
            isFavorite = false,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )
        every { dao.getFolders() } returns flowOf(listOf(entity))

        val result = repository.getFolders().first()

        assertEquals(1, result.size)
        assertEquals("f1", result[0].id)
    }

    @Test
    fun `insert syncs if api succeeds`() = runTest {
        val folder = Folder(
            id = "f1",
            name = "Folder",
            parentFolderId = "p1",
            sortKey = "100",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val apiItem = ApiItem(
            id = "f1", userId = "u1", type = "folder", parentId = "p1",
            name = "Folder", content = "", sortKey = "100",
            version = 1, deviceId = "device1", lastSyncedVersion = 1,
            deletedAt = null, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"
        )
        coEvery { api.createFolder(any()) } returns apiItem
        coEvery { syncCoordinator.syncAll() } returns Unit

        repository.insert(folder)

        coVerify { api.createFolder(match { it.id == "f1" && it.name == "Folder" }) }
        coVerify { syncCoordinator.syncAll() }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `insert saves locally if api fails`() = runTest {
        val folder = Folder(
            id = "f1",
            name = "Folder",
            parentFolderId = "p1",
            sortKey = "100",
            version = 1,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        coEvery { api.createFolder(any()) } throws IOException("Network error")
        coEvery { dao.insert(any()) } returns Unit

        repository.insert(folder)

        coVerify { api.createFolder(match { it.id == "f1" }) }
        coVerify(exactly = 0) { syncCoordinator.syncAll() }
        coVerify { dao.insert(match { it.id == "f1" && it.version == 2L }) }
    }

    @Test
    fun `update syncs if api succeeds`() = runTest {
        val folder = Folder(id = "f1", name = "New Name", createdAt = 1000L, updatedAt = 1000L)
        val apiItem = ApiItem(
            id = "f1", userId = "u1", type = "folder", parentId = null,
            name = "New Name", content = "", sortKey = "100",
            version = 1, deviceId = "device1", lastSyncedVersion = 1,
            deletedAt = null, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"
        )
        val mutationResult = MutationResultDto(status = "success", item = apiItem)
        coEvery { api.renameItem("f1", any()) } returns mutationResult
        coEvery { syncCoordinator.syncAll() } returns Unit

        repository.update(folder)

        coVerify { api.renameItem("f1", match { it.name == "New Name" }) }
        coVerify { syncCoordinator.syncAll() }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `update saves locally if api fails`() = runTest {
        val folder = Folder(id = "f1", name = "New Name", version = 2, createdAt = 1000L, updatedAt = 1000L)
        coEvery { api.renameItem("f1", any()) } throws IOException("Network error")
        coEvery { dao.insert(any()) } returns Unit

        repository.update(folder)

        coVerify { dao.insert(match { it.id == "f1" && it.version == 3L && it.name == "New Name" }) }
    }

    @Test
    fun `updateDescription syncs if api succeeds`() = runTest {
        val folder = Folder(
            id = "f1",
            name = "Folder",
            description = "",
            version = 2,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val apiItem = ApiItem(
            id = "f1", userId = "u1", type = "folder", parentId = null,
            name = "Folder", content = "Client receipts", sortKey = "100",
            version = 3, deviceId = "device1", lastSyncedVersion = 2,
            deletedAt = null, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"
        )
        val mutationResult = MutationResultDto(status = "success", item = apiItem)
        coEvery { api.updateItemContent("f1", any()) } returns mutationResult
        coEvery { syncCoordinator.syncAll() } returns Unit

        repository.updateDescription(folder, "Client receipts")

        coVerify {
            api.updateItemContent(
                "f1",
                match {
                    it.content == "Client receipts" &&
                        it.deviceId == "device1" &&
                        it.lastSyncedVersion == 2L
                }
            )
        }
        coVerify { syncCoordinator.syncAll() }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `updateDescription saves locally if api fails`() = runTest {
        val folder = Folder(
            id = "f1",
            name = "Folder",
            description = "",
            version = 2,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        coEvery { api.updateItemContent("f1", any()) } throws IOException("Network error")
        coEvery { dao.insert(any()) } returns Unit

        repository.updateDescription(folder, "Client receipts")

        coVerify {
            dao.insert(
                match {
                    it.id == "f1" &&
                        it.description == "Client receipts" &&
                        it.version == 3L &&
                        it.deviceId == "device1" &&
                        it.lastSyncedVersion == 2L
                }
            )
        }
    }
}
