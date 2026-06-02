package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteShareDao
import com.example.notesapp.data.remote.CreateNoteShareRequest
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.UpdateNoteShareRequest
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareException
import com.example.notesapp.domain.share.NoteShareRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

@Singleton
class NoteShareRepositoryImpl @Inject constructor(
    private val noteShareDao: NoteShareDao,
    private val api: NotesApiService
) : NoteShareRepository {
    override fun observeNoteShares(noteId: String): Flow<List<NoteShare>> =
        noteShareDao.observeByNoteId(noteId).map { shares -> shares.map { it.toDomain() } }
    override suspend fun refreshNoteShares(noteId: String) {
        val shares = api.listNoteShares(noteId)
        noteShareDao.clearByNoteId(noteId)
        noteShareDao.insertAll(shares.map { it.toEntity() })
    }
    override suspend fun inviteNoteShare(noteId: String, email: String, accessRole: NoteShareAccessRole): NoteShare {
        try {
            val created = api.createNoteShare(
                noteId,
                CreateNoteShareRequest(email = email, accessRole = accessRole.toApiValue())
            )
            val entity = created.toEntity()
            noteShareDao.insert(entity)
            return entity.toDomain()
        } catch (exception: HttpException) {
            throw if (exception.code() == 409) {
                NoteShareException.DuplicateShareException("Duplicate invite", exception)
            } else {
                NoteShareException.GenericShareException("Invite failed", exception)
            }
        } catch (exception: Exception) {
            throw NoteShareException.GenericShareException("Invite failed", exception)
        }
    }
    override suspend fun updateNoteShareRole(
        noteId: String,
        shareId: String,
        accessRole: NoteShareAccessRole
    ): NoteShare {
        val updated = api.updateNoteShare(
            noteId,
            shareId,
            UpdateNoteShareRequest(accessRole = accessRole.toApiValue())
        )
        val entity = updated.toEntity()
        noteShareDao.insert(entity)
        return entity.toDomain()
    }
    override suspend fun deleteNoteShare(noteId: String, shareId: String) {
        api.deleteNoteShare(noteId, shareId)
        noteShareDao.deleteById(shareId)
    }
}
