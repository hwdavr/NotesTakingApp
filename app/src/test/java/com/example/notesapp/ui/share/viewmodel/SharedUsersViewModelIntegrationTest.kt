package com.example.notesapp.ui.share.viewmodel

import com.example.notesapp.auth.AuthManager
import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.ui.share.model.AccessRole
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedUsersViewModelIntegrationTest : BaseViewModelIntegrationTest() {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val scenarioAdapter = moshi.adapter(NoteShareScenario::class.java)
    private val anyAdapter = moshi.adapter(Any::class.java)

    @Test
    fun `load adds local owner row on top of api collaborators`() = runTest {
        fakeNoteDao.insert(
            NoteEntity(
                id = "note_001",
                title = "Force update strategy",
                content = "[]",
                folderId = null,
                sortKey = "1",
                version = 1,
                deviceId = "test_device",
                lastSyncedVersion = 1,
                createdAt = 1L,
                updatedAt = 1L
            )
        )
        val scenario = File("../sharedContracts/test-scenarios/note_shares_list_001.json")
        val parsed = scenarioAdapter.fromJson(scenario.readText())!!
        val mock = parsed.apiMocks.first()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(mock.status)
                .setBody(anyAdapter.toJson(mock.response))
        )
        val authManager = mockk<AuthManager>()
        every { authManager.profileEmail } returns MutableStateFlow("owner@example.com")
        val viewModel = SharedUsersViewModel(noteRepository, noteShareRepository, authManager)
        viewModel.load("note_001")
        waitUntil {
            !viewModel.uiState.value.isLoading && viewModel.uiState.value.users.size == 3
        }
        val state = viewModel.uiState.value
        assertEquals("Force update strategy", state.noteTitle)
        assertEquals(AccessRole.OWNER, state.users.first().role)
        assertEquals("owner@example.com", state.users.first().email)
        assertNull(state.errorMessageRes)
    }
    data class NoteShareScenario(
        val apiMocks: List<ApiMock>
    )
    data class ApiMock(
        val status: Int,
        val response: Any
    )
}
