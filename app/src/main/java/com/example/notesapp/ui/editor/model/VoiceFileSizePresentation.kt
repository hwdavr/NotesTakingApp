package com.example.notesapp.ui.editor.model

enum class VoiceFileSizeUnit {
    Kilobytes,
    Megabytes
}

data class VoiceFileSizePresentation(
    val unit: VoiceFileSizeUnit,
    val value: Number
)

fun voiceFileSizePresentation(fileSizeBytes: Long): VoiceFileSizePresentation =
    if (fileSizeBytes < BYTES_PER_MEGABYTE) {
        VoiceFileSizePresentation(
            unit = VoiceFileSizeUnit.Kilobytes,
            value = (fileSizeBytes / BYTES_PER_KILOBYTE).coerceAtLeast(1L)
        )
    } else {
        VoiceFileSizePresentation(
            unit = VoiceFileSizeUnit.Megabytes,
            value = fileSizeBytes.toDouble() / BYTES_PER_MEGABYTE
        )
    }

private const val BYTES_PER_KILOBYTE = 1_024L
private const val BYTES_PER_MEGABYTE = 1_024L * 1_024L
