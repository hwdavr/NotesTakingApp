package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import com.example.notesapp.ui.home.model.HomeUiState
import com.example.notesapp.ui.home.screen.HomeNotesScreenContent
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceEntryNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeRecordAllocatesPlaceholderAndOpensRecorder() {
        var selectedEntry: String? = null
        var placeholderId: String? = null
        val repository = FakeNoteRepository()
        val placeholderUseCase = VoiceNotePlaceholderUseCase(repository)
        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    state = HomeUiState(),
                    onAddNote = { selectedEntry = "text" },
                    onRecordNote = {
                        placeholderId = runBlocking { placeholderUseCase.create().id }
                        selectedEntry = "home"
                    },
                    onOpenNote = {},
                    onSelectFolder = {}
                )
            }
        }

        composeRule.onNodeWithTag("home_add_fab").performClick()
        composeRule.onNodeWithTag("home_fab_sheet_title").assertIsDisplayed()
        composeRule.onNodeWithText("Text Note").assertIsDisplayed()
        composeRule.onNodeWithText("Record Note").assertIsDisplayed()

        composeRule.onNodeWithTag("home_fab_record_note").performClick()

        assertEquals("home", selectedEntry)
        assertNotNull(placeholderId)
        assertNotNull(runBlocking { repository.getNoteById(placeholderId.orEmpty()) })
    }
}
