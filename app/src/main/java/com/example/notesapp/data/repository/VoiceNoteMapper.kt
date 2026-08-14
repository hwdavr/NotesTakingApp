package com.example.notesapp.data.repository

import com.example.notesapp.data.local.VoiceNoteBlockEntity
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.VoiceNoteBlock

fun VoiceNoteBlockEntity.toDomain(): VoiceNoteBlock = VoiceNoteBlock(
    blockId = blockId,
    noteId = noteId,
    audioFilePath = audioFilePath,
    audioFormat = AudioFormat.fromStorageValue(audioFormat),
    durationMs = durationMs,
    fileSizeBytes = fileSizeBytes,
    sampleRateHertz = sampleRateHertz,
    channels = channels,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun VoiceNoteBlock.toEntity(): VoiceNoteBlockEntity = VoiceNoteBlockEntity(
    blockId = blockId,
    noteId = noteId,
    audioFilePath = audioFilePath,
    audioFormat = audioFormat.storageValue,
    durationMs = durationMs,
    fileSizeBytes = fileSizeBytes,
    sampleRateHertz = sampleRateHertz,
    channels = channels,
    createdAt = createdAt,
    updatedAt = updatedAt
)
