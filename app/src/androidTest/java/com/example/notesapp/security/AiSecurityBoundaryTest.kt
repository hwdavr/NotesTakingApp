package com.example.notesapp.security

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.components.MermaidBlockCard
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AiSecurityBoundaryTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun productionMermaidWebViewDisablesUnneededCapabilities() {
        composeRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = EditorBlock.MermaidBlock(
                        id = "security-boundary",
                        title = "Boundary test",
                        code = "graph TD\nA[Input] --> B[Output]"
                    ),
                    isEditable = false,
                    onUpdateTitle = {},
                    onUpdateCode = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeRule.waitForIdle()

        lateinit var settingsSnapshot: WebViewSettingsSnapshot
        composeRule.runOnIdle {
            val webView = findWebView(composeRule.activity.window.decorView)
                ?: error("Production Mermaid WebView was not created")
            settingsSnapshot = WebViewSettingsSnapshot(
                javaScriptEnabled = webView.settings.javaScriptEnabled,
                domStorageEnabled = webView.settings.domStorageEnabled,
                allowFileAccess = webView.settings.allowFileAccess,
                allowContentAccess = webView.settings.allowContentAccess,
                blockNetworkLoads = webView.settings.blockNetworkLoads,
                usesFileBaseUrl = webView.url.orEmpty().startsWith("file:")
            )
        }

        assertFalse(settingsSnapshot.javaScriptEnabled)
        assertFalse(settingsSnapshot.domStorageEnabled)
        assertFalse(settingsSnapshot.allowFileAccess)
        assertFalse(settingsSnapshot.allowContentAccess)
        assertTrue(settingsSnapshot.blockNetworkLoads)
        assertFalse(settingsSnapshot.usesFileBaseUrl)
    }

    private fun findWebView(root: View): WebView? {
        if (root is WebView) return root
        if (root !is ViewGroup) return null
        for (index in 0 until root.childCount) {
            findWebView(root.getChildAt(index))?.let { return it }
        }
        return null
    }

    private data class WebViewSettingsSnapshot(
        val javaScriptEnabled: Boolean,
        val domStorageEnabled: Boolean,
        val allowFileAccess: Boolean,
        val allowContentAccess: Boolean,
        val blockNetworkLoads: Boolean,
        val usesFileBaseUrl: Boolean
    )
}
