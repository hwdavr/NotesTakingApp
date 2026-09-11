package com.example.notesapp

import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import java.util.Collections

/** Deterministic metadata boundary for instrumented tests; records every requested URL. */
class FakeWebBookmarkMetadataSource(
    private val metadata: WebBookmarkMetadata? = null
) : WebBookmarkMetadataSource {

    val fetchedUrls: MutableList<String> = Collections.synchronizedList(mutableListOf())

    val fetchCount: Int
        get() = fetchedUrls.size

    override suspend fun fetch(url: String): WebBookmarkMetadata? {
        fetchedUrls += url
        return metadata ?: WebBookmarkMetadata.fallback(url)
    }
}
