package com.example.notesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteShareDao {
    @Query("SELECT * FROM note_shares WHERE noteId = :noteId ORDER BY updatedAt ASC")
    fun observeByNoteId(noteId: String): Flow<List<NoteShareEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteShare: NoteShareEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(noteShares: List<NoteShareEntity>)
    @Query("DELETE FROM note_shares WHERE noteId = :noteId")
    suspend fun clearByNoteId(noteId: String)
    @Query("DELETE FROM note_shares WHERE id = :shareId")
    suspend fun deleteById(shareId: String)
}
