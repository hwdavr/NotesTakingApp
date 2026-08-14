package com.example.notesapp.voice

import com.example.notesapp.R
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.ui.voice.model.VoiceRecorderError
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderStatusLabel
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import com.example.notesapp.ui.voice.model.toRenderState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceRecorderRenderStateTest {
    @Test
    fun `maps recording and paused states to active render controls`() {
        val recording = VoiceRecorderUiState(
            status = VoiceRecorderStatus.Recording,
            elapsedMs = 65_000L,
            amplitudes = listOf(0.5f),
            format = AudioFormat.AAC
        ).toRenderState()

        assertEquals(VoiceRecorderStatusLabel.Recording, recording.statusLabel)
        assertTrue(recording.isRecording)
        assertTrue(recording.isActive)
        assertFalse(recording.isPaused)
        assertEquals("01:05", recording.elapsedText)
        assertEquals(R.string.voice_recorder_format_aac, recording.formatLabelRes)

        val paused = VoiceRecorderUiState(status = VoiceRecorderStatus.Paused).toRenderState()

        assertEquals(VoiceRecorderStatusLabel.Paused, paused.statusLabel)
        assertTrue(paused.isPaused)
        assertTrue(paused.isActive)
        assertFalse(paused.isRecording)
    }

    @Test
    fun `maps non active statuses and the OPUS format`() {
        val loading = VoiceRecorderUiState(status = VoiceRecorderStatus.Loading).toRenderState()
        val permission = VoiceRecorderUiState(status = VoiceRecorderStatus.PermissionRequired).toRenderState()
        val ready = VoiceRecorderUiState(status = VoiceRecorderStatus.Ready).toRenderState()
        val saved = VoiceRecorderUiState(status = VoiceRecorderStatus.Saved).toRenderState()
        val opus = VoiceRecorderUiState(format = AudioFormat.OPUS).toRenderState()

        assertTrue(loading.showLoading)
        assertTrue(permission.showPermissionRetry)
        assertTrue(ready.isReady)
        assertTrue(saved.isSaved)
        assertEquals(R.string.voice_recorder_format_opus, opus.formatLabelRes)
        assertEquals(VoiceRecorderStatusLabel.Ready, ready.statusLabel)
        assertFalse(ready.isActive)
    }

    @Test
    fun `maps every recorder error to localized dialog metadata`() {
        for (error in VoiceRecorderError.entries) {
            val renderState = VoiceRecorderUiState(error = error).toRenderState()

            assertEquals(error, renderState.error)
            assertNotNull(renderState.errorTitleRes)
            assertNotNull(renderState.errorMessageRes)
            assertNotNull(renderState.errorDialogTag)
        }
    }
}
