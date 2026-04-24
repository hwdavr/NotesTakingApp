package com.example.notesapp.navigation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notesapp.ui.editor.NoteEditorScreen
import com.example.notesapp.ui.folders.FoldersScreen
import com.example.notesapp.ui.settings.SettingsScreen

import androidx.compose.runtime.collectAsState
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.ui.common.components.ErrorDialog
import com.example.notesapp.ui.home.HomeNotesScreen
import com.example.notesapp.ui.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(authManager: AuthManager, activity: Context) {
    val navController = rememberNavController()
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    
    val authRoutes = listOf(Destinations.Onboarding.route)
    val showBottomBar = isLoggedIn && currentRoute?.startsWith("editor") != true && currentRoute !in authRoutes

    var authError by remember { mutableStateOf<String?>(null) }

    if (authError != null) {
        ErrorDialog(
            message = authError!!,
            onDismiss = { authError = null }
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding: PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Destinations.Notes.route else Destinations.Onboarding.route
        ) {
            // Auth Flow
            composable(Destinations.Onboarding.route) {
                OnboardingScreen(
                    onLoginClick = { 
                        authManager.login(
                            activityContext = activity,
                            onSuccess = { 
                                authError = null
                                navController.navigate(Destinations.Notes.route) {
                                    popUpTo(Destinations.Onboarding.route) { inclusive = true }
                                }
                            },
                            onError = { description ->
                                authError = description
                            }
                        )
                    },
                    onSignupClick = { 
                        authManager.login( // Universal login handles both
                            activityContext = activity,
                            onSuccess = { 
                                authError = null
                                navController.navigate(Destinations.Notes.route) {
                                    popUpTo(Destinations.Onboarding.route) { inclusive = true }
                                }
                            },
                            onError = { description ->
                                authError = description
                            }
                        )
                    }
                )
            }

            // Main Flow
            composable(Destinations.Notes.route) {
                HomeNotesScreen(
                    onAddNote = { navController.navigate(Destinations.Editor.createRoute()) },
                    onOpenNote = { noteId -> navController.navigate(Destinations.Editor.createRoute(noteId)) }
                )
            }
            composable(Destinations.Folders.route) { FoldersScreen(innerPadding) }
            composable(Destinations.Settings.route) { 
                SettingsScreen(
                    parentPadding = innerPadding,
                    onLogoutSuccess = {
                        navController.navigate(Destinations.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }
            composable(
                route = Destinations.Editor.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                NoteEditorScreen(
                    parentPadding = innerPadding,
                    noteId = noteId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
