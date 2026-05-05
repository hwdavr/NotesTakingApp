package com.example.notesapp.ui.folders

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.domain.folder.Folder
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoldersViewModelIntegrationTest : BaseViewModelIntegrationTest() {

    private lateinit var viewModel: FoldersViewModel

    @Test
    fun `test add folder and sync updates UI state using shared scenario`() = runTest {
        val scenarioFile = File("../sharedContracts/test-scenarios/folder_add_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())

        val apiMocks = jsonObject.getJSONArray("apiMocks")

        // In FoldersViewModel init, folderRepository.sync() is called -> hits /v1/items
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel = FoldersViewModel(folderRepository, noteRepository)

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        advanceUntilIdle() // let init sync finish

        // Now enqueue the mock responses for the addFolder action
        // 1. POST /v1/folders
        val firstMock = apiMocks.getJSONObject(0)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(firstMock.getInt("status"))
                .setBody(firstMock.getJSONObject("response").toString())
        )
        // 2. GET /v1/items (called by syncCoordinator after successful insert)
        val secondMock = apiMocks.getJSONObject(1)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(secondMock.getInt("status"))
                .setBody(secondMock.getJSONArray("response").toString())
        )

        // Perform the action
        viewModel.addFolder("Work")

        // Let the coroutine start and make the first network request
        advanceUntilIdle()

        // Wait for network requests to be processed
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // init sync
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // add folder

        // Let the coroutine proceed to syncAll()
        advanceUntilIdle()

        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // sync request

        // Process final response
        advanceUntilIdle()

        // Give coroutines some time to update flows using base utility
        waitUntil { viewModel.uiState.value.treeItems.isNotEmpty() }

        val uiState = viewModel.uiState.value
        val expectedUi = jsonObject.getJSONObject("expected").getJSONObject("ui")
        val expectedItemCount = expectedUi.getInt("itemCount")
        val expectedFirstItemName = expectedUi.getJSONArray("items").getJSONObject(0).getString("name")

        assertEquals(expectedItemCount, uiState.treeItems.size)

        val firstTreeItem = uiState.treeItems[0] as FolderTreeItem.FolderItem
        assertEquals(expectedFirstItemName, firstTreeItem.folder.name)

        collectJob.cancel()
    }

    @Test
    fun `test rename folder updates UI state`() = runTest {
        val scenarioFile = File("../sharedContracts/test-scenarios/folder_rename_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        val apiMocks = jsonObject.getJSONArray("apiMocks")

        // Initial sync (empty)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel = FoldersViewModel(folderRepository, noteRepository)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // init sync

        val folderId = "folder_1"
        val initialFolder = Folder(id = folderId, name = "Old Name", createdAt = 0, updatedAt = 0)

        // Enqueue for insert (not part of the rename scenario itself, but needed for setup)
        mockWebServer.enqueue(
            MockResponse().setResponseCode(
                200
            ).setBody("""{"id":"$folderId","name":"Old Name","type":"folder","version":1}""")
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]")) // sync after insert

        folderRepository.insert(initialFolder)
        advanceUntilIdle()
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // insert
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // sync

        // Now the actual rename test using scenario
        // 1. Rename PATCH
        val firstMock = apiMocks.getJSONObject(0)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(firstMock.getInt("status"))
                .setBody(firstMock.getJSONObject("response").toString())
        )
        // 2. Follow-up sync
        val secondMock = apiMocks.getJSONObject(1)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(secondMock.getInt("status"))
                .setBody(secondMock.getJSONArray("response").toString())
        )

        viewModel.renameFolder(initialFolder.copy(version = 1), "New Name")
        advanceUntilIdle()

        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // rename PATCH
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // follow-up sync

        waitUntil {
            val item = viewModel.uiState.value.treeItems.firstOrNull() as? FolderTreeItem.FolderItem
            item?.folder?.name == "New Name"
        }

        val uiState = viewModel.uiState.value
        val expectedUi = jsonObject.getJSONObject("expected").getJSONObject("ui")
        val expectedFirstItemName = expectedUi.getJSONArray("items").getJSONObject(0).getString("name")

        val firstTreeItem = uiState.treeItems[0] as FolderTreeItem.FolderItem
        assertEquals(expectedFirstItemName, firstTreeItem.folder.name)

        collectJob.cancel()
    }

    @Test
    fun `test delete folder updates UI state`() = runTest {
        val scenarioFile = File("../sharedContracts/test-scenarios/folder_delete_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        val apiMocks = jsonObject.getJSONArray("apiMocks")

        // Initial sync (empty)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel = FoldersViewModel(folderRepository, noteRepository)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // init sync

        // 1. Setup: Add folder f1
        val folderId = "f1"
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """
            {
                "id": "$folderId", "userId": "u1", "type": "folder", "parentId": null, "name": "Delete Me",
                "content": "", "sortKey": "a0", "version": 1, "deviceId": "dev", "lastSyncedVersion": 1,
                "createdAt": "2026-04-26T10:00:00Z", "updatedAt": "2026-04-26T10:00:00Z"
            }
                """.trimIndent()
            )
        )
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
            [{
                "id": "$folderId", "userId": "u1", "type": "folder", "parentId": null, "name": "Delete Me",
                "content": "", "sortKey": "a0", "version": 1, "deviceId": "dev", "lastSyncedVersion": 1,
                "createdAt": "2026-04-26T10:00:00Z", "updatedAt": "2026-04-26T10:00:00Z"
            }]
                """.trimIndent()
            )
        )

        viewModel.addFolder("Delete Me")
        advanceUntilIdle()

        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // create
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // sync

        waitUntil { viewModel.uiState.value.treeItems.isNotEmpty() }
        val folder = (viewModel.uiState.value.treeItems[0] as FolderTreeItem.FolderItem).folder

        // 2. Action: Delete folder using scenario mocks
        val firstMock = apiMocks.getJSONObject(0)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(firstMock.getInt("status"))
                .setBody(firstMock.getJSONObject("response").toString())
        )
        val secondMock = apiMocks.getJSONObject(1)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(secondMock.getInt("status"))
                .setBody(secondMock.getJSONArray("response").toString())
        )

        viewModel.deleteFolder(folder)
        advanceUntilIdle()

        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // delete
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // sync

        waitUntil { viewModel.uiState.value.treeItems.isEmpty() }
        assertEquals(0, viewModel.uiState.value.treeItems.size)

        collectJob.cancel()
    }
}
