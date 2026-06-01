package com.example.notesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteBlockCommentDao {
    @Query("SELECT * FROM note_block_comments WHERE noteId = :noteId AND blockId = :blockId ORDER BY createdAt ASC")
    fun observeComments(noteId: String, blockId: String): Flow<List<NoteBlockCommentEntity>>

    @Query("SELECT * FROM note_block_comments WHERE noteId = :noteId AND blockId = :blockId ORDER BY createdAt ASC")
    suspend fun getComments(noteId: String, blockId: String): List<NoteBlockCommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: NoteBlockCommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<NoteBlockCommentEntity>)

    @Query("DELETE FROM note_block_comments WHERE noteId = :noteId AND blockId = :blockId")
    suspend fun clearComments(noteId: String, blockId: String)
}
