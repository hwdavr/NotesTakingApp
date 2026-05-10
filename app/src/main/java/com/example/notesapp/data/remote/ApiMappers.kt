package com.example.notesapp.data.remote

import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.note.NoteAccessRole
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
    updatedAt = updatedAt.toEpochMillis(),
    isFavorite = isFavorite
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
    updatedAt = updatedAt.toEpochMillis(),
    isFavorite = isFavorite,
    isShared = isShared,
    accessRole = accessRole
)

internal fun String.toNoteAccessRole(): NoteAccessRole = when (this) {
    "read_only" -> NoteAccessRole.READ_ONLY
    else -> NoteAccessRole.FULL_ACCESS
}
private fun String.toEpochMillis(): Long = Instant.parse(this).toEpochMilli()
