package com.example.notesapp.data.repository

import com.example.notesapp.auth.AuthManager
import com.example.notesapp.data.local.NoteBlockCommentDao
import com.example.notesapp.data.local.NoteBlockCommentEntity
import com.example.notesapp.data.remote.ApiNoteBlockComment
import com.example.notesapp.data.remote.CreateNoteBlockCommentRequest
import com.example.notesapp.data.remote.NotesApiService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NoteCommentRepositoryImplTest {

    private lateinit var dao: NoteBlockCommentDao
    private lateinit var api: NotesApiService
    private lateinit var authManager: AuthManager
    private lateinit var repository: NoteCommentRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        api = mockk()
        authManager = mockk()
        every { authManager.profileEmail } returns MutableStateFlow("me@example.com")
        repository = NoteCommentRepositoryImpl(dao, api, authManager)
    }

    @Test
    fun `observeComments maps cached entities`() = runTest {
        every { dao.observeComments("note-1", "block-1") } returns flowOf(
            listOf(
                NoteBlockCommentEntity(
                    id = "comment-1",
                    noteId = "note-1",
                    blockId = "block-1",
                    authorUserId = "user-1",
                    authorDisplayName = "Writer",
                    authorEmail = "writer@example.com",
                    body = "Cached comment",
                    createdAt = 1_000L,
                    updatedAt = 1_000L
                )
            )
        )

        val result = repository.observeComments("note-1", "block-1").first()

        assertEquals("comment-1", result.single().id)
        assertEquals("Cached comment", result.single().body)
    }

    @Test
    fun `refreshComments replaces cached block comments`() = runTest {
        val apiComment = apiComment()
        coEvery { api.listNoteBlockComments("note-1", "block-1") } returns listOf(apiComment)
        coEvery { dao.clearComments("note-1", "block-1") } just Runs
        coEvery { dao.insertAll(any()) } just Runs

        repository.refreshComments("note-1", "block-1")

        coVerify { dao.clearComments("note-1", "block-1") }
        coVerify { dao.insertAll(match { comments -> comments.single().id == "comment-1" }) }
    }

    @Test
    fun `addComment sends request and caches server response`() = runTest {
        val apiComment = apiComment()
        coEvery {
            api.createNoteBlockComment(
                "note-1",
                "block-1",
                CreateNoteBlockCommentRequest(body = "Server comment")
            )
        } returns apiComment
        coEvery { dao.insert(any()) } just Runs

        val result = repository.addComment("note-1", "block-1", "Server comment")

        assertEquals("comment-1", result.id)
        coVerify { dao.insert(match { comment -> comment.id == "comment-1" }) }
    }

    @Test
    fun `addComment keeps composer usable when server is unavailable`() = runTest {
        coEvery {
            api.createNoteBlockComment(any(), any(), any())
        } throws IOException("offline")
        coEvery { dao.insert(any()) } just Runs

        val result = repository.addComment("note-1", "block-1", "Offline comment")

        assertEquals("Offline comment", result.body)
        assertEquals("me@example.com", result.authorEmail)
        coVerify {
            dao.insert(
                match { comment ->
                    comment.noteId == "note-1" &&
                        comment.blockId == "block-1" &&
                        comment.body == "Offline comment"
                }
            )
        }
    }

    private fun apiComment() = ApiNoteBlockComment(
        id = "comment-1",
        noteId = "note-1",
        blockId = "block-1",
        authorUserId = "user-1",
        authorDisplayName = "Writer",
        authorEmail = "writer@example.com",
        body = "Server comment",
        createdAt = "1970-01-01T00:00:01Z",
        updatedAt = "1970-01-01T00:00:02Z"
    )
}
