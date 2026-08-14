package com.example.notesapp.domain.voice

import java.util.Locale
import javax.inject.Inject

class AudioFilenameGenerator @Inject constructor() {
    fun generate(noteId: String, blockId: String, timestampMs: Long, format: AudioFormat): String {
        require(timestampMs >= 0) { "timestampMs must not be negative" }
        return "vn_${safePart(noteId)}_${safePart(blockId)}_$timestampMs.${format.fileExtension}"
    }

    private fun safePart(value: String): String {
        val normalized = value.trim().lowercase(Locale.ROOT)
            .replace(UNSAFE_CHARACTER_REGEX, "_")
            .trim('_')
        require(normalized.isNotBlank()) { "identifier must contain a safe character" }
        return normalized
    }

    private companion object {
        val UNSAFE_CHARACTER_REGEX = Regex("[^a-z0-9_-]")
    }
}
