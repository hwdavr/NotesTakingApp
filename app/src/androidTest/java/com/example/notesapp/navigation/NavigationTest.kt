package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.folders.screen.FoldersScreenContent
import com.example.notesapp.ui.folders.viewmodel.FoldersUiState
import com.example.notesapp.ui.home.model.HomeUiState
import com.example.notesapp.ui.home.screen.HomeNotesScreenContent
import com.example.notesapp.ui.onboarding.screen.OnboardingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeRule = createComposeRule()
    @Test
    fun test_navigation_from_onboarding_to_home() {
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "onboarding"
            ) {
                composable("onboarding") {
                    OnboardingScreen(
                        onLoginClick = { navController.navigate("home") },
                        onSignupClick = { }
                    )
                }
                composable("home") {
                    HomeNotesScreenContent(
                        parentPadding = PaddingValues(0.dp),
                        state = HomeUiState(),
                        onAddNote = { },
                        onOpenNote = { _: String -> },
                        onSelectFolder = { _: String -> }
                    )
                }
            }
        }
        // Onboarding Screen should be visible
        composeRule.onNodeWithText("Save and share notes", ignoreCase = true).assertIsDisplayed()
        // Navigate to Home
        composeRule.onNodeWithText("Log in", ignoreCase = true).performClick()
        // Home Screen should be visible
        composeRule.onNodeWithText("Recent folders", ignoreCase = true).assertIsDisplayed()
    }
    @Test
    fun test_navigation_to_folders() {
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeNotesScreenContent(
                        parentPadding = PaddingValues(0.dp),
                        state = HomeUiState(),
                        onAddNote = { },
                        onOpenNote = { _: String -> },
                        onSelectFolder = { _: String -> }
                    )
                }
                composable("folders") {
                    FoldersScreenContent(
                        parentPadding = PaddingValues(0.dp),
                        state = FoldersUiState(),
                        onSearchChanged = { _: String -> },
                        onAddFolder = { _: String, _: String? -> },
                        onRenameFolder = { _: Folder, _: String -> },
                        onRenameNote = { _: Note, _: String -> },
                        onDeleteFolder = { _: Folder -> },
                        onDeleteNote = { _: Note -> },
                        onAddNote = { _: String -> },
                        onOpenNote = { _: String -> },
                        onOpenCollection = { _: String, _: String, _: String? -> }
                    )
                }
            }
            navController.navigate("folders")
        }
        // Folders Screen Header
        composeRule.onNodeWithText("My Notes", ignoreCase = true).assertIsDisplayed()
    }
}
