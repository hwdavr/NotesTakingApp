@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.AppColors
import com.example.notesapp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun CodeBlockCard(
    block: EditorBlock.CodeBlock,
    isEditable: Boolean,
    onUpdateCode: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = remember(block.language) { CodeLanguage.fromStoredValue(block.language) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("editor_code_block_${block.id}")
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CodeBlockHeader(
                block = block,
                isEditable = isEditable,
                language = language,
                onUpdateLanguage = onUpdateLanguage,
                onDelete = onDelete
            )
            CodeBlockBody(
                block = block,
                isEditable = isEditable,
                language = language,
                onUpdateCode = onUpdateCode
            )
        }
    }
}

@Composable
private fun CodeBlockHeader(
    block: EditorBlock.CodeBlock,
    isEditable: Boolean,
    language: CodeLanguage,
    onUpdateLanguage: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    val clipboardManager = LocalClipboardManager.current
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val copyDescription = stringResource(
        if (copied) R.string.code_block_copied else R.string.code_block_copy_description
    )

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_code_header_${block.id}")
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Code,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        CodeLanguageSelector(
            blockId = block.id,
            language = language,
            enabled = isEditable,
            expanded = languageMenuExpanded,
            onToggle = { languageMenuExpanded = !languageMenuExpanded },
            onDismiss = { languageMenuExpanded = false },
            onLanguageSelected = { selected ->
                languageMenuExpanded = false
                onUpdateLanguage(selected.englishName)
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(block.code))
                copied = true
            },
            enabled = block.code.isNotEmpty(),
            modifier = Modifier
                .size(48.dp)
                .testTag("editor_code_copy_btn_${block.id}")
        ) {
            Icon(
                imageVector = if (copied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                contentDescription = copyDescription,
                tint = colors.textSecondary
            )
        }
        if (isEditable) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("editor_code_delete_btn_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.code_block_delete_description),
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun CodeLanguageSelector(
    blockId: String,
    language: CodeLanguage,
    enabled: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onLanguageSelected: (CodeLanguage) -> Unit
) {
    val colors = LocalAppColors.current
    Box {
        Surface(
            onClick = onToggle,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            color = colors.roleEditorBg,
            modifier = Modifier.testTag("editor_code_lang_selector_$blockId")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(language.labelRes),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(
                        R.string.code_block_language_selector_description,
                        stringResource(language.labelRes)
                    ),
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.testTag("editor_code_lang_dropdown_$blockId")
        ) {
            CodeLanguage.supportedLanguages.forEach { candidate ->
                DropdownMenuItem(
                    text = { Text(stringResource(candidate.labelRes)) },
                    onClick = { onLanguageSelected(candidate) },
                    modifier = Modifier.testTag("editor_code_lang_item_${candidate.key}")
                )
            }
        }
    }
}

@Composable
private fun CodeBlockBody(
    block: EditorBlock.CodeBlock,
    isEditable: Boolean,
    language: CodeLanguage,
    onUpdateCode: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val lineNumbers = remember(block.code) { CodeSyntaxHighlighter.lineNumbers(block.code) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text = lineNumbers.joinToString("\n"),
            modifier = Modifier
                .testTag("editor_code_line_numbers_${block.id}")
                .padding(end = 8.dp),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.End
            )
        )
        Box(modifier = Modifier.weight(1f)) {
            if (isEditable) {
                CodeBlockEditor(
                    block = block,
                    language = language,
                    colors = colors,
                    onUpdateCode = onUpdateCode
                )
            } else {
                val highlightedCode = remember(block.code, language, colors) {
                    highlightCode(block.code, language, colors)
                }
                SelectionContainer {
                    Text(
                        text = highlightedCode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_code_readonly_${block.id}"),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = colors.textPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockEditor(
    block: EditorBlock.CodeBlock,
    language: CodeLanguage,
    colors: AppColors,
    onUpdateCode: (String) -> Unit
) {
    val visualTransformation = remember(language, colors) {
        CodeSyntaxVisualTransformation(language, colors)
    }
    BasicTextField(
        value = block.code,
        onValueChange = onUpdateCode,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .testTag("editor_code_editor_${block.id}"),
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = colors.textPrimary
        ),
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            if (block.code.isEmpty()) {
                Text(
                    text = stringResource(R.string.code_block_empty_placeholder),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = colors.textTertiary
                    )
                )
            }
            innerTextField()
        }
    )
}

private class CodeSyntaxVisualTransformation(
    private val language: CodeLanguage,
    private val colors: AppColors
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(highlightCode(text.text, language, colors), OffsetMapping.Identity)
}

private fun highlightCode(code: String, language: CodeLanguage, colors: AppColors): AnnotatedString {
    if (code.isEmpty()) return AnnotatedString("")
    val tokens = CodeSyntaxHighlighter.tokenize(code, language.key)
    return buildAnnotatedString {
        tokens.forEach { token ->
            val color = when (token.type) {
                CodeTokenType.KEYWORD -> colors.codeKeyword
                CodeTokenType.TYPE -> colors.codeType
                CodeTokenType.STRING -> colors.codeString
                CodeTokenType.COMMENT -> colors.codeComment
                CodeTokenType.NUMBER -> colors.codeNumber
                CodeTokenType.OPERATOR -> colors.codeOperator
                CodeTokenType.PLAIN_TEXT -> colors.textPrimary
            }
            withStyle(SpanStyle(color = color)) {
                append(code.substring(token.start, token.endExclusive))
            }
        }
    }
}
