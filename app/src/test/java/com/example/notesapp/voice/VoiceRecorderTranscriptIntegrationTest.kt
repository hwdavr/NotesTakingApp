package com.example.notesapp.voice

import android.util.Log
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.data.voice.RecordingTranscriptCoordinator
import com.example.notesapp.domain.voice.MicrophoneAvailability
import com.example.notesapp.domain.voice.RecordingSessionManager
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.RecordingStartRequest
import com.example.notesapp.domain.voice.RecordingStoragePreflighter
import com.example.notesapp.domain.voice.StorageInfoProvider
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptStartRequest
import com.example.notesapp.domain.voice.VoiceRecordingController
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import com.example.notesapp.domain.voice.VoiceTranscriptSession
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderTranscriptStatus
import com.example.notesapp.ui.voice.viewmodel.VoiceRecorderViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceRecorderTranscriptIntegrationTest : BaseViewModelTest() {
    private lateinit var recognizer: FakeTranscriptRecognizer
    private lateinit var transcriptSession: RecordingTranscriptCoordinator
    private lateinit var controller: FakeRecordingController
    private lateinit var viewModel: VoiceRecorderViewModel

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        recognizer = FakeTranscriptRecognizer()
        transcriptSession = RecordingTranscriptCoordinator(recognizer)
        controller = FakeRecordingController(transcriptSession)
        viewModel = VoiceRecorderViewModel(
            recordingController = controller,
            storagePreflighter = RecordingStoragePreflighter(
                object : StorageInfoProvider {
                    override fun availableBytes(): Long = 256L * 1024L * 1024L
                }
            ),
            microphoneAvailability = object : MicrophoneAvailability {
                override fun isAvailable(): Boolean = true
            },
            transcriptSession = transcriptSession,
            recordingSessionManager = RecordingSessionManager(),
            voiceNotePlaceholderUseCase = VoiceNotePlaceholderUseCase(mockk(relaxed = true))
        )
    }

    @Test
    fun appendsOverlappingChunksThroughProductionViewModel() = runTest {
        viewModel.onScreenReady(targetNoteId = "note", permissionGranted = true)
        advanceUntilIdle()

        recognizer.emit(
            TranscriptRecognitionEvent.Partial(
                sessionId = controller.sessionId,
                chunkIndex = 0,
                text = "The launch timeline needs to be finalized"
            )
        )
        recognizer.emit(
            TranscriptRecognitionEvent.Final(
                sessionId = controller.sessionId,
                chunkIndex = 0,
                text = "The launch timeline needs to be finalized"
            )
        )
        recognizer.emit(
            TranscriptRecognitionEvent.Final(
                sessionId = controller.sessionId,
                chunkIndex = 1,
                text = "to be finalized by Friday"
            )
        )
        advanceUntilIdle()

        assertEquals(
            "The launch timeline needs to be finalized by Friday",
            viewModel.uiState.value.transcriptPreview
        )

        controller.stopAndSave()
        advanceUntilIdle()

        assertEquals(
            viewModel.uiState.value.transcript,
            controller.savedTranscript
        )
        assertTrue(controller.savedTranscript.contains("by Friday"))
    }

    @Test
    fun fallsBackAndCancelsCleanly() = runTest {
        viewModel.onScreenReady(targetNoteId = "note", permissionGranted = true)
        advanceUntilIdle()

        recognizer.emit(
            TranscriptRecognitionEvent.ModelUnavailable(
                sessionId = controller.sessionId,
                languageTag = "en-US"
            )
        )
        advanceUntilIdle()

        assertEquals(
            VoiceRecorderTranscriptStatus.AudioOnly,
            viewModel.uiState.value.transcriptStatus
        )
        assertTrue(viewModel.uiState.value.transcriptWarning != null)

        recognizer.emit(
            TranscriptRecognitionEvent.ChunkTimedOut(
                sessionId = controller.sessionId,
                chunkIndex = 0
            )
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.transcript.contains("transcription failed for this segment"))

        viewModel.onDiscard()
        advanceUntilIdle()

        assertTrue(recognizer.cancelled)
        assertEquals(VoiceRecorderStatus.Ready, viewModel.uiState.value.status)
        assertEquals("", viewModel.uiState.value.transcript)
        assertEquals("", viewModel.uiState.value.transcriptPartial)
    }

    private class FakeTranscriptRecognizer : VoiceTranscriptRecognizer {
        private var callback: ((TranscriptRecognitionEvent) -> Unit)? = null
        var cancelled: Boolean = false

        override fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit) {
            callback = onEvent
            cancelled = false
        }

        override fun pause() = Unit

        override fun resume() = Unit

        override fun stop() = Unit

        override fun cancel() {
            cancelled = true
            callback = null
        }

        fun emit(event: TranscriptRecognitionEvent) {
            callback?.invoke(event)
        }
    }

    private class FakeRecordingController(
        private val transcriptSession: VoiceTranscriptSession
    ) : VoiceRecordingController {
        private val mutableState = MutableStateFlow<RecordingSessionState>(RecordingSessionState.Idle)
        override val state: StateFlow<RecordingSessionState> = mutableState
        var sessionId: String = ""
        var savedTranscript: String = ""

        override fun start(request: RecordingStartRequest) {
            sessionId = "session-1"
            val metadata = RecordingSessionMetadata(
                sessionId = sessionId,
                noteId = request.noteId,
                blockId = request.blockId,
                audioFilePath = File("/tmp/voice-${request.blockId}.m4a").absolutePath,
                format = request.format
            )
            transcriptSession.start(metadata)
            mutableState.value = RecordingSessionState.Recording(metadata, 0L, emptyList())
        }

        override fun togglePauseResume() = Unit

        override fun stopAndSave() {
            savedTranscript = transcriptSession.stop()
            val current = mutableState.value as RecordingSessionState.Recording
            mutableState.value = RecordingSessionState.Saved(
                metadata = current.metadata,
                elapsedMs = current.elapsedMs,
                fileSizeBytes = 0L,
                transcript = savedTranscript
            )
        }

        override fun discard() {
            transcriptSession.cancel()
            mutableState.value = RecordingSessionState.Idle
        }
    }
}
