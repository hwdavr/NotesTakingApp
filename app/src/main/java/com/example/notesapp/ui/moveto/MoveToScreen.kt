package com.example.notesapp.ui.moveto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R

@Composable
fun MoveToScreen(
    parentPadding: PaddingValues,
    onBack: () -> Unit,
    onMoved: () -> Unit,
    viewModel: MoveToViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MoveToScreenContent(
        parentPadding = parentPadding,
        state = state,
        onBack = onBack,
        onSearchChanged = viewModel::onSearchChanged,
        onDestinationSelected = { destinationId ->
            viewModel.moveTo(destinationId, onMoved)
        }
    )
}

@Composable
fun MoveToScreenContent(
    parentPadding: PaddingValues,
    state: MoveToUiState,
    onBack: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onDestinationSelected: (String?) -> Unit
) {
    Scaffold(
        modifier = Modifier.padding(parentPadding),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF3F7FF), Color(0xFFEDF3FF))
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("move_to_screen")
        ) {
            MoveToTopBar(onBack = onBack)
            MoveToSearchBox(
                value = state.searchQuery,
                onValueChange = onSearchChanged
            )

            Spacer(modifier = Modifier.height(18.dp))

            MoveDestinationRow(
                name = stringResource(R.string.move_to_root_destination),
                depth = 0,
                isRoot = true,
                testTag = "move_to_root_destination",
                onClick = { onDestinationSelected(null) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(
                    if (state.searchQuery.isBlank()) {
                        R.string.move_to_recent_folders
                    } else {
                        R.string.move_to_folder_results
                    }
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2F343A)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            val folders = if (state.searchQuery.isBlank()) state.recentFolders else state.folderResults
            if (folders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.move_to_empty_folders),
                        color = Color(0xFF7D848B),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folders.forEach { folder ->
                        MoveDestinationRow(
                            name = folder.name,
                            depth = folder.depth,
                            isRoot = false,
                            testTag = "move_to_folder_${folder.id}",
                            onClick = { onDestinationSelected(folder.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveToTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.move_to_back)
            )
        }
        Text(
            text = stringResource(R.string.move_to_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2A30)
            )
        )
    }
}

@Composable
private fun MoveToSearchBox(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFFEEEFF1), RoundedCornerShape(8.dp))
            .testTag("move_to_search_input"),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        ),
        singleLine = true,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF5F6EFA)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color(0xFF8E959B),
                    modifier = Modifier.size(20.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.move_to_search_placeholder),
                            color = Color(0xFFA0A6AC),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun MoveDestinationRow(name: String, depth: Int, isRoot: Boolean, testTag: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(start = (14 + depth * 20).dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isRoot) {
                    Icons.AutoMirrored.Outlined.StickyNote2
                } else {
                    Icons.Outlined.Folder
                },
                contentDescription = null,
                tint = Color(0xFF5F6770),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3E444A)
                )
            )
        }
    }
}
