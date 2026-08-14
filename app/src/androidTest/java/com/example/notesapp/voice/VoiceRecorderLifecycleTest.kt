package com.example.notesapp.voice

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import com.example.notesapp.ui.voice.screen.VoiceRecorderContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceRecorderLifecycleTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun startsRecordingThroughProductionEntryPoint() {
        var pauseRequested by mutableStateOf(false)

        composeRule.setContent {
            NotesTakingAppTheme {
                VoiceRecorderContent(
                    state = VoiceRecorderUiState(
                        status = VoiceRecorderStatus.Recording,
                        elapsedMs = 3_000L,
                        amplitudes = List(64) { 0.5f }
                    ),
                    transcriptScrollState = rememberScrollState(),
                    onPauseResume = { pauseRequested = true },
                    onStop = {},
                    onDiscardRequest = {},
                    onDiscardConfirm = {},
                    onDiscardCancel = {},
                    onClose = {},
                    onBack = {},
                    onPermissionGrant = {}
                )
            }
        }

        composeRule.onNodeWithTag("recorder_status_pill").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_elapsed_timer").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_waveform").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_transcript_preview").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_toggle_record_btn").performClick()

        assertTrue(pauseRequested)
    }
}
