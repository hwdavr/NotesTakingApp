package com.example.notesapp.ui.editor

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.data.local.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelIntegrationTest : BaseViewModelIntegrationTest() {

    private lateinit var viewModel: NoteEditorViewModel

    @Test
    fun `test onContentChange triggers auto-save using shared scenario`() = runTest {
        // 1. Prepare initial state
        val initialNote = NoteEntity(
            id = "note_001",
            folderId = "folder_001",
            title = "My Note",
            content = "Initial content",
            sortKey = "b0",
            version = 1,
            deviceId = "test_device",
            lastSyncedVersion = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeNoteDao.insert(initialNote)

        // 2. Load the scenario
        val scenarioFile = File("../sharedContracts/test-scenarios/note_update_content_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        val apiMocks = jsonObject.getJSONArray("apiMocks")

        // 3. Enqueue mocks
        // Request 1: load() sync
        val initialNoteJson = """
            [
              {
                "id": "note_001",
                "userId": "auth0|abc123",
                "type": "note",
                "parentId": "folder_001",
                "name": "My Note",
                "content": "Initial content",
                "sortKey": "b0",
                "version": 1,
                "deviceId": "test_device",
                "lastSyncedVersion": 1,
                "createdAt": "2026-04-26T10:00:00Z",
                "updatedAt": "2026-04-26T10:00:00Z"
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(initialNoteJson))
        
        // Request 2: auto-save patch
        val patchMock = apiMocks.getJSONObject(0)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(patchMock.getInt("status"))
                .setBody(patchMock.getJSONObject("response").toString())
        )
        
        // Request 3: post-save sync
        val syncMock = apiMocks.getJSONObject(1)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(syncMock.getInt("status"))
                .setBody(syncMock.getJSONArray("response").toString())
        )

        // 4. Load the note into the editor
        viewModel = NoteEditorViewModel(noteRepository, folderRepository)
        viewModel.load("note_001")
        advanceUntilIdle()
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        
        // Wait for the load operation to complete
        waitUntil { viewModel.uiState.value.isLoaded }

        assertTrue("ViewModel should be loaded", viewModel.uiState.value.isLoaded)
        assertEquals("note_001", viewModel.uiState.value.noteId)

        // 5. Change content
        val newContent = jsonObject.getJSONObject("expected").getJSONObject("ui").getString("content")
        viewModel.onContentChange(newContent)
        assertEquals(newContent, viewModel.uiState.value.content)

        // 6. Advance time to trigger auto-save (delay is 2000ms)
        advanceTimeBy(3000)
        advanceUntilIdle()
        
        // 7. Wait for patch request
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        
        // Wait for post-save sync request (the Repository calls syncAll after patch)
        waitUntil { mockWebServer.requestCount == 3 }
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        
        // Wait for the DAO to be updated by the sync operation
        waitUntil { fakeNoteDao.getNoteById("note_001")?.content == newContent }

        // 9. Final assertions
        val uiState = viewModel.uiState.value
        assertEquals(newContent, uiState.content)
        
        // Verify DAO was updated
        val noteInDao = fakeNoteDao.getNoteById("note_001")
        assertEquals(newContent, noteInDao?.content)
    }
}
