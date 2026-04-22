package com.example.notesapp

import android.app.Application
import com.example.notesapp.data.local.AppDatabase
import com.example.notesapp.data.local.SeedDataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class NotesApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Keep database lazy for seeding — Hilt provides it to the rest of the app via AppModule
    private val database by lazy { AppDatabase.getInstance(this) }

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
