package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.components.WebBookmarkBlockCard
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebBookmarkCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersMetadataAndActionsWithStableTags() {
        var opened = false
        var moreOpened = false
        val block = bookmark()

        composeRule.setContent {
            NotesTakingAppTheme {
                WebBookmarkBlockCard(
                    block = block,
                    isEditable = true,
                    onOpen = { opened = true },
                    onMore = { moreOpened = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        composeRule.onNodeWithTag("editor_web_bookmark_block_${block.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_web_bookmark_card_visual_${block.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_web_bookmark_title_${block.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_web_bookmark_description_${block.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_web_bookmark_url_${block.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_web_bookmark_open_${block.id}").performClick()
        composeRule.onNodeWithTag("editor_web_bookmark_actions_${block.id}").performClick()

        assertTrue(opened)
        assertTrue(moreOpened)
    }

    @Test
    fun hidesMutationControlsInReadOnlyNote() {
        var opened = false
        var moreOpened = false
        val block = bookmark()

        composeRule.setContent {
            NotesTakingAppTheme {
                WebBookmarkBlockCard(
                    block = block,
                    isEditable = false,
                    onOpen = { opened = true },
                    onMore = { moreOpened = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        composeRule.onNodeWithTag("editor_web_bookmark_open_${block.id}").performClick()
        composeRule.onAllNodesWithTag("editor_web_bookmark_actions_${block.id}").assertCountEquals(0)
        assertTrue(opened)
        assertTrue(!moreOpened)
    }

    private fun bookmark() = EditorBlock.WebBookmarkBlock(
        id = "bookmark-card-1",
        url = "https://example.com/article",
        title = "Example article",
        description = "A persisted bookmark description"
    )
}
