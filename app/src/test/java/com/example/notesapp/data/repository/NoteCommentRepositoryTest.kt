package com.example.notesapp.data.repository

import com.example.notesapp.auth.AuthManager
import com.example.notesapp.data.local.NoteBlockCommentDao
import com.example.notesapp.data.local.NoteBlockCommentEntity
import com.example.notesapp.data.remote.ApiNoteBlockComment
import com.example.notesapp.data.remote.CreateNoteBlockCommentRequest
import com.example.notesapp.data.remote.NotesApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteCommentRepositoryTest {

    private lateinit var dao: NoteBlockCommentDao
    private lateinit var api: NotesApiService
    private lateinit var authManager: AuthManager
    private lateinit var repository: NoteCommentRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        api = mockk()
        authManager = mockk()
        repository = NoteCommentRepositoryImpl(dao, api, authManager)
    }

    @Test
    fun `observeComments returns mapped domain comments from flow`() = runTest {
        val entity = NoteBlockCommentEntity(
            id = "c1",
            noteId = "n1",
            blockId = "b1",
            authorUserId = "u1",
            authorDisplayName = "Hannah",
            authorEmail = "hannah@test.com",
            body = "Hello world",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        every { dao.observeComments("n1", "b1") } returns flowOf(listOf(entity))

        val result = repository.observeComments("n1", "b1").first()

        assertEquals(1, result.size)
        assertEquals("c1", result[0].id)
        assertEquals("Hello world", result[0].body)
    }

    @Test
    fun `refreshComments fetches from api, clears local cache, and inserts new entities`() = runTest {
        val apiComment = ApiNoteBlockComment(
            id = "c1",
            noteId = "n1",
            blockId = "b1",
            authorUserId = "u1",
            authorDisplayName = "Hannah",
            authorEmail = "hannah@test.com",
            body = "Hello world",
            createdAt = "2026-05-27T08:00:00Z",
            updatedAt = "2026-05-27T08:00:00Z"
        )
        coEvery { api.listNoteBlockComments("n1", "b1") } returns listOf(apiComment)
        coEvery { dao.clearComments("n1", "b1") } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        repository.refreshComments("n1", "b1")

        coVerify { dao.clearComments("n1", "b1") }
        coVerify { dao.insertAll(match { it.size == 1 && it[0].id == "c1" }) }
    }

    @Test
    fun `addComment success calls api and inserts into database`() = runTest {
        val apiComment = ApiNoteBlockComment(
            id = "c1",
            noteId = "n1",
            blockId = "b1",
            authorUserId = "u1",
            authorDisplayName = "Hannah",
            authorEmail = "hannah@test.com",
            body = "Hello world",
            createdAt = "2026-05-27T08:00:00Z",
            updatedAt = "2026-05-27T08:00:00Z"
        )
        val request = CreateNoteBlockCommentRequest("Hello world")
        coEvery { api.createNoteBlockComment("n1", "b1", request) } returns apiComment
        coEvery { dao.insert(any()) } returns Unit

        val result = repository.addComment("n1", "b1", "Hello world")

        coVerify { api.createNoteBlockComment("n1", "b1", request) }
        coVerify { dao.insert(match { it.id == "c1" }) }
        assertEquals("c1", result.id)
    }

    @Test
    fun `addComment failure falls back to saving a local representation offline`() = runTest {
        coEvery { api.createNoteBlockComment("n1", "b1", any()) } throws RuntimeException("Network error")
        every { authManager.profileEmail } returns MutableStateFlow("test@example.com")
        coEvery { dao.insert(any()) } returns Unit

        val result = repository.addComment("n1", "b1", "Hello offline")

        coVerify { dao.insert(match { it.authorEmail == "test@example.com" && it.body == "Hello offline" }) }
        assertTrue(result.id.startsWith("comment_"))
        assertEquals("test@example.com", result.authorEmail)
        assertEquals("Hello offline", result.body)
    }
}
