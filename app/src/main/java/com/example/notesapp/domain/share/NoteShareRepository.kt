package com.example.notesapp.domain.share

import kotlinx.coroutines.flow.Flow

interface NoteShareRepository {
    fun observeNoteShares(noteId: String): Flow<List<NoteShare>>
    suspend fun refreshNoteShares(noteId: String)
    suspend fun inviteNoteShare(noteId: String, email: String, accessRole: NoteShareAccessRole): NoteShare
    suspend fun updateNoteShareRole(noteId: String, shareId: String, accessRole: NoteShareAccessRole): NoteShare
    suspend fun deleteNoteShare(noteId: String, shareId: String)
}
