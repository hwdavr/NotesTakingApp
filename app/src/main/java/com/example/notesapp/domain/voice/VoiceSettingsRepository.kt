package com.example.notesapp.domain.voice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class VoiceStorageUsage(
    val totalBytes: Long = 0L,
    val recordingCount: Int = 0
)

interface VoiceSettingsRepository {
    val selectedAudioFormat: StateFlow<AudioFormat>
    val storageUsage: Flow<VoiceStorageUsage>

    fun currentAudioFormat(): AudioFormat = selectedAudioFormat.value

    suspend fun setAudioFormat(format: AudioFormat)
}
