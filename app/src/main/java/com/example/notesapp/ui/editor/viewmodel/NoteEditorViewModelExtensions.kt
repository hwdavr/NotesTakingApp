package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.createEmptyTextBlock

fun NoteEditorViewModel.createBasicBlock(type: BasicBlockType): EditorBlock.TextBlock = type.createEmptyTextBlock()
