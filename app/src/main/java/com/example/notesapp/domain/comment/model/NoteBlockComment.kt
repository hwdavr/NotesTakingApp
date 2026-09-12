package com.example.notesapp.domain.comment.model

data class NoteBlockComment(
    val id: String,
    val noteId: String,
    val blockId: String,
    val authorUserId: String,
    val authorDisplayName: String?,
    val authorEmail: String?,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long
)
