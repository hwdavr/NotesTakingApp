package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getActiveNotes(): Flow<List<Note>> =
        noteDao.getActiveNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override fun getFavoriteNotes(): Flow<List<Note>> =
        noteDao.getFavoriteNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveNoteCount(): Int = noteDao.getActiveNoteCount()

    override suspend fun getFavoriteCount(): Int = noteDao.getFavoriteCount()

    override suspend fun getArchivedCount(): Int = noteDao.getArchivedCount()

    override suspend fun getActiveNoteCountForFolder(folderId: Long): Int =
        noteDao.getActiveNoteCountForFolder(folderId)

    override suspend fun save(note: Note): Long = noteDao.insert(note.toEntity())

    override suspend fun delete(note: Note) = noteDao.delete(note.toEntity())
}
