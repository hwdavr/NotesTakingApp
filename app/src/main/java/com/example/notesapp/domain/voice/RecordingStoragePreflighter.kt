package com.example.notesapp.domain.voice

import javax.inject.Inject

const val MINIMUM_RECORDING_FREE_BYTES: Long = 128L * 1024L * 1024L

interface StorageInfoProvider {
    fun availableBytes(): Long
}

class RecordingStoragePreflighter @Inject constructor(
    private val storageInfoProvider: StorageInfoProvider
) {
    fun check(): StoragePreflightResult {
        val availableBytes = storageInfoProvider.availableBytes()
        return if (availableBytes >= MINIMUM_RECORDING_FREE_BYTES) {
            StoragePreflightResult.Available(availableBytes)
        } else {
            StoragePreflightResult.Insufficient(availableBytes, MINIMUM_RECORDING_FREE_BYTES)
        }
    }
}

sealed interface StoragePreflightResult {
    data class Available(val availableBytes: Long) : StoragePreflightResult

    data class Insufficient(
        val availableBytes: Long,
        val minimumFreeBytes: Long
    ) : StoragePreflightResult
}
