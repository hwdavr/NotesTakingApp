package com.example.notesapp.ui.voice.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.MicrophoneAvailability
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.RecordingStartRequest
import com.example.notesapp.domain.voice.RecordingStoragePreflighter
import com.example.notesapp.domain.voice.StoragePreflightResult
import com.example.notesapp.domain.voice.TranscriptSessionState
import com.example.notesapp.domain.voice.TranscriptSessionStatus
import com.example.notesapp.domain.voice.TranscriptWarning
import com.example.notesapp.domain.voice.VoiceRecordingController
import com.example.notesapp.domain.voice.VoiceTranscriptSession
import com.example.notesapp.ui.voice.model.VoiceRecorderError
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderTranscriptStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderTranscriptWarning
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class VoiceRecorderViewModel @Inject constructor(
    private val recordingController: VoiceRecordingController,
    private val storagePreflighter: RecordingStoragePreflighter,
    private val microphoneAvailability: MicrophoneAvailability,
    private val transcriptSession: VoiceTranscriptSession
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(VoiceRecorderUiState())
    val uiState: StateFlow<VoiceRecorderUiState> = mutableUiState.asStateFlow()
    private var startAttempted = false
    private var noteId: String? = null

    init {
        viewModelScope.launch {
            recordingController.state.collect(::onRecordingStateChanged)
        }
        viewModelScope.launch {
            transcriptSession.state.collect(::onTranscriptStateChanged)
        }
    }

    fun onScreenReady(targetNoteId: String?, permissionGranted: Boolean) {
        noteId = targetNoteId
        if (startAttempted) return
        if (!permissionGranted) {
            mutableUiState.value = mutableUiState.value.copy(
                status = VoiceRecorderStatus.PermissionRequired,
                error = null
            )
            return
        }
        startAttempted = true
        startRecording()
    }

    fun onPermissionResult(granted: Boolean, permanentlyDenied: Boolean) {
        if (granted) {
            startAttempted = true
            mutableUiState.value = mutableUiState.value.copy(
                status = VoiceRecorderStatus.Loading,
                permissionPermanentlyDenied = false,
                error = null
            )
            startRecording()
        } else {
            mutableUiState.value = mutableUiState.value.copy(
                status = VoiceRecorderStatus.PermissionRequired,
                permissionPermanentlyDenied = permanentlyDenied
            )
        }
    }

    fun onPauseResume() {
        recordingController.togglePauseResume()
    }

    fun onStop() {
        recordingController.stopAndSave()
    }

    fun onDiscard() {
        recordingController.discard()
    }

    fun clearPermissionDenial() {
        mutableUiState.value = mutableUiState.value.copy(permissionPermanentlyDenied = false)
    }

    private fun startRecording() {
        if (!microphoneAvailability.isAvailable()) {
            mutableUiState.value = mutableUiState.value.copy(
                status = VoiceRecorderStatus.Error,
                error = VoiceRecorderError.MicrophoneUnavailable
            )
            return
        }
        when (storagePreflighter.check()) {
            is StoragePreflightResult.Available -> {
                val request = RecordingStartRequest(
                    noteId = noteId ?: "draft_${UUID.randomUUID()}",
                    blockId = UUID.randomUUID().toString(),
                    format = AudioFormat.AAC
                )
                recordingController.start(request)
            }

            is StoragePreflightResult.Insufficient -> {
                mutableUiState.value = mutableUiState.value.copy(
                    status = VoiceRecorderStatus.Error,
                    error = VoiceRecorderError.StorageInsufficient
                )
            }
        }
    }

    private fun onRecordingStateChanged(state: RecordingSessionState) {
        val current = mutableUiState.value
        mutableUiState.value = when (state) {
            RecordingSessionState.Idle -> current.copy(status = VoiceRecorderStatus.Ready)
            is RecordingSessionState.Recording -> current.copy(
                status = VoiceRecorderStatus.Recording,
                format = state.metadata.format,
                elapsedMs = state.elapsedMs,
                amplitudes = state.amplitudes,
                error = null
            )

            is RecordingSessionState.Paused -> current.copy(
                status = VoiceRecorderStatus.Paused,
                format = state.metadata.format,
                elapsedMs = state.elapsedMs,
                amplitudes = state.amplitudes,
                error = null
            )

            is RecordingSessionState.Saving -> current.copy(
                status = VoiceRecorderStatus.Saving,
                format = state.metadata.format,
                elapsedMs = state.elapsedMs,
                error = null
            )

            is RecordingSessionState.Saved -> current.copy(
                status = VoiceRecorderStatus.Saved,
                format = state.metadata.format,
                elapsedMs = state.elapsedMs,
                savedFilePath = state.metadata.audioFilePath,
                savedFileSizeBytes = state.fileSizeBytes,
                transcript = state.transcript,
                transcriptPartial = "",
                transcriptStatus = VoiceRecorderTranscriptStatus.Completed,
                error = null
            )

            is RecordingSessionState.Error -> current.copy(
                status = VoiceRecorderStatus.Error,
                elapsedMs = state.elapsedMs,
                error = if (state.message.contains("save", ignoreCase = true)) {
                    VoiceRecorderError.SavingFailed
                } else {
                    VoiceRecorderError.RecordingFailed
                }
            )
        }
        Log.d(TAG, "Recorder state changed to ${mutableUiState.value.status}")
    }

    private fun onTranscriptStateChanged(state: TranscriptSessionState) {
        val current = mutableUiState.value
        if (state.sessionId != null && state.sessionId != currentSessionId()) return
        mutableUiState.value = current.copy(
            transcript = state.committedText,
            transcriptPartial = state.partialText,
            transcriptStatus = state.status.toUiStatus(),
            transcriptWarning = state.warning.toUiWarning()
        )
    }

    private fun currentSessionId(): String? = when (val state = recordingController.state.value) {
        RecordingSessionState.Idle -> null
        is RecordingSessionState.Recording -> state.metadata.sessionId
        is RecordingSessionState.Paused -> state.metadata.sessionId
        is RecordingSessionState.Saving -> state.metadata.sessionId
        is RecordingSessionState.Saved -> state.metadata.sessionId
        is RecordingSessionState.Error -> state.metadata?.sessionId
    }

    private fun TranscriptSessionStatus.toUiStatus(): VoiceRecorderTranscriptStatus = when (this) {
        TranscriptSessionStatus.Idle -> VoiceRecorderTranscriptStatus.Idle
        TranscriptSessionStatus.Recognizing -> VoiceRecorderTranscriptStatus.Recognizing
        TranscriptSessionStatus.Paused -> VoiceRecorderTranscriptStatus.Paused
        TranscriptSessionStatus.AudioOnly -> VoiceRecorderTranscriptStatus.AudioOnly
        TranscriptSessionStatus.Completed -> VoiceRecorderTranscriptStatus.Completed
    }

    private fun TranscriptWarning?.toUiWarning(): VoiceRecorderTranscriptWarning? = when (this) {
        null -> null
        is TranscriptWarning.ModelUnavailable ->
            VoiceRecorderTranscriptWarning.ModelUnavailable
        is TranscriptWarning.AudioSourceUnavailable ->
            VoiceRecorderTranscriptWarning.AudioSourceUnavailable
        is TranscriptWarning.ChunkTimedOut ->
            VoiceRecorderTranscriptWarning.ChunkTimedOut
        is TranscriptWarning.RecognitionFailed ->
            VoiceRecorderTranscriptWarning.RecognitionFailed
    }

    companion object {
        private const val TAG = "NotesApp/VoiceRecorderViewModel"
    }
}
