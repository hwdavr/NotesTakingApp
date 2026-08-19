package com.example.notesapp.ui.editor.components

import androidx.annotation.StringRes
import com.example.notesapp.R

/**
 * The 14 languages supported by the code block card. [key] is the canonical
 * tokenizer slug used by [CodeSyntaxHighlighter]; [englishName] is the stable
 * stored-value label used for backward compatibility with existing documents.
 */
enum class CodeLanguage(
    val key: String,
    @StringRes val labelRes: Int,
    val englishName: String
) {
    KOTLIN("kotlin", R.string.code_language_kotlin, "Kotlin"),
    JAVA("java", R.string.code_language_java, "Java"),
    PYTHON("python", R.string.code_language_python, "Python"),
    JAVASCRIPT("javascript", R.string.code_language_javascript, "JavaScript"),
    TYPESCRIPT("typescript", R.string.code_language_typescript, "TypeScript"),
    HTML("html", R.string.code_language_html, "HTML"),
    CSS("css", R.string.code_language_css, "CSS"),
    JSON("json", R.string.code_language_json, "JSON"),
    SQL("sql", R.string.code_language_sql, "SQL"),
    SHELL("shell", R.string.code_language_shell, "Shell"),
    C_CPP("c_cpp", R.string.code_language_c_cpp, "C/C++"),
    RUST("rust", R.string.code_language_rust, "Rust"),
    GO("go", R.string.code_language_go, "Go"),
    PLAIN_TEXT("plaintext", R.string.code_language_plain_text, "Plain Text");

    companion object {
        val supportedLanguages: List<CodeLanguage> = entries

        fun fromStoredValue(value: String): CodeLanguage {
            val trimmed = value.trim()
            val normalized = trimmed.lowercase()
            return entries.firstOrNull {
                it.key == normalized || it.englishName.equals(trimmed, ignoreCase = true)
            } ?: PLAIN_TEXT
        }
    }
}
