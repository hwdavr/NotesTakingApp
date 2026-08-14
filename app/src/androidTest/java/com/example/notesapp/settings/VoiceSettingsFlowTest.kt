package com.example.notesapp.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.settings.screen.SettingsScreenContent
import com.example.notesapp.ui.settings.viewmodel.SettingsAudioFormat
import com.example.notesapp.ui.settings.viewmodel.VoiceStorageUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceSettingsFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsShowsVoiceNotesAndPersistsOpusSelection() {
        var selectedFormat by mutableStateOf(SettingsAudioFormat.AAC)

        composeRule.setContent {
            NotesTakingAppTheme {
                SettingsScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    voiceAudioFormat = selectedFormat,
                    voiceStorage = VoiceStorageUiState(
                        totalBytes = 184_600_000L,
                        recordingCount = 12
                    ),
                    onVoiceAudioFormatSelected = { selectedFormat = it },
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_voice_notes_section")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings_voice_section_header")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("184.6 MB").assertIsDisplayed()
        composeRule.onNodeWithText("12 recordings").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_voice_format_aac")
            .assertIsSelected()
        composeRule.onNodeWithTag("settings_voice_format_opus")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(SettingsAudioFormat.OPUS, selectedFormat)
        }
        composeRule.onNodeWithTag("settings_voice_format_opus")
            .assertIsSelected()
        composeRule.onNodeWithText("OPUS · 32 kbps · OGG").assertIsDisplayed()
    }
}
