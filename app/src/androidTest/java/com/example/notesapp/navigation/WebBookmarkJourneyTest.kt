package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.FakeWebBookmarkMetadataSource
import com.example.notesapp.HiltTestActivity
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

    private companion object {
        // Generous bound: the first journey test in a process pays cold start-up and Room setup.
        const val WAIT_TIMEOUT_MS = 20_000L
        const val POLL_INTERVAL_MS = 250L
        const val FIXTURE_TITLE = "Fixture page title"
        const val FIXTURE_DESCRIPTION = "Fixture page description"
        const val ADD_URL = "https://fixture.example/article"
        const val SECOND_ADD_URL = "https://fixture.example/second"
        const val FIRST_BLOCK_TEXT = "First block"
        const val SECOND_BLOCK_TEXT = "Second block"
    }
}
