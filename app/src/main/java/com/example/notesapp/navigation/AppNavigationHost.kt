package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notesapp.domain.voice.RecordingEntryPoint
import com.example.notesapp.ui.editor.screen.ExportNoteScreen
import com.example.notesapp.ui.editor.screen.NoteEditorScreen
import com.example.notesapp.ui.folderdescription.screen.FolderDescriptionScreen
import com.example.notesapp.ui.folders.screen.FoldersScreen
import com.example.notesapp.ui.home.screen.HomeNotesScreen
import com.example.notesapp.ui.home.viewmodel.HomeViewModel
import com.example.notesapp.ui.notes.screen.CollectionNotesScreen
import com.example.notesapp.ui.notes.screen.MoveToScreen
import com.example.notesapp.ui.notes.viewmodel.MoveToViewModel
import com.example.notesapp.ui.onboarding.screen.OnboardingScreen
import com.example.notesapp.ui.settings.screen.SettingsScreen
import com.example.notesapp.ui.share.screen.ManageAccessScreen
import com.example.notesapp.ui.share.screen.ShareInviteScreen
import com.example.notesapp.ui.share.screen.SharedUsersScreen
import com.example.notesapp.ui.voice.screen.VoiceRecorderScreen
import com.example.notesapp.ui.voice.viewmodel.VoiceEntryViewModel

@Composable
internal fun AppNavigationHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    isLoggedIn: Boolean,
    onLogin: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onAuthError: (String?) -> Unit
) {
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
                            onAuthError(null)
                            navController.navigate(Destinations.Notes.route) {
                                popUpTo(Destinations.Onboarding.route) { inclusive = true }
                            }
                        },
                        { description ->
                            onAuthError(description)
                        }
                    )
                },
                onSignupClick = {
                    onLogin(
                        {
                            onAuthError(null)
                            navController.navigate(Destinations.Notes.route) {
                                popUpTo(Destinations.Onboarding.route) { inclusive = true }
                            }
                        },
                        { description ->
                            onAuthError(description)
                        }
                    )
                }
            )
        }
        // Main Flow
        composable(Destinations.Notes.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val voiceEntryViewModel: VoiceEntryViewModel = hiltViewModel()
            HomeNotesScreen(
                parentPadding = innerPadding,
                onAddNote = { navController.navigate(Destinations.Editor.createRoute()) },
                onRecordNote = {
                    voiceEntryViewModel.createHomePlaceholder { placeholderId ->
                        navController.navigate(
                            Destinations.VoiceRecorder.createRoute(
                                noteId = placeholderId,
                                source = RecordingEntryPoint.HOME.name
                            )
                        )
                    }
                },
                onOpenNote = { noteId -> navController.navigate(Destinations.Editor.createRoute(noteId)) },
                onMoveNote = { note ->
                    navController.navigate(Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_NOTE, note.id))
                },
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
                onMoveFolder = { folder ->
                    navController.navigate(
                        Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_FOLDER, folder.id)
                    )
                },
                onAddDescription = { folder ->
                    navController.navigate(Destinations.FolderDescription.createRoute(folder.id))
                },
                onMoveNote = { note ->
                    navController.navigate(Destinations.MoveTo.createRoute(MoveToViewModel.ITEM_TYPE_NOTE, note.id))
                },
                viewModel = hiltViewModel()
            )
        }
        composable(
            route = Destinations.FolderDescription.route,
            arguments = listOf(
                navArgument("folderId") {
                    type = NavType.StringType
                }
            )
        ) {
            FolderDescriptionScreen(
                parentPadding = innerPadding,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Destinations.MoveTo.route,
            arguments = listOf(
                navArgument("itemType") {
                    type = NavType.StringType
                },
                navArgument("itemId") {
                    type = NavType.StringType
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
            val voiceNoteSaved = backStackEntry.savedStateHandle
                .getStateFlow(VOICE_NOTE_SAVED_KEY, false)
                .collectAsStateWithLifecycle()
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
                onOpenVoiceRecorder = { currentNoteId, focusedBlockId ->
                    navController.navigate(
                        Destinations.VoiceRecorder.createRoute(
                            noteId = currentNoteId,
                            source = RecordingEntryPoint.EDITOR.name,
                            focusedBlockId = focusedBlockId
                        )
                    )
                },
                voiceNoteSaved = voiceNoteSaved.value,
                onVoiceNoteSavedConsumed = {
                    backStackEntry.savedStateHandle[VOICE_NOTE_SAVED_KEY] = false
                },
                viewModel = hiltViewModel()
            )
        }
        composable(
            route = Destinations.VoiceRecorder.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("source") {
                    type = NavType.StringType
                    defaultValue = RecordingEntryPoint.EDITOR.name
                },
                navArgument("focusedBlockId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId").orEmpty().ifBlank { null }
            val source = RecordingEntryPoint.fromRoute(
                backStackEntry.arguments?.getString("source").orEmpty()
            )
            val focusedBlockId = backStackEntry.arguments?.getString("focusedBlockId").orEmpty().ifBlank { null }
            VoiceRecorderScreen(
                noteId = noteId,
                source = source,
                focusedBlockId = focusedBlockId,
                onSaved = {
                    if (source == RecordingEntryPoint.HOME && noteId != null) {
                        navController.popBackStack()
                        navController.navigate(Destinations.Editor.createRoute(noteId))
                    } else {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            VOICE_NOTE_SAVED_KEY,
                            true
                        )
                        navController.popBackStack()
                    }
                },
                onDiscarded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Destinations.ExportNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
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

private const val VOICE_NOTE_SAVED_KEY = "voice_note_saved"
