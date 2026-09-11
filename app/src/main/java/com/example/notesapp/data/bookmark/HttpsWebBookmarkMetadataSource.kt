package com.example.notesapp.data.bookmark

import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataParser
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import com.example.notesapp.domain.bookmark.webBookmarkHost
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/** Maximum number of HTML bytes read from a single page response. */
private const val MAX_HTML_BYTES = 262_144L

private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"

/**
 * Isolated HTTPS-only metadata data source for web bookmarks.
 *
 * Uses a dedicated [OkHttpClient] that carries no application authentication and never follows
 * HTTPS-to-HTTP downgrades, so untrusted redirect locations cannot force cleartext traffic.
 * Every response is bounded by [MAX_HTML_BYTES]; timeouts and every failure surface as `null`
 * so the caller can save with host/blank fallback.
 */
class HttpsWebBookmarkMetadataSource(
    private val client: OkHttpClient = defaultClient(),
    private val parser: WebBookmarkMetadataParser = WebBookmarkMetadataParser()
) : WebBookmarkMetadataSource {

    override suspend fun fetch(url: String): WebBookmarkMetadata? {
        if (!url.startsWith("https://", ignoreCase = true)) return null
        return withContext(Dispatchers.IO) {
            runCatching { fetchBlocking(url) }.getOrNull()
        }
    }

    private fun fetchBlocking(url: String): WebBookmarkMetadata? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        return client.newCall(request).execute().use { response -> metadataOrNull(response, url) }
    }

    /** Parses metadata from an eligible HTML response and returns `null` for every rejection. */
    private fun metadataOrNull(response: Response, url: String): WebBookmarkMetadata? {
        val contentType = response.header("Content-Type").orEmpty()
        val isHtml = contentType.substringBefore(';').trim().equals("text/html", ignoreCase = true)
        val body = if (response.isSuccessful && isHtml) response.body else null
        val html = body
            ?.takeIf { it.contentLength() <= MAX_HTML_BYTES }
            ?.let { readBoundedBody(it) }
        val charset = body?.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        return html?.let { parser.parse(String(it, charset), webBookmarkHost(url)) }
    }

    /** Reads up to [MAX_HTML_BYTES] bytes; returns `null` when the body exceeds that bound. */
    private fun readBoundedBody(body: okhttp3.ResponseBody): ByteArray? {
        body.byteStream().use { stream ->
            val buffer = ByteArray(MAX_HTML_BYTES.toInt() + 1)
            var total = 0
            while (total < buffer.size) {
                val read = stream.read(buffer, total, buffer.size - total)
                if (read == -1) return buffer.copyOf(total)
                total += read
            }
            // A full buffer means the body is at least MAX_HTML_BYTES + 1 bytes: oversized.
            return null
        }
    }

    private companion object {
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(false)
            .build()
    }
}
