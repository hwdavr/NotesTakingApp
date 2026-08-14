package com.example.notesapp.ui.voice.viewmodel

import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.usecase.SaveVoiceNoteRecordingUseCase
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import javax.inject.Inject

data class VoiceRecordingSaveRequest(
    val noteId: String,
    val blockId: String,
    val audioFilePath: String,
    val audioFormat: AudioFormat,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val transcript: String,
    val focusedBlockId: String?
)

class VoiceRecorderPersistence @Inject constructor(
    private val voiceNotePlaceholderUseCase: VoiceNotePlaceholderUseCase,
    private val saveVoiceNoteRecordingUseCase: SaveVoiceNoteRecordingUseCase
) {
    suspend fun discardPlaceholder(noteId: String) {
        voiceNotePlaceholderUseCase.discard(noteId)
    }

    suspend fun saveRecording(request: VoiceRecordingSaveRequest) {
        saveVoiceNoteRecordingUseCase(
            noteId = request.noteId,
            blockId = request.blockId,
            audioFilePath = request.audioFilePath,
            audioFormat = request.audioFormat,
            durationMs = request.durationMs,
            fileSizeBytes = request.fileSizeBytes,
            transcript = request.transcript,
            focusedBlockId = request.focusedBlockId
        )
    }
}
