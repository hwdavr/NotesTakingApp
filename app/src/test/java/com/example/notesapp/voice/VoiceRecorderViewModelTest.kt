package com.example.notesapp.voice

import android.util.Log
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.MicrophoneAvailability
import com.example.notesapp.domain.voice.RecordingEntryPoint
import com.example.notesapp.domain.voice.RecordingSessionManager
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.RecordingStoragePreflighter
import com.example.notesapp.domain.voice.StorageInfoProvider
import com.example.notesapp.domain.voice.TranscriptSessionState
import com.example.notesapp.domain.voice.VoiceRecordingController
import com.example.notesapp.domain.voice.VoiceTranscriptSession
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import com.example.notesapp.ui.voice.model.VoiceRecorderError
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.viewmodel.VoiceRecorderViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceRecorderViewModelTest : BaseViewModelTest() {
    private lateinit var controller: VoiceRecordingController
    private lateinit var controllerState: MutableStateFlow<RecordingSessionState>
    private lateinit var microphoneAvailability: MicrophoneAvailability
    private lateinit var storageInfoProvider: StorageInfoProvider
    private lateinit var transcriptSession: VoiceTranscriptSession
    private lateinit var transcriptState: MutableStateFlow<TranscriptSessionState>
    private lateinit var recordingSessionManager: RecordingSessionManager
    private lateinit var voiceNotePlaceholderUseCase: VoiceNotePlaceholderUseCase
    private lateinit var viewModel: VoiceRecorderViewModel

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        controller = mockk(relaxed = true)
        controllerState = MutableStateFlow(RecordingSessionState.Idle)
        every { controller.state } returns controllerState
        microphoneAvailability = mockk()
        storageInfoProvider = mockk()
        transcriptSession = mockk(relaxed = true)
        transcriptState = MutableStateFlow(TranscriptSessionState())
        every { transcriptSession.state } returns transcriptState
        every { microphoneAvailability.isAvailable() } returns true
        every { storageInfoProvider.availableBytes() } returns 256L * 1024L * 1024L
        recordingSessionManager = RecordingSessionManager()
        voiceNotePlaceholderUseCase = mockk(relaxed = true)
        viewModel = VoiceRecorderViewModel(
            recordingController = controller,
            storagePreflighter = RecordingStoragePreflighter(storageInfoProvider),
            microphoneAvailability = microphoneAvailability,
            transcriptSession = transcriptSession,
            recordingSessionManager = recordingSessionManager,
            voiceNotePlaceholderUseCase = voiceNotePlaceholderUseCase
        )
    }

    @Test
    fun `discard removes a Home placeholder`() = runTest {
        viewModel.onScreenReady(
            targetNoteId = "home-placeholder",
            permissionGranted = false,
            source = RecordingEntryPoint.HOME
        )

        viewModel.onDiscard()
        advanceUntilIdle()

        coVerify { voiceNotePlaceholderUseCase.discard("home-placeholder") }
    }

    @Test
    fun `starting from the editor discards an active Home placeholder first`() = runTest {
        recordingSessionManager.replace(
            metadata = RecordingSessionMetadata(
                sessionId = "home-session",
                noteId = "home-placeholder",
                blockId = "home-block",
                audioFilePath = "/tmp/home.m4a",
                format = AudioFormat.AAC,
                entryPoint = RecordingEntryPoint.HOME
            ),
            discardActive = {}
        )

        viewModel.onScreenReady(
            targetNoteId = "editor-note",
            permissionGranted = true,
            source = RecordingEntryPoint.EDITOR
        )
        advanceUntilIdle()

        coVerify { voiceNotePlaceholderUseCase.discard("home-placeholder") }
        verify {
            controller.start(
                match { request ->
                    request.noteId == "editor-note" && request.entryPoint == RecordingEntryPoint.EDITOR
                }
            )
        }
    }

    @Test
    fun `permission is required before recording starts`() = runTest {
        viewModel.onScreenReady(targetNoteId = "note", permissionGranted = false)

        assertEquals(VoiceRecorderStatus.PermissionRequired, viewModel.uiState.value.status)
        verify(exactly = 0) { controller.start(any()) }
    }

    @Test
    fun `missing microphone blocks service start`() = runTest {
        every { microphoneAvailability.isAvailable() } returns false

        viewModel.onScreenReady(targetNoteId = "note", permissionGranted = true)

        assertEquals(VoiceRecorderError.MicrophoneUnavailable, viewModel.uiState.value.error)
        verify(exactly = 0) { controller.start(any()) }
    }

    @Test
    fun `insufficient storage blocks service start`() = runTest {
        every { storageInfoProvider.availableBytes() } returns 50L * 1024L * 1024L

        viewModel.onScreenReady(targetNoteId = "note", permissionGranted = true)

        assertEquals(VoiceRecorderError.StorageInsufficient, viewModel.uiState.value.error)
        verify(exactly = 0) { controller.start(any()) }
    }

    @Test
    fun `permission grant starts an AAC recording request`() = runTest {
        viewModel.onScreenReady(targetNoteId = "note", permissionGranted = false)
        viewModel.onPermissionResult(granted = true, permanentlyDenied = false)

        verify {
            controller.start(
                match { request ->
                    request.noteId == "note" && request.format == AudioFormat.AAC
                }
            )
        }
    }

    @Test
    fun `permission denial is surfaced and can be cleared`() = runTest {
        viewModel.onPermissionResult(granted = false, permanentlyDenied = true)

        assertEquals(VoiceRecorderStatus.PermissionRequired, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.permissionPermanentlyDenied)

        viewModel.clearPermissionDenial()

        assertFalse(viewModel.uiState.value.permissionPermanentlyDenied)
    }

    @Test
    fun `screen starts only once and creates a draft note when no note is supplied`() = runTest {
        viewModel.onScreenReady(targetNoteId = null, permissionGranted = true)
        viewModel.onScreenReady(targetNoteId = "second-note", permissionGranted = true)

        verify(exactly = 1) {
            controller.start(
                match { request ->
                    request.noteId.startsWith("draft_")
                }
            )
        }
    }

    @Test
    fun `service recording state is mapped to recorder controls`() = runTest {
        val metadata = RecordingSessionMetadata(
            sessionId = "session",
            noteId = "note",
            blockId = "block",
            audioFilePath = "/data/data/app/files/voice-notes/vn_note_block_1.m4a",
            format = AudioFormat.AAC
        )
        controllerState.value = RecordingSessionState.Recording(
            metadata = metadata,
            elapsedMs = 5_000L,
            amplitudes = listOf(0.4f)
        )

        assertEquals(VoiceRecorderStatus.Recording, viewModel.uiState.value.status)
        assertEquals(5_000L, viewModel.uiState.value.elapsedMs)
        assertEquals(listOf(0.4f), viewModel.uiState.value.amplitudes)
    }

    @Test
    fun `paused saving saved and failed service states map to their UI states`() = runTest {
        val metadata = RecordingSessionMetadata(
            sessionId = "session",
            noteId = "note",
            blockId = "block",
            audioFilePath = "/data/data/app/files/voice-notes/vn_note_block_1.m4a",
            format = AudioFormat.AAC
        )

        controllerState.value = RecordingSessionState.Paused(metadata, 2_000L, listOf(0.2f))
        assertEquals(VoiceRecorderStatus.Paused, viewModel.uiState.value.status)
        assertEquals(listOf(0.2f), viewModel.uiState.value.amplitudes)

        controllerState.value = RecordingSessionState.Saving(metadata, 3_000L)
        assertEquals(VoiceRecorderStatus.Saving, viewModel.uiState.value.status)

        controllerState.value = RecordingSessionState.Saved(metadata, 4_000L, 128L)
        assertEquals(VoiceRecorderStatus.Saved, viewModel.uiState.value.status)
        assertEquals(metadata.audioFilePath, viewModel.uiState.value.savedFilePath)
        assertEquals(128L, viewModel.uiState.value.savedFileSizeBytes)

        controllerState.value = RecordingSessionState.Error("save failed", metadata, 5_000L)
        assertEquals(VoiceRecorderError.SavingFailed, viewModel.uiState.value.error)
        assertEquals(5_000L, viewModel.uiState.value.elapsedMs)

        controllerState.value = RecordingSessionState.Error("microphone stopped", metadata, 6_000L)
        assertEquals(VoiceRecorderError.RecordingFailed, viewModel.uiState.value.error)
    }

    @Test
    fun `non permanent permission denial remains retryable`() = runTest {
        viewModel.onPermissionResult(granted = false, permanentlyDenied = false)

        assertEquals(VoiceRecorderStatus.PermissionRequired, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.permissionPermanentlyDenied)
    }

    @Test
    fun `active controls delegate pause stop and discard`() = runTest {
        viewModel.onPauseResume()
        viewModel.onStop()
        viewModel.onDiscard()

        verify { controller.togglePauseResume() }
        verify { controller.stopAndSave() }
        verify { controller.discard() }
    }
}
