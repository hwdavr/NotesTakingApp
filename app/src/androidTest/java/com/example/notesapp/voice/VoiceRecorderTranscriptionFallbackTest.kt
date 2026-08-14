package com.example.notesapp.voice

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderTranscriptStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderTranscriptWarning
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import com.example.notesapp.ui.voice.screen.VoiceRecorderContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceRecorderTranscriptionFallbackTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unavailableTranscriptionKeepsRecorderControlsAndShowsSafeFallback() {
        composeRule.setContent {
            NotesTakingAppTheme {
                VoiceRecorderContent(
                    state = VoiceRecorderUiState(
                        status = VoiceRecorderStatus.Recording,
                        transcriptStatus = VoiceRecorderTranscriptStatus.AudioOnly,
                        transcriptWarning = VoiceRecorderTranscriptWarning.ModelUnavailable
                    ),
                    onPauseResume = {},
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

        composeRule.onNodeWithTag("recorder_stt_warning").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_transcript_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_toggle_record_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_stop_btn").assertIsDisplayed()
    }
}
