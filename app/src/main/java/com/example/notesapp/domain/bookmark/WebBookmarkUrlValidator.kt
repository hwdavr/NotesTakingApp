package com.example.notesapp.domain.bookmark

import java.net.URI

/** Bounded length applied to every bookmark URL before it is stored, exported, or opened. */
const val WEB_BOOKMARK_MAX_URL_LENGTH = 2048

/** Result of validating a raw bookmark URL draft. */
sealed interface WebBookmarkValidationResult {
    /** The URL is a credential-free absolute HTTP(S) URL; [url] is the trimmed value. */
    data class Valid(val url: String) : WebBookmarkValidationResult

    /** The draft is blank, malformed, unsafe, or over the length bound. */
    data object Invalid : WebBookmarkValidationResult
}

/**
 * Validates untrusted bookmark URLs before any metadata request, persistence, or external launch.
 *
 * Accepted URLs must be absolute `http`/`https` URLs with a host, no embedded credentials
 * (userinfo), no whitespace, and a length within [WEB_BOOKMARK_MAX_URL_LENGTH].
 */
object WebBookmarkUrlValidator {

    fun validate(rawUrl: String): WebBookmarkValidationResult {
        val trimmed = rawUrl.trim()
        return if (isValid(trimmed)) {
            WebBookmarkValidationResult.Valid(trimmed)
        } else {
            WebBookmarkValidationResult.Invalid
        }
    }

    private fun isValid(trimmed: String): Boolean {
        if (trimmed.isEmpty() || trimmed.length > WEB_BOOKMARK_MAX_URL_LENGTH) return false
        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase()
        val hasSupportedScheme = scheme == "http" || scheme == "https"
        val hasHost = !uri.host.isNullOrBlank()
        val isCredentialAndSpaceFree = uri.userInfo == null && trimmed.none { it.isWhitespace() }
        return hasSupportedScheme && hasHost && isCredentialAndSpaceFree
    }
}
