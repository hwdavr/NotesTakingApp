package com.example.notesapp.domain.note

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getActiveNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    fun getFavoriteNotes(): Flow<List<Note>>
    suspend fun getActiveNoteCount(): Int
    suspend fun getFavoriteCount(): Int
    suspend fun getArchivedCount(): Int
    suspend fun getActiveNoteCountForFolder(folderId: Long): Int
    suspend fun save(note: Note): Long
    suspend fun delete(note: Note)
}
