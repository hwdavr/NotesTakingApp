package com.example.notesapp.integration

import androidx.lifecycle.SavedStateHandle
import com.example.notesapp.MainDispatcherRule
import com.example.notesapp.data.bookmark.HttpsWebBookmarkMetadataSource
import com.example.notesapp.domain.bookmark.webBookmarkHost
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkEditorViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration coverage for the production HTTPS metadata data source and its bounded parsing.
 * A real MockWebServer TLS endpoint is used so the shipped OkHttp client, response bounding,
 * and parser run end to end.
 */
class WebBookmarkMetadataIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var server: MockWebServer
    private lateinit var source: HttpsWebBookmarkMetadataSource

    @Before
    fun setUp() {
        val heldCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName("localhost")
            .addSubjectAlternativeName("127.0.0.1")
            .build()
        val certificates = HandshakeCertificates.Builder()
            .heldCertificate(heldCertificate)
            .addTrustedCertificate(heldCertificate.certificate)
            .build()
        server = MockWebServer()
        server.useHttps(certificates.sslSocketFactory(), false)
        server.start()
        source = HttpsWebBookmarkMetadataSource(
            client = OkHttpClient.Builder()
                .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .build()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun savesMetadataFromHttpsHtml() = runBlocking {
        val html = """
            <html><head>
            <meta property="og:title" content="Fixture title">
            <meta property="og:description" content="Fixture description">
            </head><body>Body</body></html>
        """.trimIndent()
        enqueueHtml(html)
        val url = server.url("/page").toString()
        val viewModel = WebBookmarkEditorViewModel(
            metadataSource = source,
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onUrlChanged(url)
        viewModel.save()
        val result = withTimeout(10_000) { viewModel.saveResult.first() }

        assertEquals(url, result.url)
        assertEquals("Fixture title", result.title)
        assertEquals("Fixture description", result.description)
    }

    @Test
    fun usesHostAndBlankFallbackForPartialMetadata() = runBlocking {
        enqueueHtml(
            """<head><meta property="og:title" content="Only a title"></head>"""
        )
        val titleUrl = server.url("/partial").toString()
        val partial = source.fetch(titleUrl)

        assertNotNull(partial)
        assertEquals("Only a title", partial?.title)
        assertEquals("", partial?.description)

        enqueueHtml(
            """<head>
               <meta property="og:title" content="  &#9; ">
               <meta name="description" content="Only a description">
               </head>"""
        )
        val descriptionUrl = server.url("/description-only").toString()
        val descriptionOnly = source.fetch(descriptionUrl)

        assertNotNull(descriptionOnly)
        assertEquals(webBookmarkHost(descriptionUrl), descriptionOnly?.title)
        assertEquals("Only a description", descriptionOnly?.description)
    }

    @Test
    fun sanitizesAndBoundsMetadataReturnedOverHttps() = runBlocking {
        enqueueHtml(
            """<head>
               <meta property="og:title" content="Tom &amp; Jerry <b>again</b>">
               <meta property="og:description" content="Line&#10;break &#x27;quoted&#x27;">
               </head>"""
        )

        val metadata = source.fetch(server.url("/sanitized").toString())

        assertEquals("Tom & Jerry again", metadata?.title)
        assertEquals("Line break 'quoted'", metadata?.description)
    }

    @Test
    fun savesFallbackForMetadataFailures() = runBlocking {
        val requestCountBeforeHttp = server.requestCount
        val httpUrl = server.url("/cleartext").toString().replaceFirst("https://", "http://")
        assertNull(source.fetch(httpUrl))
        assertEquals(requestCountBeforeHttp, server.requestCount)

        server.enqueue(MockResponse().setResponseCode(500).setHeader("Content-Type", "text/html"))
        assertNull(source.fetch(server.url("/server-error").toString()))

        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody("""{"title":"json"}""")
        )
        assertNull(source.fetch(server.url("/json").toString()))

        server.enqueue(
            MockResponse().setHeader("Content-Type", "text/html").setBody("a".repeat(300_000))
        )
        assertNull(source.fetch(server.url("/oversized").toString()))

        server.enqueue(
            MockResponse().setHeader("Content-Type", "text/html").setBody("<html><body>no head</body></html>")
        )
        val malformedUrl = server.url("/malformed").toString()
        val malformed = source.fetch(malformedUrl)
        assertNotNull(malformed)
        assertEquals(webBookmarkHost(malformedUrl), malformed?.title)
        assertEquals("", malformed?.description)

        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/html")
                .setBody("<html></html>")
                .setBodyDelay(3, TimeUnit.SECONDS)
        )
        assertNull(source.fetch(server.url("/timed-out").toString()))

        server.shutdown()
        assertNull(source.fetch("https://127.0.0.1:1/offline"))
    }

    private fun enqueueHtml(html: String) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/html; charset=utf-8")
                .setBody("<html><head>$html</head></html>")
        )
    }
}
