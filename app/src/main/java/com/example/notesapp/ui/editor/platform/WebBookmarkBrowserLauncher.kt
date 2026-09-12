package com.example.notesapp.ui.editor.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.notesapp.domain.bookmark.WebBookmarkUrlValidator
import com.example.notesapp.domain.bookmark.WebBookmarkValidationResult

sealed interface WebBookmarkOpenResult {
    data object Opened : WebBookmarkOpenResult
    data object InvalidUrl : WebBookmarkOpenResult
    data object NoHandler : WebBookmarkOpenResult
}

/** Owns the Android external-browser boundary for persisted bookmark URLs. */
class WebBookmarkBrowserLauncher(
    private val context: Context,
    private val packageNameOverride: String? = null
) {
    fun open(url: String): WebBookmarkOpenResult {
        val validatedUrl = when (val validation = WebBookmarkUrlValidator.validate(url)) {
            WebBookmarkValidationResult.Invalid -> return WebBookmarkOpenResult.InvalidUrl
            is WebBookmarkValidationResult.Valid -> validation.url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validatedUrl)).apply {
            packageNameOverride?.let(::setPackage)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val handler = intent.resolveActivity(context.packageManager) ?: return WebBookmarkOpenResult.NoHandler
        return try {
            context.startActivity(intent.setPackage(handler.packageName))
            WebBookmarkOpenResult.Opened
        } catch (_: ActivityNotFoundException) {
            WebBookmarkOpenResult.NoHandler
        } catch (_: SecurityException) {
            WebBookmarkOpenResult.NoHandler
        }
    }
}
