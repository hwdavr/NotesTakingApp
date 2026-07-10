package com.example.notesapp.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiMappersTest {

    @Test
    fun `toFolderEntity maps all fields correctly`() {
        val apiItem = ApiItem(
            id = "f1",
            type = "folder",
            name = "Folder",
            parentId = "p1",
            userId = "u1",
            content = "Client receipts",
            sortKey = "100",
            version = 1L,
            deviceId = "device1",
            lastSyncedVersion = 0,
            deletedAt = "1970-01-01T00:00:03Z",
            createdAt = "1970-01-01T00:00:01Z",
            updatedAt = "1970-01-01T00:00:02Z",
            isFavorite = true
        )
        val entity = apiItem.toFolderEntity()
        assertEquals(apiItem.id, entity.id)
        assertEquals(apiItem.name, entity.name)
        assertEquals(apiItem.content, entity.description)
        assertEquals(apiItem.parentId, entity.parentFolderId)
        assertEquals(apiItem.sortKey, entity.sortKey)
        assertEquals(apiItem.version, entity.version)
        assertEquals(apiItem.deviceId, entity.deviceId)
        assertEquals(apiItem.lastSyncedVersion, entity.lastSyncedVersion)
        assertEquals(3000L, entity.deletedAt)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
        assertEquals(apiItem.isFavorite, entity.isFavorite)
    }

    @Test
    fun `toNoteEntity maps all fields correctly`() {
        val apiItem = ApiItem(
            id = "n1",
            type = "note",
            name = "Note Title",
            parentId = "p1",
            userId = "u1",
            content = "Note Content",
            sortKey = "100",
            version = 1L,
            deviceId = "device1",
            lastSyncedVersion = 0,
            deletedAt = null,
            createdAt = "1970-01-01T00:00:01Z",
            updatedAt = "1970-01-01T00:00:02Z",
            isFavorite = false,
            accessRole = "read_only"
        )
        val entity = apiItem.toNoteEntity()
        assertEquals(apiItem.id, entity.id)
        assertEquals(apiItem.name, entity.title)
        assertEquals(apiItem.parentId, entity.folderId)
        assertEquals(apiItem.content, entity.content)
        assertEquals(apiItem.sortKey, entity.sortKey)
        assertEquals(apiItem.version, entity.version)
        assertEquals(apiItem.deviceId, entity.deviceId)
        assertEquals(apiItem.lastSyncedVersion, entity.lastSyncedVersion)
        assertEquals(null, entity.deletedAt)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
        assertEquals(apiItem.isFavorite, entity.isFavorite)
        assertEquals("read_only", entity.accessRole)
    }
}
