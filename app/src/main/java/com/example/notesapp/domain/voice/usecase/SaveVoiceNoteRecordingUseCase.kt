package com.example.notesapp.domain.voice.usecase

import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.VoiceNoteBlock
import com.example.notesapp.domain.voice.VoiceNoteDocumentInsertion
import com.example.notesapp.domain.voice.VoiceNoteDocumentStore
import com.example.notesapp.domain.voice.VoiceNoteRepository
import javax.inject.Inject

class SaveVoiceNoteRecordingUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val voiceNoteRepository: VoiceNoteRepository,
    private val documentStore: VoiceNoteDocumentStore
) {
    suspend operator fun invoke(
        noteId: String,
        blockId: String,
        audioFilePath: String,
        audioFormat: AudioFormat,
        durationMs: Long,
        fileSizeBytes: Long,
        transcript: String,
        focusedBlockId: String?,
        createdAt: Long = System.currentTimeMillis()
    ) {
        val now = System.currentTimeMillis()
        val block = VoiceNoteBlock(
            blockId = blockId,
            noteId = noteId,
            audioFilePath = audioFilePath,
            audioFormat = audioFormat,
            durationMs = durationMs,
            fileSizeBytes = fileSizeBytes,
            sampleRateHertz = if (audioFormat == AudioFormat.OPUS) 16_000 else 44_100,
            channels = 1,
            createdAt = createdAt,
            updatedAt = now
        )
        val existing = noteRepository.getNoteById(noteId)
        val source = existing ?: Note(
            id = noteId,
            title = "",
            content = "",
            createdAt = createdAt,
            updatedAt = createdAt
        )
        val updatedNote = source.copy(
            content = documentStore.insertVoiceNote(
                content = source.content,
                insertion = VoiceNoteDocumentInsertion(
                    block = block,
                    transcript = transcript,
                    focusedBlockId = focusedBlockId
                )
            ),
            updatedAt = now
        )
        noteRepository.save(updatedNote)
        voiceNoteRepository.upsert(block)
    }
}
