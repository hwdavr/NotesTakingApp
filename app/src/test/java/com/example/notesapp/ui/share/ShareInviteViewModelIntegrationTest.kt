package com.example.notesapp.ui.share

import com.example.notesapp.R
import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareInviteViewModelIntegrationTest : BaseViewModelIntegrationTest() {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val scenarioAdapter = moshi.adapter(NoteShareScenario::class.java)
    private val anyAdapter = moshi.adapter(Any::class.java)

    @Test
    fun `invite success emits completion event`() = runTest {
        val scenario = File("../sharedContracts/test-scenarios/note_share_invite_001.json")
        val parsed = scenarioAdapter.fromJson(scenario.readText())!!
        val mock = parsed.apiMocks.first()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(mock.status)
                .setBody(anyAdapter.toJson(mock.response))
        )

        val viewModel = ShareInviteViewModel(noteShareRepository)
        viewModel.load("note_001")
        viewModel.onEmailChange("invitee@example.com")

        val eventDeferred = async { viewModel.events.first() }
        viewModel.invite()

        waitUntil { !viewModel.uiState.value.isSubmitting }

        assertTrue(eventDeferred.await() is ShareInviteEvent.InviteSucceeded)
        assertEquals(null, viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun `duplicate invite exposes conflict error`() = runTest {
        val scenario = File("../sharedContracts/test-scenarios/note_share_duplicate_invite_001.json")
        val parsed = scenarioAdapter.fromJson(scenario.readText())!!
        val mock = parsed.apiMocks.first()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(mock.status)
                .setBody(anyAdapter.toJson(mock.response))
        )

        val viewModel = ShareInviteViewModel(noteShareRepository)
        viewModel.load("note_001")
        viewModel.onEmailChange("invitee@example.com")
        assertTrue(viewModel.uiState.value.isInviteEnabled)

        viewModel.invite()

        waitUntil { viewModel.uiState.value.errorMessageRes != null }

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals(R.string.share_invite_duplicate_error, viewModel.uiState.value.errorMessageRes)
    }

    data class NoteShareScenario(
        val apiMocks: List<ApiMock>
    )

    data class ApiMock(
        val status: Int,
        val response: Any
    )
}
