package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMapperTest {
    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = NoteEntity(
            id = "n1",
            title = "Title",
            content = "Content",
            folderId = "f1",
            sortKey = "100",
            version = 1L,
            deviceId = "device1",
            lastSyncedVersion = 0,
            deletedAt = null,
            createdAt = 1000L,
            updatedAt = 2000L,
            isFavorite = true,
            accessRole = "read_only"
        )
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.content, domain.content)
        assertEquals(entity.folderId, domain.folderId)
        assertEquals(entity.sortKey, domain.sortKey)
        assertEquals(entity.version, domain.version)
        assertEquals(entity.deviceId, domain.deviceId)
        assertEquals(entity.lastSyncedVersion, domain.lastSyncedVersion)
        assertEquals(entity.deletedAt, domain.deletedAt)
        assertEquals(entity.createdAt, domain.createdAt)
        assertEquals(entity.updatedAt, domain.updatedAt)
        assertEquals(entity.isFavorite, domain.isFavorite)
        assertEquals(NoteAccessRole.READ_ONLY, domain.accessRole)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val domain = Note(
            id = "n1",
            title = "Title",
            content = "Content",
            folderId = "f1",
            sortKey = "100",
            version = 1L,
            deviceId = "device1",
            lastSyncedVersion = 0,
            deletedAt = null,
            createdAt = 1000L,
            updatedAt = 2000L,
            isFavorite = true,
            accessRole = NoteAccessRole.READ_ONLY
        )
        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.content, entity.content)
        assertEquals(domain.folderId, entity.folderId)
        assertEquals(domain.sortKey, entity.sortKey)
        assertEquals(domain.version, entity.version)
        assertEquals(domain.deviceId, entity.deviceId)
        assertEquals(domain.lastSyncedVersion, entity.lastSyncedVersion)
        assertEquals(domain.deletedAt, entity.deletedAt)
        assertEquals(domain.createdAt, entity.createdAt)
        assertEquals(domain.updatedAt, entity.updatedAt)
        assertEquals(domain.isFavorite, entity.isFavorite)
        assertEquals("read_only", entity.accessRole)
    }
}
