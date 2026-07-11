package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.domain.folder.Folder
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderMapperTest {
    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = FolderEntity(
            id = "f1",
            name = "Folder",
            description = "Client receipts",
            parentFolderId = "p1",
            sortKey = "100",
            version = 1L,
            deviceId = "device1",
            lastSyncedVersion = 0,
            deletedAt = null,
            createdAt = 1000L,
            updatedAt = 2000L,
            isFavorite = true
        )
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.description, domain.description)
        assertEquals(entity.parentFolderId, domain.parentFolderId)
        assertEquals(entity.sortKey, domain.sortKey)
        assertEquals(entity.version, domain.version)
        assertEquals(entity.deviceId, domain.deviceId)
        assertEquals(entity.lastSyncedVersion, domain.lastSyncedVersion)
        assertEquals(entity.deletedAt, domain.deletedAt)
        assertEquals(entity.createdAt, domain.createdAt)
        assertEquals(entity.updatedAt, domain.updatedAt)
        assertEquals(entity.isFavorite, domain.isFavorite)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val domain = Folder(
            id = "f1",
            name = "Folder",
            description = "Client receipts",
            parentFolderId = "p1",
            sortKey = "100",
            version = 1,
            deviceId = "device1",
            lastSyncedVersion = 0,
            deletedAt = null,
            createdAt = 1000L,
            updatedAt = 2000L,
            isFavorite = true
        )
        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.description, entity.description)
        assertEquals(domain.parentFolderId, entity.parentFolderId)
        assertEquals(domain.sortKey, entity.sortKey)
        assertEquals(domain.version, entity.version)
        assertEquals(domain.deviceId, entity.deviceId)
        assertEquals(domain.lastSyncedVersion, entity.lastSyncedVersion)
        assertEquals(domain.deletedAt, entity.deletedAt)
        assertEquals(domain.createdAt, entity.createdAt)
        assertEquals(domain.updatedAt, entity.updatedAt)
        assertEquals(domain.isFavorite, entity.isFavorite)
    }
}
