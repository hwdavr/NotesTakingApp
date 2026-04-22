package com.example.notesapp.domain.folder

data class Folder(
    val id: Long = 0,
    val name: String,
    val parentFolderId: Long? = null,
    val createdAt: Long
)
