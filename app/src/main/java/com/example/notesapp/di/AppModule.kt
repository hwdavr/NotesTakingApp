package com.example.notesapp.di

import android.content.Context
import com.example.notesapp.BuildConfig
import com.example.notesapp.data.local.AppDatabase
import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.NoteBlockCommentDao
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteShareDao
import com.example.notesapp.data.remote.AuthInterceptor
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.repository.FolderRepositoryImpl
import com.example.notesapp.data.repository.NoteCommentRepositoryImpl
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.data.repository.NoteShareRepositoryImpl
import com.example.notesapp.domain.comment.repository.NoteCommentRepository
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.util.NoteExporter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Clock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindNoteShareRepository(impl: NoteShareRepositoryImpl): NoteShareRepository

    @Binds
    @Singleton
    abstract fun bindNoteCommentRepository(impl: NoteCommentRepositoryImpl): NoteCommentRepository
    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getInstance(context)

        @Provides
        fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

        @Provides
        fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()

        @Provides
        fun provideNoteShareDao(database: AppDatabase): NoteShareDao = database.noteShareDao()

        @Provides
        fun provideNoteBlockCommentDao(database: AppDatabase): NoteBlockCommentDao = database.noteBlockCommentDao()

        @Provides
        @Singleton
        fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.HEADERS
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()

        @Provides
        @Singleton
        fun provideMoshi(): Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        @Provides
        @Singleton
        fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        @Provides
        @Singleton
        fun provideNotesApiService(retrofit: Retrofit): NotesApiService = retrofit.create(NotesApiService::class.java)

        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemDefaultZone()

        @Provides
        @Singleton
        fun provideNoteExporter(@ApplicationContext context: Context): NoteExporter = NoteExporter(context)
    }
}
