package com.example.notesapp.data.repository

import com.example.notesapp.auth.AuthManager
import com.example.notesapp.data.local.NoteBlockCommentDao
import com.example.notesapp.data.local.NoteBlockCommentEntity
import com.example.notesapp.data.remote.CreateNoteBlockCommentRequest
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.domain.comment.repository.NoteCommentRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class NoteCommentRepositoryImpl @Inject constructor(
    private val noteBlockCommentDao: NoteBlockCommentDao,
    private val api: NotesApiService,
    private val authManager: AuthManager
) : NoteCommentRepository {

    override fun observeComments(noteId: String, blockId: String): Flow<List<NoteBlockComment>> {
        return noteBlockCommentDao.observeComments(noteId, blockId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshComments(noteId: String, blockId: String) {
        val comments = api.listNoteBlockComments(noteId, blockId)
        noteBlockCommentDao.clearComments(noteId, blockId)
        noteBlockCommentDao.insertAll(comments.map { it.toEntity() })
    }

    override suspend fun addComment(noteId: String, blockId: String, body: String): NoteBlockComment {
        return try {
            val created = api.createNoteBlockComment(
                noteId,
                blockId,
                CreateNoteBlockCommentRequest(body = body)
            )
            val entity = created.toEntity()
            noteBlockCommentDao.insert(entity)
            entity.toDomain()
        } catch (_: Exception) {
            val localEmail = authManager.profileEmail.value ?: "me@example.com"
            val localDisplayName = localEmail.substringBefore("@")
            val localEntity = NoteBlockCommentEntity(
                id = "comment_" + UUID.randomUUID().toString(),
                noteId = noteId,
                blockId = blockId,
                authorUserId = "local_user",
                authorDisplayName = localDisplayName,
                authorEmail = localEmail,
                body = body,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteBlockCommentDao.insert(localEntity)
            localEntity.toDomain()
        }
    }
}
