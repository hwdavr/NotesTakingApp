package com.example.notesapp.voice

import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.recordingAudioFormatForApi
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingAudioFormatPolicyTest {
    @Test
    fun opusFallsBackToAacBeforeApi29() {
        assertEquals(AudioFormat.AAC, recordingAudioFormatForApi(AudioFormat.OPUS, 24))
        assertEquals(AudioFormat.AAC, recordingAudioFormatForApi(AudioFormat.OPUS, 28))
    }

    @Test
    fun opusRemainsOpusFromApi29() {
        assertEquals(AudioFormat.OPUS, recordingAudioFormatForApi(AudioFormat.OPUS, 29))
        assertEquals(AudioFormat.OPUS, recordingAudioFormatForApi(AudioFormat.OPUS, 34))
    }

    @Test
    fun aacRemainsAacAcrossSupportedApis() {
        assertEquals(AudioFormat.AAC, recordingAudioFormatForApi(AudioFormat.AAC, 24))
        assertEquals(AudioFormat.AAC, recordingAudioFormatForApi(AudioFormat.AAC, 34))
    }
}
