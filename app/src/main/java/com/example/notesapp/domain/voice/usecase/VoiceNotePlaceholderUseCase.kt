package com.example.notesapp.domain.voice.usecase

import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import java.util.UUID
import javax.inject.Inject

class VoiceNotePlaceholderUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend fun create(): Note {
        val now = System.currentTimeMillis()
        val placeholder = Note(
            id = "voice_placeholder_${UUID.randomUUID()}",
            title = "",
            content = "",
            createdAt = now,
            updatedAt = now
        )
        noteRepository.save(placeholder)
        return placeholder
    }

    suspend fun discard(noteId: String) {
        val note = noteRepository.getNoteById(noteId)
        if (note != null) {
            noteRepository.delete(note)
        }
    }
}
