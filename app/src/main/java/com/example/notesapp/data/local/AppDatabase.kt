package com.example.notesapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, FolderEntity::class, NoteShareEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun noteShareDao(): NoteShareDao
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_app.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
