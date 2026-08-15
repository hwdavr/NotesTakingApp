package com.example.notesapp.voice

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.data.voice.AndroidVoiceAudioCapture
import com.example.notesapp.data.voice.AndroidVoiceAudioEncoder
import com.example.notesapp.data.voice.VoiceAudioCaptureConfig
import com.example.notesapp.domain.voice.AudioFormat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class VoiceAudioPlatformBoundaryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `audio capture pause resume and stop are safe before start`() {
        val capture = AndroidVoiceAudioCapture(context)

        capture.pause()
        capture.resume()
        capture.stop()
    }

    @Test
    fun `audio capture rejects an invalid sample rate before opening microphone`() {
        Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .grantPermissions(Manifest.permission.RECORD_AUDIO)
        val capture = AndroidVoiceAudioCapture(context)

        assertThrows(IllegalArgumentException::class.java) {
            capture.start(
                config = VoiceAudioCaptureConfig(sampleRateHertz = 0),
                onFrame = {},
                onError = {}
            )
        }
        capture.stop()
    }

    @Test
    fun `audio encoder rejects stereo voice input`() {
        val encoder = AndroidVoiceAudioEncoder()

        assertThrows(IllegalArgumentException::class.java) {
            encoder.start(
                outputPath = "/tmp/stereo.m4a",
                format = AudioFormat.AAC,
                config = VoiceAudioCaptureConfig(
                    sampleRateHertz = 44_100,
                    channelCount = 2
                )
            )
        }
        encoder.stop()
    }

    @Test
    fun `audio encoder rejects PCM input before start`() {
        val encoder = AndroidVoiceAudioEncoder()

        assertThrows(IllegalStateException::class.java) {
            encoder.writePcm(byteArrayOf(0, 0))
        }
        encoder.stop()
    }

    @Test
    @Config(sdk = [28])
    fun `audio encoder rejects opus before Android Q`() {
        val encoder = AndroidVoiceAudioEncoder()

        assertThrows(IllegalStateException::class.java) {
            encoder.start(
                outputPath = "/tmp/legacy.ogg",
                format = AudioFormat.OPUS,
                config = VoiceAudioCaptureConfig(sampleRateHertz = 16_000)
            )
        }
        encoder.stop()
    }
}
