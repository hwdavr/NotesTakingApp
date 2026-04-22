package com.example.notesapp.domain.note

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
