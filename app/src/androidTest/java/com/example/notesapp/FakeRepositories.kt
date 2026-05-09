package com.example.notesapp

import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFolderRepository(
    initialFolders: List<Folder> = emptyList()
) : FolderRepository {
    private val folders = MutableStateFlow(initialFolders)
    override fun getFolders(): Flow<List<Folder>> = folders.map { list -> list.filter { it.deletedAt == null } }
    override fun getArchivedFolders(): Flow<List<Folder>> = folders.map { list -> list.filter { it.deletedAt != null } }
    override suspend fun getArchivedFolderCount(): Int = folders.value.count { it.deletedAt != null }
    override suspend fun insert(folder: Folder) {
        folders.value = folders.value + folder
    }
    override suspend fun update(folder: Folder) {
        folders.value = folders.value.map { if (it.id == folder.id) folder else it }
    }
    override suspend fun move(folder: Folder, parentFolderId: String?) {
        folders.value = folders.value.map {
            if (it.id == folder.id) {
                it.copy(parentFolderId = parentFolderId)
            } else {
                it
            }
        }
    }
    override suspend fun delete(folder: Folder) {
        folders.value = folders.value.map {
            if (it.id == folder.id) it.copy(deletedAt = System.currentTimeMillis()) else it
        }
    }
    override suspend fun toggleFavorite(folder: Folder) {
        folders.value = folders.value.map {
            if (it.id == folder.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }
    override suspend fun sync() = Unit
}
class FakeNoteRepository(
    initialNotes: List<Note> = emptyList()
) : NoteRepository {
    private val notes = MutableStateFlow(initialNotes)
    override fun getActiveNotes(): Flow<List<Note>> = notes.map { list -> list.filter { it.deletedAt == null } }
    override fun getArchivedNotes(): Flow<List<Note>> = notes.map { list -> list.filter { it.deletedAt != null } }
    override suspend fun getNoteById(id: String): Note? = notes.value.firstOrNull { it.id == id }
    override suspend fun getActiveNoteCount(): Int = notes.value.count { it.deletedAt == null }
    override suspend fun getActiveNoteCountForFolder(folderId: String): Int =
        notes.value.count { it.folderId == folderId && it.deletedAt == null }
    override suspend fun getFavoriteNoteCount(): Int = notes.value.count { it.isFavorite && it.deletedAt == null }
    override suspend fun getArchivedNoteCount(): Int = notes.value.count { it.deletedAt != null }
    override suspend fun save(note: Note) {
        notes.value = notes.value
            .filterNot { it.id == note.id } + note
    }
    override suspend fun move(note: Note, folderId: String?) {
        save(note.copy(folderId = folderId))
    }
    override suspend fun delete(note: Note) {
        notes.value = notes.value.map {
            if (it.id == note.id) it.copy(deletedAt = System.currentTimeMillis()) else it
        }
    }
    override suspend fun toggleFavorite(note: Note) {
        notes.value = notes.value.map {
            if (it.id == note.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }
    override suspend fun sync() = Unit
}
