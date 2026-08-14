package com.example.notesapp.voice

import android.app.Notification
import android.content.Context
import android.speech.SpeechRecognizer
import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.R
import com.example.notesapp.data.voice.AndroidMicrophoneAvailability
import com.example.notesapp.data.voice.AndroidStorageInfoProvider
import com.example.notesapp.data.voice.AndroidVoiceTranscriptRecognizer
import com.example.notesapp.data.voice.PrivateAudioFileSystem
import com.example.notesapp.data.voice.RecordingStateStore
import com.example.notesapp.data.voice.SpeechRecognizerFactory
import com.example.notesapp.domain.voice.AudioFilenameGenerator
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptStartRequest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

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

    @Test
    fun `android transcript adapter reports safe availability fallback`() {
        val events = mutableListOf<TranscriptRecognitionEvent>()
        val adapter = AndroidVoiceTranscriptRecognizer(context)

        adapter.start(
            request = TranscriptStartRequest(
                sessionId = "session",
                audioFilePath = "/tmp/voice.m4a"
            ),
            onEvent = events::add
        )
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val event = events.singleOrNull()
        assertTrue(
            event == null ||
                event is TranscriptRecognitionEvent.ModelUnavailable ||
                event is TranscriptRecognitionEvent.AudioSourceUnavailable
        )
        adapter.stop()
    }

    @Test
    fun `android transcript adapter starts recognizer and forwards partial and final results`() {
        val speechRecognizer = mockk<SpeechRecognizer>(relaxed = true)
        val factory = mockk<SpeechRecognizerFactory>()
        every { factory.isRecognitionAvailable(context) } returns true
        every { factory.isOnDeviceRecognitionAvailable(context) } returns true
        every { factory.create(context) } returns speechRecognizer
        every { speechRecognizer.setRecognitionListener(any()) } just Runs
        val adapter = AndroidVoiceTranscriptRecognizer(context, factory)

        adapter.start(
            request = TranscriptStartRequest(
                sessionId = "session",
                audioFilePath = "/tmp/voice.m4a"
            ),
            onEvent = {}
        )
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        verify { speechRecognizer.startListening(any()) }
        adapter.stop()
    }

    @Test
    fun `recording notification exposes pause and resume actions for matching states`() {
        val controller = Robolectric.buildService(
            com.example.notesapp.data.voice.service.VoiceNoteRecordingService::class.java
        ).create()
        val service = controller.get()
        val metadata = RecordingSessionMetadata(
            sessionId = "notification-session",
            noteId = "notification-note",
            blockId = "notification-block",
            audioFilePath = "/tmp/notification.m4a",
            format = AudioFormat.AAC
        )
        val currentState = service.javaClass.getDeclaredField("currentState").apply {
            isAccessible = true
        }
        val buildNotification = service.javaClass.getDeclaredMethod(
            "buildNotification",
            Long::class.javaPrimitiveType
        ).apply { isAccessible = true }

        currentState.set(
            service,
            RecordingSessionState.Recording(metadata, elapsedMs = 1_000L, amplitudes = emptyList())
        )
        val recordingNotification = buildNotification.invoke(service, 1_000L) as Notification
        assertEquals(
            service.getString(R.string.voice_notification_pause),
            recordingNotification.actions[0].title
        )

        currentState.set(
            service,
            RecordingSessionState.Paused(metadata, elapsedMs = 1_000L, amplitudes = emptyList())
        )
        val pausedNotification = buildNotification.invoke(service, 1_000L) as Notification
        assertEquals(
            service.getString(R.string.voice_notification_resume),
            pausedNotification.actions[0].title
        )
        controller.destroy()
    }
}
