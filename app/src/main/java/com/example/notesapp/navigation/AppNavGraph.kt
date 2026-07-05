package com.example.notesapp.navigation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.ui.common.components.ErrorDialog
import com.example.notesapp.ui.editor.screen.ExportNoteScreen
import com.example.notesapp.ui.editor.screen.NoteEditorScreen
import com.example.notesapp.ui.folders.screen.FoldersScreen
import com.example.notesapp.ui.home.screen.HomeNotesScreen
import com.example.notesapp.ui.notes.screen.CollectionNotesScreen
import com.example.notesapp.ui.notes.screen.MoveToScreen
import com.example.notesapp.ui.notes.viewmodel.MoveToViewModel
import com.example.notesapp.ui.onboarding.screen.OnboardingScreen
import com.example.notesapp.ui.settings.screen.SettingsScreen
import com.example.notesapp.ui.share.screen.ManageAccessScreen
import com.example.notesapp.ui.share.screen.ShareInviteScreen
import com.example.notesapp.ui.share.screen.SharedUsersScreen

@Composable
fun AppNavGraph(authManager: AuthManager, activity: Context) {
    AppNavHost(
        authManager = authManager,
        onLogin = { onSuccess, onError ->
            authManager.login(
                activityContext = activity,
                onSuccess = onSuccess,
                onError = onError
            )
        }
    )
}

@Composable
fun AppNavHost(authManager: AuthManager, onLogin: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit) {
    val navController = rememberNavController()
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val authRoutes = listOf(Destinations.Onboarding.route)

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentRoute != Destinations.Onboarding.route) {
            navController.navigate(Destinations.Onboarding.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    val showBottomBar = isLoggedIn &&
        currentRoute?.startsWith("editor") != true &&
        currentRoute?.startsWith("collectionNotes") != true &&
        currentRoute?.startsWith("moveTo") != true &&
        currentRoute?.startsWith("sharedUsers") != true &&
        currentRoute?.startsWith("manageAccess") != true &&
        currentRoute?.startsWith("shareInvite") != true &&
        currentRoute !in authRoutes
    var authError by remember { mutableStateOf<String?>(null) }
    authError?.let { error ->
        ErrorDialog(
            message = error,
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
                    onMoveNote = { note ->
                        navController.navigate(Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_NOTE, note.id))
                    },
                    viewModel = hiltViewModel()
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
                    onMoveFolder = { folder ->
                        navController.navigate(
                            Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_FOLDER, folder.id)
                        )
                    },
                    onMoveNote = { note ->
                        navController.navigate(Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_NOTE, note.id))
                    },
                    viewModel = hiltViewModel()
                )
            }
            composable(
                route = Destinations.MoveTo.route,
                arguments = listOf(
                    navArgument("itemType") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("itemId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                MoveToScreen(
                    parentPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                    onMoved = { navController.popBackStack() }
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
                    viewModel = hiltViewModel()
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
                    viewModel = hiltViewModel()
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
                    onShareNote = { sharedNoteId ->
                        navController.navigate(Destinations.SharedUsers.createRoute(sharedNoteId))
                    },
                    onMoveNote = { id ->
                        navController.navigate(Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_NOTE, id))
                    },
                    onExportNote = { id ->
                        navController.navigate(Destinations.ExportNote.createRoute(id))
                    },
                    viewModel = hiltViewModel()
                )
            }
            composable(
                route = Destinations.ExportNote.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId").orEmpty()
                ExportNoteScreen(
                    parentPadding = innerPadding,
                    noteId = noteId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Destinations.SharedUsers.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId").orEmpty()
                SharedUsersScreen(
                    parentPadding = innerPadding,
                    noteId = noteId,
                    onBack = { navController.popBackStack() },
                    onManageAccess = { navController.navigate(Destinations.ManageAccess.createRoute(noteId)) },
                    onShareToNewUser = { navController.navigate(Destinations.ShareInvite.createRoute(noteId)) }
                )
            }
            composable(
                route = Destinations.ManageAccess.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                ManageAccessScreen(
                    parentPadding = innerPadding,
                    noteId = backStackEntry.arguments?.getString("noteId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onConfirmSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = Destinations.ShareInvite.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                ShareInviteScreen(
                    parentPadding = innerPadding,
                    noteId = backStackEntry.arguments?.getString("noteId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onInviteSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}
