package com.example.notesapp.voice

import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceNotePlaceholderUseCaseTest {
    @Test
    fun createPersistsUntitledPlaceholderBeforeReturningId() = runTest {
        val repository = RecordingNoteRepository()
        val placeholder = VoiceNotePlaceholderUseCase(repository).create()

        assertTrue(placeholder.id.startsWith("voice_placeholder_"))
        assertEquals(placeholder, repository.savedNote)
        assertEquals("", placeholder.title)
        assertEquals("", placeholder.content)
    }

    @Test
    fun discardArchivesThePlaceholderAndLeavesOtherNotesUntouched() = runTest {
        val repository = RecordingNoteRepository()
        val placeholder = Note(
            id = "voice_placeholder_existing",
            title = "",
            content = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        repository.notes = listOf(placeholder)

        VoiceNotePlaceholderUseCase(repository).discard(placeholder.id)

        assertEquals(placeholder, repository.deletedNote)
    }

    private class RecordingNoteRepository : NoteRepository {
        var notes: List<Note> = emptyList()
        var savedNote: Note? = null
        var deletedNote: Note? = null

        override fun getActiveNotes(): Flow<List<Note>> = emptyFlow()
        override fun getSharedNotes(): Flow<List<Note>> = emptyFlow()
        override fun getArchivedNotes(): Flow<List<Note>> = emptyFlow()
        override suspend fun getNoteById(id: String): Note? = notes.firstOrNull { it.id == id }
        override suspend fun getActiveNoteCount(): Int = 0
        override suspend fun getActiveNoteCountForFolder(folderId: String): Int = 0
        override suspend fun getFavoriteNoteCount(): Int = 0
        override suspend fun getArchivedNoteCount(): Int = 0
        override suspend fun save(note: Note) {
            savedNote = note
            notes = notes.filterNot { it.id == note.id } + note
        }
        override suspend fun move(note: Note, folderId: String?) = Unit
        override suspend fun delete(note: Note) {
            deletedNote = note
        }
        override suspend fun toggleFavorite(note: Note) = Unit
        override suspend fun sync() = Unit
    }
}
