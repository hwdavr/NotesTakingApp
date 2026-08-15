package com.example.notesapp.data.repository

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.note.Note
import java.io.File
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteRepositoryImplIntegrationTest : BaseViewModelIntegrationTest() {

    @Test
    fun `save keeps acknowledged voice document when shared item list is stale`() = runTest {
        val scenario = JSONObject(
            File("../sharedContracts/test-scenarios/voice_note_stale_sync_001.json").readText()
        )
        val apiMocks = scenario.getJSONArray("apiMocks")
        repeat(apiMocks.length()) { index ->
            val apiMock = apiMocks.getJSONObject(index)
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(apiMock.getInt("status"))
                    .setBody(apiMockResponse(apiMock))
            )
        }
        val expectedDomain = scenario.getJSONObject("expected").getJSONObject("domain")
        val noteId = expectedDomain.getString("noteId")
        val expectedContent = expectedDomain.getString("content")
        fakeNoteDao.insert(
            NoteEntity(
                id = noteId,
                title = "",
                content = "",
                sortKey = "voice-sort",
                version = 1L,
                deviceId = "test_device",
                lastSyncedVersion = 1L,
                createdAt = 1L,
                updatedAt = 1L
            )
        )

        noteRepository.save(
            Note(
                id = noteId,
                title = "",
                content = expectedContent,
                sortKey = "voice-sort",
                createdAt = 1L,
                updatedAt = 2L
            )
        )

        assertEquals(expectedContent, fakeNoteDao.getNoteById(noteId)?.content)
        assertEquals(apiMocks.length(), mockWebServer.requestCount)
        repeat(apiMocks.length()) { index ->
            val expectedApiMock = apiMocks.getJSONObject(index)
            val request = mockWebServer.takeRequest()
            assertEquals(expectedApiMock.getString("method"), request.method)
            assertEquals(expectedApiMock.getString("path"), request.path?.substringBefore("?"))
        }
    }

    private fun apiMockResponse(apiMock: JSONObject): String = when (val response = apiMock.get("response")) {
        is JSONArray -> response.toString()
        is JSONObject -> response.toString()
        else -> error("Unsupported API mock response")
    }
}
