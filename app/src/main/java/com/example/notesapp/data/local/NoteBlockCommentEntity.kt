package com.example.notesapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_block_comments",
    indices = [
        Index("noteId"),
        Index("blockId"),
        Index(value = ["noteId", "blockId"])
    ]
)
data class NoteBlockCommentEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val blockId: String,
    val authorUserId: String,
    val authorDisplayName: String?,
    val authorEmail: String?,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long
)
