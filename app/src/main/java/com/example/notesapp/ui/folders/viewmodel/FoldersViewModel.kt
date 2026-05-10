package com.example.notesapp.ui.folders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SmartCollectionCounts(
    val allNotes: Int = 0,
    val favorites: Int = 0,
    val archive: Int = 0
)
sealed class FolderTreeItem {
    data class FolderItem(
        val folder: Folder,
        val depth: Int,
        val noteCount: Int,
        val hasChildren: Boolean
    ) : FolderTreeItem()
    data class NoteItem(val note: Note, val depth: Int) : FolderTreeItem()
}
data class FoldersUiState(
    val smartCounts: SmartCollectionCounts = SmartCollectionCounts(),
    val treeItems: List<FolderTreeItem> = emptyList(),
    val sharedTreeItems: List<FolderTreeItem> = emptyList(),
    val isSearchActive: Boolean = false
)
@HiltViewModel
open class FoldersViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val smartCounts = MutableStateFlow(SmartCollectionCounts())
    private val folderCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val allFolders = folderRepository.getFolders()
    private val allActiveNotes = combine(
        noteRepository.getActiveNotes(),
        noteRepository.getSharedNotes()
    ) { owned, shared -> owned to shared }
    private val _uiState = MutableStateFlow(FoldersUiState())
    open val uiState: StateFlow<FoldersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                allFolders,
                allActiveNotes,
                searchQuery,
                smartCounts,
                folderCounts
            ) { folders, (notes, shared), query, counts, perFolderCounts ->
                val items = if (query.isBlank()) {
                    buildTree(folders, notes, null, 0, perFolderCounts)
                } else {
                    val matchingFolders = folders
                        .filter { it.name.contains(query, ignoreCase = true) }
                        .map {
                            FolderTreeItem.FolderItem(
                                folder = it,
                                depth = 0,
                                noteCount = perFolderCounts[it.id] ?: 0,
                                hasChildren = folders.any { child -> child.parentFolderId == it.id } ||
                                    notes.any { note -> note.folderId == it.id }
                            )
                        }
                    val matchingNotes = notes
                        .filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.content.contains(query, ignoreCase = true)
                        }
                        .map { FolderTreeItem.NoteItem(it, 0) }
                    
                    val matchingShared = shared
                        .filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.content.contains(query, ignoreCase = true)
                        }
                        .map { FolderTreeItem.NoteItem(it, 0) }

                    matchingFolders + matchingNotes + matchingShared
                }

                val sharedItems = if (query.isBlank()) {
                    shared.map { FolderTreeItem.NoteItem(it, 0) }
                } else {
                    emptyList() // Already included in search results
                }

                FoldersUiState(
                    smartCounts = counts,
                    treeItems = items,
                    sharedTreeItems = sharedItems,
                    isSearchActive = query.isNotBlank()
                )
            }.collect { state -> _uiState.value = state }
        }
        viewModelScope.launch {
            folderRepository.sync()
        }
        refreshCounts()
    }
    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }
    private fun refreshCounts() {
        viewModelScope.launch {
            try {
                val folders = folderRepository.getFolders().first()
                smartCounts.value = SmartCollectionCounts(
                    allNotes = noteRepository.getActiveNoteCount(),
                    favorites = noteRepository.getFavoriteNoteCount(),
                    archive = folderRepository.getArchivedFolderCount() + noteRepository.getArchivedNoteCount()
                )
                folderCounts.value = folders.associate { folder ->
                    folder.id to async { noteRepository.getActiveNoteCountForFolder(folder.id) }.await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun addFolder(name: String, parentId: String? = null) {
        viewModelScope.launch {
            folderRepository.insert(
                Folder(
                    name = name,
                    parentFolderId = parentId,
                    sortKey = System.currentTimeMillis().toString(),
                    deviceId = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            refreshCounts()
        }
    }
    fun renameFolder(folder: Folder, newName: String) {
        viewModelScope.launch {
            folderRepository.update(folder.copy(name = newName, updatedAt = System.currentTimeMillis()))
            refreshCounts()
        }
    }
    fun renameNote(note: Note, newName: String) {
        viewModelScope.launch {
            noteRepository.save(note.copy(title = newName, updatedAt = System.currentTimeMillis()))
            refreshCounts()
        }
    }
    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            val folders = folderRepository.getFolders().first()
            val notes = noteRepository.getActiveNotes().first()
            val foldersToArchive = mutableListOf<Folder>()
            val notesToArchive = mutableListOf<Note>()
            fun collectChildren(parentId: String) {
                folders.filter { it.parentFolderId == parentId }.forEach { childFolder ->
                    foldersToArchive.add(childFolder)
                    collectChildren(childFolder.id)
                }
                notes.filter { it.folderId == parentId }.forEach { childNote ->
                    notesToArchive.add(childNote)
                }
            }
            foldersToArchive.add(folder)
            collectChildren(folder.id)
            notesToArchive.forEach { noteRepository.delete(it) }
            foldersToArchive.reversed().forEach { folderRepository.delete(it) }
            refreshCounts()
        }
    }
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
            refreshCounts()
        }
    }
    fun addNoteToFavorites(note: Note) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(note)
            refreshCounts()
        }
    }
    fun addFolderToFavorites(folder: Folder) {
        viewModelScope.launch {
            folderRepository.toggleFavorite(folder)
            refreshCounts()
        }
    }
    private fun buildTree(
        folders: List<Folder>,
        notes: List<Note>,
        parentId: String?,
        depth: Int,
        perFolderCounts: Map<String, Int>
    ): List<FolderTreeItem> {
        val result = mutableListOf<FolderTreeItem>()
        folders.filter { it.parentFolderId == parentId }.forEach { folder ->
            val hasChildFolders = folders.any { it.parentFolderId == folder.id }
            val hasChildNotes = notes.any { it.folderId == folder.id }
            result.add(
                FolderTreeItem.FolderItem(
                    folder = folder,
                    depth = depth,
                    noteCount = perFolderCounts[folder.id] ?: 0,
                    hasChildren = hasChildFolders || hasChildNotes
                )
            )
            notes.filter { it.folderId == folder.id }.forEach { note ->
                result.add(FolderTreeItem.NoteItem(note, depth + 1))
            }
            result.addAll(buildTree(folders, notes, folder.id, depth + 1, perFolderCounts))
        }
        return result
    }
}
