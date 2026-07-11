package com.example.notesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE deletedAt IS NULL ORDER BY sortKey ASC, name ASC")
    fun getFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    fun getFolder(id: String): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC, updatedAt DESC")
    fun getArchivedFolders(): Flow<List<FolderEntity>>

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int

    @Query("SELECT COUNT(*) FROM folders WHERE deletedAt IS NOT NULL")
    suspend fun getArchivedFolderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    @Query("DELETE FROM folders")
    suspend fun clearAll()
}
