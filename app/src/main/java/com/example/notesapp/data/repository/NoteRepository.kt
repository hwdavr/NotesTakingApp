package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getActiveNotes(): Flow<List<NoteEntity>> = noteDao.getActiveNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun getFavoriteNotes(): Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun getActiveNoteCount(): Int = noteDao.getActiveNoteCount()

    suspend fun getFavoriteCount(): Int = noteDao.getFavoriteCount()

    suspend fun getArchivedCount(): Int = noteDao.getArchivedCount()

    suspend fun getActiveNoteCountForFolder(folderId: Long): Int = noteDao.getActiveNoteCountForFolder(folderId)

    suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)
}
