package com.example.notesapp.domain.voice

import kotlinx.coroutines.flow.StateFlow

data class RecordingStartRequest(
    val noteId: String,
    val blockId: String,
    val format: AudioFormat
)

interface VoiceRecordingController {
    val state: StateFlow<RecordingSessionState>

    fun start(request: RecordingStartRequest)

    fun togglePauseResume()

    fun stopAndSave()

    fun discard()
}

interface MicrophoneAvailability {
    fun isAvailable(): Boolean
}
