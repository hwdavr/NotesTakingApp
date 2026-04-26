package com.example.notesapp.domain.note

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getActiveNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun getActiveNoteCount(): Int
    suspend fun getActiveNoteCountForFolder(folderId: String): Int
    suspend fun save(note: Note)
    suspend fun delete(note: Note)
    suspend fun sync()
}
