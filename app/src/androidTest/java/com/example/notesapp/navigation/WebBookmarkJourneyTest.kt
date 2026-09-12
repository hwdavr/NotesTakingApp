package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.FakeWebBookmarkMetadataSource
import com.example.notesapp.HiltTestActivity
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.auth.TokenStorage
import com.example.notesapp.di.WebBookmarkModule
import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end production journey for the Add Web Bookmark flow. The test mounts the shipped
 * [AppNavigationHost] graph against the real note editor and bookmark destination, replacing only
 * the network metadata boundary with a deterministic fixture so the journey stays offline.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(WebBookmarkModule::class)
class WebBookmarkJourneyTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private val fixtureMetadataSource = FakeWebBookmarkMetadataSource(
        WebBookmarkMetadata(title = FIXTURE_TITLE, description = FIXTURE_DESCRIPTION)
    )

    @BindValue
    @JvmField
    val metadataSource: WebBookmarkMetadataSource = fixtureMetadataSource

    @Inject
    lateinit var noteRepository: NoteRepository

    private lateinit var navController: NavHostController
    private val loggedInAuthManager = FakeLoggedInAuthManager()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun opensAddBookmarkPageAndReturnsWithSavedCard() {
        mountProductionGraph()
        openEditor(seedNote(documentWithTextBlock()))

        addBookmarkThroughAdvancedPanel(ADD_URL)

        composeRule.onNodeWithText(FIXTURE_TITLE).assertIsDisplayed()
        assertEquals(listOf(ADD_URL), fixtureMetadataSource.fetchedUrls.toList())
    }

    @Test
    fun appShell_hidesGlobalTabsAtWebBookmarkEditor() {
        composeRule.setContent {
            NotesTakingAppTheme {
                AppNavHost(
                    authManager = loggedInAuthManager,
                    onLogin = { _, _ -> }
                )
            }
        }
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("home_add_fab").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_add_fab").performClick()
        composeRule.onNodeWithTag("home_fab_text_note").performClick()
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("editor_basic_blocks_trigger").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_web_bookmark"))
        composeRule.onNodeWithTag("basic_blocks_web_bookmark").performClick()
        awaitBookmarkPage()

        composeRule.onAllNodesWithTag("app_bottom_navigation").assertCountEquals(0)

        composeRule.onNodeWithTag("web_bookmark_back_button").performClick()
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("editor_basic_blocks_trigger").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("home_add_fab").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("home_add_fab").assertIsDisplayed()
        composeRule.onNodeWithTag("app_bottom_navigation").assertIsDisplayed()
    }

    @Test
    fun insertsBookmarkAfterFocusedBlock() {
        mountProductionGraph()
        openEditor(
            seedNote(
                NoteDocument(
                    blocks = listOf(
                        EditorBlock.TextBlock(id = "text-first", children = listOf(RichText(FIRST_BLOCK_TEXT))),
                        EditorBlock.TextBlock(id = "text-second", children = listOf(RichText(SECOND_BLOCK_TEXT)))
                    )
                )
            )
        )

        composeRule.onAllNodesWithTag("editor_text_block")[1].performClick()
        composeRule.waitForIdle()

        addBookmarkThroughAdvancedPanel(ADD_URL)

        composeRule.onNodeWithText(FIXTURE_TITLE).assertIsDisplayed()
        val focusedBlockBottom = composeRule.onNodeWithText(SECOND_BLOCK_TEXT).getUnclippedBoundsInRoot().bottom
        val insertedCardTop = composeRule.onNodeWithText(FIXTURE_TITLE).getUnclippedBoundsInRoot().top
        assertTrue(
            "Inserted bookmark (top=$insertedCardTop) must render after the focused block (bottom=$focusedBlockBottom)",
            insertedCardTop >= focusedBlockBottom
        )
    }

    @Test
    fun appendsBookmarkWhenNoBlockIsFocused() {
        mountProductionGraph()
        val noteId = seedNote(documentWithTextBlock())
        openEditor(noteId)

        addBookmarkThroughAdvancedPanel(ADD_URL)
        addBookmarkThroughAdvancedPanel(SECOND_ADD_URL)

        // Autosave is debounced, so poll the persisted document until both appends land.
        val deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS
        var persistedUrls = persistedBookmarkUrls(noteId)
        while (persistedUrls.size < 2 && System.currentTimeMillis() < deadline) {
            composeRule.waitForIdle()
            Thread.sleep(POLL_INTERVAL_MS)
            persistedUrls = persistedBookmarkUrls(noteId)
        }

        assertEquals(
            "Persisted bookmark URLs should append in order when no block is focused",
            listOf(ADD_URL, SECOND_ADD_URL),
            persistedUrls
        )
        assertEquals(
            listOf(ADD_URL, SECOND_ADD_URL),
            fixtureMetadataSource.fetchedUrls.toList()
        )
    }

    private fun persistedBookmarkUrls(noteId: String): List<String> = runBlocking {
        NoteDocument
            .fromContent(noteRepository.getNoteById(noteId)!!.content)
            .blocks
            .filterIsInstance<EditorBlock.WebBookmarkBlock>()
            .map { it.url }
    }

    @Test
    fun retainsUrlDraftAcrossRecreationAndKeyboard() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ProductionGraphContent() }
        composeRule.waitForIdle()
        openEditor(seedNote(documentWithTextBlock()))

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_web_bookmark"))
        composeRule.onNodeWithTag("basic_blocks_web_bookmark").performClick()
        awaitBookmarkPage()
        composeRule.onNodeWithTag("web_bookmark_url_field").performClick()
        composeRule.onNodeWithTag("web_bookmark_url_field").performTextInput(ADD_URL)
        composeRule.onNodeWithTag("web_bookmark_bottom_actions").assertIsDisplayed()

        // Simulates the Activity-composition recreation that a configuration change triggers: the
        // navigation back stack and the ViewModel-backed URL draft must both come back.
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        awaitBookmarkPage()

        composeRule.onNodeWithTag("web_bookmark_url_field").assertTextContains(ADD_URL)
        composeRule.onNodeWithTag("web_bookmark_bottom_actions").assertIsDisplayed()
    }

    @Test
    fun opensEditFromActionsAndReturnsWithSameBookmark() {
        mountProductionGraph()
        val noteId = seedNote(documentWithBookmark())
        openEditor(noteId)

        openBookmarkEdit()
        composeRule.onNodeWithTag("web_bookmark_editor_page_title").assertTextEquals("Edit Web Bookmark")
        composeRule.onNodeWithTag("web_bookmark_url_field").assertTextContains(BOOKMARK_URL)
        composeRule.onNodeWithTag("web_bookmark_save_button").performClick()
        awaitEditor()

        val bookmark = persistedBookmarks(noteId).single()
        assertEquals(BOOKMARK_ID, bookmark.id)
        assertEquals(BOOKMARK_URL, bookmark.url)
        composeRule.onNodeWithTag("editor_web_bookmark_block_$BOOKMARK_ID").assertIsDisplayed()
    }

    @Test
    fun changesUrlAndRefreshesMetadataInPlace() {
        mountProductionGraph()
        val noteId = seedNote(documentWithBookmark())
        openEditor(noteId)

        openBookmarkEdit()
        composeRule.onNodeWithTag("web_bookmark_url_field").performTextReplacement(CHANGED_URL)
        composeRule.onNodeWithTag("web_bookmark_save_button").performClick()
        awaitEditor()

        val bookmark = awaitPersistedBookmark(noteId) { it.url == CHANGED_URL }
        assertEquals(BOOKMARK_ID, bookmark.id)
        assertEquals(CHANGED_URL, bookmark.url)
        assertEquals(FIXTURE_TITLE, bookmark.title)
        assertEquals(FIXTURE_DESCRIPTION, bookmark.description)
        assertEquals(listOf(CHANGED_URL), fixtureMetadataSource.fetchedUrls.toList())
    }

    @Test
    fun backsOutOfEditWithoutMutation() {
        mountProductionGraph()
        val noteId = seedNote(documentWithBookmark())
        openEditor(noteId)

        openBookmarkEdit()
        composeRule.onNodeWithTag("web_bookmark_url_field").performTextReplacement(CHANGED_URL)
        composeRule.onNodeWithTag("web_bookmark_back_button").performClick()
        awaitEditor()

        val bookmark = persistedBookmarks(noteId).single()
        assertEquals(BOOKMARK_ID, bookmark.id)
        assertEquals(BOOKMARK_URL, bookmark.url)
        assertEquals(BOOKMARK_TITLE, bookmark.title)
        composeRule.onNodeWithTag("editor_web_bookmark_block_$BOOKMARK_ID").assertIsDisplayed()
    }

    private fun mountProductionGraph() {
        composeRule.setContent { ProductionGraphContent() }
        composeRule.waitForIdle()
    }

    @Composable
    private fun ProductionGraphContent() {
        NotesTakingAppTheme {
            val controller = rememberNavController()
            navController = controller
            AppNavigationHost(
                navController = controller,
                innerPadding = PaddingValues(0.dp),
                isLoggedIn = true,
                onLogin = { _, _ -> },
                onAuthError = {}
            )
        }
    }

    private fun openEditor(noteId: String) {
        composeRule.runOnIdle { navController.navigate(Destinations.Editor.createRoute(noteId)) }
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("editor_basic_blocks_trigger").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun addBookmarkThroughAdvancedPanel(url: String) {
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_web_bookmark"))
        composeRule.onNodeWithTag("basic_blocks_web_bookmark").performClick()
        awaitBookmarkPage()
        composeRule.onNodeWithTag("web_bookmark_url_field").performTextInput(url)
        composeRule.onNodeWithTag("web_bookmark_save_button").performClick()
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("web_bookmark_editor_page").fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun openBookmarkEdit() {
        composeRule.onNodeWithTag("editor_web_bookmark_actions_$BOOKMARK_ID").performClick()
        composeRule.onNodeWithTag("web_bookmark_actions_edit").performClick()
        awaitBookmarkPage()
    }

    private fun awaitEditor() {
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("editor_web_bookmark_block_$BOOKMARK_ID")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun awaitBookmarkPage() {
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("web_bookmark_editor_page").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun seedNote(document: NoteDocument): String {
        val noteId = "web-bookmark-note-${System.nanoTime()}"
        runBlocking {
            noteRepository.save(
                Note(
                    id = noteId,
                    title = "Bookmark note",
                    content = document.toJsonString(),
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
        return noteId
    }

    private fun documentWithTextBlock(): NoteDocument = NoteDocument(
        blocks = listOf(
            EditorBlock.TextBlock(id = "text-1", children = listOf(RichText(FIRST_BLOCK_TEXT)))
        )
    )

    private fun documentWithBookmark(): NoteDocument = NoteDocument(
        blocks = listOf(
            EditorBlock.TextBlock(id = "text-1", children = listOf(RichText(FIRST_BLOCK_TEXT))),
            EditorBlock.WebBookmarkBlock(
                id = BOOKMARK_ID,
                url = BOOKMARK_URL,
                title = BOOKMARK_TITLE,
                description = BOOKMARK_DESCRIPTION
            )
        )
    )

    private fun persistedBookmarks(noteId: String): List<EditorBlock.WebBookmarkBlock> = runBlocking {
        NoteDocument
            .fromContent(noteRepository.getNoteById(noteId)!!.content)
            .blocks
            .filterIsInstance<EditorBlock.WebBookmarkBlock>()
    }

    private fun awaitPersistedBookmark(
        noteId: String,
        predicate: (EditorBlock.WebBookmarkBlock) -> Boolean
    ): EditorBlock.WebBookmarkBlock {
        val deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS
        var bookmarks = persistedBookmarks(noteId)
        while (System.currentTimeMillis() < deadline) {
            val match = bookmarks.firstOrNull(predicate)
            if (match != null) return match
            composeRule.waitForIdle()
            Thread.sleep(POLL_INTERVAL_MS)
            bookmarks = persistedBookmarks(noteId)
        }
        return bookmarks.first(predicate)
    }

    private companion object {
        // Generous bound: the first journey test in a process pays cold start-up and Room setup.
        const val WAIT_TIMEOUT_MS = 20_000L
        const val POLL_INTERVAL_MS = 250L
        const val FIXTURE_TITLE = "Fixture page title"
        const val FIXTURE_DESCRIPTION = "Fixture page description"
        const val ADD_URL = "https://fixture.example/article"
        const val SECOND_ADD_URL = "https://fixture.example/second"
        const val BOOKMARK_ID = "bookmark-journey-1"
        const val BOOKMARK_URL = "https://fixture.example/original"
        const val CHANGED_URL = "https://fixture.example/changed"
        const val BOOKMARK_TITLE = "Original bookmark"
        const val BOOKMARK_DESCRIPTION = "Original description"
        const val FIRST_BLOCK_TEXT = "First block"
        const val SECOND_BLOCK_TEXT = "Second block"
    }

    private class FakeLoggedInAuthManager : AuthManager(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        tokenStorage = object : TokenStorage(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) = Unit

            override fun getAccessToken(): String? = null

            override fun getRefreshToken(): String? = null

            override fun getIdToken(): String? = null

            override fun clearTokens() = Unit
        }
    ) {
        override val isLoggedIn = MutableStateFlow(true)
        override val logoutMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    }
}
