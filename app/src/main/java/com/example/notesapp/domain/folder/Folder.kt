package com.example.notesapp.domain.folder

import com.example.notesapp.domain.note.NoteAccessRole

data class Folder(
    val id: String = "",
    val name: String,
    val parentFolderId: String? = null,
    val sortKey: String = "",
    val version: Long = 0,
    val deviceId: String = "",
    val lastSyncedVersion: Long = 0,
    val deletedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val isShared: Boolean = false,
    val accessRole: NoteAccessRole = NoteAccessRole.FULL_ACCESS
)
