package com.example.notesapp.domain.voice

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class RecordingSessionToken(
    val value: String = UUID.randomUUID().toString()
)

data class ActiveRecordingSession(
    val token: RecordingSessionToken,
    val metadata: RecordingSessionMetadata
)

@Singleton
class RecordingSessionManager @Inject constructor() {
    private var activeSession: ActiveRecordingSession? = null

    @Synchronized
    fun replace(
        metadata: RecordingSessionMetadata,
        discardActive: (ActiveRecordingSession) -> Unit
    ): ActiveRecordingSession {
        val previous = activeSession
        previous?.let(discardActive)
        return ActiveRecordingSession(
            token = RecordingSessionToken(),
            metadata = metadata
        ).also { activeSession = it }
    }

    @Synchronized
    fun clear(token: RecordingSessionToken) {
        if (activeSession?.token == token) {
            activeSession = null
        }
    }

    @Synchronized
    fun current(): ActiveRecordingSession? = activeSession
}
