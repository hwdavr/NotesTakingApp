package com.example.notesapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class, FolderEntity::class, NoteShareEntity::class, VoiceNoteBlockEntity::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun noteShareDao(): NoteShareDao
    abstract fun voiceNoteBlockDao(): VoiceNoteBlockDao
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_app.db"
                ).addMigrations(MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS voice_note_blocks (
                        blockId TEXT NOT NULL PRIMARY KEY,
                        noteId TEXT NOT NULL,
                        audioFilePath TEXT,
                        audioFormat TEXT NOT NULL,
                        durationMs INTEGER NOT NULL,
                        fileSizeBytes INTEGER NOT NULL,
                        sampleRateHertz INTEGER NOT NULL,
                        channels INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(noteId) REFERENCES notes(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_voice_note_blocks_noteId " +
                        "ON voice_note_blocks(noteId)"
                )
            }
        }
    }
}
