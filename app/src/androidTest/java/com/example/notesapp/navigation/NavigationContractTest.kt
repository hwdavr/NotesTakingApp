package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationContractTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenExportRoute_whenGraphIsBuilt_thenNoteIdIsRequired() {
        val navController = mountProductionGraph()

        val noteIdArgument = navController.graph
            .findNode(Destinations.ExportNote.route)
            ?.arguments
            ?.get("noteId")

        assertNotNull(noteIdArgument)
        assertFalse(noteIdArgument!!.isNullable)
        assertNull(noteIdArgument.defaultValue)
    }

    @Test
    fun givenMoveToRoute_whenGraphIsBuilt_thenItemArgumentsAreRequired() {
        val navController = mountProductionGraph()

        val arguments = navController.graph
            .findNode(Destinations.MoveTo.route)
            ?.arguments

        assertNotNull(arguments)
        assertNull(arguments!!["itemType"]?.defaultValue)
        assertNull(arguments["itemId"]?.defaultValue)
    }

    private fun mountProductionGraph(): NavHostController {
        var navController: NavHostController? = null
        composeRule.setContent {
            val controller = rememberNavController()
            navController = controller
            AppNavigationHost(
                navController = controller,
                innerPadding = PaddingValues(0.dp),
                isLoggedIn = false,
                onLogin = { _, _ -> },
                onAuthError = {}
            )
        }
        composeRule.waitForIdle()
        return checkNotNull(navController)
    }
}
