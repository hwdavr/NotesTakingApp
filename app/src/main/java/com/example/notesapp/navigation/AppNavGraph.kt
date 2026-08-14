package com.example.notesapp.navigation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.ui.common.components.ErrorDialog

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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentRoute != Destinations.Onboarding.route) {
            navController.navigate(Destinations.Onboarding.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    LaunchedEffect(Unit) {
        authManager.logoutMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    val showBottomBar = isLoggedIn &&
        currentRoute?.startsWith("editor") != true &&
        currentRoute?.startsWith("collectionNotes") != true &&
        currentRoute?.startsWith("folderDescription") != true &&
        currentRoute?.startsWith("moveTo") != true &&
        currentRoute?.startsWith("sharedUsers") != true &&
        currentRoute?.startsWith("manageAccess") != true &&
        currentRoute?.startsWith("shareInvite") != true &&
        currentRoute?.startsWith("voiceRecorder") != true &&
        currentRoute !in authRoutes
    var authError by remember { mutableStateOf<String?>(null) }
    authError?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = { authError = null }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        AppNavigationHost(
            navController = navController,
            innerPadding = innerPadding,
            isLoggedIn = isLoggedIn,
            onLogin = onLogin,
            onAuthError = { error -> authError = error }
        )
    }
}
