package com.example.notesapp.data.remote

import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.local.NoteEntity
import java.time.Instant

fun ApiItem.toFolderEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    parentFolderId = parentId,
    sortKey = sortKey,
    version = version,
    deviceId = deviceId,
    lastSyncedVersion = lastSyncedVersion,
    deletedAt = deletedAt?.toEpochMillis(),
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis()
)

fun ApiItem.toNoteEntity(): NoteEntity = NoteEntity(
    id = id,
    title = name,
    content = content,
    folderId = parentId,
    sortKey = sortKey,
    version = version,
    deviceId = deviceId,
    lastSyncedVersion = lastSyncedVersion,
    deletedAt = deletedAt?.toEpochMillis(),
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis()
)

private fun String.toEpochMillis(): Long = Instant.parse(this).toEpochMilli()
