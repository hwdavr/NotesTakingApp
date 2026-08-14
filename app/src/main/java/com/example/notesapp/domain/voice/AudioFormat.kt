package com.example.notesapp.domain.voice

enum class AudioFormat(
    val fileExtension: String,
    val storageValue: String
) {
    AAC(fileExtension = "m4a", storageValue = "aac"),
    OPUS(fileExtension = "ogg", storageValue = "opus");

    companion object {
        fun fromStorageValue(value: String): AudioFormat = entries.firstOrNull { it.storageValue == value } ?: AAC
    }
}
