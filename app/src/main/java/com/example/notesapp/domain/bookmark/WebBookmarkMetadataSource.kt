package com.example.notesapp.domain.bookmark

/**
 * Best-effort page metadata retrieval for a validated bookmark URL.
 *
 * Returns `null` whenever enrichment is unavailable (non-HTTPS URL, network error, timeout,
 * non-HTML or non-2xx response, oversized or malformed body); callers must fall back to
 * [WebBookmarkMetadata.fallback] without blocking the save.
 */
interface WebBookmarkMetadataSource {
    suspend fun fetch(url: String): WebBookmarkMetadata?
}
