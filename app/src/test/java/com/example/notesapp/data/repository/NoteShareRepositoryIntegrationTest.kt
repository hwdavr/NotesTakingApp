package com.example.notesapp.data.repository

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteShareRepositoryIntegrationTest : BaseViewModelIntegrationTest() {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val scenarioAdapter = moshi.adapter(NoteShareScenario::class.java)
    private val anyAdapter = moshi.adapter(Any::class.java)
    @Test
    fun `refreshNoteShares maps api shares into domain`() = runTest {
        val scenario = File("../sharedContracts/test-scenarios/note_shares_list_001.json")
        val parsed = scenarioAdapter.fromJson(scenario.readText())!!
        val mock = parsed.apiMocks.first()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(mock.status)
                .setBody(anyAdapter.toJson(mock.response))
        )
        noteShareRepository.refreshNoteShares("note_001")
        val shares = noteShareRepository.observeNoteShares("note_001").first()
        assertEquals(2, shares.size)
        assertEquals("share_001", shares[0].id)
        assertEquals(NoteShareAccessRole.FULL_ACCESS, shares[0].accessRole)
        assertEquals(NoteShareStatus.ACTIVE, shares[0].status)
        assertEquals("share_002", shares[1].id)
        assertEquals(NoteShareAccessRole.READ_ONLY, shares[1].accessRole)
        assertEquals(NoteShareStatus.PENDING, shares[1].status)
    }
    @Test
    fun `inviteNoteShare stores created share`() = runTest {
        val scenario = File("../sharedContracts/test-scenarios/note_share_invite_001.json")
        val parsed = scenarioAdapter.fromJson(scenario.readText())!!
        val mock = parsed.apiMocks.first()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(mock.status)
                .setBody(anyAdapter.toJson(mock.response))
        )
        val created = noteShareRepository.inviteNoteShare(
            noteId = "note_001",
            email = "invitee@example.com",
            accessRole = NoteShareAccessRole.READ_ONLY
        )
        val shares = noteShareRepository.observeNoteShares("note_001").first()
        assertEquals("share_003", created.id)
        assertEquals(1, shares.size)
        assertEquals("invitee@example.com", shares[0].email)
        assertEquals(NoteShareStatus.PENDING, shares[0].status)
    }
    data class NoteShareScenario(
        val apiMocks: List<ApiMock>
    )
    data class ApiMock(
        val status: Int,
        val response: Any
    )
}
