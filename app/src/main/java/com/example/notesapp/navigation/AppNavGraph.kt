package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notesapp.ui.editor.NoteEditorScreen
import com.example.notesapp.ui.folders.FoldersScreen
import com.example.notesapp.ui.notes.NotesScreen
import com.example.notesapp.ui.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = currentRoute?.startsWith("editor") != true

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
            startDestination = Destinations.Notes.route
        ) {
            composable(Destinations.Notes.route) {
                NotesScreen(
                    parentPadding = innerPadding,
                    onAddNote = { navController.navigate(Destinations.Editor.createRoute()) },
                    onOpenNote = { id -> navController.navigate(Destinations.Editor.createRoute(id)) }
                )
            }
            composable(Destinations.Folders.route) { FoldersScreen(innerPadding) }
            composable(Destinations.Settings.route) { SettingsScreen(innerPadding) }
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
