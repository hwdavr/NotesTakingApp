package com.example.notesapp.domain.bookmark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebBookmarkValidationTest {

    @Test
    fun acceptsAbsoluteHttpAndHttpsUrlsAndTrimsSurroundingWhitespace() {
        val result = WebBookmarkUrlValidator.validate("  https://example.com/articles/one  ")

        assertEquals(WebBookmarkValidationResult.Valid("https://example.com/articles/one"), result)
        assertTrue(
            WebBookmarkUrlValidator.validate("http://example.com") is WebBookmarkValidationResult.Valid
        )
        assertTrue(
            WebBookmarkUrlValidator.validate("HTTPS://EXAMPLE.COM") is WebBookmarkValidationResult.Valid
        )
    }

    @Test
    fun rejectsBlankAndWhitespaceOnlyDrafts() {
        assertEquals(WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate(""))
        assertEquals(WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate("   "))
    }

    @Test
    fun rejectsDraftsWithoutSchemeOrHost() {
        assertEquals(WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate("example.com"))
        assertEquals(WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate("https:///path"))
        assertEquals(WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate("https://"))
    }

    @Test
    fun rejectsUnsupportedSchemes() {
        listOf(
            "ftp://example.com/file",
            "file:///sdcard/secret.txt",
            "javascript:alert(1)",
            "content://com.example.notesapp/notes/1",
            "mailto:person@example.com"
        ).forEach { draft ->
            assertEquals(draft, WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate(draft))
        }
    }

    @Test
    fun rejectsCredentialBearingUrls() {
        assertEquals(
            WebBookmarkValidationResult.Invalid,
            WebBookmarkUrlValidator.validate("https://user:secret@example.com/private")
        )
        assertEquals(
            WebBookmarkValidationResult.Invalid,
            WebBookmarkUrlValidator.validate("https://user@example.com/private")
        )
    }

    @Test
    fun rejectsDraftsWithWhitespaceOrMalformedSyntax() {
        assertEquals(
            WebBookmarkValidationResult.Invalid,
            WebBookmarkUrlValidator.validate("https://exa mple.com")
        )
        assertEquals(
            WebBookmarkValidationResult.Invalid,
            WebBookmarkUrlValidator.validate("https://example.com/[bad")
        )
    }

    @Test
    fun rejectsDraftsBeyondTheLengthBound() {
        val oversized = "https://example.com/" + "a".repeat(WEB_BOOKMARK_MAX_URL_LENGTH)
        assertTrue(oversized.length > WEB_BOOKMARK_MAX_URL_LENGTH)

        assertEquals(WebBookmarkValidationResult.Invalid, WebBookmarkUrlValidator.validate(oversized))
    }

    @Test
    fun hostFallbackReturnsTheUrlHostAndFallsBackToTheTrimmedValue() {
        assertEquals("example.com", webBookmarkHost("https://example.com/some/path?q=1"))
        assertEquals("sub.example.co.uk", webBookmarkHost("  http://sub.example.co.uk  "))
        assertEquals("plain-text", webBookmarkHost("plain-text"))
    }

    @Test
    fun metadataFallbackUsesHostTitleAndBlankDescription() {
        val fallback = WebBookmarkMetadata.fallback("https://example.com/page")

        assertEquals("example.com", fallback.title)
        assertEquals("", fallback.description)
    }
}
