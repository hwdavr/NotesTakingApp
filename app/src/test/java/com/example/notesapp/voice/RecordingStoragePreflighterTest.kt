package com.example.notesapp.voice

import com.example.notesapp.domain.voice.MINIMUM_RECORDING_FREE_BYTES
import com.example.notesapp.domain.voice.RecordingStoragePreflighter
import com.example.notesapp.domain.voice.StorageInfoProvider
import com.example.notesapp.domain.voice.StoragePreflightResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingStoragePreflighterTest {
    @Test
    fun `allows recording at exactly the 128 MB threshold`() {
        val result = RecordingStoragePreflighter(
            storageInfoProvider = FixedStorageInfoProvider(MINIMUM_RECORDING_FREE_BYTES)
        ).check()

        assertTrue(result is StoragePreflightResult.Available)
    }

    @Test
    fun blocksRecordingBelowThreshold() {
        val result = RecordingStoragePreflighter(
            storageInfoProvider = FixedStorageInfoProvider(127L * 1024L * 1024L)
        ).check()

        assertEquals(
            StoragePreflightResult.Insufficient(
                availableBytes = 127L * 1024L * 1024L,
                minimumFreeBytes = MINIMUM_RECORDING_FREE_BYTES
            ),
            result
        )
    }

    @Test
    fun `preserves available byte count for diagnostics`() {
        val availableBytes = 50L * 1024L * 1024L

        val result = RecordingStoragePreflighter(
            storageInfoProvider = FixedStorageInfoProvider(availableBytes)
        ).check()

        assertEquals(availableBytes, (result as StoragePreflightResult.Insufficient).availableBytes)
    }

    private class FixedStorageInfoProvider(
        private val bytes: Long
    ) : StorageInfoProvider {
        override fun availableBytes(): Long = bytes
    }
}
