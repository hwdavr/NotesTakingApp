package com.example.notesapp.ui.settings.model

enum class VoiceStorageSizeUnit {
    Zero,
    Kilobytes,
    Megabytes,
    Gigabytes
}

data class VoiceStorageSizePresentation(
    val unit: VoiceStorageSizeUnit,
    val value: Number = 0L
)

fun voiceStorageSizePresentation(totalBytes: Long): VoiceStorageSizePresentation = when {
    totalBytes <= 0L -> VoiceStorageSizePresentation(VoiceStorageSizeUnit.Zero)
    totalBytes < BYTES_PER_MEGABYTE -> VoiceStorageSizePresentation(
        VoiceStorageSizeUnit.Kilobytes,
        totalBytes / BYTES_PER_KILOBYTE
    )
    totalBytes < BYTES_PER_GIGABYTE -> VoiceStorageSizePresentation(
        VoiceStorageSizeUnit.Megabytes,
        totalBytes.toDouble() / BYTES_PER_MEGABYTE
    )
    else -> VoiceStorageSizePresentation(
        VoiceStorageSizeUnit.Gigabytes,
        totalBytes.toDouble() / BYTES_PER_GIGABYTE
    )
}

private const val BYTES_PER_KILOBYTE = 1_000L
private const val BYTES_PER_MEGABYTE = 1_000_000L
private const val BYTES_PER_GIGABYTE = 1_000_000_000L
