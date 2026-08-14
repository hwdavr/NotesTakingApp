package com.example.notesapp.data.voice

import com.example.notesapp.domain.voice.RecordingSessionState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class RecordingStateStore @Inject constructor() {
    private val mutableState = MutableStateFlow<RecordingSessionState>(RecordingSessionState.Idle)
    val state: StateFlow<RecordingSessionState> = mutableState.asStateFlow()

    fun update(state: RecordingSessionState) {
        mutableState.value = state
    }
}
