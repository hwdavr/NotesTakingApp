package com.example.notesapp.ui.editor.components

/**
 * Token categories produced by the lightweight lexical syntax highlighter.
 * These map to theme-appropriate colors in the Composable layer.
 */
enum class CodeTokenType {
    KEYWORD,
    TYPE,
    STRING,
    COMMENT,
    NUMBER,
    OPERATOR,
    PLAIN_TEXT
}

/**
 * A single tokenized range over the source code. Ranges are [start, endExclusive)
 * and are emitted in source order without gaps.
 */
data class CodeToken(
    val type: CodeTokenType,
    val start: Int,
    val endExclusive: Int
)

/**
 * Pure, UI-free client-side syntax highlighter. It tokenizes code for the 14
 * supported languages without any Android dependencies so it can be verified
 * with plain JVM unit tests.
 */
object CodeSyntaxHighlighter {

    private data class LanguageSpec(
        val lineComment: String?,
        val blockCommentStart: String?,
        val blockCommentEnd: String?,
        val stringDelimiters: Set<Char>,
        val backtickString: Boolean,
        val tripleQuotedString: Boolean,
        val keywords: Set<String>,
        val types: Set<String>
    )

    private val specs = mapOf(
        "kotlin" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = true,
            keywords = setOf(
                "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
                "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
                "true", "try", "typealias", "val", "var", "when", "while", "by", "catch", "constructor",
                "dynamic", "finally", "get", "import", "init", "set", "where", "actual", "abstract",
                "annotation", "companion", "const", "crossinline", "data", "enum", "expect", "external",
                "final", "infix", "inline", "inner", "internal", "lateinit", "noinline", "open",
                "operator", "out", "override", "private", "protected", "public", "reified", "sealed",
                "suspend", "tailrec", "vararg"
            ),
            types = setOf(
                "Int", "Long", "Short", "Byte", "Double", "Float", "Boolean", "Char", "String",
                "Unit", "Any", "Nothing", "Array", "List", "Set", "Map", "MutableList", "MutableSet",
                "MutableMap", "Sequence", "IntArray", "LongArray", "ShortArray", "ByteArray",
                "DoubleArray", "FloatArray", "BooleanArray", "CharArray"
            )
        ),
        "java" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf(
                "abstract", "assert", "break", "case", "catch", "class", "continue", "default", "do",
                "else", "enum", "extends", "final", "finally", "for", "if", "implements", "import",
                "instanceof", "interface", "native", "new", "package", "private", "protected", "public",
                "return", "static", "strictfp", "super", "switch", "synchronized", "this", "throw",
                "throws", "transient", "try", "volatile", "while", "true", "false", "null", "var",
                "record", "sealed", "permits", "yield"
            ),
            types = setOf(
                "boolean", "byte", "char", "double", "float", "int", "long", "short", "void",
                "String", "Object", "Integer", "Long", "Boolean", "Double", "Float", "Character",
                "Byte", "Short"
            )
        ),
        "python" to LanguageSpec(
            lineComment = "#",
            blockCommentStart = null,
            blockCommentEnd = null,
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = true,
            keywords = setOf(
                "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del",
                "elif", "else", "except", "finally", "for", "from", "global", "if", "import", "in",
                "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while",
                "with", "yield", "True", "False", "None"
            ),
            types = setOf("str", "int", "float", "bool", "list", "dict", "set", "tuple", "bytes", "object")
        ),
        "javascript" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = true,
            tripleQuotedString = false,
            keywords = setOf(
                "async", "await", "break", "case", "catch", "class", "const", "continue", "debugger",
                "default", "delete", "do", "else", "export", "extends", "finally", "for", "function",
                "if", "import", "in", "instanceof", "let", "new", "of", "return", "static", "super",
                "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield",
                "true", "false", "null", "undefined"
            ),
            types = setOf("String", "Number", "Boolean", "Object", "Array", "Function", "Promise", "Map", "Set")
        ),
        "typescript" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = true,
            tripleQuotedString = false,
            keywords = setOf(
                "abstract", "any", "as", "asserts", "async", "await", "break", "case", "catch", "class",
                "const", "continue", "debugger", "declare", "default", "delete", "do", "else", "enum",
                "export", "extends", "finally", "for", "from", "function", "get", "if", "implements",
                "import", "in", "infer", "instanceof", "interface", "is", "keyof", "let", "namespace",
                "never", "new", "of", "private", "protected", "public", "readonly", "return", "set",
                "static", "super", "switch", "this", "throw", "try", "type", "typeof", "undefined",
                "unknown", "var", "void", "while", "with", "yield", "true", "false", "null", "satisfies"
            ),
            types = setOf("string", "number", "boolean", "object", "Array", "Promise", "Map", "Set", "Record")
        ),
        "html" to LanguageSpec(
            lineComment = null,
            blockCommentStart = "<!--",
            blockCommentEnd = "-->",
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf("DOCTYPE", "doctype", "html", "head", "body", "meta", "link", "title", "style", "script"),
            types = setOf(
                "div", "span", "p", "a", "img", "ul", "ol", "li", "h1", "h2", "h3", "h4", "h5", "h6",
                "table", "tr", "td", "th", "form", "input", "button", "select", "option", "section",
                "article", "header", "footer", "nav", "main", "aside"
            )
        ),
        "css" to LanguageSpec(
            lineComment = null,
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf("media", "import", "supports", "keyframes", "font-face", "important"),
            types = emptySet()
        ),
        "json" to LanguageSpec(
            lineComment = null,
            blockCommentStart = null,
            blockCommentEnd = null,
            stringDelimiters = setOf('"'),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf("true", "false", "null"),
            types = emptySet()
        ),
        "sql" to LanguageSpec(
            lineComment = "--",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('\''),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf(
                "select", "from", "where", "insert", "into", "values", "update", "set", "delete",
                "create", "table", "alter", "drop", "index", "view", "join", "inner", "left", "right",
                "full", "outer", "on", "group", "by", "order", "having", "limit", "offset", "and", "or",
                "not", "null", "is", "in", "like", "between", "exists", "case", "when", "then", "else",
                "end", "union", "all", "distinct", "as", "primary", "key", "foreign", "references",
                "default", "unique", "check", "begin", "commit", "rollback", "transaction"
            ),
            types = setOf(
                "int", "integer", "bigint", "smallint", "decimal", "numeric", "real", "double",
                "float", "char", "varchar", "text", "date", "time", "timestamp", "boolean", "blob"
            )
        ),
        "shell" to LanguageSpec(
            lineComment = "#",
            blockCommentStart = null,
            blockCommentEnd = null,
            stringDelimiters = setOf('"', '\''),
            backtickString = true,
            tripleQuotedString = false,
            keywords = setOf(
                "if", "then", "else", "elif", "fi", "for", "while", "until", "do", "done", "case",
                "esac", "function", "in", "select", "time", "coproc", "return", "exit", "break",
                "continue", "echo", "read", "local", "export", "source", "set", "unset", "shift"
            ),
            types = emptySet()
        ),
        "c_cpp" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf(
                "auto", "break", "case", "char", "const", "continue", "default", "do", "double",
                "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long",
                "register", "restrict", "return", "short", "signed", "sizeof", "static", "struct",
                "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "class",
                "namespace", "template", "typename", "public", "private", "protected", "virtual",
                "new", "delete", "this", "try", "catch", "throw", "constexpr", "nullptr", "true",
                "false", "using", "operator", "friend", "override", "final", "static_cast",
                "dynamic_cast", "const_cast", "reinterpret_cast"
            ),
            types = setOf("bool", "string", "vector", "map", "set", "pair", "shared_ptr", "unique_ptr", "size_t")
        ),
        "rust" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = false,
            tripleQuotedString = false,
            keywords = setOf(
                "as", "async", "await", "break", "const", "continue", "crate", "dyn", "else", "enum",
                "extern", "false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod",
                "move", "mut", "pub", "ref", "return", "self", "Self", "static", "struct", "super",
                "trait", "true", "type", "unsafe", "use", "where", "while", "abstract", "become", "box",
                "do", "final", "macro", "override", "priv", "typeof", "unsized", "virtual", "yield"
            ),
            types = setOf(
                "bool", "char", "str", "String", "i8", "i16", "i32", "i64", "i128", "isize", "u8",
                "u16", "u32", "u64", "u128", "usize", "f32", "f64", "Vec", "Option", "Result", "Box"
            )
        ),
        "go" to LanguageSpec(
            lineComment = "//",
            blockCommentStart = "/*",
            blockCommentEnd = "*/",
            stringDelimiters = setOf('"', '\''),
            backtickString = true,
            tripleQuotedString = false,
            keywords = setOf(
                "break", "case", "chan", "const", "continue", "default", "defer", "else", "fallthrough",
                "for", "func", "go", "goto", "if", "import", "interface", "map", "package", "range",
                "return", "select", "struct", "switch", "type", "var", "true", "false", "nil"
            ),
            types = setOf(
                "bool", "byte", "complex64", "complex128", "error", "float32", "float64", "int", "int8",
                "int16", "int32", "int64", "rune", "string", "uint", "uint8", "uint16", "uint32",
                "uint64", "uintptr"
            )
        )
    )

    /**
     * Returns the number of visible lines for a code string. An empty string
     * is a single line; each newline adds one more line.
     */
    fun lineCount(code: String): Int = code.count { it == '\n' } + 1

    /**
     * Returns the gutter sequence 1..[lineCount] used by the code block card.
     */
    fun lineNumbers(code: String): IntRange = 1..lineCount(code)

    /**
     * Tokenizes [code] using the lexical rules for [languageKey]. Unknown or
     * blank language keys fall back to a single plain-text token.
     */
    fun tokenize(code: String, languageKey: String): List<CodeToken> {
        if (code.isEmpty()) return emptyList()
        val spec = specFor(languageKey)
        if (spec == null) return listOf(CodeToken(CodeTokenType.PLAIN_TEXT, 0, code.length))

        val tokens = mutableListOf<CodeToken>()
        var index = 0
        while (index < code.length) {
            val token = nextToken(code, index, spec) ?: break
            tokens += token
            index = token.endExclusive
        }
        return tokens
    }

    private fun specFor(languageKey: String): LanguageSpec? {
        val normalized = languageKey.trim().lowercase().replace(" ", "")
        return specs[normalized]
    }

    private fun nextToken(code: String, index: Int, spec: LanguageSpec): CodeToken? {
        return scanLineComment(code, index, spec)
            ?: scanBlockComment(code, index, spec)
            ?: scanTripleQuotedString(code, index, spec)
            ?: scanQuotedString(code, index, spec)
            ?: scanNumber(code, index)
            ?: scanIdentifier(code, index, spec)
            ?: scanOperator(code, index)
    }

    private fun scanLineComment(code: String, index: Int, spec: LanguageSpec): CodeToken? {
        val prefix = spec.lineComment ?: return null
        if (!code.startsWith(prefix, index)) return null
        val end = code.indexOf('\n', index).let { if (it < 0) code.length else it }
        return CodeToken(CodeTokenType.COMMENT, index, end)
    }

    private fun scanBlockComment(code: String, index: Int, spec: LanguageSpec): CodeToken? {
        val prefix = spec.blockCommentStart ?: return null
        if (!code.startsWith(prefix, index)) return null
        val close = spec.blockCommentEnd?.let { code.indexOf(it, index + prefix.length) } ?: -1
        val end = if (close < 0) code.length else close + (spec.blockCommentEnd?.length ?: 0)
        return CodeToken(CodeTokenType.COMMENT, index, end)
    }

    private fun scanTripleQuotedString(code: String, index: Int, spec: LanguageSpec): CodeToken? {
        if (!spec.tripleQuotedString) return null
        val char = code[index]
        if (char != '"' && char != '\'') return null
        val delimiter = char.toString().repeat(3)
        if (!code.startsWith(delimiter, index)) return null
        val end = code.indexOf(delimiter, index + 3).let { if (it < 0) code.length else it + 3 }
        return CodeToken(CodeTokenType.STRING, index, end)
    }

    private fun scanQuotedString(code: String, index: Int, spec: LanguageSpec): CodeToken? {
        val char = code[index]
        val isBacktick = char == '`' && spec.backtickString
        if (char !in spec.stringDelimiters && !isBacktick) return null
        var cursor = index + 1
        while (cursor < code.length) {
            val current = code[cursor]
            if (current == '\\' && char != '`') {
                cursor += 2
                continue
            }
            if (current == char) return CodeToken(CodeTokenType.STRING, index, cursor + 1)
            cursor++
        }
        return CodeToken(CodeTokenType.STRING, index, code.length)
    }

    private fun scanNumber(code: String, index: Int): CodeToken? {
        val char = code[index]
        val startsNumber = char.isDigit() || (char == '.' && code.getOrNull(index + 1)?.isDigit() == true)
        if (!startsNumber) return null
        return CodeToken(CodeTokenType.NUMBER, index, numberEnd(code, index))
    }

    private fun numberEnd(code: String, start: Int): Int {
        var index = start
        if (code.startsWith("0x", index, ignoreCase = true)) {
            index += 2
            while (index < code.length && isHexDigit(code[index])) {
                index++
            }
            return index
        }
        var hasDot = false
        while (index < code.length) {
            val char = code[index]
            when {
                char.isDigit() -> index++
                char == '.' && !hasDot && code.getOrNull(index + 1)?.isDigit() == true -> {
                    hasDot = true
                    index++
                }
                isExponentStart(code, index) -> {
                    index++
                    if (code.getOrNull(index) == '+' || code.getOrNull(index) == '-') index++
                    while (index < code.length && code[index].isDigit()) index++
                }
                else -> return index
            }
        }
        return index
    }

    private fun scanIdentifier(code: String, index: Int, spec: LanguageSpec): CodeToken? {
        val char = code[index]
        if (!char.isLetter() && char != '_') return null
        val end = identifierEnd(code, index)
        val word = code.substring(index, end)
        val type = when {
            spec.keywords.contains(word) -> CodeTokenType.KEYWORD
            spec.types.contains(word) -> CodeTokenType.TYPE
            else -> CodeTokenType.PLAIN_TEXT
        }
        return CodeToken(type, index, end)
    }

    private fun scanOperator(code: String, index: Int): CodeToken? {
        val char = code[index]
        if (char.isWhitespace()) return CodeToken(CodeTokenType.PLAIN_TEXT, index, index + 1)
        val two = if (index + 1 < code.length) "$char${code[index + 1]}" else null
        val end = if (two != null && two in multiCharOperators) index + 2 else index + 1
        return CodeToken(CodeTokenType.OPERATOR, index, end)
    }

    private fun identifierEnd(code: String, start: Int): Int {
        var index = start
        while (index < code.length && (code[index].isLetterOrDigit() || code[index] == '_')) {
            index++
        }
        return index
    }

    private fun isHexDigit(char: Char): Boolean = char.isDigit() || char.lowercaseChar() in 'a'..'f'

    private fun isExponentStart(code: String, index: Int): Boolean {
        val char = code[index]
        if (char != 'e' && char != 'E') return false
        val next = code.getOrNull(index + 1) ?: return false
        return next.isDigit() || next == '+' || next == '-'
    }

    private val multiCharOperators = setOf(
        "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=", "%=", "->", "=>",
        "::", "?.", "?:", "===", "!==", "<<", ">>", "**", "//", ".."
    )
}
