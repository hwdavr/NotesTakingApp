package com.example.notesapp.domain.voice

data class VoiceNoteBlock(
    val blockId: String,
    val noteId: String,
    val audioFilePath: String?,
    val audioFormat: AudioFormat,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val sampleRateHertz: Int,
    val channels: Int,
    val createdAt: Long,
    val updatedAt: Long
)
