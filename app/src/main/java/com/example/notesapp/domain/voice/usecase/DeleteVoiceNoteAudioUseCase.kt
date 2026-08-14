package com.example.notesapp.domain.voice.usecase

import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.voice.VoiceNoteDocumentStore
import com.example.notesapp.domain.voice.VoiceNoteRepository
import javax.inject.Inject

class DeleteVoiceNoteAudioUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val voiceNoteRepository: VoiceNoteRepository,
    private val documentStore: VoiceNoteDocumentStore
) {
    suspend operator fun invoke(noteId: String, blockId: String): String? {
        val note = noteRepository.getNoteById(noteId)
        val updatedContent = note?.let {
            documentStore.updateAudioFilePath(it.content, blockId, audioFilePath = null)
        }
        voiceNoteRepository.deleteAudioOnly(blockId)
        if (note != null && updatedContent != null) {
            noteRepository.save(note.copy(content = updatedContent, updatedAt = System.currentTimeMillis()))
        }
        return updatedContent
    }
}
