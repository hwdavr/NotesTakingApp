package com.example.notesapp.ui.folders

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.repository.FolderRepositoryImpl
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.util.DeviceIdProvider
import com.example.notesapp.fakes.FakeFolderDao
import com.example.notesapp.fakes.FakeNoteDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FoldersViewModelIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: NotesApiService
    private lateinit var syncCoordinator: ItemsSyncCoordinator
    private lateinit var viewModel: FoldersViewModel

    private val testDispatcher = StandardTestDispatcher()
    @Before
    fun setup() {
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

        val fakeFolderDao = FakeFolderDao()
        val fakeNoteDao = FakeNoteDao()

        syncCoordinator = ItemsSyncCoordinator(apiService, fakeFolderDao, fakeNoteDao)

        val fakeDeviceIdProvider = mockk<DeviceIdProvider>()
        every { fakeDeviceIdProvider.deviceId } returns "test_device"

        val folderRepository = FolderRepositoryImpl(fakeFolderDao, apiService, syncCoordinator, fakeDeviceIdProvider)
        val noteRepository = NoteRepositoryImpl(fakeNoteDao, apiService, syncCoordinator, fakeDeviceIdProvider)

        // Do not instantiate ViewModel yet, because it triggers sync on init.
        // We will instantiate it in the test after enqueuing the mock responses.
        fun createViewModel(): FoldersViewModel {
            return FoldersViewModel(folderRepository, noteRepository)
        }
        
        // Save creator for test
        viewModelCreator = ::createViewModel
    }
    
    private lateinit var viewModelCreator: () -> FoldersViewModel

    @After
    fun teardown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun `test add folder and sync updates UI state using shared scenario`() = runTest {
        val scenarioFile = File("../sharedContracts/test-scenarios/folder_add_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        
        val apiMocks = jsonObject.getJSONArray("apiMocks")

        // In FoldersViewModel init, folderRepository.sync() is called -> hits /v1/items
        // We don't have that in the mock right now, so let's enqueue an empty list for the init sync.
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        
        viewModel = viewModelCreator()
        
        val collectJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        
        advanceUntilIdle() // let init sync finish

        // Now enqueue the mock responses for the addFolder action
        // 1. POST /v1/folders
        val firstMock = apiMocks.getJSONObject(0)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(firstMock.getInt("status"))
                .setBody(firstMock.getJSONObject("response").toString())
        )
        // 2. GET /v1/items (called by syncCoordinator after successful insert)
        val secondMock = apiMocks.getJSONObject(1)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(secondMock.getInt("status"))
                .setBody(secondMock.getJSONArray("response").toString())
        )

        // Perform the action
        viewModel.addFolder("Work")
        
        // Let the coroutine start and make the first network request
        advanceUntilIdle()

        // Wait for network requests to be processed
        // 1. init sync request
        mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS)
        // 2. add folder request
        mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS)
        
        // Let the coroutine proceed to syncAll()
        advanceUntilIdle()
        
        // 3. sync request
        mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS)

        // Process final response
        advanceUntilIdle()

        // Give coroutines some time to update flows
        var retry = 0
        while (viewModel.uiState.value.treeItems.isEmpty() && retry < 20) {
            kotlinx.coroutines.delay(100)
            retry++
        }

        val uiState = viewModel.uiState.value
        val expectedUi = jsonObject.getJSONObject("expected").getJSONObject("ui")
        val expectedItemCount = expectedUi.getInt("itemCount")
        val expectedFirstItemName = expectedUi.getJSONArray("items").getJSONObject(0).getString("name")

        assertEquals(expectedItemCount, uiState.treeItems.size)
        
        collectJob.cancel()
        
        val firstTreeItem = uiState.treeItems[0] as FolderTreeItem.FolderItem
        assertEquals(expectedFirstItemName, firstTreeItem.folder.name)
    }
}
