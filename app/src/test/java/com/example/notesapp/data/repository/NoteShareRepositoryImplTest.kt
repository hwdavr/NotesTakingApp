package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteShareDao
import com.example.notesapp.data.local.NoteShareEntity
import com.example.notesapp.data.remote.CreateNoteShareRequest
import com.example.notesapp.data.remote.NoteShareDto
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.UpdateNoteShareRequest
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NoteShareRepositoryImplTest {

    private lateinit var dao: NoteShareDao
    private lateinit var api: NotesApiService
    private lateinit var repository: NoteShareRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        api = mockk()
        repository = NoteShareRepositoryImpl(dao, api)
    }

    @Test
    fun `observeNoteShares returns mapped domain objects`() = runTest {
        val entity = NoteShareEntity(
            id = "share1",
            noteId = "n1",
            userId = "u1",
            email = "test@test.com",
            displayName = "Test User",
            accessRole = "read_only",
            status = "pending",
            invitedByUserId = "owner",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        every { dao.observeByNoteId("n1") } returns flowOf(listOf(entity))

        val result = repository.observeNoteShares("n1").first()

        assertEquals(1, result.size)
        assertEquals("share1", result[0].id)
        assertEquals(NoteShareAccessRole.VIEWER, result[0].accessRole)
        assertEquals(NoteShareStatus.PENDING, result[0].status)
    }

    @Test
    fun `refreshNoteShares clears and inserts new shares`() = runTest {
        val apiShare = NoteShareDto(
            id = "share1",
            noteId = "n1",
            userId = "u1",
            email = "test@test.com",
            displayName = "Test User",
            accessRole = "read_only",
            status = "pending",
            invitedByUserId = "owner",
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z"
        )
        coEvery { api.listNoteShares("n1") } returns listOf(apiShare)
        coEvery { dao.clearByNoteId("n1") } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        repository.refreshNoteShares("n1")

        coVerify { dao.clearByNoteId("n1") }
        coVerify { dao.insertAll(match { it.size == 1 && it[0].id == "share1" }) }
    }

    @Test
    fun `inviteNoteShare calls api and saves locally`() = runTest {
        val apiShare = NoteShareDto(
            id = "share2",
            noteId = "n1",
            userId = "u2",
            email = "new@test.com",
            displayName = "New User",
            accessRole = "full_access",
            status = "active",
            invitedByUserId = "owner",
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z"
        )
        coEvery { api.createNoteShare("n1", any()) } returns apiShare
        coEvery { dao.insert(any()) } returns Unit

        val result = repository.inviteNoteShare("n1", "new@test.com", NoteShareAccessRole.EDITOR)

        coVerify { api.createNoteShare("n1", CreateNoteShareRequest("new@test.com", "full_access")) }
        coVerify { dao.insert(match { it.id == "share2" }) }
        assertEquals("share2", result.id)
    }

    @Test
    fun `updateNoteShareRole calls api and updates locally`() = runTest {
        val apiShare = NoteShareDto(
            id = "share1",
            noteId = "n1",
            userId = "u1",
            email = "test@test.com",
            displayName = "Test User",
            accessRole = "full_access",
            status = "active",
            invitedByUserId = "owner",
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z"
        )
        coEvery { api.updateNoteShare("n1", "share1", any()) } returns apiShare
        coEvery { dao.insert(any()) } returns Unit

        val result = repository.updateNoteShareRole("n1", "share1", NoteShareAccessRole.EDITOR)

        coVerify { api.updateNoteShare("n1", "share1", UpdateNoteShareRequest("full_access")) }
        coVerify { dao.insert(match { it.id == "share1" && it.accessRole == "full_access" }) }
        assertEquals(NoteShareAccessRole.EDITOR, result.accessRole)
    }

    @Test
    fun `deleteNoteShare calls api and deletes locally`() = runTest {
        coEvery { api.deleteNoteShare("n1", "share1") } returns Unit
        coEvery { dao.deleteById("share1") } returns Unit

        repository.deleteNoteShare("n1", "share1")

        coVerify { api.deleteNoteShare("n1", "share1") }
        coVerify { dao.deleteById("share1") }
    }
}
