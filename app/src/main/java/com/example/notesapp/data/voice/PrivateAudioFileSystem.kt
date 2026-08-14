package com.example.notesapp.data.voice

import android.content.Context
import com.example.notesapp.domain.voice.AudioFilenameGenerator
import com.example.notesapp.domain.voice.AudioFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface AudioFileSystem {
    fun createRecordingFile(noteId: String, blockId: String, format: AudioFormat): File

    fun delete(path: String): Boolean

    fun fileSize(path: String): Long
}

@Singleton
class PrivateAudioFileSystem @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filenameGenerator: AudioFilenameGenerator
) : AudioFileSystem {
    override fun createRecordingFile(noteId: String, blockId: String, format: AudioFormat): File {
        val directory = File(context.filesDir, VOICE_NOTES_DIRECTORY).apply { mkdirs() }
        val filename = filenameGenerator.generate(
            noteId = noteId,
            blockId = blockId,
            timestampMs = System.currentTimeMillis(),
            format = format
        )
        return File(directory, filename)
    }

    override fun delete(path: String): Boolean {
        val file = File(path)
        if (!file.isFile) return false
        return file.delete()
    }

    override fun fileSize(path: String): Long = File(path).takeIf(File::isFile)?.length() ?: 0L

    companion object {
        const val VOICE_NOTES_DIRECTORY = "voice-notes"
    }
}
