package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteShareEntity
import com.example.notesapp.data.remote.NoteShareDto
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteShareMapperTest {

    @Test
    fun `toDomain maps all fields correctly for full_access and active`() {
        val entity = NoteShareEntity(
            id = "s1",
            noteId = "n1",
            userId = "u1",
            email = "test@example.com",
            displayName = "Test User",
            accessRole = "full_access",
            status = "active",
            invitedByUserId = "owner1",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.noteId, domain.noteId)
        assertEquals(entity.userId, domain.userId)
        assertEquals(entity.email, domain.email)
        assertEquals(entity.displayName, domain.displayName)
        assertEquals(NoteShareAccessRole.EDITOR, domain.accessRole)
        assertEquals(NoteShareStatus.ACTIVE, domain.status)
        assertEquals(entity.invitedByUserId, domain.invitedByUserId)
        assertEquals(entity.createdAt, domain.createdAt)
        assertEquals(entity.updatedAt, domain.updatedAt)
    }

    @Test
    fun `toDomain maps default fields correctly for read_only and pending`() {
        val entity = NoteShareEntity(
            id = "s1",
            noteId = "n1",
            userId = "u1",
            email = "test@example.com",
            displayName = "Test User",
            accessRole = "other_role",
            status = "other_status",
            invitedByUserId = "owner1",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val domain = entity.toDomain()
        assertEquals(NoteShareAccessRole.VIEWER, domain.accessRole)
        assertEquals(NoteShareStatus.PENDING, domain.status)
    }

    @Test
    fun `toEntity maps all fields correctly from Dto`() {
        val dto = NoteShareDto(
            id = "s1",
            noteId = "n1",
            userId = "u1",
            email = "test@example.com",
            displayName = "Test User",
            accessRole = "full_access",
            status = "active",
            invitedByUserId = "owner1",
            createdAt = "1970-01-01T00:00:01Z",
            updatedAt = "1970-01-01T00:00:02Z"
        )
        val entity = dto.toEntity()
        assertEquals(dto.id, entity.id)
        assertEquals(dto.noteId, entity.noteId)
        assertEquals(dto.userId, entity.userId)
        assertEquals(dto.email, entity.email)
        assertEquals(dto.displayName, entity.displayName)
        assertEquals(dto.accessRole, entity.accessRole)
        assertEquals(dto.status, entity.status)
        assertEquals(dto.invitedByUserId, entity.invitedByUserId)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
    }

    @Test
    fun `toApiValue maps access roles correctly`() {
        assertEquals("read_only", NoteShareAccessRole.VIEWER.toApiValue())
        assertEquals("full_access", NoteShareAccessRole.EDITOR.toApiValue())
    }
}
