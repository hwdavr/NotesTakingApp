package com.example.notesapp.data.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat as AndroidAudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceAudioCapture @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceAudioCapture {
    private val lock = java.lang.Object()
    private var audioRecord: AudioRecord? = null
    private var captureThread: Thread? = null

    @Volatile
    private var running = false

    @Volatile
    private var paused = false

    override fun start(
        config: VoiceAudioCaptureConfig,
        onFrame: (VoiceAudioFrame) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("RECORD_AUDIO permission is required to capture voice notes")
        }
        stop()
        val minBufferSize = AudioRecord.getMinBufferSize(
            config.sampleRateHertz,
            AndroidAudioFormat.CHANNEL_IN_MONO,
            config.encoding
        )
        require(minBufferSize > 0) { "AudioRecord buffer size is unavailable" }
        val bufferSize = maxOf(minBufferSize * 2, config.bufferSizeBytes)
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            config.sampleRateHertz,
            AndroidAudioFormat.CHANNEL_IN_MONO,
            config.encoding,
            bufferSize
        )
        check(record.state == AudioRecord.STATE_INITIALIZED) {
            "AudioRecord could not initialize"
        }
        try {
            record.startRecording()
        } catch (error: Throwable) {
            record.release()
            throw error
        }
        synchronized(lock) {
            audioRecord = record
            running = true
            paused = false
            captureThread = Thread {
                captureLoop(record, bufferSize, onFrame, onError)
            }.apply {
                name = "NotesApp/VoiceAudioCapture"
                start()
            }
        }
    }

    override fun pause() {
        synchronized(lock) {
            if (!running || paused) return
            paused = true
            audioRecord?.runCatching { stop() }
            lock.notifyAll()
        }
    }

    override fun resume() {
        synchronized(lock) {
            if (!running || !paused) return
            audioRecord?.startRecording()
            paused = false
            lock.notifyAll()
        }
    }

    override fun stop() {
        val threadToJoin: Thread?
        synchronized(lock) {
            running = false
            paused = false
            audioRecord?.runCatching { stop() }
            lock.notifyAll()
            threadToJoin = captureThread
            captureThread = null
        }
        if (threadToJoin != null && threadToJoin !== Thread.currentThread()) {
            runCatching { threadToJoin.join(CAPTURE_THREAD_JOIN_TIMEOUT_MS) }
        }
        synchronized(lock) {
            audioRecord?.release()
            audioRecord = null
        }
    }

    private fun captureLoop(
        record: AudioRecord,
        bufferSize: Int,
        onFrame: (VoiceAudioFrame) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val buffer = ByteArray(bufferSize)
        try {
            while (running) {
                synchronized(lock) {
                    while (running && paused) lock.wait(PAUSE_WAIT_TIMEOUT_MS)
                }
                if (!running) break
                val bytesRead = record.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                when {
                    bytesRead > 0 -> onFrame(
                        VoiceAudioFrame(
                            pcmBytes = buffer.copyOf(bytesRead),
                            amplitude = calculateAmplitude(buffer, bytesRead)
                        )
                    )

                    bytesRead < 0 -> onError(
                        IllegalStateException("AudioRecord read failed: $bytesRead")
                    )
                }
            }
        } catch (error: Throwable) {
            if (running) onError(error)
        }
    }

    private fun calculateAmplitude(buffer: ByteArray, length: Int): Float {
        var maximum = 0
        var index = 0
        while (index + 1 < length) {
            val sample = ((buffer[index + 1].toInt() shl 8) or (buffer[index].toInt() and 0xFF))
                .toShort()
                .toInt()
            maximum = maxOf(maximum, kotlin.math.abs(sample))
            index += 2
        }
        return (maximum / MAX_PCM_AMPLITUDE.toFloat()).coerceIn(0f, 1f)
    }

    private companion object {
        const val CAPTURE_THREAD_JOIN_TIMEOUT_MS = 1_000L
        const val MAX_PCM_AMPLITUDE = 32_767
        const val PAUSE_WAIT_TIMEOUT_MS = 50L
    }
}
