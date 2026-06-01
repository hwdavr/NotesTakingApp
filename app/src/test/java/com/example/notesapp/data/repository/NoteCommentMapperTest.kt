package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteBlockCommentEntity
import com.example.notesapp.data.remote.ApiNoteBlockComment
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteCommentMapperTest {

    @Test
    fun `NoteBlockCommentEntity mapped to Domain is correct`() {
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

        val domain = entity.toDomain()

        assertEquals("c1", domain.id)
        assertEquals("n1", domain.noteId)
        assertEquals("b1", domain.blockId)
        assertEquals("u1", domain.authorUserId)
        assertEquals("Hannah", domain.authorDisplayName)
        assertEquals("hannah@test.com", domain.authorEmail)
        assertEquals("Hello world", domain.body)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
    }

    @Test
    fun `ApiNoteBlockComment mapped to Entity is correct`() {
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

        val entity = apiComment.toEntity()

        assertEquals("c1", entity.id)
        assertEquals("n1", entity.noteId)
        assertEquals("b1", entity.blockId)
        assertEquals("u1", entity.authorUserId)
        assertEquals("Hannah", entity.authorDisplayName)
        assertEquals("hannah@test.com", entity.authorEmail)
        assertEquals("Hello world", entity.body)
        assertEquals(Instant.parse("2026-05-27T08:00:00Z").toEpochMilli(), entity.createdAt)
        assertEquals(Instant.parse("2026-05-27T08:00:00Z").toEpochMilli(), entity.updatedAt)
    }

    @Test
    fun `ApiNoteBlockComment mapped to Domain is correct`() {
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

        val domain = apiComment.toDomain()

        assertEquals("c1", domain.id)
        assertEquals("n1", domain.noteId)
        assertEquals("b1", domain.blockId)
        assertEquals("u1", domain.authorUserId)
        assertEquals("Hannah", domain.authorDisplayName)
        assertEquals("hannah@test.com", domain.authorEmail)
        assertEquals("Hello world", domain.body)
        assertEquals(Instant.parse("2026-05-27T08:00:00Z").toEpochMilli(), domain.createdAt)
        assertEquals(Instant.parse("2026-05-27T08:00:00Z").toEpochMilli(), domain.updatedAt)
    }
}
