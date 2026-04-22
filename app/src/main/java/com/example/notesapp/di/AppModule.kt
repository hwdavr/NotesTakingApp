package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.data.local.AppDatabase
import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.repository.FolderRepositoryImpl
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            AppDatabase.getInstance(context)

        @Provides
        fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

        @Provides
        fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()
    }
}
