package com.example.notesapp.domain.voice

fun recordingAudioFormatForApi(selectedFormat: AudioFormat, apiLevel: Int): AudioFormat =
    if (selectedFormat == AudioFormat.OPUS && apiLevel < OPUS_MIN_API_LEVEL) {
        AudioFormat.AAC
    } else {
        selectedFormat
    }

private const val OPUS_MIN_API_LEVEL = 29
