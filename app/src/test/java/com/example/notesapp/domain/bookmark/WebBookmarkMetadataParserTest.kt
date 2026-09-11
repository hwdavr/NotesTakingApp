package com.example.notesapp.domain.bookmark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebBookmarkMetadataParserTest {

    private val parser = WebBookmarkMetadataParser()

    @Test
    fun prefersOpenGraphTitleAndDescription() {
        val html = """
            <html><head>
            <title>Plain title</title>
            <meta property="og:title" content="Open Graph title" />
            <meta name="description" content="Plain description" />
            <meta property="og:description" content="Open Graph description" />
            </head></html>
        """.trimIndent()

        val metadata = parser.parse(html, host = "example.com")

        assertEquals("Open Graph title", metadata.title)
        assertEquals("Open Graph description", metadata.description)
    }

    @Test
    fun fallsBackThroughTwitterThenHtmlTitleTag() {
        val twitter = parser.parse(
            """<head><meta name="twitter:title" content="Twitter title"></head>""",
            host = "example.com"
        )
        assertEquals("Twitter title", twitter.title)

        val titleTag = parser.parse(
            "<head><title>  Document   title  </title></head>",
            host = "example.com"
        )
        assertEquals("Document title", titleTag.title)
    }

    @Test
    fun usesHostWhenNoTitleCandidateIsUsable() {
        val metadata = parser.parse("<html><body>No head</body></html>", host = "fallback.example")

        assertEquals("fallback.example", metadata.title)
        assertEquals("", metadata.description)
    }

    @Test
    fun skipsWhitespaceOnlyCandidatesAndUsesTheNextOne() {
        val html = """
            <head>
            <meta property="og:title" content="   ">
            <meta name="twitter:title" content="Real title">
            <meta property="og:description" content="&#9; &#10; ">
            <meta name="description" content="Real description">
            </head>
        """.trimIndent()

        val metadata = parser.parse(html, host = "example.com")

        assertEquals("Real title", metadata.title)
        assertEquals("Real description", metadata.description)
    }

    @Test
    fun sanitizesMarkupEntitiesAndControlCharacters() {
        val html = """
            <head>
            <meta property="og:title" content="Tom &amp; Jerry <b>ride</b> again">
            <meta property="og:description" content="Line&#10;break &#x27;quoted&#x27; &nbsp; spaced">
            </head>
        """.trimIndent()

        val metadata = parser.parse(html, host = "example.com")

        assertEquals("Tom & Jerry ride again", metadata.title)
        assertEquals("Line break 'quoted' spaced", metadata.description)
    }

    @Test
    fun boundsTitleAndDescriptionLengths() {
        val longTitle = "t".repeat(WEB_BOOKMARK_MAX_TITLE_LENGTH + 75)
        val longDescription = "d".repeat(WEB_BOOKMARK_MAX_DESCRIPTION_LENGTH + 75)
        val html = """
            <head>
            <meta property="og:title" content="$longTitle">
            <meta property="og:description" content="$longDescription">
            </head>
        """.trimIndent()

        val metadata = parser.parse(html, host = "example.com")

        assertEquals(WEB_BOOKMARK_MAX_TITLE_LENGTH, metadata.title.length)
        assertEquals(WEB_BOOKMARK_MAX_DESCRIPTION_LENGTH, metadata.description.length)
        assertTrue(metadata.title.all { it == 't' })
        assertTrue(metadata.description.all { it == 'd' })
    }

    @Test
    fun acceptsAttributeOrderAndQuotingVariations() {
        val html = """
            <head>
            <meta content='Reversed order' property='og:title'>
            <meta content="Single quoted description" name='description'>
            </head>
        """.trimIndent()

        val metadata = parser.parse(html, host = "example.com")

        assertEquals("Reversed order", metadata.title)
        assertEquals("Single quoted description", metadata.description)
    }

    @Test
    fun partialMetadataResolvesTitleAndDescriptionIndependently() {
        val html = """<head><meta property="og:title" content="Only a title"></head>"""

        val metadata = parser.parse(html, host = "example.com")

        assertEquals("Only a title", metadata.title)
        assertEquals("", metadata.description)
    }
}
