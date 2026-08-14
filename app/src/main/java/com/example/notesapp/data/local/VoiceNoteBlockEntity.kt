package com.example.notesapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "voice_note_blocks",
    primaryKeys = ["blockId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class VoiceNoteBlockEntity(
    val blockId: String,
    val noteId: String,
    val audioFilePath: String?,
    val audioFormat: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val sampleRateHertz: Int,
    val channels: Int,
    val createdAt: Long,
    val updatedAt: Long
)
