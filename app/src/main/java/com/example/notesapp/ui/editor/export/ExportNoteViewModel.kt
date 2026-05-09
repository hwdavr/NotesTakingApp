package com.example.notesapp.ui.editor.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.util.NoteExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExportFormat {
    Markdown, PDF
}
data class ExportUiState(
    val note: Note? = null,
    val selectedFormat: ExportFormat = ExportFormat.Markdown,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val error: String? = null
)
@HiltViewModel
class ExportNoteViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val noteExporter: NoteExporter
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()
    fun loadNote(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            _uiState.value = _uiState.value.copy(note = note)
        }
    }
    fun selectFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }
    fun exportToUri(uri: Uri) {
        val note = _uiState.value.note ?: return
        val format = _uiState.value.selectedFormat
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null)
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        when (format) {
                            ExportFormat.Markdown -> noteExporter.exportToMarkdown(note, outputStream)
                            ExportFormat.PDF -> noteExporter.exportToPdf(note, outputStream)
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(isExporting = false, exportSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message ?: "Unknown error")
            }
        }
    }
    fun resetStatus() {
        _uiState.value = _uiState.value.copy(exportSuccess = false, error = null)
    }
}
