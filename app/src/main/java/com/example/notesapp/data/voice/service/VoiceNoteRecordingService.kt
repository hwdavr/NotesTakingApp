package com.example.notesapp.data.voice.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.notesapp.R
import com.example.notesapp.data.voice.AudioFileSystem
import com.example.notesapp.data.voice.PcmAudioSource
import com.example.notesapp.data.voice.RecordingStateStore
import com.example.notesapp.data.voice.SpeechRecognizerFactory
import com.example.notesapp.data.voice.TranscriptAudioSourceRegistry
import com.example.notesapp.data.voice.VoiceAudioCapture
import com.example.notesapp.data.voice.VoiceAudioEncoder
import com.example.notesapp.data.voice.VoiceAudioFrame
import com.example.notesapp.data.voice.voiceAudioCaptureConfigForFormat
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingEntryPoint
import com.example.notesapp.domain.voice.RecordingSessionEvent
import com.example.notesapp.domain.voice.RecordingSessionManager
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.RecordingSessionStateReducer
import com.example.notesapp.domain.voice.VoiceTranscriptSession
import com.example.notesapp.domain.voice.formatElapsedTime
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VoiceNoteRecordingService : LifecycleService() {
    @Inject
    lateinit var audioFileSystem: AudioFileSystem

    @Inject
    lateinit var recordingStateStore: RecordingStateStore

    @Inject
    lateinit var sessionManager: RecordingSessionManager

    @Inject
    lateinit var transcriptSession: VoiceTranscriptSession

    @Inject
    lateinit var audioCapture: VoiceAudioCapture

    @Inject
    lateinit var audioEncoder: VoiceAudioEncoder

    @Inject
    lateinit var transcriptAudioSourceRegistry: TranscriptAudioSourceRegistry

    @Inject
    lateinit var speechRecognizerFactory: SpeechRecognizerFactory

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val reducer = RecordingSessionStateReducer()
    private var metadata: RecordingSessionMetadata? = null
    private var activeJob: Job? = null
    private var recordingStartedAt = 0L
    private var pausedElapsedMs = 0L
    private var currentState: RecordingSessionState = RecordingSessionState.Idle
    private var pausedForFocus = false
    private var audioFocusRequest: AudioFocusRequest? = null
    private var failureHandled = false
    private var captureStarted = false
    private var transcriptAudioSource: PcmAudioSource? = null

    @Volatile
    private var latestAmplitude = 0f

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (currentState is RecordingSessionState.Recording) {
                    pausedForFocus = true
                    togglePauseResume()
                }
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (pausedForFocus && currentState is RecordingSessionState.Paused) {
                    pausedForFocus = false
                    togglePauseResume()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Received foreground-service action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> startRecording(intent)
            ACTION_TOGGLE -> togglePauseResume()
            ACTION_STOP -> stopAndSave()
            ACTION_DISCARD -> discard(intent.getStringExtra(EXTRA_SESSION_ID))
        }
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        activeJob?.cancel()
        serviceScope.cancel()
        if (captureStarted) {
            discard(metadata?.sessionId)
        }
        super.onDestroy()
    }

    private fun startRecording(intent: Intent) {
        if (captureStarted) return
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: return
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
        val blockId = intent.getStringExtra(EXTRA_BLOCK_ID) ?: return
        val requestedPath = intent.getStringExtra(EXTRA_FILE_PATH) ?: return
        val requestedFormat = AudioFormat.fromStorageValue(intent.getStringExtra(EXTRA_FORMAT).orEmpty())
        val (path, format) = normalizeFormat(requestedPath, requestedFormat, noteId, blockId)
        val entryPoint = RecordingEntryPoint.fromRoute(intent.getStringExtra(EXTRA_ENTRY_POINT).orEmpty())
        val nextMetadata = RecordingSessionMetadata(
            sessionId = sessionId,
            noteId = noteId,
            blockId = blockId,
            audioFilePath = path,
            format = format,
            entryPoint = entryPoint
        )
        metadata = nextMetadata
        try {
            createNotificationChannel()
            startForegroundWithNotification(0L)
            startCapture(nextMetadata)
        } catch (exception: Exception) {
            releaseAudioPipeline()
            if (nextMetadata.format == AudioFormat.OPUS) {
                audioFileSystem.delete(path)
                val fallbackMetadata = nextMetadata.copy(
                    audioFilePath = audioFileSystem.createRecordingFile(
                        noteId = nextMetadata.noteId,
                        blockId = nextMetadata.blockId,
                        format = AudioFormat.AAC
                    ).absolutePath,
                    format = AudioFormat.AAC
                )
                metadata = fallbackMetadata
                runCatching { startCapture(fallbackMetadata) }
                    .onSuccess { return }
                    .onFailure { fallbackException ->
                        publishStartFailure(fallbackException, fallbackMetadata, token)
                    }
            } else {
                publishStartFailure(exception, nextMetadata, token)
            }
        }
    }

    private fun startCapture(metadata: RecordingSessionMetadata) {
        failureHandled = false
        val captureConfig = voiceAudioCaptureConfigForFormat(metadata.format)
        audioEncoder.start(metadata.audioFilePath, metadata.format, captureConfig)
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            speechRecognizerFactory.isOnDeviceRecognitionAvailable(this)
        ) {
            transcriptAudioSource = PcmAudioSource(
                sampleRateHertz = captureConfig.sampleRateHertz,
                channelCount = captureConfig.channelCount,
                encoding = captureConfig.encoding
            ).also { source ->
                transcriptAudioSourceRegistry.register(metadata.sessionId, source)
            }
        }
        recordingStartedAt = SystemClock.elapsedRealtime()
        pausedElapsedMs = 0L
        latestAmplitude = 0f
        reduce(RecordingSessionEvent.Started(metadata))
        transcriptSession.start(metadata)
        if (!requestAudioFocus()) {
            failRecording(IOException("Audio focus unavailable"))
            return
        }
        audioCapture.start(
            config = captureConfig,
            onFrame = ::onAudioFrame,
            onError = ::failRecording
        )
        captureStarted = true
        startAmplitudeUpdates()
    }

    private fun onAudioFrame(frame: VoiceAudioFrame) {
        runCatching {
            audioEncoder.writePcm(frame.pcmBytes)
            transcriptAudioSource?.write(frame.pcmBytes)
            latestAmplitude = frame.amplitude
        }.onFailure(::failRecording)
    }

    private fun publishStartFailure(exception: Throwable, failedMetadata: RecordingSessionMetadata, token: String) {
        Log.e(TAG, "Unable to start voice recording", exception)
        recordingStateStore.update(
            RecordingSessionState.Error(
                message = exception.message ?: getString(R.string.voice_recording_start_error),
                metadata = failedMetadata
            )
        )
        releaseAudioPipeline()
        audioFileSystem.delete(failedMetadata.audioFilePath)
        sessionManager.current()?.takeIf { it.token.value == token }?.let {
            sessionManager.clear(it.token)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun normalizeFormat(
        requestedPath: String,
        requestedFormat: AudioFormat,
        noteId: String,
        blockId: String
    ): Pair<String, AudioFormat> {
        if (requestedFormat != AudioFormat.OPUS || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return requestedPath to requestedFormat
        }
        audioFileSystem.delete(requestedPath)
        return audioFileSystem.createRecordingFile(noteId, blockId, AudioFormat.AAC).absolutePath to AudioFormat.AAC
    }

    private fun startAmplitudeUpdates() {
        activeJob?.cancel()
        activeJob = serviceScope.launch {
            while (isActive && captureStarted) {
                if (currentState is RecordingSessionState.Recording) {
                    val elapsed = pausedElapsedMs + (SystemClock.elapsedRealtime() - recordingStartedAt)
                    reduce(RecordingSessionEvent.Tick(elapsed, latestAmplitude))
                    updateNotification(elapsed)
                }
                delay(250L)
            }
        }
    }

    private fun togglePauseResume() {
        when (currentState) {
            is RecordingSessionState.Recording -> {
                runCatching { audioCapture.pause() }.onSuccess {
                    pausedElapsedMs += SystemClock.elapsedRealtime() - recordingStartedAt
                    reduce(RecordingSessionEvent.PauseRequested)
                    transcriptSession.pause()
                    updateNotification(pausedElapsedMs)
                }.onFailure { error -> failRecording(error) }
            }

            is RecordingSessionState.Paused -> {
                runCatching { audioCapture.resume() }.onSuccess {
                    recordingStartedAt = SystemClock.elapsedRealtime()
                    reduce(RecordingSessionEvent.ResumeRequested)
                    transcriptSession.resume()
                    updateNotification(pausedElapsedMs)
                }.onFailure { error -> failRecording(error) }
            }

            else -> Unit
        }
    }

    private fun stopAndSave() {
        val currentMetadata = metadata ?: return
        val currentElapsed = when (val state = currentState) {
            is RecordingSessionState.Recording -> pausedElapsedMs + (SystemClock.elapsedRealtime() - recordingStartedAt)
            is RecordingSessionState.Paused -> pausedElapsedMs
            else -> return
        }
        reduce(RecordingSessionEvent.StopRequested)
        activeJob?.cancel()
        val transcript = transcriptSession.stop()
        runCatching { stopAudioPipeline() }
            .onFailure { error ->
                publishPartialOrError(
                    metadata = currentMetadata,
                    elapsedMs = currentElapsed,
                    transcript = transcript,
                    message = error.message ?: getString(R.string.voice_recording_save_error)
                )
            }
            .onSuccess {
                val fileSize = audioFileSystem.fileSize(currentMetadata.audioFilePath)
                recordingStateStore.update(
                    RecordingSessionState.Saved(
                        metadata = currentMetadata,
                        elapsedMs = currentElapsed,
                        fileSizeBytes = fileSize,
                        transcript = transcript
                    )
                )
            }
        clearActiveSession()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun discard(requestedSessionId: String?) {
        val currentMetadata = metadata
        if (requestedSessionId != null && currentMetadata?.sessionId != requestedSessionId) return
        activeJob?.cancel()
        transcriptSession.cancel()
        releaseAudioPipeline()
        currentMetadata?.audioFilePath?.let(audioFileSystem::delete)
        clearActiveSession()
        val currentStateSessionId = recordingStateStore.state.value.sessionIdOrNull()
        if (currentStateSessionId == null || currentStateSessionId == currentMetadata?.sessionId) {
            recordingStateStore.update(RecordingSessionState.Idle)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun failRecording(error: Throwable) {
        if (failureHandled) return
        failureHandled = true
        Log.e(TAG, "Voice recording failed", error)
        val currentMetadata = metadata
        val elapsed = when (val state = currentState) {
            is RecordingSessionState.Recording -> pausedElapsedMs + (SystemClock.elapsedRealtime() - recordingStartedAt)
            is RecordingSessionState.Paused -> pausedElapsedMs
            else -> 0L
        }
        val transcript = transcriptSession.stop()
        releaseAudioPipeline()
        if (currentMetadata != null) {
            publishPartialOrError(
                metadata = currentMetadata,
                elapsedMs = elapsed,
                transcript = transcript,
                message = error.message ?: getString(R.string.voice_recording_error)
            )
        } else {
            recordingStateStore.update(
                RecordingSessionState.Error(
                    message = error.message ?: getString(R.string.voice_recording_error),
                    elapsedMs = elapsed
                )
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun publishPartialOrError(
        metadata: RecordingSessionMetadata,
        elapsedMs: Long,
        transcript: String,
        message: String
    ) {
        val fileSize = audioFileSystem.fileSize(metadata.audioFilePath)
        if (fileSize > 0L) {
            recordingStateStore.update(
                RecordingSessionState.Saved(
                    metadata = metadata,
                    elapsedMs = elapsedMs,
                    fileSizeBytes = fileSize,
                    transcript = transcript,
                    isPartial = true
                )
            )
        } else {
            audioFileSystem.delete(metadata.audioFilePath)
            recordingStateStore.update(
                RecordingSessionState.Error(
                    message = message,
                    metadata = metadata,
                    elapsedMs = elapsedMs
                )
            )
        }
        clearActiveSession()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun reduce(event: RecordingSessionEvent) {
        currentState = reducer.reduce(currentState, event)
        recordingStateStore.update(currentState)
    }

    private fun stopAudioPipeline() {
        audioCapture.stop()
        audioEncoder.stop()
        releaseAudioPipelineState()
    }

    private fun releaseAudioPipeline() {
        audioCapture.stop()
        runCatching { audioEncoder.stop() }
        releaseAudioPipelineState()
    }

    private fun releaseAudioPipelineState() {
        metadata?.sessionId?.let(transcriptAudioSourceRegistry::remove)
        transcriptAudioSource?.close()
        transcriptAudioSource = null
        captureStarted = false
        releaseAudioFocus()
    }

    private fun clearActiveSession() {
        val current = sessionManager.current()
        if (current?.metadata?.sessionId == metadata?.sessionId) {
            current?.let { sessionManager.clear(it.token) }
        }
    }

    private fun startForegroundWithNotification(elapsedMs: Long) {
        val notification = buildNotification(elapsedMs)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(elapsedMs: Long) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(elapsedMs))
    }

    private fun buildNotification(elapsedMs: Long): Notification {
        val isPaused = currentState is RecordingSessionState.Paused
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(getString(R.string.voice_notification_title))
            .setContentText(
                getString(
                    if (isPaused) {
                        R.string.voice_notification_paused
                    } else {
                        R.string.voice_notification_recording
                    },
                    formatElapsedTime(elapsedMs)
                )
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                if (isPaused) {
                    android.R.drawable.ic_media_play
                } else {
                    android.R.drawable.ic_media_pause
                },
                getString(
                    if (isPaused) R.string.voice_notification_resume else R.string.voice_notification_pause
                ),
                servicePendingIntent(ACTION_TOGGLE, REQUEST_TOGGLE)
            )
            .addAction(
                android.R.drawable.ic_menu_save,
                getString(R.string.voice_notification_stop),
                servicePendingIntent(ACTION_STOP, REQUEST_STOP)
            )
            .build()
    }

    private fun requestAudioFocus(): Boolean {
        val audioManager = getSystemService(AudioManager::class.java) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setOnAudioFocusChangeListener(audioFocusListener)
                .setWillPauseWhenDucked(true)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            requestLegacyAudioFocus(audioManager)
        }
    }

    private fun releaseAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let(audioManager::abandonAudioFocusRequest)
            audioFocusRequest = null
        } else {
            abandonLegacyAudioFocus(audioManager)
        }
        pausedForFocus = false
    }

    private fun requestLegacyAudioFocus(audioManager: AudioManager): Boolean = runCatching {
        val method = AudioManager::class.java.getMethod(
            "requestAudioFocus",
            AudioManager.OnAudioFocusChangeListener::class.java,
            Integer.TYPE,
            Integer.TYPE
        )
        method.invoke(
            audioManager,
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        ) as Int == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }.getOrDefault(false)

    private fun abandonLegacyAudioFocus(audioManager: AudioManager) {
        runCatching {
            val method = AudioManager::class.java.getMethod(
                "abandonAudioFocus",
                AudioManager.OnAudioFocusChangeListener::class.java
            )
            method.invoke(audioManager, audioFocusListener)
        }
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent = PendingIntent.getService(
        this,
        requestCode,
        Intent(this, VoiceNoteRecordingService::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.voice_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val ACTION_START = "com.example.notesapp.voice.START"
        private const val ACTION_TOGGLE = "com.example.notesapp.voice.TOGGLE"
        private const val ACTION_STOP = "com.example.notesapp.voice.STOP"
        private const val ACTION_DISCARD = "com.example.notesapp.voice.DISCARD"
        private const val EXTRA_TOKEN = "extra_token"
        private const val EXTRA_SESSION_ID = "extra_session_id"
        private const val EXTRA_NOTE_ID = "extra_note_id"
        private const val EXTRA_BLOCK_ID = "extra_block_id"
        private const val EXTRA_FILE_PATH = "extra_file_path"
        private const val EXTRA_FORMAT = "extra_format"
        private const val EXTRA_ENTRY_POINT = "extra_entry_point"
        private const val NOTIFICATION_CHANNEL_ID = "voice_recording"
        private const val NOTIFICATION_ID = 4101
        private const val REQUEST_TOGGLE = 4102
        private const val REQUEST_STOP = 4103
        private const val TAG = "NotesApp/VoiceNoteRecordingService"

        fun startIntent(context: Context, token: String, metadata: RecordingSessionMetadata): Intent =
            Intent(context, VoiceNoteRecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_SESSION_ID, metadata.sessionId)
                putExtra(EXTRA_NOTE_ID, metadata.noteId)
                putExtra(EXTRA_BLOCK_ID, metadata.blockId)
                putExtra(EXTRA_FILE_PATH, metadata.audioFilePath)
                putExtra(EXTRA_FORMAT, metadata.format.storageValue)
                putExtra(EXTRA_ENTRY_POINT, metadata.entryPoint.name)
            }

        fun toggleIntent(context: Context): Intent =
            Intent(context, VoiceNoteRecordingService::class.java).setAction(ACTION_TOGGLE)

        fun stopIntent(context: Context): Intent =
            Intent(context, VoiceNoteRecordingService::class.java).setAction(ACTION_STOP)

        fun discardIntent(context: Context, sessionId: String?): Intent =
            Intent(context, VoiceNoteRecordingService::class.java).apply {
                action = ACTION_DISCARD
                sessionId?.let { putExtra(EXTRA_SESSION_ID, it) }
            }
    }
}

private fun RecordingSessionState.sessionIdOrNull(): String? = when (this) {
    RecordingSessionState.Idle -> null
    is RecordingSessionState.Recording -> metadata.sessionId
    is RecordingSessionState.Paused -> metadata.sessionId
    is RecordingSessionState.Saving -> metadata.sessionId
    is RecordingSessionState.Saved -> metadata.sessionId
    is RecordingSessionState.Error -> metadata?.sessionId
}
