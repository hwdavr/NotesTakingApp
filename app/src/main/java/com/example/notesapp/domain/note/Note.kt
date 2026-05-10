package com.example.notesapp.domain.note

enum class NoteAccessRole {
    READ_ONLY,
    FULL_ACCESS
}

data class Note(
    val id: String = "",
    val title: String,
    val content: String,
    val folderId: String? = null,
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
