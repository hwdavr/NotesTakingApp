package com.example.notesapp.data.voice

import com.example.notesapp.domain.voice.AudioFormat

interface VoiceAudioEncoder {
    fun start(outputPath: String, format: AudioFormat, config: VoiceAudioCaptureConfig)

    fun writePcm(pcmBytes: ByteArray)

    fun stop()
}
