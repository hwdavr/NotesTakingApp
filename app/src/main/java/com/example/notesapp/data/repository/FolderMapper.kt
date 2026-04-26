package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.domain.folder.Folder

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
    updatedAt = updatedAt
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
    updatedAt = updatedAt
)
