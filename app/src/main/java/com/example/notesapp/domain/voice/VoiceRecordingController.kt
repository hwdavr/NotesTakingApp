package com.example.notesapp.domain.voice

import kotlinx.coroutines.flow.StateFlow

data class RecordingStartRequest(
    val noteId: String,
    val blockId: String,
    val format: AudioFormat,
    val entryPoint: RecordingEntryPoint = RecordingEntryPoint.EDITOR
)

enum class RecordingEntryPoint {
    HOME,
    EDITOR;

    companion object {
        fun fromRoute(value: String): RecordingEntryPoint =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: EDITOR
    }
}

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
