package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun CodeBlockCard(
    block: EditorBlock.CodeBlock,
    isEditable: Boolean,
    onUpdateCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
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
            CodeBlockHeader(block)
            if (isEditable) {
                CodeBlockEditor(block, onUpdateCode)
            } else {
                CodeBlockReadOnlyContent(block)
            }
        }
    }
}

@Composable
private fun CodeBlockHeader(block: EditorBlock.CodeBlock) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
        Text(
            text = block.language,
            modifier = Modifier.testTag("editor_code_block_language_${block.id}"),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        )
    }
}

@Composable
private fun CodeBlockEditor(block: EditorBlock.CodeBlock, onUpdateCode: (String) -> Unit) {
    val colors = LocalAppColors.current
    BasicTextField(
        value = block.code,
        onValueChange = onUpdateCode,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .testTag("editor_code_block_input_${block.id}"),
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = colors.textPrimary
        ),
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

@Composable
private fun CodeBlockReadOnlyContent(block: EditorBlock.CodeBlock) {
    val colors = LocalAppColors.current
    Text(
        text = block.code.ifBlank { stringResource(R.string.code_block_empty_placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .testTag("editor_code_block_readonly_${block.id}"),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = colors.textPrimary
        )
    )
}
