package com.example.notesapp.ui.home.viewmodel

import com.example.notesapp.base.BaseViewModelIntegrationTest
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
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelIntegrationTest : BaseViewModelIntegrationTest() {
    private lateinit var viewModel: HomeViewModel

    @Test
    fun `test shared pill appears and filters notes using shared scenario`() = runTest {
        val scenarioFile = File("../sharedContracts/test-scenarios/home_shared_pill_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        val apiMocks = jsonObject.getJSONArray("apiMocks")
        val firstMock = apiMocks.getJSONObject(0)

        repeat(2) {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(firstMock.getInt("status"))
                    .setBody(firstMock.getJSONArray("response").toString())
            )
        }

        viewModel = HomeViewModel(noteRepository, folderRepository)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        advanceUntilIdle()
        mockWebServer.takeRequest(5, TimeUnit.SECONDS) // init sync

        val expectedUi = jsonObject.getJSONObject("expected").getJSONObject("ui")

        // 1. Verify Shared pill is present
        waitUntil { viewModel.uiState.value.recentFolders.any { it.name == "Shared" } }
        assertTrue("Shared pill should be present", viewModel.uiState.value.recentFolders.any { it.name == "Shared" })

        // 2. Verify initial notes (should be only owned ones by default)
        waitUntil { viewModel.uiState.value.recentNotes.size == expectedUi.getInt("initialNoteCount") }
        assertEquals(expectedUi.getInt("initialNoteCount"), viewModel.uiState.value.recentNotes.size)
        assertEquals("My Note", viewModel.uiState.value.recentNotes[0].title)

        // 3. Select Shared pill
        val sharedFolder = viewModel.uiState.value.recentFolders.first { it.name == "Shared" }
        viewModel.selectFolder(sharedFolder.id)

        // 4. Verify filtered notes (should be only shared ones)
        waitUntil { viewModel.uiState.value.recentNotes.any { it.isShared } }
        assertEquals(expectedUi.getInt("filteredNoteCount"), viewModel.uiState.value.recentNotes.size)
        assertEquals("Shared Note", viewModel.uiState.value.recentNotes[0].title)
        assertTrue(viewModel.uiState.value.recentNotes[0].isShared)

        collectJob.cancel()
    }
}
