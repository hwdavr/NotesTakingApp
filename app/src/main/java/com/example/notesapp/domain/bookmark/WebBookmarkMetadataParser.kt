package com.example.notesapp.domain.bookmark

/** Bounded title length applied to every parsed metadata title. */
const val WEB_BOOKMARK_MAX_TITLE_LENGTH = 200

/** Bounded description length applied to every parsed metadata description. */
const val WEB_BOOKMARK_MAX_DESCRIPTION_LENGTH = 500

/**
 * Extracts bounded, sanitized page metadata from HTML.
 *
 * Title candidates resolve in order `og:title`, `twitter:title`, HTML `<title>`, then the URL
 * host. Description candidates resolve in order `og:description`, standard `description`, then
 * blank. Every candidate is stripped of tags, entity-decoded, control characters replaced with
 * spaces, whitespace-collapsed, and length-bounded before it is accepted; blank results fall
 * through to the next candidate.
 */
class WebBookmarkMetadataParser {

    fun parse(html: String, host: String): WebBookmarkMetadata {
        val title = firstSanitized(
            candidates = listOf(
                metaContent(html, value = "og:title"),
                metaContent(html, value = "twitter:title"),
                titleTagContent(html)
            ),
            maxChars = WEB_BOOKMARK_MAX_TITLE_LENGTH
        ).ifBlank { host }
        val description = firstSanitized(
            candidates = listOf(
                metaContent(html, value = "og:description"),
                metaContent(html, value = "description")
            ),
            maxChars = WEB_BOOKMARK_MAX_DESCRIPTION_LENGTH
        )
        return WebBookmarkMetadata(title = title, description = description)
    }

    private fun firstSanitized(candidates: List<String?>, maxChars: Int): String =
        candidates.firstNotNullOfOrNull { candidate ->
            candidate?.let { sanitize(it, maxChars) }.takeIf { !it.isNullOrBlank() }
        }.orEmpty()

    /**
     * Returns the `content` of the first `<meta>` tag whose `property` or `name` equals [value],
     * because Open Graph and Twitter card metadata are published under either attribute.
     */
    private fun metaContent(html: String, value: String): String? = META_TAG.findAll(html).firstNotNullOfOrNull { tag ->
        val attributes = tag.value
        val matches = attributeMatches(attributes, "property", value) ||
            attributeMatches(attributes, "name", value)
        if (matches) contentValue(attributes) else null
    }

    /** Returns the non-blank `content` attribute value of [attributes], or `null`. */
    private fun contentValue(attributes: String): String? {
        val contentMatch = CONTENT_ATTRIBUTE.find(attributes) ?: return null
        return contentMatch.groupValues[2].ifEmpty { contentMatch.groupValues[3] }.ifEmpty { null }
    }

    private fun titleTagContent(html: String): String? = TITLE_TAG.find(html)?.groupValues?.get(1)

    private fun sanitize(raw: String, maxChars: Int): String {
        val withoutTags = TAG.replace(raw, " ")
        val decoded = decodeEntities(withoutTags)
        val withoutControls = decoded.map { if (it < ' ' || it == '\u007F') ' ' else it }.joinToString("")
        return withoutControls.replace(WHITESPACE_RUN, " ").trim().take(maxChars)
    }

    private fun decodeEntities(raw: String): String = ENTITY.replace(raw) { match ->
        val hex = match.groupValues[1]
        val decimal = match.groupValues[2]
        val named = match.groupValues[3]
        when {
            hex.isNotEmpty() -> hex.toIntOrNull(16)?.toChar()?.toString() ?: match.value
            decimal.isNotEmpty() -> decimal.toIntOrNull()?.toChar()?.toString() ?: match.value
            named.isNotEmpty() -> when (named) {
                "amp" -> "&"
                "lt" -> "<"
                "gt" -> ">"
                "quot" -> "\""
                "apos" -> "'"
                "nbsp" -> " "
                else -> match.value
            }
            else -> match.value
        }
    }

    private companion object {
        val META_TAG = Regex("""<meta\b(?:[^>"']|"[^"]*"|'[^']*')*>""", RegexOption.IGNORE_CASE)
        val TITLE_TAG = Regex(
            """<title\b[^>]*>(.*?)</title\s*>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val TAG = Regex("""<[^>]*>""")
        val CONTENT_ATTRIBUTE = Regex("""\bcontent\s*=\s*("([^"]*)"|'([^']*)')""", RegexOption.IGNORE_CASE)
        val WHITESPACE_RUN = Regex("""\s+""")
        val ENTITY = Regex("""&#x([0-9a-fA-F]+);|&#([0-9]+);|&(amp|lt|gt|quot|apos|nbsp);""")

        /** True when the tag declares `[attribute]="value"` or `[attribute]='value'`. */
        fun attributeMatches(tag: String, attribute: String, value: String): Boolean {
            val pattern = Regex("""\b${Regex.escape(attribute)}\s*=\s*("([^"]*)"|'([^']*)')""", RegexOption.IGNORE_CASE)
            val match = pattern.find(tag) ?: return false
            val quoted = match.groupValues[2].ifEmpty { match.groupValues[3] }
            return quoted.equals(value, ignoreCase = true)
        }
    }
}
