package com.example.notesapp.ui.voice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class VoiceEntryViewModel @Inject constructor(
    private val voiceNotePlaceholderUseCase: VoiceNotePlaceholderUseCase
) : ViewModel() {
    fun createHomePlaceholder(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            onCreated(voiceNotePlaceholderUseCase.create().id)
        }
    }
}
