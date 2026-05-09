package com.example.notesapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_shares",
    indices = [Index("noteId"), Index(value = ["noteId", "email"], unique = true)]
)
data class NoteShareEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val userId: String?,
    val email: String,
    val displayName: String?,
    val accessRole: String,
    val status: String,
    val invitedByUserId: String,
    val createdAt: Long,
    val updatedAt: Long
)
