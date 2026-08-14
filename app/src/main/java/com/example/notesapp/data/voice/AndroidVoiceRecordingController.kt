package com.example.notesapp.data.voice

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.notesapp.data.voice.service.VoiceNoteRecordingService
import com.example.notesapp.domain.voice.RecordingSessionManager
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.RecordingStartRequest
import com.example.notesapp.domain.voice.VoiceRecordingController
import com.example.notesapp.domain.voice.VoiceSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class AndroidVoiceRecordingController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFileSystem: AudioFileSystem,
    private val sessionManager: RecordingSessionManager,
    private val stateStore: RecordingStateStore,
    private val voiceSettingsRepository: VoiceSettingsRepository
) : VoiceRecordingController {
    override val state: StateFlow<RecordingSessionState> = stateStore.state

    override fun start(request: RecordingStartRequest) {
        val sessionId = UUID.randomUUID().toString()
        val blockId = request.blockId.ifBlank { UUID.randomUUID().toString() }
        val selectedFormat = voiceSettingsRepository.currentAudioFormat()
        val file = audioFileSystem.createRecordingFile(request.noteId, blockId, selectedFormat)
        val metadata = RecordingSessionMetadata(
            sessionId = sessionId,
            noteId = request.noteId,
            blockId = blockId,
            audioFilePath = file.absolutePath,
            format = selectedFormat,
            entryPoint = request.entryPoint
        )
        val active = sessionManager.replace(metadata) { oldSession ->
            context.startService(
                VoiceNoteRecordingService.discardIntent(
                    context = context,
                    sessionId = oldSession.metadata.sessionId
                )
            )
        }
        val intent = VoiceNoteRecordingService.startIntent(
            context = context,
            token = active.token.value,
            metadata = metadata
        )
        ContextCompat.startForegroundService(context, intent)
    }

    override fun togglePauseResume() {
        context.startService(VoiceNoteRecordingService.toggleIntent(context))
    }

    override fun stopAndSave() {
        context.startService(VoiceNoteRecordingService.stopIntent(context))
    }

    override fun discard() {
        context.startService(VoiceNoteRecordingService.discardIntent(context = context, sessionId = null))
    }
}
