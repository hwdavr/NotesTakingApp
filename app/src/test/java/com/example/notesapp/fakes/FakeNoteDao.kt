package com.example.notesapp.fakes

import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteDao : NoteDao {
    val notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())
    override fun getActiveNotes() = notesFlow.map { it.filter { note -> note.deletedAt == null && !note.isShared } }
    override fun getSharedNotes() = notesFlow.map { it.filter { note -> note.deletedAt == null && note.isShared } }
    override fun getArchivedNotes() = notesFlow.map { it.filter { note -> note.deletedAt != null } }
    override suspend fun getNoteById(id: String) = notesFlow.value.find { it.id == id }
    override fun getNotesByFolder(folderId: String) =
        notesFlow.map { list -> list.filter { it.folderId == folderId && it.deletedAt == null } }
    override suspend fun getActiveNoteCountForFolder(folderId: String) =
        notesFlow.value.count { it.folderId == folderId && it.deletedAt == null }
    override suspend fun getActiveNoteCount() = notesFlow.value.count { it.deletedAt == null }
    override suspend fun getFavoriteNoteCount() = notesFlow.value.count { it.isFavorite && it.deletedAt == null }
    override suspend fun getArchivedNoteCount() = notesFlow.value.count { it.deletedAt != null }
    override fun searchNotes(query: String) = notesFlow.map { list -> list.filter { it.title.contains(query) } }
    override suspend fun getNoteCount() = notesFlow.value.size
    override suspend fun insert(note: NoteEntity) {
        val newList = notesFlow.value.toMutableList()
        newList.removeIf { it.id == note.id }
        newList.add(note)
        notesFlow.value = newList
    }
    override suspend fun insertAll(newNotes: List<NoteEntity>) {
        val newList = notesFlow.value.toMutableList()
        newNotes.forEach { n ->
            newList.removeIf { it.id == n.id }
            newList.add(n)
        }
        notesFlow.value = newList
    }
    override suspend fun update(note: NoteEntity) {
        insert(note)
    }
    override suspend fun clearAll() {
        notesFlow.value = emptyList()
    }
}
