package com.example.notesapp.navigation

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.FakeWebBookmarkMetadataSource
import com.example.notesapp.HiltTestActivity
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.auth.TokenStorage
import com.example.notesapp.di.WebBookmarkModule
import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(WebBookmarkModule::class)
class WebBookmarkAppShellVisualFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @BindValue
    @JvmField
    val metadataSource: WebBookmarkMetadataSource = FakeWebBookmarkMetadataSource(
        WebBookmarkMetadata(title = "Visual fixture", description = "Deterministic metadata")
    )

    private val loggedInAuthManager = FakeLoggedInAuthManager()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun capturesAddBookmarkPageFromAppShell() {
        composeRule.setContent {
            NotesTakingAppTheme {
                AppNavHost(
                    authManager = loggedInAuthManager,
                    onLogin = { _, _ -> }
                )
            }
        }
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("home_add_fab").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_add_fab").performClick()
        composeRule.onNodeWithTag("home_fab_text_note").performClick()
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("editor_basic_blocks_trigger").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_web_bookmark"))
        composeRule.onNodeWithTag("basic_blocks_web_bookmark").performClick()
        composeRule.waitUntil(WAIT_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("web_bookmark_editor_page").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        val pageBounds = composeRule
            .onNodeWithTag("web_bookmark_editor_page")
            .getUnclippedBoundsInRoot()
        assertTrue(pageBounds.right > pageBounds.left && pageBounds.bottom > pageBounds.top)
        composeRule.onNodeWithTag("web_bookmark_editor_page").assertIsDisplayed()
        composeRule.onAllNodesWithTag("app_bottom_navigation").assertCountEquals(0)
        saveActiveWindowCapture("web_bookmark_add_page.png")
    }

    private fun saveActiveWindowCapture(fileName: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        try {
            writeBitmapCapture(fileName, bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun writeBitmapCapture(fileName: String, bitmap: Bitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create visual evidence item $fileName in $DOWNLOAD_CAPTURE_DIRECTORY")
        try {
            context.contentResolver.openOutputStream(uri).use { output ->
                checkNotNull(output)
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Unable to encode visual evidence item $fileName"
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null
                )
            }
        } catch (error: Exception) {
            context.contentResolver.delete(uri, null, null)
            throw error
        }
    }

    private class FakeLoggedInAuthManager : AuthManager(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        tokenStorage = object : TokenStorage(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) = Unit

            override fun getAccessToken(): String? = null

            override fun getRefreshToken(): String? = null

            override fun getIdToken(): String? = null

            override fun clearTokens() = Unit
        }
    ) {
        override val isLoggedIn = MutableStateFlow(true)
        override val logoutMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    }

    private companion object {
        const val WAIT_TIMEOUT_MS = 20_000L
        const val DOWNLOAD_CAPTURE_DIRECTORY = "/sdcard/Download/"
    }
}
