package com.example.notesapp.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class CollectionItemUiModel {
    data class FolderItem(
        val id: String,
        val name: String,
        val noteCount: Int
    ) : CollectionItemUiModel()

    data class NoteItem(
        val note: NoteUiModel
    ) : CollectionItemUiModel()
}

data class CollectionNotesUiState(
    val isLoading: Boolean = false,
    val label: String = "",
    val type: String = "all",
    val folderId: String? = null,
    val items: List<CollectionItemUiModel> = emptyList()
)

@HiltViewModel
open class CollectionNotesViewModel @Inject constructor(
    folderRepository: FolderRepository,
    noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val type: String = savedStateHandle["type"] ?: "all"
    private val folderId: String? = savedStateHandle.get<String>("folderId")?.ifBlank { null }
    private val label: String = savedStateHandle["label"] ?: defaultLabel(type)

    open val uiState: StateFlow<CollectionNotesUiState> = combine(
        folderRepository.getFolders(),
        noteRepository.getActiveNotes()
    ) { folders, notes ->
            val items = when (type) {
                "folder" -> buildFolderCollectionItems(
                    folders = folders,
                    notes = notes,
                    parentFolderId = folderId
                )
                "favorites" -> {
                    val favFolders = folders.filter { it.isFavorite }.map { folder ->
                        CollectionItemUiModel.FolderItem(
                            id = folder.id,
                            name = folder.name,
                            noteCount = notes.count { it.folderId == folder.id }
                        )
                    }
                    val favNotes = notes.filter { it.isFavorite }.map { note ->
                        CollectionItemUiModel.NoteItem(note = note.toUiModel())
                    }
                    favFolders + favNotes
                }
                "archive" -> emptyList()
                else -> notes.map { note ->
                    CollectionItemUiModel.NoteItem(
                        note = note.toUiModel()
                    )
                }
            }

            CollectionNotesUiState(
                isLoading = false,
                label = label,
                type = type,
                folderId = folderId,
                items = items
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CollectionNotesUiState(isLoading = true)
        )
}

private fun buildFolderCollectionItems(
    folders: List<Folder>,
    notes: List<Note>,
    parentFolderId: String?
): List<CollectionItemUiModel> {
    val childFolders = folders
        .filter { it.parentFolderId == parentFolderId }
        .map { folder ->
            CollectionItemUiModel.FolderItem(
                id = folder.id,
                name = folder.name,
                noteCount = notes.count { it.folderId == folder.id }
            )
        }

    val childNotes = notes
        .filter { it.folderId == parentFolderId }
        .map { note ->
            CollectionItemUiModel.NoteItem(note = note.toUiModel())
        }

    return childFolders + childNotes
}

private fun Note.toUiModel(): NoteUiModel =
    NoteUiModel(
        id = id,
        title = title,
        preview = content,
        colorIndex = id.hashCode().mod(4).let { if (it < 0) it + 4 else it }
    )

private fun defaultLabel(type: String): String = when (type) {
    "favorites" -> "Favorites"
    "archive" -> "Archive"
    else -> "All Notes"
}
