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
import com.example.notesapp.domain.voice.VoiceRecordingController
import com.example.notesapp.ui.voice.model.VoiceRecorderError
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
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
    private val microphoneAvailability: MicrophoneAvailability
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(VoiceRecorderUiState())
    val uiState: StateFlow<VoiceRecorderUiState> = mutableUiState.asStateFlow()
    private var startAttempted = false
    private var noteId: String? = null

    init {
        viewModelScope.launch {
            recordingController.state.collect(::onRecordingStateChanged)
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

    companion object {
        private const val TAG = "NotesApp/VoiceRecorderViewModel"
    }
}
