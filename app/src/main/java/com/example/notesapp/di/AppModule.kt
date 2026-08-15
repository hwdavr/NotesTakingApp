package com.example.notesapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.notesapp.BuildConfig
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.auth.SessionInvalidator
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.data.emoji.DataStoreRecentEmojiRepository
import com.example.notesapp.data.local.AppDatabase
import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteShareDao
import com.example.notesapp.data.local.VoiceNoteBlockDao
import com.example.notesapp.data.remote.AuthInterceptor
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.TokenAuthenticator
import com.example.notesapp.data.repository.FolderRepositoryImpl
import com.example.notesapp.data.repository.JsonVoiceNoteDocumentStore
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.data.repository.NoteShareRepositoryImpl
import com.example.notesapp.data.repository.VoiceNoteRepositoryImpl
import com.example.notesapp.data.repository.VoiceSettingsRepositoryImpl
import com.example.notesapp.data.summary.AicoreFolderCategoryPromptClient
import com.example.notesapp.data.summary.FolderTextEmbeddingClient
import com.example.notesapp.data.summary.GeminiNanoFolderCategoryPromptClient
import com.example.notesapp.data.summary.GeminiNanoNoteSummarizer
import com.example.notesapp.data.summary.GeminiNanoSummaryConfig
import com.example.notesapp.data.summary.MediaPipeFolderTextEmbeddingClient
import com.example.notesapp.data.summary.MediaPipeTextFolderCategorizer
import com.example.notesapp.data.voice.AndroidMicrophoneAvailability
import com.example.notesapp.data.voice.AndroidSpeechRecognizerFactory
import com.example.notesapp.data.voice.AndroidStorageInfoProvider
import com.example.notesapp.data.voice.AndroidVoiceAudioCapture
import com.example.notesapp.data.voice.AndroidVoiceAudioEncoder
import com.example.notesapp.data.voice.AndroidVoiceRecordingController
import com.example.notesapp.data.voice.AndroidVoiceTranscriptRecognizer
import com.example.notesapp.data.voice.AudioFileSystem
import com.example.notesapp.data.voice.PrivateAudioFileSystem
import com.example.notesapp.data.voice.RecordingTranscriptCoordinator
import com.example.notesapp.data.voice.SpeechRecognizerFactory
import com.example.notesapp.data.voice.VoiceAudioCapture
import com.example.notesapp.data.voice.VoiceAudioEncoder
import com.example.notesapp.domain.emoji.EmojiCatalogRepository
import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.voice.MicrophoneAvailability
import com.example.notesapp.domain.voice.StorageInfoProvider
import com.example.notesapp.domain.voice.VoiceNoteDocumentStore
import com.example.notesapp.domain.voice.VoiceNoteRepository
import com.example.notesapp.domain.voice.VoiceRecordingController
import com.example.notesapp.domain.voice.VoiceSettingsRepository
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import com.example.notesapp.domain.voice.VoiceTranscriptSession
import com.example.notesapp.util.NoteExporter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
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
    abstract fun bindEmojiCatalogRepository(impl: BundledEmojiCatalogRepository): EmojiCatalogRepository

    @Binds
    @Singleton
    abstract fun bindRecentEmojiRepository(impl: DataStoreRecentEmojiRepository): RecentEmojiRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindNoteShareRepository(impl: NoteShareRepositoryImpl): NoteShareRepository

    @Binds
    @Singleton
    abstract fun bindVoiceNoteRepository(impl: VoiceNoteRepositoryImpl): VoiceNoteRepository

    @Binds
    @Singleton
    abstract fun bindVoiceSettingsRepository(impl: VoiceSettingsRepositoryImpl): VoiceSettingsRepository

    @Binds
    @Singleton
    abstract fun bindVoiceNoteDocumentStore(impl: JsonVoiceNoteDocumentStore): VoiceNoteDocumentStore

    @Binds
    @Singleton
    abstract fun bindNoteSummarizer(impl: GeminiNanoNoteSummarizer): NoteSummarizer

    @Binds
    @Singleton
    abstract fun bindFolderCategorizer(impl: MediaPipeTextFolderCategorizer): FolderCategorizer

    @Binds
    @Singleton
    abstract fun bindFolderCategoryPromptClient(
        impl: AicoreFolderCategoryPromptClient
    ): GeminiNanoFolderCategoryPromptClient

    @Binds
    @Singleton
    abstract fun bindFolderTextEmbeddingClient(impl: MediaPipeFolderTextEmbeddingClient): FolderTextEmbeddingClient

    @Binds
    @Singleton
    abstract fun bindVoiceRecordingController(impl: AndroidVoiceRecordingController): VoiceRecordingController

    @Binds
    @Singleton
    abstract fun bindVoiceAudioCapture(impl: AndroidVoiceAudioCapture): VoiceAudioCapture

    @Binds
    @Singleton
    abstract fun bindVoiceAudioEncoder(impl: AndroidVoiceAudioEncoder): VoiceAudioEncoder

    @Binds
    @Singleton
    abstract fun bindVoiceTranscriptRecognizer(impl: AndroidVoiceTranscriptRecognizer): VoiceTranscriptRecognizer

    @Binds
    @Singleton
    abstract fun bindSpeechRecognizerFactory(impl: AndroidSpeechRecognizerFactory): SpeechRecognizerFactory

    @Binds
    @Singleton
    abstract fun bindVoiceTranscriptSession(impl: RecordingTranscriptCoordinator): VoiceTranscriptSession

    @Binds
    @Singleton
    abstract fun bindStorageInfoProvider(impl: AndroidStorageInfoProvider): StorageInfoProvider

    @Binds
    @Singleton
    abstract fun bindMicrophoneAvailability(impl: AndroidMicrophoneAvailability): MicrophoneAvailability

    @Binds
    @Singleton
    abstract fun bindAudioFileSystem(impl: PrivateAudioFileSystem): AudioFileSystem

    @Binds
    @Singleton
    abstract fun bindSessionInvalidator(impl: AuthManager): SessionInvalidator
    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getInstance(context)

        @Provides
        @Singleton
        @Named("emojiRecentDataStore")
        fun provideEmojiRecentDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.filesDir.resolve("datastore/emoji_recent.preferences_pb")
            }

        @Provides
        fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

        @Provides
        fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()

        @Provides
        fun provideNoteShareDao(database: AppDatabase): NoteShareDao = database.noteShareDao()

        @Provides
        fun provideVoiceNoteBlockDao(database: AppDatabase): VoiceNoteBlockDao = database.voiceNoteBlockDao()

        @Provides
        @Singleton
        fun provideOkHttpClient(
            authInterceptor: AuthInterceptor,
            tokenAuthenticator: TokenAuthenticator
        ): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
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
        fun provideNoteExporter(@ApplicationContext context: Context): NoteExporter = NoteExporter(context)

        @Provides
        @Singleton
        fun provideGeminiNanoSummaryConfig(): GeminiNanoSummaryConfig = GeminiNanoSummaryConfig()
    }
}
