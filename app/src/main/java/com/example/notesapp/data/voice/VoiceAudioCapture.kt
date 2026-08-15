package com.example.notesapp.data.voice

import android.media.AudioFormat as AndroidAudioFormat
import com.example.notesapp.domain.voice.AudioFormat

data class VoiceAudioCaptureConfig(
    val sampleRateHertz: Int,
    val channelCount: Int = 1,
    val encoding: Int = AndroidAudioFormat.ENCODING_PCM_16BIT,
    val bufferSizeBytes: Int = 0
)

data class VoiceAudioFrame(
    val pcmBytes: ByteArray,
    val amplitude: Float
)

interface VoiceAudioCapture {
    fun start(config: VoiceAudioCaptureConfig, onFrame: (VoiceAudioFrame) -> Unit, onError: (Throwable) -> Unit)

    fun pause()

    fun resume()

    fun stop()
}

fun voiceAudioCaptureConfigForFormat(format: AudioFormat): VoiceAudioCaptureConfig = VoiceAudioCaptureConfig(
    sampleRateHertz = if (format == AudioFormat.OPUS) 16_000 else 44_100
)
