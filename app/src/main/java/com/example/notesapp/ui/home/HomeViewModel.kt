package com.example.notesapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.notes.NoteUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        noteRepository.getActiveNotes(),
        folderRepository.getFolders()
    ) { notes, folders ->
        HomeUiState(
            recentNotes = notes.take(5).map { note ->
                NoteUiModel(
                    id = note.id,
                    title = note.title,
                    preview = note.content,
                    isFavorite = note.isFavorite,
                    colorIndex = (note.id % 4).toInt()
                )
            },
            recentFolders = folders.take(2).mapIndexed { index, folder ->
                FolderUiModel(
                    id = folder.id,
                    name = folder.name,
                    noteCount = 0, // Simplified for now, could be improved with a proper count query
                    isPrimary = index == 1 // Just to match the existing design hint
                )
            },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )
}
