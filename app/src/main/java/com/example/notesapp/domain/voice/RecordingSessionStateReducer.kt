package com.example.notesapp.domain.voice

class RecordingSessionStateReducer {
    fun reduce(state: RecordingSessionState, event: RecordingSessionEvent): RecordingSessionState = when (event) {
        is RecordingSessionEvent.Started -> RecordingSessionState.Recording(
            metadata = event.metadata,
            elapsedMs = 0L,
            amplitudes = emptyList()
        )

        is RecordingSessionEvent.Tick -> when (state) {
            is RecordingSessionState.Recording -> state.copy(
                elapsedMs = event.elapsedMs.coerceAtLeast(0L),
                amplitudes = appendAmplitude(state.amplitudes, event.amplitude)
            )

            else -> state
        }

        RecordingSessionEvent.PauseRequested -> when (state) {
            is RecordingSessionState.Recording -> RecordingSessionState.Paused(
                metadata = state.metadata,
                elapsedMs = state.elapsedMs,
                amplitudes = state.amplitudes
            )

            else -> state
        }

        RecordingSessionEvent.ResumeRequested -> when (state) {
            is RecordingSessionState.Paused -> RecordingSessionState.Recording(
                metadata = state.metadata,
                elapsedMs = state.elapsedMs,
                amplitudes = state.amplitudes
            )

            else -> state
        }

        RecordingSessionEvent.StopRequested -> when (state) {
            is RecordingSessionState.Recording -> RecordingSessionState.Saving(
                metadata = state.metadata,
                elapsedMs = state.elapsedMs
            )

            is RecordingSessionState.Paused -> RecordingSessionState.Saving(
                metadata = state.metadata,
                elapsedMs = state.elapsedMs
            )

            else -> state
        }

        is RecordingSessionEvent.SaveCompleted -> if (state is RecordingSessionState.Saving) {
            RecordingSessionState.Saved(
                metadata = state.metadata,
                elapsedMs = state.elapsedMs,
                fileSizeBytes = event.fileSizeBytes
            )
        } else {
            state
        }

        is RecordingSessionEvent.SaveFailed -> if (state is RecordingSessionState.Saving) {
            RecordingSessionState.Error(
                message = event.message,
                metadata = state.metadata,
                elapsedMs = state.elapsedMs
            )
        } else {
            state
        }

        is RecordingSessionEvent.RecordingFailed -> when (state) {
            is RecordingSessionState.Recording -> RecordingSessionState.Error(
                message = event.message,
                metadata = state.metadata,
                elapsedMs = state.elapsedMs
            )

            is RecordingSessionState.Paused -> RecordingSessionState.Error(
                message = event.message,
                metadata = state.metadata,
                elapsedMs = state.elapsedMs
            )

            else -> state
        }

        RecordingSessionEvent.Discarded -> when (state) {
            is RecordingSessionState.Recording,
            is RecordingSessionState.Paused,
            is RecordingSessionState.Saving -> RecordingSessionState.Idle

            else -> state
        }
    }

    private fun appendAmplitude(amplitudes: List<Float>, amplitude: Float): List<Float> =
        (amplitudes + amplitude.coerceIn(0f, 1f)).takeLast(MAX_AMPLITUDE_BARS)

    private companion object {
        const val MAX_AMPLITUDE_BARS = 64
    }
}
