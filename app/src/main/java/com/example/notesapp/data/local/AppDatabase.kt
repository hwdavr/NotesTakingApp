package com.example.notesapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        NoteShareEntity::class,
        VoiceNoteBlockEntity::class,
        NoteBlockCommentEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun noteShareDao(): NoteShareDao
    abstract fun voiceNoteBlockDao(): VoiceNoteBlockDao
    abstract fun noteBlockCommentDao(): NoteBlockCommentDao
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_app.db"
                ).addMigrations(MIGRATION_8_9, MIGRATION_9_10)
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

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS note_block_comments (
                        id TEXT NOT NULL PRIMARY KEY,
                        noteId TEXT NOT NULL,
                        blockId TEXT NOT NULL,
                        authorUserId TEXT NOT NULL,
                        authorDisplayName TEXT,
                        authorEmail TEXT,
                        body TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_note_block_comments_noteId " +
                        "ON note_block_comments(noteId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_note_block_comments_blockId " +
                        "ON note_block_comments(blockId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_note_block_comments_noteId_blockId " +
                        "ON note_block_comments(noteId, blockId)"
                )
            }
        }
    }
}
