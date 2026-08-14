package com.example.notesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VoiceNoteBlockDao {
    @Query("SELECT * FROM voice_note_blocks WHERE noteId = :noteId ORDER BY createdAt ASC")
    suspend fun getForNote(noteId: String): List<VoiceNoteBlockEntity>

    @Query("SELECT * FROM voice_note_blocks WHERE blockId = :blockId LIMIT 1")
    suspend fun getByBlockId(blockId: String): VoiceNoteBlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(block: VoiceNoteBlockEntity)

    @Update
    suspend fun update(block: VoiceNoteBlockEntity)

    @Query("DELETE FROM voice_note_blocks WHERE blockId = :blockId")
    suspend fun delete(blockId: String)

    @Query("DELETE FROM voice_note_blocks WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: String)
}
