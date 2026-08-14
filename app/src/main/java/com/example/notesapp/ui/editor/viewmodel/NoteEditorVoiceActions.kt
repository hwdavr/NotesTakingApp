package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import kotlinx.coroutines.launch

fun NoteEditorViewModel.deleteVoiceAudio(blockId: String) {
    val current = uiStateInternal.value
    if (!current.isEditable) return
    val noteId = current.noteId ?: return
    viewModelScope.launch {
        val updatedContent = deleteVoiceNoteAudioUseCase(noteId, blockId)
        val latest = uiStateInternal.value
        val updatedDocument = updatedContent?.let(NoteDocument::fromContent) ?: latest.document.copy(
            blocks = latest.document.blocks.map { block ->
                if (block is EditorBlock.Voice && block.blockId == blockId) {
                    block.copy(audioFilePath = null)
                } else {
                    block
                }
            }
        )
        uiStateInternal.value = latest.copy(document = updatedDocument)
    }
}
