package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.domain.summary.NoteSummaryResult
import java.util.UUID
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal fun NoteEditorViewModel.observeLinkTargetChanges() {
    viewModelScope.launch {
        try {
            combine(
                noteRepository.getActiveNotes(),
                noteRepository.getArchivedNotes()
            ) { active, archived ->
                val activeIds = active.map { it.id }.toSet()
                val deletedIds = archived.map { it.id }.toSet()
                Pair(activeIds, deletedIds)
            }.collect { (activeIds, deletedIds) ->
                val current = uiStateInternal.value
                if (current.isLoaded && current.document.hasLinkAnnotations()) {
                    val resolved = current.document.resolveLinks(activeIds, deletedIds)
                    if (resolved != current.document) {
                        uiStateInternal.value = current.copy(document = resolved)
                        scheduleAutoSave()
                    }
                }
            }
        } catch (_: Throwable) {
            // Ignore if repository flows are unmocked or empty
        }
    }
}

internal suspend fun NoteEditorViewModel.saveInternally() {
    val current = uiStateInternal.value
    if (!current.isEditable && current.createdAt != 0L) return
    if (current.title.isBlank() && current.content.isBlank()) return
    val now = System.currentTimeMillis()
    val noteId = current.noteId ?: "note_${UUID.randomUUID()}"
    val note = Note(
        id = noteId,
        title = current.title.ifBlank { "Untitled note" },
        content = current.document.toJsonString(),
        folderId = current.folderId,
        sortKey = now.toString(),
        deviceId = "",
        createdAt = if (current.createdAt == 0L) now else current.createdAt,
        updatedAt = now,
        isFavorite = current.isFavorite,
        accessRole = if (current.isEditable) NoteAccessRole.FULL_ACCESS else NoteAccessRole.READ_ONLY
    )
    noteRepository.save(note)
    uiStateInternal.value = uiStateInternal.value.copy(
        noteId = noteId,
        createdAt = note.createdAt
    )
}

internal fun NoteEditorViewModel.generateSummaryForLoadedNote(state: NoteEditorUiState) {
    summaryJob?.cancel()
    if (state.noteId.isNullOrBlank()) {
        uiStateInternal.value = state.copy(summaryState = NoteSummaryUiState.Empty)
        return
    }
    val noteText = state.content
    summaryJob = viewModelScope.launch {
        val summaryState = when (val result = summarizeNoteUseCase(state.title, noteText)) {
            is NoteSummaryResult.Success -> NoteSummaryUiState.Content(result.summary.text)
            NoteSummaryResult.Empty -> NoteSummaryUiState.Empty
            NoteSummaryResult.Unavailable -> NoteSummaryUiState.Error
        }
        uiStateInternal.value = uiStateInternal.value.copy(summaryState = summaryState)
    }
}
