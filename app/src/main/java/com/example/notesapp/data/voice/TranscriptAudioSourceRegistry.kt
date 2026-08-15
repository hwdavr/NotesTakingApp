package com.example.notesapp.data.voice

import android.media.AudioFormat as AndroidAudioFormat
import android.os.ParcelFileDescriptor
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptAudioSourceRegistry @Inject constructor() {
    private val sources = ConcurrentHashMap<String, PcmAudioSource>()

    fun register(sessionId: String, source: PcmAudioSource) {
        sources.put(sessionId, source)?.close()
    }

    fun find(sessionId: String): PcmAudioSource? = sources[sessionId]

    fun remove(sessionId: String) {
        sources.remove(sessionId)?.close()
    }
}

class PcmAudioSource(
    val sampleRateHertz: Int,
    val channelCount: Int = 1,
    val encoding: Int = AndroidAudioFormat.ENCODING_PCM_16BIT
) : AutoCloseable {
    private val descriptors = ParcelFileDescriptor.createPipe()
    private val output: OutputStream = ParcelFileDescriptor.AutoCloseOutputStream(descriptors[1])
    private var attached = false
    private var closed = false

    @Synchronized
    fun attachForRecognizer(): ParcelFileDescriptor? {
        if (closed) return null
        attached = true
        return descriptors[0]
    }

    @Synchronized
    fun disable() {
        if (closed) return
        attached = false
        output.runCatching { close() }
    }

    @Synchronized
    fun write(bytes: ByteArray) {
        if (closed || !attached || bytes.isEmpty()) return
        try {
            output.write(bytes)
        } catch (_: IOException) {
            attached = false
        }
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        output.runCatching { close() }
        descriptors[0].runCatching { close() }
    }
}
