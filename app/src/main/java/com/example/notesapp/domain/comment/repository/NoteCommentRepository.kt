package com.example.notesapp.domain.comment.repository

import com.example.notesapp.domain.comment.model.NoteBlockComment
import kotlinx.coroutines.flow.Flow

interface NoteCommentRepository {
    fun observeComments(noteId: String, blockId: String): Flow<List<NoteBlockComment>>
    suspend fun refreshComments(noteId: String, blockId: String)
    suspend fun addComment(noteId: String, blockId: String, body: String): NoteBlockComment
}
