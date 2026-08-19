package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.createEmptyTextBlock
import io.mockk.mockk
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelIntegrationTest : BaseViewModelIntegrationTest() {
    private lateinit var viewModel: NoteEditorViewModel

    @Test
    fun `load read only note exposes non editable ui state from shared scenario`() = runTest {
        val scenarioFile = File("../sharedContracts/test-scenarios/note_read_only_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        val apiMock = jsonObject.getJSONArray("apiMocks").getJSONObject(0)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(apiMock.getInt("status"))
                .setBody(apiMock.getJSONArray("response").toString())
        )

        viewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            testSummaryUseCase(),
            testCategorizeUseCase(),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.load("note_001")
        advanceUntilIdle()

        waitUntil { viewModel.uiState.value.isLoaded }

        val expectedUi = jsonObject.getJSONObject("expected").getJSONObject("ui")
        val state = viewModel.uiState.value
        assertEquals(expectedUi.getString("noteId"), state.noteId)
        assertEquals(expectedUi.getString("title"), state.title)
        assertEquals(expectedUi.getString("content"), state.content)
        assertEquals(expectedUi.getBoolean("isEditable"), state.isEditable)
    }

    @Test
    fun `test onContentChange triggers auto-save using shared scenario`() = runTest {
        // 1. Prepare initial state
        val initialNote = NoteEntity(
            id = "note_001",
            folderId = "folder_001",
            title = "My Note",
            content = "Initial content",
            sortKey = "b0",
            version = 1,
            deviceId = "test_device",
            lastSyncedVersion = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeNoteDao.insert(initialNote)
        // 2. Load the scenario
        val scenarioFile = File("../sharedContracts/test-scenarios/note_update_content_001.json")
        val jsonObject = JSONObject(scenarioFile.readText())
        val apiMocks = jsonObject.getJSONArray("apiMocks")
        // 3. Enqueue mocks
        // Request 1: load() sync
        val initialNoteJson = """
            [
              {
                "id": "note_001",
                "userId": "auth0|abc123",
                "type": "note",
                "parentId": "folder_001",
                "name": "My Note",
                "content": "Initial content",
                "sortKey": "b0",
                "version": 1,
                "deviceId": "test_device",
                "lastSyncedVersion": 1,
                "createdAt": "2026-04-26T10:00:00Z",
                "updatedAt": "2026-04-26T10:00:00Z"
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(initialNoteJson))
        // Request 2: auto-save patch
        val patchMock = apiMocks.getJSONObject(0)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(patchMock.getInt("status"))
                .setBody(patchMock.getJSONObject("response").toString())
        )
        // Request 3: post-save sync
        val syncMock = apiMocks.getJSONObject(1)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(syncMock.getInt("status"))
                .setBody(syncMock.getJSONArray("response").toString())
        )
        // 4. Load the note into the editor
        viewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            testSummaryUseCase(),
            testCategorizeUseCase(),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.load("note_001")
        advanceUntilIdle()
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        // Wait for the load operation to complete
        waitUntil { viewModel.uiState.value.isLoaded }
        assertTrue("ViewModel should be loaded", viewModel.uiState.value.isLoaded)
        assertEquals("note_001", viewModel.uiState.value.noteId)
        // 5. Change content
        val newContent = jsonObject.getJSONObject("expected").getJSONObject("ui").getString("content")
        viewModel.onContentChange(newContent)
        assertEquals(newContent, viewModel.uiState.value.content)
        // 6. Advance time to trigger auto-save (delay is 2000ms)
        advanceTimeBy(3000)
        advanceUntilIdle()
        // 7. Wait for patch request
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        // Wait for post-save sync request (the Repository calls syncAll after patch)
        waitUntil { mockWebServer.requestCount == 3 }
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        // Wait for the DAO to be updated by the sync operation
        waitUntil { fakeNoteDao.getNoteById("note_001")?.content == newContent }
        // 9. Final assertions
        val uiState = viewModel.uiState.value
        assertEquals(newContent, uiState.content)
        // Verify DAO was updated
        val noteInDao = fakeNoteDao.getNoteById("note_001")
        assertEquals(newContent, noteInDao?.content)
    }

    @Test
    fun basicBlockAutoSaveAndReloadPreservesDocument() = runTest {
        val noteId = "basic-blocks-note"
        val toggleId = "toggle-block"
        val calloutId = "callout-block"
        val scenario = JSONObject(
            File("../sharedContracts/test-scenarios/basic_blocks_autosave_001.json").readText()
        )
        val apiMocks = scenario.getJSONArray("apiMocks")
        val expectedUi = scenario.getJSONObject("expected").getJSONObject("ui")
        val initialDocument = NoteDocument(
            blocks = listOf(
                BasicBlockType.HEADING_2.createEmptyTextBlock("heading-block").copy(
                    children = listOf(RichText("Section"))
                ),
                BasicBlockType.NUMBERED_LIST.createEmptyTextBlock("numbered-block").copy(
                    children = listOf(RichText("First"))
                ),
                BasicBlockType.TODO_LIST.createEmptyTextBlock("todo-block").copy(
                    children = listOf(RichText("Task")),
                    checked = true
                ),
                BasicBlockType.TOGGLE_LIST.createEmptyTextBlock(toggleId).copy(
                    children = listOf(RichText("Toggle content"))
                ),
                BasicBlockType.CALLOUT.createEmptyTextBlock(calloutId),
                BasicBlockType.QUOTE.createEmptyTextBlock("quote-block").copy(
                    children = listOf(RichText("Quoted text"))
                )
            )
        )
        fakeNoteDao.insert(
            NoteEntity(
                id = noteId,
                folderId = null,
                title = "Basic blocks",
                content = initialDocument.toJsonString(),
                sortKey = "b0",
                version = 1,
                deviceId = "test_device",
                lastSyncedVersion = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        (0 until apiMocks.length()).forEach { index ->
            val apiMock = apiMocks.getJSONObject(index)
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(apiMock.getInt("status"))
                    .setBody(apiMock.get("response").toString())
            )
        }

        viewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            testSummaryUseCase(),
            testCategorizeUseCase(),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.load(noteId)
        advanceUntilIdle()
        waitUntil { viewModel.uiState.value.isLoaded }

        assertTrue(viewModel.toggleToggleExpanded(toggleId))
        viewModel.onTextBlockChange(calloutId, "Callout body")
        advanceTimeBy(2_001)
        advanceUntilIdle()
        waitUntil {
            fakeNoteDao.getNoteById(noteId)?.content?.contains("\"expanded\":false") == true
        }

        val savedContent = fakeNoteDao.getNoteById(noteId)?.content
        assertTrue(!savedContent.isNullOrBlank())
        assertTrue(savedContent!!.contains("\"expanded\":false"))

        val reloadedViewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            testSummaryUseCase(),
            testCategorizeUseCase(),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        reloadedViewModel.load(noteId)
        advanceUntilIdle()
        waitUntil { reloadedViewModel.uiState.value.isLoaded }

        val reloadedBlocks = reloadedViewModel.uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>()
        val expectedTypes = expectedUi.getJSONArray("textBlockTypes").let { types ->
            (0 until types.length()).map(types::getString)
        }
        assertEquals(
            expectedTypes,
            reloadedBlocks.map(EditorBlock.TextBlock::type)
        )
        assertEquals(expectedUi.getBoolean("todoChecked"), reloadedBlocks.single { it.id == "todo-block" }.checked)
        assertEquals(
            expectedUi.getBoolean("toggleExpanded"),
            reloadedBlocks.single { it.id == toggleId }.isExpanded
        )
        assertEquals(
            expectedUi.getString("calloutText"),
            reloadedBlocks.single { it.id == calloutId }.children.single().text
        )
    }

    @Test
    fun testInsertMermaidBlockFromBasicBlocksPanel() = runTest {
        val noteId = "mermaid-insert-note"
        val syncResponse = """
            [
              {
                "id": "mermaid-insert-note",
                "userId": "auth0|abc123",
                "type": "note",
                "parentId": null,
                "name": "Mermaid Test Note",
                "content": "{\"version\":1,\"blocks\":[{\"id\":\"b1\",\"type\":\"paragraph\",\"children\":[{\"text\":\"\"}]}]}",
                "sortKey": "b0",
                "version": 1,
                "deviceId": "test_device",
                "lastSyncedVersion": 1,
                "createdAt": "2026-08-18T10:00:00Z",
                "updatedAt": "2026-08-18T10:00:00Z"
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(syncResponse))
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"mermaid-insert-note","version":2,"updatedAt":"2026-08-18T10:00:01Z"}""")
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(syncResponse))

        fakeNoteDao.insert(
            NoteEntity(
                id = noteId,
                folderId = null,
                title = "Mermaid Test Note",
                content = NoteDocument.empty().toJsonString(),
                sortKey = "b0",
                version = 1,
                deviceId = "test_device",
                lastSyncedVersion = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        viewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            testSummaryUseCase(),
            testCategorizeUseCase(),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.load(noteId)
        advanceUntilIdle()
        waitUntil { viewModel.uiState.value.isLoaded }

        val inserted = viewModel.insertBasicBlock(BasicBlockType.MERMAID)
        assertTrue(inserted)

        val state = viewModel.uiState.value
        val mermaidBlock = state.document.blocks.filterIsInstance<EditorBlock.MermaidBlock>().firstOrNull()
        assertTrue(
            "Mermaid block should have default starter flowchart code",
            mermaidBlock?.code?.contains("graph TD") == true
        )
        assertEquals(mermaidBlock?.id, state.focusedBlockId)
        assertTrue(
            "Basic blocks panel should be auto-collapsed after insertion",
            !state.showBasicBlocksPanel
        )

        advanceTimeBy(3000)
        advanceUntilIdle()
        waitUntil { fakeNoteDao.getNoteById(noteId)?.content?.contains("\"type\":\"mermaid\"") == true }

        val savedContent = fakeNoteDao.getNoteById(noteId)?.content
        assertTrue(!savedContent.isNullOrBlank())
        assertTrue(savedContent!!.contains("\"type\":\"mermaid\""))
    }

    @Test
    fun testInsertCodeBlockFromBasicBlocksPanel() = runTest {
        val noteId = "code-insert-note"
        val syncResponse = """
            [
              {
                "id": "code-insert-note",
                "userId": "auth0|abc123",
                "type": "note",
                "parentId": null,
                "name": "Code Test Note",
                "content": "{\"version\":1,\"blocks\":[{\"id\":\"b1\",\"type\":\"paragraph\",\"children\":[{\"text\":\"\"}]}]}",
                "sortKey": "b0",
                "version": 1,
                "deviceId": "test_device",
                "lastSyncedVersion": 1,
                "createdAt": "2026-08-19T10:00:00Z",
                "updatedAt": "2026-08-19T10:00:00Z"
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(syncResponse))
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"code-insert-note","version":2,"updatedAt":"2026-08-19T10:00:01Z"}""")
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(syncResponse))

        fakeNoteDao.insert(
            NoteEntity(
                id = noteId,
                folderId = null,
                title = "Code Test Note",
                content = NoteDocument.empty().toJsonString(),
                sortKey = "b0",
                version = 1,
                deviceId = "test_device",
                lastSyncedVersion = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        viewModel = NoteEditorViewModel(
            noteRepository,
            folderRepository,
            testSummaryUseCase(),
            testCategorizeUseCase(),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.load(noteId)
        advanceUntilIdle()
        waitUntil { viewModel.uiState.value.isLoaded }

        val inserted = viewModel.insertBasicBlock(BasicBlockType.CODE)
        assertTrue(inserted)

        val state = viewModel.uiState.value
        val codeBlock = state.document.blocks.filterIsInstance<EditorBlock.CodeBlock>().firstOrNull()
        assertTrue("Code block should be inserted", codeBlock != null)
        assertEquals("Plain Text", codeBlock?.language)
        assertEquals("", codeBlock?.code)
        assertEquals(codeBlock?.id, state.focusedBlockId)
        assertTrue("Basic blocks panel should be auto-collapsed after insertion", !state.showBasicBlocksPanel)

        advanceTimeBy(3000)
        advanceUntilIdle()
        waitUntil { fakeNoteDao.getNoteById(noteId)?.content?.contains("\"type\":\"code\"") == true }

        val savedContent = fakeNoteDao.getNoteById(noteId)?.content
        assertTrue(!savedContent.isNullOrBlank())
        assertTrue(savedContent!!.contains("\"type\":\"code\""))
    }

    private fun testSummaryUseCase(): SummarizeNoteUseCase = SummarizeNoteUseCase(
        object : NoteSummarizer {
            override suspend fun summarize(title: String, noteText: String): NoteSummary =
                NoteSummary("Integration summary")
        }
    )

    private fun testCategorizeUseCase(): CategorizeNoteUseCase = CategorizeNoteUseCase(
        object : FolderCategorizer {
            override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? = null
        }
    )
}
