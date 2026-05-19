package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.remote.toNoteAccessRole
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.NoteAccessRole

fun FolderEntity.toDomain(): Folder = Folder(
    id = id,
    name = name,
    parentFolderId = parentFolderId,
    sortKey = sortKey,
    version = version,
    deviceId = deviceId,
    lastSyncedVersion = lastSyncedVersion,
    deletedAt = deletedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isFavorite = isFavorite,
    isShared = isShared,
    accessRole = accessRole.toNoteAccessRole()
)
fun Folder.toEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    parentFolderId = parentFolderId,
    sortKey = sortKey,
    version = version,
    deviceId = deviceId,
    lastSyncedVersion = lastSyncedVersion,
    deletedAt = deletedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isFavorite = isFavorite,
    isShared = isShared,
    accessRole = when (accessRole) {
        NoteAccessRole.READ_ONLY -> "read_only"
        NoteAccessRole.FULL_ACCESS -> "full_access"
    }
)
