package com.example.notesapp.fakes

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.FolderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFolderDao : FolderDao {
    val foldersFlow = MutableStateFlow<List<FolderEntity>>(emptyList())
    override fun getFolders() = foldersFlow.map { folders -> folders.filter { it.deletedAt == null } }
    override fun getFolder(id: String) = foldersFlow.map { folders ->
        folders.firstOrNull { it.id == id && it.deletedAt == null }
    }
    override suspend fun getFolderById(id: String) = foldersFlow.value.firstOrNull { it.id == id }
    override fun getArchivedFolders() = foldersFlow.map { folders -> folders.filter { it.deletedAt != null } }
    override suspend fun getFolderCount() = foldersFlow.value.size
    override suspend fun getArchivedFolderCount() = foldersFlow.value.count { it.deletedAt != null }
    override suspend fun insert(folder: FolderEntity) {
        val newList = foldersFlow.value.toMutableList()
        newList.removeIf { it.id == folder.id }
        newList.add(folder)
        foldersFlow.value = newList
    }
    override suspend fun insertAll(newFolders: List<FolderEntity>) {
        val newList = foldersFlow.value.toMutableList()
        newFolders.forEach { f ->
            newList.removeIf { it.id == f.id }
            newList.add(f)
        }
        foldersFlow.value = newList
    }
    override suspend fun clearAll() {
        foldersFlow.value = emptyList()
    }
}
