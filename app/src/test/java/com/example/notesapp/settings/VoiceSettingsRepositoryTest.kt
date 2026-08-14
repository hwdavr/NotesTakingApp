package com.example.notesapp.settings

import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.data.local.VoiceNoteBlockDao
import com.example.notesapp.data.repository.VoiceSettingsRepositoryImpl
import com.example.notesapp.domain.voice.AudioFormat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoiceSettingsRepositoryTest {
    private lateinit var repository: VoiceSettingsRepositoryImpl

    @Before
    fun setUp() {
        val dao = mockk<VoiceNoteBlockDao>()
        every { dao.observeTotalAudioBytes() } returns flowOf(184_600_000L)
        every { dao.observeAudioRecordingCount() } returns flowOf(12)
        repository = VoiceSettingsRepositoryImpl(
            context = ApplicationProvider.getApplicationContext(),
            voiceNoteBlockDao = dao
        )
    }

    @Test
    fun persistsFormatAndExposesItForTheNextRecording() = runTest {
        repository.setAudioFormat(AudioFormat.OPUS)

        assertEquals(AudioFormat.OPUS, repository.currentAudioFormat())
        assertEquals(AudioFormat.OPUS, repository.selectedAudioFormat.value)
    }

    @Test
    fun reportsTotalStorageAndRecordingCountFromVoiceFiles() = runTest {
        val usage = repository.storageUsage.first()

        assertEquals(184_600_000L, usage.totalBytes)
        assertEquals(12, usage.recordingCount)
    }
}
