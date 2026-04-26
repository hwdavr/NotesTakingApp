package com.example.notesapp.domain.folder

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
    val updatedAt: Long
)
