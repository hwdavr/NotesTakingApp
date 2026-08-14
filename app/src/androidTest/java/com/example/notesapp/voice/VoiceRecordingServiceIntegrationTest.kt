package com.example.notesapp.voice

import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.data.voice.service.VoiceNoteRecordingService
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceRecordingServiceIntegrationTest {
    @Test
    fun foregroundServiceOwnsOnePrivateContiguousFile() {
        runBlocking {
            assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val context = instrumentation.targetContext
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.RECORD_AUDIO
            )
            val directory = File(context.filesDir, "voice-notes").apply { mkdirs() }
            val outputFile = File(directory, "vn_service_test_block_1.m4a")
            outputFile.delete()
            val metadata = RecordingSessionMetadata(
                sessionId = "service-test-session",
                noteId = "service-test-note",
                blockId = "service-test-block",
                audioFilePath = outputFile.absolutePath,
                format = AudioFormat.AAC
            )

            ContextCompat.startForegroundService(
                context,
                VoiceNoteRecordingService.startIntent(context, "service-test-token", metadata)
            )
            val started = withTimeoutOrNull(4_000L) {
                while (!outputFile.exists() || outputFile.length() == 0L) {
                    delay(100L)
                }
                true
            } ?: false
            context.startService(VoiceNoteRecordingService.stopIntent(context))

            assertTrue(started)
            assertTrue(outputFile.exists())
            assertTrue(outputFile.length() > 0L)
            outputFile.delete()
        }
    }
}
