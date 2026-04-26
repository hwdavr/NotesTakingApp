package com.example.notesapp.domain.note

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
    val updatedAt: Long
)
