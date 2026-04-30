package com.example.notesapp.ui.folders

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.ui.folders.FolderTreeItem
import com.example.notesapp.ui.folders.FoldersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

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
        
        val collectJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
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
        
        collectJob.cancel()
        
        val firstTreeItem = uiState.treeItems[0] as FolderTreeItem.FolderItem
        assertEquals(expectedFirstItemName, firstTreeItem.folder.name)
    }
}
