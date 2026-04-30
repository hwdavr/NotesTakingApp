package com.example.notesapp.fakes

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.FolderEntity
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFolderDao : FolderDao {
    val foldersFlow = MutableStateFlow<List<FolderEntity>>(emptyList())
    override fun getFolders() = foldersFlow
    override suspend fun getFolderCount() = foldersFlow.value.size
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
