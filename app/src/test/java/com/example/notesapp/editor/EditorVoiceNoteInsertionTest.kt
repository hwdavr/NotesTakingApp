package com.example.notesapp.editor

import com.example.notesapp.data.repository.JsonVoiceNoteDocumentStore
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.VoiceNoteBlock
import com.example.notesapp.domain.voice.VoiceNoteRepository
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.domain.voice.usecase.SaveVoiceNoteRecordingUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorVoiceNoteInsertionTest {
    @Test
    fun savesEditorRecordingAtFocusedPositionWithEditableTranscript() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>(relaxed = true)
        val existing = Note(
            id = "note-editor",
            title = "Meeting",
            content = NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(id = "focus", children = listOf(RichText("Before"))),
                    EditorBlock.TextBlock(id = "after", children = listOf(RichText("After")))
                )
            ).toJsonString(),
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { repository.getNoteById("note-editor") } returns existing
        coEvery { repository.save(any()) } returns Unit
        val useCase = SaveVoiceNoteRecordingUseCase(
            noteRepository = repository,
            voiceNoteRepository = voiceRepository,
            documentStore = JsonVoiceNoteDocumentStore()
        )

        useCase(
            noteId = "note-editor",
            blockId = "voice-editor",
            audioFilePath = "/private/vn_note-editor_voice-editor_1.m4a",
            audioFormat = AudioFormat.AAC,
            durationMs = 272_000L,
            fileSizeBytes = 2_400_000L,
            transcript = "Recorded transcript",
            focusedBlockId = "focus"
        )

        coVerify {
            repository.save(
                match { saved ->
                    val blocks = NoteDocument.fromContent(saved.content).blocks
                    val voiceBlock = blocks.getOrNull(1) as? EditorBlock.Voice
                    blocks[0].id == "focus" &&
                        voiceBlock?.audioFilePath == "/private/vn_note-editor_voice-editor_1.m4a" &&
                        blocks[2] is EditorBlock.TextBlock &&
                        (blocks[2] as EditorBlock.TextBlock).text() == "Recorded transcript"
                }
            )
            voiceRepository.upsert(match { it.blockId == "voice-editor" && it.noteId == "note-editor" })
        }
    }

    @Test
    fun savesHomeRecordingAtDocumentPositionZero() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>(relaxed = true)
        val placeholder = Note(
            id = "voice-placeholder",
            title = "",
            content = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { repository.getNoteById("voice-placeholder") } returns placeholder
        coEvery { repository.save(any()) } returns Unit
        val useCase = SaveVoiceNoteRecordingUseCase(repository, voiceRepository, JsonVoiceNoteDocumentStore())

        useCase(
            noteId = placeholder.id,
            blockId = "voice-home",
            audioFilePath = "/private/vn_voice-placeholder_voice-home_1.ogg",
            audioFormat = AudioFormat.OPUS,
            durationMs = 45_000L,
            fileSizeBytes = 64_000L,
            transcript = "Home transcript",
            focusedBlockId = null
        )

        coVerify {
            repository.save(
                match { saved ->
                    val blocks = NoteDocument.fromContent(saved.content).blocks
                    blocks.first() is EditorBlock.Voice &&
                        blocks[1] is EditorBlock.TextBlock &&
                        (blocks[1] as EditorBlock.TextBlock).text() == "Home transcript"
                }
            )
            voiceRepository.upsert(
                match {
                    it.audioFormat == AudioFormat.OPUS && it.audioFilePath?.endsWith(".ogg") == true
                }
            )
        }
    }

    @Test
    fun createsTheNoteSourceWhenHomePlaceholderIsMissing() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>(relaxed = true)
        coEvery { repository.getNoteById("new-home-note") } returns null
        coEvery { repository.save(any()) } returns Unit

        SaveVoiceNoteRecordingUseCase(repository, voiceRepository, JsonVoiceNoteDocumentStore())(
            noteId = "new-home-note",
            blockId = "voice-new-home",
            audioFilePath = "/private/voice.m4a",
            audioFormat = AudioFormat.AAC,
            durationMs = 1_000L,
            fileSizeBytes = 100L,
            transcript = "New note transcript",
            focusedBlockId = null
        )

        coVerify {
            repository.save(match { it.id == "new-home-note" && it.title.isEmpty() })
        }
        coVerify { voiceRepository.upsert(match { it.blockId == "voice-new-home" }) }
    }

    @Test
    fun deletesAudioAndKeepsTranscriptInDocument() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>()
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.Voice(
                    blockId = "voice-delete",
                    audioFilePath = "/private/voice.m4a",
                    audioFormat = AudioFormat.AAC,
                    durationMs = 1_000L,
                    fileSizeBytes = 100L,
                    sampleRateHertz = 44_100,
                    channels = 1,
                    createdAt = 1L,
                    updatedAt = 1L
                ),
                EditorBlock.TextBlock(
                    id = "transcript-delete",
                    children = listOf(RichText("Keep this transcript"))
                )
            )
        )
        val note = Note(
            id = "note-delete",
            title = "Voice",
            content = document.toJsonString(),
            createdAt = 1L,
            updatedAt = 1L
        )
        val updatedBlock = VoiceNoteBlock(
            blockId = "voice-delete",
            noteId = note.id,
            audioFilePath = null,
            audioFormat = AudioFormat.AAC,
            durationMs = 1_000L,
            fileSizeBytes = 100L,
            sampleRateHertz = 44_100,
            channels = 1,
            createdAt = 1L,
            updatedAt = 2L
        )
        coEvery { repository.getNoteById(note.id) } returns note
        coEvery { repository.save(any()) } returns Unit
        coEvery { voiceRepository.deleteAudioOnly("voice-delete") } returns updatedBlock

        val updatedContent = DeleteVoiceNoteAudioUseCase(
            noteRepository = repository,
            voiceNoteRepository = voiceRepository,
            documentStore = JsonVoiceNoteDocumentStore()
        )(note.id, "voice-delete")

        val updatedDocument = updatedContent?.let(NoteDocument::fromContent)
        val voiceBlock = updatedDocument?.blocks?.firstOrNull() as? EditorBlock.Voice
        val transcriptBlock = updatedDocument?.blocks?.getOrNull(1) as? EditorBlock.TextBlock
        assertNotNull(voiceBlock)
        assertNull(voiceBlock?.audioFilePath)
        assertEquals("Keep this transcript", transcriptBlock?.text())
        coVerify { repository.save(match { it.content == updatedContent }) }
    }

    @Test
    fun deletingAudioForMissingNoteDoesNotWriteDocument() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>()
        coEvery { repository.getNoteById("missing-note") } returns null
        coEvery { voiceRepository.deleteAudioOnly("missing-voice") } returns null

        val result = DeleteVoiceNoteAudioUseCase(
            repository,
            voiceRepository,
            JsonVoiceNoteDocumentStore()
        )("missing-note", "missing-voice")

        assertNull(result)
        coVerify { voiceRepository.deleteAudioOnly("missing-voice") }
        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun restoresDocumentWhenAudioMetadataDeletionFails() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>()
        val note = Note(
            id = "note-delete-failure",
            title = "Voice",
            content = NoteDocument.empty().toJsonString(),
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { repository.getNoteById(note.id) } returns note
        coEvery { repository.save(any()) } returns Unit
        coEvery { voiceRepository.deleteAudioOnly("voice-delete-failure") } throws
            IllegalStateException("metadata delete failed")

        val result = runCatching {
            DeleteVoiceNoteAudioUseCase(
                repository,
                voiceRepository,
                JsonVoiceNoteDocumentStore()
            )(note.id, "voice-delete-failure")
        }

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { repository.save(note) }
    }

    @Test
    fun deletesVoiceBlockThroughRepositoryUseCase() = runTest {
        val voiceRepository = mockk<VoiceNoteRepository>(relaxed = true)

        DeleteVoiceNoteBlockUseCase(voiceRepository)("voice-block")

        coVerify { voiceRepository.deleteBlock("voice-block") }
    }

    @Test
    fun rollsBackDocumentWhenVoiceMetadataPersistenceFails() = runTest {
        val repository = mockk<NoteRepository>()
        val voiceRepository = mockk<VoiceNoteRepository>()
        val existing = Note(
            id = "note-rollback",
            title = "Rollback",
            content = NoteDocument.empty().toJsonString(),
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { repository.getNoteById(existing.id) } returns existing
        coEvery { repository.save(any()) } returns Unit
        coEvery { voiceRepository.upsert(any()) } throws IllegalStateException("metadata write failed")
        coEvery { voiceRepository.deleteBlock("voice-rollback") } returns Unit

        val result = runCatching {
            SaveVoiceNoteRecordingUseCase(
                repository,
                voiceRepository,
                JsonVoiceNoteDocumentStore()
            )(
                noteId = existing.id,
                blockId = "voice-rollback",
                audioFilePath = "/private/voice.m4a",
                audioFormat = AudioFormat.AAC,
                durationMs = 1_000L,
                fileSizeBytes = 100L,
                transcript = "Transcript",
                focusedBlockId = null
            )
        }

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { repository.save(existing) }
        coVerify { voiceRepository.deleteBlock("voice-rollback") }
    }
}
