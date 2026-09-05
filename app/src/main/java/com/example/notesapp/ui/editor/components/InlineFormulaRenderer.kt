package com.example.notesapp.ui.editor.components

/**
 * A small deterministic formula renderer used by the editor and exporters.
 *
 * It intentionally renders to readable Unicode/text instead of relying on a
 * network service or a platform-specific math engine. The source remains the
 * persisted/exported value while [displayText] is the editor preview.
 */
object InlineFormulaRenderer {
    fun render(source: String): FormulaRenderResult {
        val trimmed = source.trim()
        if (trimmed.isEmpty()) {
            return FormulaRenderResult.invalid(trimmed, FormulaRenderError.EMPTY)
        }
        if (!hasBalancedDelimiters(trimmed)) {
            return FormulaRenderResult.invalid(trimmed, FormulaRenderError.UNBALANCED_DELIMITER)
        }

        return runCatching {
            val parser = FormulaParser(trimmed)
            val display = parser.parse()
            if (display.isBlank()) {
                FormulaRenderResult.invalid(trimmed, FormulaRenderError.EMPTY)
            } else {
                FormulaRenderResult.valid(trimmed, display)
            }
        }.getOrElse { error ->
            val renderError = when (error) {
                is UnsupportedFormulaCommandException -> FormulaRenderError.UNSUPPORTED_COMMAND
                else -> FormulaRenderError.MALFORMED_EXPRESSION
            }
            FormulaRenderResult.invalid(trimmed, renderError)
        }
    }

    private fun hasBalancedDelimiters(source: String): Boolean {
        val stack = ArrayDeque<Char>()
        val pairs = mapOf('}' to '{', ')' to '(', ']' to '[')
        source.forEach { character ->
            when (character) {
                '{', '(', '[' -> stack.addLast(character)
                '}', ')', ']' -> if (stack.removeLastOrNull() != pairs[character]) return false
            }
        }
        return stack.isEmpty()
    }

    private class FormulaParser(private val source: String) {
        private var index = 0

        fun parse(): String {
            val result = parseUntil()
            if (index != source.length) {
                throw MalformedFormulaException()
            }
            return result.trim()
        }

        private fun parseUntil(endCharacter: Char? = null): String {
            val output = StringBuilder()
            while (index < source.length && source[index] != endCharacter) {
                when (val character = source[index]) {
                    '\\' -> output.append(parseCommand())
                    '{' -> output.append(parseGroup())
                    '^', '_' -> {
                        index++
                        val token = parseScriptToken()
                        val script = if (character == '^') token.toSuperscript() else token.toSubscript()
                        output.append(script)
                    }
                    '}' -> throw MalformedFormulaException()
                    ')', ']' -> {
                        output.append(character)
                        index++
                    }
                    else -> {
                        output.append(character)
                        index++
                    }
                }
            }
            return output.toString()
        }

        private fun parseCommand(): String {
            index++
            if (index >= source.length) return malformed()

            val commandStart = index
            while (index < source.length && source[index].isLetter()) index++
            if (commandStart == index) return parseCommandSymbol()

            val command = source.substring(commandStart, index)
            return parseNamedCommand(command)
        }

        private fun parseCommandSymbol(): String {
            val symbol = source[index++]
            return commandSymbols[symbol]?.toString() ?: unsupported()
        }

        private fun parseNamedCommand(command: String): String {
            return when (command) {
                "frac", "dfrac", "tfrac" -> {
                    val numerator = parseRequiredGroup()
                    val denominator = parseRequiredGroup()
                    "$numerator⁄$denominator"
                }
                "sqrt" -> "√${parseRequiredGroup()}"
                "text", "mathrm", "mathbf", "mathit", "operatorname" -> parseRequiredGroup()
                "left", "right" -> {
                    skipOptionalWhitespace()
                    ""
                }
                "quad", "qquad", "enspace", ";", ",", ":", "!" -> " "
                "cdot" -> "·"
                "times" -> "×"
                "div" -> "÷"
                "pm" -> "±"
                "mp" -> "∓"
                "le", "leq" -> "≤"
                "ge", "geq" -> "≥"
                "neq" -> "≠"
                "approx" -> "≈"
                "to", "rightarrow" -> "→"
                "leftarrow" -> "←"
                "infty" -> "∞"
                "sum" -> "∑"
                "prod" -> "∏"
                "int" -> "∫"
                "partial" -> "∂"
                "nabla" -> "∇"
                "sin", "cos", "tan", "log", "ln", "exp", "lim" -> command
                else -> greekSymbols[command] ?: unsupported()
            }
        }

