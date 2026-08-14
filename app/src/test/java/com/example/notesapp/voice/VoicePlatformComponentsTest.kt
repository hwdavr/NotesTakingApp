package com.example.notesapp.voice

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.data.voice.AndroidMicrophoneAvailability
import com.example.notesapp.data.voice.AndroidStorageInfoProvider
import com.example.notesapp.data.voice.PrivateAudioFileSystem
import com.example.notesapp.data.voice.RecordingStateStore
import com.example.notesapp.domain.voice.AudioFilenameGenerator
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoicePlatformComponentsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `private file system creates measures and deletes recording files`() {
        val fileSystem = PrivateAudioFileSystem(context, AudioFilenameGenerator())

        val file = fileSystem.createRecordingFile("note", "block", AudioFormat.AAC)
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertTrue(file.parentFile?.name == PrivateAudioFileSystem.VOICE_NOTES_DIRECTORY)
        assertEquals(3L, fileSystem.fileSize(file.absolutePath))
        assertTrue(fileSystem.delete(file.absolutePath))
        assertEquals(0L, fileSystem.fileSize(file.absolutePath))
        assertFalse(fileSystem.delete(file.absolutePath))
    }

    @Test
    fun `android providers delegate to platform storage and feature checks`() {
        val microphoneAvailable = AndroidMicrophoneAvailability(context).isAvailable()
        val expectedMicrophoneAvailability = context.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_MICROPHONE
        )

        assertEquals(expectedMicrophoneAvailability, microphoneAvailable)
        assertTrue(AndroidStorageInfoProvider(context).availableBytes() >= 0L)
    }

    @Test
    fun `recording state store publishes the latest state`() {
        val store = RecordingStateStore()
        val metadata = RecordingSessionMetadata(
            sessionId = "session",
            noteId = "note",
            blockId = "block",
            audioFilePath = "/tmp/voice.m4a",
            format = AudioFormat.AAC
        )
        val state = RecordingSessionState.Saved(
            metadata = metadata,
            elapsedMs = 1_000L,
            fileSizeBytes = 12L
        )

        store.update(state)

        assertEquals(state, store.state.value)
    }
}
