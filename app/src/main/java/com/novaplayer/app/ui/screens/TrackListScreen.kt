package com.novaplayer.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.novaplayer.app.R
import com.novaplayer.app.model.Track
import com.novaplayer.app.ui.theme.GlassBorder
import com.novaplayer.app.ui.theme.NovaCyan
import com.novaplayer.app.ui.theme.NovaPink
import com.novaplayer.app.ui.theme.NovaPurple
import com.novaplayer.app.ui.theme.NovaPurpleLight
import com.novaplayer.app.ui.theme.NovaTextSecondary
import com.novaplayer.app.ui.theme.glassCard

@Composable
fun TrackListScreen(
    tracks: List<Track>,
    isLoading: Boolean,
    searchQuery: String,
    totalListeningTimeMs: Long = 0,
    listeningStreak: Int = 0,
    mostPlayed: List<Pair<Track, Int>> = emptyList(),
    onSearchQueryChange: (String) -> Unit,
    onTrackClick: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = NovaPurple,
                strokeWidth = 3.dp
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    // Header with logo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            NovaPurple.copy(alpha = 0.3f),
                                            NovaCyan.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_nova_player),
                                contentDescription = "NovaPlayer logo",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "NovaPlayer",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${tracks.size} tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextSecondary
                        )
                    }
                }

                // Listening Insights
                if (totalListeningTimeMs > 0 || listeningStreak > 0) {
                    item {
                        ListeningInsightsCard(
                            totalTimeMs = totalListeningTimeMs,
                            streak = listeningStreak,
                            mostPlayed = mostPlayed
                        )
                    }
                }

                item {
                    // Glassmorphic search bar
                    val searchShape = RoundedCornerShape(16.dp)
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(searchShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x1AFFFFFF),
                                        Color(0x0AFFFFFF)
                                    )
                                ),
                                searchShape
                            )
                            .border(1.dp, GlassBorder, searchShape),
                        placeholder = {
                            Text(
                                "Search songs, artists, albums...",
                                color = NovaTextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = NovaPurple.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = NovaPurple
                        )
                    )
                }

                if (tracks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = NovaTextSecondary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No music found",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaTextSecondary
                            )
                            Text(
                                text = "Add music to your device to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    itemsIndexed(tracks) { index, track ->
                        TrackItem(
                            track = track,
                            onClick = { onTrackClick(index) }
                        )
                    }
                }

                // Bottom spacing for now playing bar
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ListeningInsightsCard(
    totalTimeMs: Long,
    streak: Int,
    mostPlayed: List<Pair<Track, Int>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .glassCard(cornerRadius = 18.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "YOUR INSIGHTS",
            style = MaterialTheme.typography.labelMedium,
            color = NovaPurpleLight.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InsightStat(
                value = formatDuration(totalTimeMs),
                label = "Listened",
                color = NovaPurple
            )
            InsightStat(
                value = if (streak > 0) "$streak" else "-",
                label = "Day streak",
                color = NovaCyan
            )
            InsightStat(
                value = "${mostPlayed.firstOrNull()?.second ?: 0}",
                label = "Top plays",
                color = NovaPink
            )
        }

        if (mostPlayed.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Most played: ${mostPlayed.first().first.title}",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextSecondary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InsightStat(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = NovaTextSecondary.copy(alpha = 0.5f)
        )
    }
}

private fun formatDuration(ms: Long): String {
    val hours = ms / 3_600_000
    val minutes = (ms % 3_600_000) / 60_000
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

@Composable
private fun TrackItem(
    track: Track,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .glassCard(cornerRadius = 14.dp)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art with rounded corners and subtle border
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(track.albumArtUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "${track.album} cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_nova_player),
                    error = painterResource(id = R.drawable.ic_nova_player)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    NovaPurple.copy(alpha = 0.2f),
                                    NovaCyan.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "No album art",
                        tint = NovaPurple.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextSecondary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
