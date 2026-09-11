package com.example.notesapp.domain.bookmark

import java.net.URI

/** Bounded, sanitized metadata resolved for a bookmark URL. */
data class WebBookmarkMetadata(
    val title: String,
    val description: String
) {
    companion object {
        /**
         * Deterministic fallback used when page metadata is unavailable (HTTP URL, fetch failure,
         * non-HTML or malformed content): the URL host becomes the title and the description is blank.
         */
        fun fallback(url: String): WebBookmarkMetadata = WebBookmarkMetadata(
            title = webBookmarkHost(url),
            description = ""
        )
    }
}

/** Returns the bounded host portion of a bookmark URL, or the trimmed URL when no host parses. */
fun webBookmarkHost(url: String): String {
    val trimmed = url.trim()
    val host = runCatching { URI(trimmed).host }.getOrNull()
    return host?.takeIf { it.isNotBlank() } ?: trimmed
}
