package com.example.notesapp.fakes

import com.example.notesapp.data.local.NoteShareDao
import com.example.notesapp.data.local.NoteShareEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteShareDao : NoteShareDao {
    private val sharesFlow = MutableStateFlow<List<NoteShareEntity>>(emptyList())
    override fun observeByNoteId(noteId: String) =
        sharesFlow.map { shares -> shares.filter { it.noteId == noteId }.sortedBy { it.updatedAt } }
    override suspend fun insert(noteShare: NoteShareEntity) {
        val newList = sharesFlow.value.toMutableList()
        newList.removeIf { it.id == noteShare.id }
        newList.add(noteShare)
        sharesFlow.value = newList
    }
    override suspend fun insertAll(noteShares: List<NoteShareEntity>) {
        val newList = sharesFlow.value.toMutableList()
        noteShares.forEach { share ->
            newList.removeIf { it.id == share.id }
            newList.add(share)
        }
        sharesFlow.value = newList
    }
    override suspend fun clearByNoteId(noteId: String) {
        sharesFlow.value = sharesFlow.value.filterNot { it.noteId == noteId }
    }
    override suspend fun deleteById(shareId: String) {
        sharesFlow.value = sharesFlow.value.filterNot { it.id == shareId }
    }
}
