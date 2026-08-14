package com.example.notesapp.base

import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.repository.FolderRepositoryImpl
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.data.repository.NoteShareRepositoryImpl
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.domain.voice.VoiceNoteRepository
import com.example.notesapp.fakes.FakeFolderDao
import com.example.notesapp.fakes.FakeNoteDao
import com.example.notesapp.fakes.FakeNoteShareDao
import com.example.notesapp.util.DeviceIdProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelIntegrationTest {
    protected lateinit var mockWebServer: MockWebServer
    protected lateinit var apiService: NotesApiService
    protected lateinit var fakeNoteDao: FakeNoteDao
    protected lateinit var fakeFolderDao: FakeFolderDao
    protected lateinit var fakeNoteShareDao: FakeNoteShareDao
    protected lateinit var syncCoordinator: ItemsSyncCoordinator
    protected lateinit var folderRepository: FolderRepositoryImpl
    protected lateinit var noteRepository: NoteRepositoryImpl
    protected lateinit var noteShareRepository: NoteShareRepositoryImpl
    protected val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun baseSetup() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val client = OkHttpClient.Builder().build()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NotesApiService::class.java)
        fakeFolderDao = FakeFolderDao()
        fakeNoteDao = FakeNoteDao()
        fakeNoteShareDao = FakeNoteShareDao()
        val fakeDeviceIdProvider = mockk<DeviceIdProvider>()
        every { fakeDeviceIdProvider.deviceId } returns "test_device"
        syncCoordinator = ItemsSyncCoordinator(apiService, fakeFolderDao, fakeNoteDao, fakeDeviceIdProvider)
        folderRepository = FolderRepositoryImpl(fakeFolderDao, apiService, syncCoordinator, fakeDeviceIdProvider)
        noteRepository = NoteRepositoryImpl(
            fakeNoteDao,
            apiService,
            syncCoordinator,
            fakeDeviceIdProvider,
            mockk<VoiceNoteRepository>(relaxed = true)
        )
        noteShareRepository = NoteShareRepositoryImpl(fakeNoteShareDao, apiService)
    }

    @After
    fun baseTeardown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
    }
    protected suspend fun TestScope.waitUntil(timeoutMs: Long = 5000, condition: suspend () -> Boolean) {
        val startTime = System.currentTimeMillis()
        while (!condition()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw AssertionError("Condition not met within $timeoutMs ms")
            }
            withContext(Dispatchers.IO) {
                delay(50)
            }
            advanceUntilIdle()
        }
    }
}