        private fun malformed(): Nothing = throw MalformedFormulaException()

        private fun unsupported(): Nothing = throw UnsupportedFormulaCommandException()

        private fun parseRequiredGroup(): String {
            skipOptionalWhitespace()
            if (index >= source.length || source[index] != '{') {
                throw MalformedFormulaException()
            }
            return parseGroup()
        }

        private fun parseGroup(): String {
            if (source.getOrNull(index) != '{') throw MalformedFormulaException()
            index++
            val result = parseUntil('}')
            if (source.getOrNull(index) != '}') throw MalformedFormulaException()
            index++
            return result
        }

        private fun parseScriptToken(): String {
            skipOptionalWhitespace()
            if (index >= source.length) throw MalformedFormulaException()
            return if (source[index] == '{') {
                parseGroup()
            } else if (source[index] == '\\') {
                parseCommand()
            } else {
                val start = index
                index++
                source.substring(start, index)
            }
        }

        private fun skipOptionalWhitespace() {
            while (index < source.length && source[index].isWhitespace()) index++
        }
    }

    private class UnsupportedFormulaCommandException : IllegalArgumentException()

    private class MalformedFormulaException : IllegalArgumentException()

    private val commandSymbols = mapOf(
        '\\' to '\\',
        '{' to '{',
        '}' to '}',
        '%' to '%',
        '&' to '&',
        '#' to '#',
        '_' to '_',
        '^' to '^'
    )

    private val greekSymbols = mapOf(
        "alpha" to "α",
        "beta" to "β",
        "gamma" to "γ",
        "delta" to "δ",
        "epsilon" to "ϵ",
        "varepsilon" to "ε",
        "zeta" to "ζ",
        "eta" to "η",
        "theta" to "θ",
        "vartheta" to "ϑ",
        "iota" to "ι",
        "kappa" to "κ",
        "lambda" to "λ",
        "mu" to "μ",
        "nu" to "ν",
        "xi" to "ξ",
        "pi" to "π",
        "varpi" to "ϖ",
        "rho" to "ρ",
        "sigma" to "σ",
        "tau" to "τ",
        "upsilon" to "υ",
        "phi" to "ϕ",
        "varphi" to "φ",
        "chi" to "χ",
        "psi" to "ψ",
        "omega" to "ω",
        "Gamma" to "Γ",
        "Delta" to "Δ",
        "Theta" to "Θ",
        "Lambda" to "Λ",
        "Xi" to "Ξ",
        "Pi" to "Π",
        "Sigma" to "Σ",
        "Upsilon" to "Υ",
        "Phi" to "Φ",
        "Psi" to "Ψ",
        "Omega" to "Ω"
    )

    private val superscriptCharacters = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ'
    )

    private val subscriptCharacters = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
        'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
        'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
        'v' to 'ᵥ', 'x' to 'ₓ'
    )

    private fun String.toSuperscript(): String = toScript(superscriptCharacters, '⁽', '⁾', '^')

    private fun String.toSubscript(): String = toScript(subscriptCharacters, '₍', '₎', '_')

    private fun String.toScript(mapping: Map<Char, Char>, open: Char, close: Char, marker: Char): String {
        val converted = map { mapping[it] ?: return "$marker($this)" }
        return if (length == 1) converted.joinToString("") else "$open${converted.joinToString("")}$close"
    }
}

enum class FormulaRenderError {
    EMPTY,
    UNBALANCED_DELIMITER,
    UNSUPPORTED_COMMAND,
    MALFORMED_EXPRESSION
}

data class FormulaRenderResult(
    val source: String,
    val displayText: String,
    val isValid: Boolean,
    val error: FormulaRenderError? = null
) {
    companion object {
        fun valid(source: String, displayText: String): FormulaRenderResult = FormulaRenderResult(
            source = source,
            displayText = displayText,
            isValid = true
        )

        fun invalid(source: String, error: FormulaRenderError): FormulaRenderResult = FormulaRenderResult(
            source = source,
            displayText = source,
            isValid = false,
            error = error
        )
    }
}
