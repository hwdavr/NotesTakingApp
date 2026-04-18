package com.example.notesapp

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsScreen_rendersCorrectly() {
        composeRule.setContent {
            SettingsScreen(parentPadding = PaddingValues(0.dp))
        }

        // Verify root screen is displayed
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()

        // Verify Top Bar
        composeRule.onNodeWithText("AI Notes").assertIsDisplayed()
        composeRule.onNodeWithText("Pro").assertIsDisplayed()

        // Verify Hero Banner
        composeRule.onNodeWithTag("settings_hero_card").assertIsDisplayed()
        composeRule.onNodeWithText("Get the most out of AI Notes").assertIsDisplayed()
        // "Unlimited Smart notes, no limits!" appears in both hero and account subtitle - check there are 2 instances
        composeRule.onAllNodesWithText("Unlimited Smart notes, no limits!")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Upgrade to pro").assertIsDisplayed()

        // Verify Account Section: single row with subtitle
        composeRule.onNodeWithTag("settings_account_section").assertIsDisplayed()
        composeRule.onNodeWithText("Account").assertIsDisplayed()
        composeRule.onNodeWithText("Test Device").assertIsDisplayed()
        // subtitle under Test Device (second occurrence of same text)
        composeRule.onAllNodesWithText("Unlimited Smart notes, no limits!")[1].assertIsDisplayed()

        // Verify General Section
        composeRule.onNodeWithTag("settings_general_section").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("General").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("App Languages").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Restore Purchase").performScrollTo().assertIsDisplayed()

        // Verify Other Section
        composeRule.onNodeWithTag("settings_other_section").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Other").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Rate Us").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Feedback").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Share").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Privacy Policy").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Terms of Use").performScrollTo().assertIsDisplayed()
    }
}
