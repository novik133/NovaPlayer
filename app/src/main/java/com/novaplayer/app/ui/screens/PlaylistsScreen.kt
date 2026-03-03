package com.novaplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novaplayer.app.model.Playlist
import com.novaplayer.app.ui.theme.GlassBorder
import com.novaplayer.app.ui.theme.NovaCyan
import com.novaplayer.app.ui.theme.NovaPink
import com.novaplayer.app.ui.theme.NovaPurple
import com.novaplayer.app.ui.theme.NovaTextSecondary
import com.novaplayer.app.ui.theme.glassCard

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit
) {
    val showCreateDialog = remember { mutableStateOf(false) }
    val showRenameDialog = remember { mutableStateOf<Pair<Playlist, Boolean>?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${playlists.size} playlists",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary
                )
            }

            // Glass add button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NovaPurple, NovaCyan)
                        ),
                        CircleShape
                    )
                    .clickable { showCreateDialog.value = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create playlist",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (playlists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = NovaTextSecondary.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No playlists yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = NovaTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap + to create your first playlist",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 4.dp
                )
            ) {
                items(playlists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onOpen = { onOpenPlaylist(playlist) },
                        onRename = { showRenameDialog.value = playlist to true },
                        onDelete = { onDeletePlaylist(playlist.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }

    if (showCreateDialog.value) {
        PlaylistNameDialog(
            title = "New playlist",
            initialName = "",
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreateDialog.value = false
            },
            onDismiss = { showCreateDialog.value = false }
        )
    }

    val renameTarget = showRenameDialog.value
    if (renameTarget != null) {
        PlaylistNameDialog(
            title = "Rename playlist",
            initialName = renameTarget.first.name,
            onConfirm = { newName ->
                onRenamePlaylist(renameTarget.first.id, newName)
                showRenameDialog.value = null
            },
            onDismiss = { showRenameDialog.value = null }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .clickable { onOpen() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Playlist icon with gradient background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NovaPurple.copy(alpha = 0.25f),
                                NovaCyan.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = null,
                    tint = NovaPurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.padding(start = 14.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.trackIds.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary.copy(alpha = 0.7f)
                )
            }
        }

        Row {
            IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Rename playlist",
                    tint = NovaTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete playlist",
                    tint = NovaPink.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaylistNameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val nameState: MutableState<String> = remember { mutableStateOf(initialName) }
    val dialogShape = RoundedCornerShape(24.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = dialogShape,
        containerColor = Color(0xFF111633),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            val fieldShape = RoundedCornerShape(14.dp)
            TextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(fieldShape)
                    .border(1.dp, GlassBorder, fieldShape),
                placeholder = { Text("Playlist name", color = NovaTextSecondary.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x0DFFFFFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = NovaPurple
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nameState.value) }) {
                Text("Save", color = NovaPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NovaTextSecondary)
            }
        }
    )
}
