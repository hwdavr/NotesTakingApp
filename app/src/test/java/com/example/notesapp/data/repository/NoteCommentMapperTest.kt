package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteBlockCommentEntity
import com.example.notesapp.data.remote.ApiNoteBlockComment
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteCommentMapperTest {

    @Test
    fun `api comment maps timestamps and nullable author fields`() {
        val apiComment = ApiNoteBlockComment(
            id = "comment-1",
            noteId = "note-1",
            blockId = "block-1",
            authorUserId = "auth0|user-1",
            authorDisplayName = null,
            authorEmail = "writer@example.com",
            body = "Please review this.",
            createdAt = "1970-01-01T00:00:01Z",
            updatedAt = "1970-01-01T00:00:02Z"
        )

        val entity = apiComment.toEntity()

        assertEquals("comment-1", entity.id)
        assertEquals("note-1", entity.noteId)
        assertEquals("block-1", entity.blockId)
        assertEquals("auth0|user-1", entity.authorUserId)
        assertEquals(null, entity.authorDisplayName)
        assertEquals("writer@example.com", entity.authorEmail)
        assertEquals(1_000L, entity.createdAt)
        assertEquals(2_000L, entity.updatedAt)
    }

    @Test
    fun `entity maps to domain without changing comment content`() {
        val entity = NoteBlockCommentEntity(
            id = "comment-2",
            noteId = "note-1",
            blockId = "block-1",
            authorUserId = "auth0|user-2",
            authorDisplayName = "Reviewer",
            authorEmail = "reviewer@example.com",
            body = "Looks good.",
            createdAt = 3_000L,
            updatedAt = 4_000L
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.noteId, domain.noteId)
        assertEquals(entity.blockId, domain.blockId)
        assertEquals(entity.authorUserId, domain.authorUserId)
        assertEquals(entity.authorDisplayName, domain.authorDisplayName)
        assertEquals(entity.authorEmail, domain.authorEmail)
        assertEquals(entity.body, domain.body)
        assertEquals(entity.createdAt, domain.createdAt)
        assertEquals(entity.updatedAt, domain.updatedAt)
    }
}
