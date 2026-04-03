package com.example.notesapp

import android.app.Application
import com.example.notesapp.data.local.AppDatabase
import com.example.notesapp.data.local.SeedDataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotesApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            SeedDataInitializer(
                folderDao = database.folderDao(),
                noteDao = database.noteDao()
            ).seedIfNeeded()
        }
    }
}
