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
import com.example.notesapp.ui.editor.NoteEditorViewModel
import com.example.notesapp.ui.folders.FoldersScreen
import com.example.notesapp.ui.folders.FoldersViewModel
import com.example.notesapp.ui.settings.SettingsScreen
import com.example.notesapp.ui.settings.SettingsViewModel

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.ui.common.components.ErrorDialog
import com.example.notesapp.ui.home.HomeNotesScreen
import com.example.notesapp.ui.home.HomeViewModel
import com.example.notesapp.ui.notes.CollectionNotesScreen
import com.example.notesapp.ui.notes.CollectionNotesViewModel
import com.example.notesapp.ui.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(
    authManager: AuthManager, 
    activity: Context
) {
    AppNavHost(
        authManager = authManager,
        onLogin = { onSuccess, onError ->
            authManager.login(
                activityContext = activity,
                onSuccess = onSuccess,
                onError = onError
            )
        },
        homeViewModel = hiltViewModel(),
        foldersViewModel = hiltViewModel(),
        settingsViewModel = hiltViewModel(),
        collectionNotesViewModel = hiltViewModel(),
        noteEditorViewModel = hiltViewModel()
    )
}

@Composable
fun AppNavHost(
    authManager: AuthManager,
    onLogin: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    homeViewModel: HomeViewModel,
    foldersViewModel: FoldersViewModel,
    settingsViewModel: SettingsViewModel,
    collectionNotesViewModel: CollectionNotesViewModel,
    noteEditorViewModel: NoteEditorViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    
    val authRoutes = listOf(Destinations.Onboarding.route)
    val showBottomBar = isLoggedIn &&
        currentRoute?.startsWith("editor") != true &&
        currentRoute?.startsWith("collectionNotes") != true &&
        currentRoute !in authRoutes

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
                        onLogin(
                            { 
                                authError = null
                                navController.navigate(Destinations.Notes.route) {
                                    popUpTo(Destinations.Onboarding.route) { inclusive = true }
                                }
                            },
                            { description ->
                                authError = description
                            }
                        )
                    },
                    onSignupClick = { 
                        onLogin(
                            { 
                                authError = null
                                navController.navigate(Destinations.Notes.route) {
                                    popUpTo(Destinations.Onboarding.route) { inclusive = true }
                                }
                            },
                            { description ->
                                authError = description
                            }
                        )
                    }
                )
            }

            // Main Flow
            composable(Destinations.Notes.route) {
                HomeNotesScreen(
                    parentPadding = innerPadding,
                    onAddNote = { navController.navigate(Destinations.Editor.createRoute()) },
                    onOpenNote = { noteId -> navController.navigate(Destinations.Editor.createRoute(noteId)) },
                    viewModel = homeViewModel
                )
            }
            composable(Destinations.Folders.route) { 
                FoldersScreen(
                    parentPadding = innerPadding,
                    onAddNote = { folderId -> 
                        navController.navigate(Destinations.Editor.createRoute(folderId = folderId))
                    },
                    onOpenNote = { noteId ->
                        navController.navigate(Destinations.Editor.createRoute(noteId = noteId))
                    },
                    onOpenCollection = { type, label, folderId ->
                        navController.navigate(
                            Destinations.CollectionNotes.createRoute(
                                type = type,
                                label = label,
                                folderId = folderId
                            )
                        )
                    },
                    viewModel = foldersViewModel
                ) 
            }
            composable(
                route = Destinations.CollectionNotes.route,
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = "all"
                    },
                    navArgument("folderId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("label") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                CollectionNotesScreen(
                    parentPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                    onAddNote = { folderId ->
                        navController.navigate(Destinations.Editor.createRoute(folderId = folderId))
                    },
                    onOpenCollection = { type, label, folderId ->
                        navController.navigate(
                            Destinations.CollectionNotes.createRoute(
                                type = type,
                                label = label,
                                folderId = folderId
                            )
                        )
                    },
                    onOpenNote = { noteId ->
                        navController.navigate(Destinations.Editor.createRoute(noteId = noteId))
                    },
                    viewModel = collectionNotesViewModel
                )
            }
            composable(Destinations.Settings.route) { 
                SettingsScreen(
                    parentPadding = innerPadding,
                    onLogoutSuccess = {
                        navController.navigate(Destinations.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = settingsViewModel
                ) 
            }
            composable(
                route = Destinations.Editor.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("folderId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId").orEmpty().ifBlank { null }
                val folderId = backStackEntry.arguments?.getString("folderId").orEmpty().ifBlank { null }
                NoteEditorScreen(
                    parentPadding = innerPadding,
                    noteId = noteId,
                    folderId = folderId,
                    onBack = { navController.popBackStack() },
                    viewModel = noteEditorViewModel
                )
            }
        }
    }
}




