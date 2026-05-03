package com.example.notesapp.domain.note

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getActiveNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun getActiveNoteCount(): Int
    suspend fun getActiveNoteCountForFolder(folderId: String): Int
    suspend fun getFavoriteNoteCount(): Int
    suspend fun getArchivedNoteCount(): Int
    suspend fun save(note: Note)
    suspend fun move(note: Note, folderId: String?)
    suspend fun delete(note: Note)
    suspend fun toggleFavorite(note: Note)
    suspend fun sync()
}
