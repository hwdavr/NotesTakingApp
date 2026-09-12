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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class NoteCommentRepositoryImpl @Inject constructor(
    private val noteBlockCommentDao: NoteBlockCommentDao,
    private val api: NotesApiService,
    private val authManager: AuthManager
) : NoteCommentRepository {

    override fun observeComments(noteId: String, blockId: String): Flow<List<NoteBlockComment>> =
        noteBlockCommentDao.observeComments(noteId, blockId).map { comments ->
            comments.map { it.toDomain() }
        }

    override suspend fun refreshComments(noteId: String, blockId: String) {
        val comments = api.listNoteBlockComments(noteId, blockId)
        noteBlockCommentDao.clearComments(noteId, blockId)
        noteBlockCommentDao.insertAll(comments.map { it.toEntity() })
    }

    override suspend fun addComment(noteId: String, blockId: String, body: String): NoteBlockComment {
        return try {
            val created = api.createNoteBlockComment(
                itemId = noteId,
                blockId = blockId,
                request = CreateNoteBlockCommentRequest(body = body)
            )
            val entity = created.toEntity()
            noteBlockCommentDao.insert(entity)
            entity.toDomain()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            val email = authManager.profileEmail.value
            val displayName = email?.substringBefore('@')
            val timestamp = System.currentTimeMillis()
            val localEntity = NoteBlockCommentEntity(
                id = "comment_${UUID.randomUUID()}",
                noteId = noteId,
                blockId = blockId,
                authorUserId = email ?: "local_user",
                authorDisplayName = displayName,
                authorEmail = email,
                body = body,
                createdAt = timestamp,
                updatedAt = timestamp
            )
            noteBlockCommentDao.insert(localEntity)
            localEntity.toDomain()
        }
    }
}
