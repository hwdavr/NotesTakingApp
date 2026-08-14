package com.example.notesapp.voice

import com.example.notesapp.data.local.VoiceNoteBlockDao
import com.example.notesapp.data.local.VoiceNoteBlockEntity
import com.example.notesapp.data.repository.VoiceNoteRepositoryImpl
import com.example.notesapp.data.voice.AudioFileSystem
import com.example.notesapp.domain.voice.AudioFormat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class VoiceNoteRepositoryIntegrationTest {
    private lateinit var dao: VoiceNoteBlockDao
    private lateinit var audioFileSystem: AudioFileSystem
    private lateinit var repository: VoiceNoteRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        audioFileSystem = mockk(relaxed = true)
        repository = VoiceNoteRepositoryImpl(dao, audioFileSystem)
    }

    @Test
    fun deletesAudioOnlyAndKeepsTranscriptMetadata() = runTest {
        val entity = voiceEntity(audioFilePath = "/data/user/0/app/files/voice-notes/clip.m4a")
        coEvery { dao.getByBlockId("voice-1") } returns entity
        every { audioFileSystem.delete(entity.audioFilePath!!) } returns true

        val updated = repository.deleteAudioOnly("voice-1")

        assertEquals(null, updated?.audioFilePath)
        coVerify { audioFileSystem.delete(entity.audioFilePath!!) }
        coVerify { dao.update(match { it.blockId == "voice-1" && it.audioFilePath == null }) }
        coVerify(exactly = 0) { dao.delete("voice-1") }
    }

    @Test
    fun deletesAllFilesWhenNoteIsDeleted() = runTest {
        val first = voiceEntity(blockId = "voice-1", audioFilePath = "/private/one.m4a")
        val second = voiceEntity(blockId = "voice-2", audioFilePath = "/private/two.ogg")
        coEvery { dao.getForNote("note-1") } returns listOf(first, second)
        every { audioFileSystem.delete(any()) } returns true

        repository.deleteForNote("note-1")

        coVerify { audioFileSystem.delete("/private/one.m4a") }
        coVerify { audioFileSystem.delete("/private/two.ogg") }
        coVerify { dao.deleteForNote("note-1") }
    }

    @Test
    fun missingAudioFileStillClearsNullablePath() = runTest {
        val entity = voiceEntity(audioFilePath = "/private/missing.ogg")
        coEvery { dao.getByBlockId("voice-1") } returns entity
        every { audioFileSystem.delete(entity.audioFilePath!!) } returns false

        val updated = repository.deleteAudioOnly("voice-1")

        assertNull(updated?.audioFilePath)
        coVerify { dao.update(match { it.audioFilePath == null }) }
    }

    private fun voiceEntity(
        blockId: String = "voice-1",
        audioFilePath: String? = "/private/clip.m4a"
    ): VoiceNoteBlockEntity = VoiceNoteBlockEntity(
        blockId = blockId,
        noteId = "note-1",
        audioFilePath = audioFilePath,
        audioFormat = AudioFormat.AAC.storageValue,
        durationMs = 272_000L,
        fileSizeBytes = 2_400_000L,
        sampleRateHertz = 44_100,
        channels = 1,
        createdAt = 1L,
        updatedAt = 1L
    )
}
