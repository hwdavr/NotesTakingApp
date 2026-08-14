package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.notesapp.R
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.model.VoiceFileSizeUnit
import com.example.notesapp.ui.editor.model.voiceFileSizePresentation
import com.example.notesapp.ui.theme.LocalAppColors
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun VoiceNotePlayer(block: EditorBlock.Voice, isEditable: Boolean, onDeleteAudio: (() -> Unit)?) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val audioPath = block.audioFilePath
    val audioFileExists = remember(audioPath) { audioPath?.let(::File)?.isFile == true }
    val player = remember(block.blockId) { ExoPlayer.Builder(context).build() }
    var isPlaying by remember(block.blockId) { mutableStateOf(false) }
    var hasPlaybackError by remember(block.blockId) { mutableStateOf(false) }
    var positionMs by remember(block.blockId) { mutableLongStateOf(0L) }
    var durationMs by remember(block.blockId) { mutableLongStateOf(block.durationMs) }
    var showDeleteDialog by remember(block.blockId) { mutableStateOf(false) }
    val playStateDescription = stringResource(
        if (isPlaying) {
            R.string.editor_voice_pause_description
        } else {
            R.string.editor_voice_play_description
        }
    )
    val seekDescription = stringResource(
        R.string.editor_voice_seek_description,
        formatDuration(positionMs),
        formatDuration(durationMs)
    )

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    positionMs = 0L
                    player.seekTo(0L)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                hasPlaybackError = true
                isPlaying = false
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player, audioPath, audioFileExists) {
        if (audioFileExists && audioPath != null) {
            hasPlaybackError = false
            player.setMediaItem(MediaItem.fromUri(File(audioPath).toURI().toString()))
            player.prepare()
            durationMs = block.durationMs
        }
    }

    LaunchedEffect(player, isPlaying) {
        while (isPlaying) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            delay(200L)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_player_card"),
        color = colors.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                contentDescription = stringResource(R.string.editor_voice_recording_description),
                tint = colors.secondary,
                modifier = Modifier.size(28.dp)
            )
            IconButton(
                onClick = {
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else if (audioFileExists && !hasPlaybackError) {
                        player.play()
                        isPlaying = true
                    }
                },
                enabled = audioFileExists && !hasPlaybackError,
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.primary, CircleShape)
                    .clip(CircleShape)
                    .semantics {
                        stateDescription = playStateDescription
                    }
                    .testTag("voice_play_pause_btn")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) {
                            R.string.editor_voice_pause_description
                        } else {
                            R.string.editor_voice_play_description
                        }
                    ),
                    tint = colors.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Slider(
                    value = progress(positionMs, durationMs),
                    onValueChange = { value ->
                        val newPosition = (durationMs * value).toLong()
                        positionMs = newPosition
                        player.seekTo(newPosition)
                    },
                    enabled = audioFileExists && !hasPlaybackError && durationMs > 0L,
                    modifier = Modifier
                        .semantics {
                            contentDescription = seekDescription
                        }
                        .testTag("voice_seek_slider")
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatDuration(positionMs),
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("voice_elapsed_label")
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatDuration(durationMs),
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("voice_duration_label")
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = with(voiceFileSizePresentation(block.fileSizeBytes)) {
                        when (unit) {
                            VoiceFileSizeUnit.Kilobytes -> stringResource(
                                R.string.editor_voice_file_size_kb,
                                value.toLong()
                            )
                            VoiceFileSizeUnit.Megabytes -> stringResource(
                                R.string.editor_voice_file_size_mb,
                                value.toDouble()
                            )
                        }
                    },
                    color = colors.textTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag("voice_file_size_label")
                )
                IconButton(
                    onClick = { showDeleteDialog = true },
                    enabled = isEditable && onDeleteAudio != null,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("voice_delete_audio_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.editor_voice_delete_audio_description),
                        tint = colors.error
                    )
                }
            }
        }
        if (!audioFileExists || hasPlaybackError) {
            Text(
                text = stringResource(R.string.editor_voice_audio_missing),
                color = colors.error,
                modifier = Modifier.padding(start = 52.dp, end = 12.dp, bottom = 8.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            modifier = Modifier.testTag("voice_delete_audio_dialog"),
            title = { Text(stringResource(R.string.editor_voice_delete_audio_title)) },
            text = { Text(stringResource(R.string.editor_voice_delete_audio_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAudio?.invoke()
                    },
                    modifier = Modifier.testTag("voice_delete_audio_confirm")
                ) {
                    Text(stringResource(R.string.editor_voice_delete_audio_action), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("voice_delete_audio_cancel")
                ) {
                    Text(stringResource(R.string.voice_recorder_cancel_action))
                }
            }
        )
    }
}

private fun progress(positionMs: Long, durationMs: Long): Float =
    if (durationMs <= 0L) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}
