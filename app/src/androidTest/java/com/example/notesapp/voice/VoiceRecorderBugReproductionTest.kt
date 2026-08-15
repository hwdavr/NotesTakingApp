package com.example.notesapp.voice

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import com.example.notesapp.ui.voice.screen.VoiceRecorderContent
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class VoiceRecorderBugReproductionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenRecorderWaveform_whenRendered_thenBarsAreCenteredWithinContent() {
        composeRule.setContent {
            NotesTakingAppTheme {
                VoiceRecorderContent(
                    state = VoiceRecorderUiState(
                        status = VoiceRecorderStatus.Recording,
                        elapsedMs = 102_000L,
                        amplitudes = List(64) { 0.5f },
                        transcript = "Live transcript"
                    ),
                    transcriptScrollState = rememberScrollState(),
                    onClose = {},
                    onDiscardRequest = {},
                    onDiscardConfirm = {},
                    onDiscardCancel = {},
                    onPauseResume = {},
                    onStop = {},
                    onBack = {},
                    onPermissionGrant = {}
                )
            }
        }
        composeRule.waitForIdle()

        val waveformBitmap = composeRule.onNodeWithTag("recorder_waveform")
            .captureToImage()
            .asAndroidBitmap()
        val occupiedX = waveformBitmap.findWaveformPixels()
        assertTrue("The waveform did not render any colored bars", occupiedX.isNotEmpty())

        val leftGap = occupiedX.minOrNull()!!
        val rightGap = waveformBitmap.width - occupiedX.maxOrNull()!! - 1
        assertTrue(
            "Waveform bars are not centered: leftGap=$leftGap, rightGap=$rightGap",
            abs(leftGap - rightGap) <= 12f
        )
    }

    private fun Bitmap.findWaveformPixels(): List<Int> {
        val minX = 0
        val maxX = width - 1
        val minY = 0
        val maxY = height - 1
        return (minX..maxX).filter { x ->
            (minY..maxY).any { y ->
                val pixel = getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                Color.alpha(pixel) > 0 && red < 220 && blue - red > 35 && green < 210
            }
        }
    }
}
