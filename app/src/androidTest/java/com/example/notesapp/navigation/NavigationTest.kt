package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun productionGraph_startsAtOnboarding_whenLoggedOut() {
        val navController = mountProductionGraph()

        assertEquals(Destinations.Onboarding.route, navController.currentDestination?.route)
    }

    @Test
    fun productionGraph_registersTopLevelRoutes() {
        val navController = mountProductionGraph()

        assertNotNull(navController.graph.findNode(Destinations.Notes.route))
        assertNotNull(navController.graph.findNode(Destinations.Folders.route))
        assertNotNull(navController.graph.findNode(Destinations.Settings.route))
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
