package com.novaplayer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novaplayer.app.playback.PlaybackViewModel
import com.novaplayer.app.ui.components.AboutDialog
import com.novaplayer.app.ui.components.NowPlayingBar
import com.novaplayer.app.ui.screens.PlaylistsScreen
import com.novaplayer.app.ui.screens.SettingsScreen
import com.novaplayer.app.ui.screens.TrackListScreen
import com.novaplayer.app.ui.theme.GlassBorder
import com.novaplayer.app.ui.theme.NovaBgDark
import com.novaplayer.app.ui.theme.NovaCyan
import com.novaplayer.app.ui.theme.NovaPurple
import com.novaplayer.app.ui.theme.NovaTextSecondary
import com.novaplayer.app.ui.theme.NovaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Destination { LIBRARY, PLAYLISTS, SETTINGS }

class MainActivity : ComponentActivity() {

    private val viewModel: PlaybackViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.loadTracks()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensurePermissions()

        setContent {
            NovaPlayerRoot(viewModel = viewModel)
        }
    }

    private fun ensurePermissions() {
        val toRequest = mutableListOf<String>()

        val hasAudioPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

        if (!hasAudioPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                toRequest += Manifest.permission.READ_MEDIA_AUDIO
            } else {
                toRequest += Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaPlayerRoot(viewModel: PlaybackViewModel) {
    NovaTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val tracks by viewModel.tracks.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
        val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
        val playlists by viewModel.playlists.collectAsStateWithLifecycle()
        val settings by viewModel.settings.collectAsStateWithLifecycle()
        val audioSessionId by viewModel.audioSessionId.collectAsStateWithLifecycle()
        val totalListeningTime by viewModel.totalListeningTime.collectAsStateWithLifecycle()
        val listeningStreak by viewModel.listeningStreak.collectAsStateWithLifecycle()

        var isNowPlayingExpanded by remember { mutableStateOf(false) }
        var showAbout by remember { mutableStateOf(false) }
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(1500)
            showSplash = false
        }

        var currentDestination by remember { mutableStateOf(Destination.LIBRARY) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NovaBgDark)
        ) {
            Scaffold(
                topBar = {
                    if (!showSplash) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "NovaPlayer",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            actions = {
                                IconButton(onClick = { showAbout = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "About NovaPlayer",
                                        tint = NovaTextSecondary.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color(0x80060918)
                            )
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Transparent,
                bottomBar = {
                    if (!showSplash) {
                        Column {
                            NowPlayingBar(
                                track = currentTrack,
                                isPlaying = isPlaying,
                                showWaveform = settings.showWaveform,
                                shuffleMode = settings.shuffleMode,
                                repeatMode = settings.repeatMode,
                                playbackSpeed = settings.playbackSpeed,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.skipNext() },
                                onPrevious = { viewModel.skipPrevious() },
                                onShuffleClick = { viewModel.setShuffleMode(!settings.shuffleMode) },
                                onRepeatClick = {
                                    val nextMode = when (settings.repeatMode) {
                                        com.novaplayer.app.model.RepeatMode.OFF -> com.novaplayer.app.model.RepeatMode.ALL
                                        com.novaplayer.app.model.RepeatMode.ALL -> com.novaplayer.app.model.RepeatMode.ONE
                                        com.novaplayer.app.model.RepeatMode.ONE -> com.novaplayer.app.model.RepeatMode.OFF
                                    }
                                    viewModel.setRepeatMode(nextMode)
                                },
                                onClick = { isNowPlayingExpanded = !isNowPlayingExpanded }
                            )

                            GlassNavigationBar(
                                currentDestination = currentDestination,
                                onDestinationChanged = { currentDestination = it }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (showSplash) {
                        SplashContent()
                    } else {
                        when (currentDestination) {
                            Destination.LIBRARY -> {
                                TrackListScreen(
                                    tracks = tracks,
                                    isLoading = isLoading,
                                    searchQuery = searchQuery,
                                    totalListeningTimeMs = totalListeningTime,
                                    listeningStreak = listeningStreak,
                                    mostPlayed = viewModel.getMostPlayedTracks(3),
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onTrackClick = { index ->
                                        if (tracks.isNotEmpty()) {
                                            viewModel.playTrack(index)
                                            isNowPlayingExpanded = true
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("No tracks to play")
                                            }
                                        }
                                    }
                                )
                            }

                            Destination.PLAYLISTS -> {
                                PlaylistsScreen(
                                    playlists = playlists,
                                    onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                                    onRenamePlaylist = { id, newName -> viewModel.renamePlaylist(id, newName) },
                                    onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                                    onOpenPlaylist = { playlist ->
                                        viewModel.playPlaylist(playlist.id, startIndex = 0)
                                    }
                                )
                            }

                            Destination.SETTINGS -> {
                                SettingsScreen(
                                    settings = settings,
                                    audioSessionId = audioSessionId,
                                    audioEffects = viewModel.audioEffects,
                                    onDarkThemeChanged = { viewModel.setDarkTheme(it) },
                                    onGaplessChanged = { viewModel.setGaplessPlayback(it) },
                                    onWaveformChanged = { viewModel.setShowWaveform(it) },
                                    onShuffleChanged = { viewModel.setShuffleMode(it) },
                                    onRepeatModeChanged = { viewModel.setRepeatMode(it) },
                                    onSleepTimerChanged = { viewModel.setSleepTimer(it) },
                                    onSpeedChanged = { viewModel.setPlaybackSpeed(it) },
                                    onSkipSilenceChanged = { viewModel.setSkipSilence(it) },
                                    onCrossfadeChanged = { viewModel.setCrossfadeDuration(it) },
                                    onBassBoostChanged = { viewModel.setBassBoost(it) },
                                    onVirtualizerChanged = { viewModel.setVirtualizer(it) },
                                    onLoudnessChanged = { viewModel.setLoudnessGain(it) }
                                )
                            }
                        }
                    }
                }
            }

            if (showAbout) {
                AboutDialog(onDismiss = { showAbout = false })
            }
        }
    }
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NovaPurple.copy(alpha = 0.15f),
                            NovaCyan.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NovaPurple.copy(alpha = 0.3f),
                                NovaCyan.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nova_player),
                    contentDescription = "NovaPlayer logo",
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "NovaPlayer",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your music, reimagined",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun GlassNavigationBar(
    currentDestination: Destination,
    onDestinationChanged: (Destination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x20FFFFFF), Color(0x08FFFFFF))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(GlassBorder, Color.Transparent)
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Filled.LibraryMusic,
                label = "Library",
                selected = currentDestination == Destination.LIBRARY,
                onClick = { onDestinationChanged(Destination.LIBRARY) }
            )
            NavItem(
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                label = "Playlists",
                selected = currentDestination == Destination.PLAYLISTS,
                onClick = { onDestinationChanged(Destination.PLAYLISTS) }
            )
            NavItem(
                icon = Icons.Filled.Settings,
                label = "Settings",
                selected = currentDestination == Destination.SETTINGS,
                onClick = { onDestinationChanged(Destination.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.5f,
        animationSpec = tween(200),
        label = "navAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NovaPurple.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) NovaPurple else NovaTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(24.dp)
                    .alpha(alpha)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else NovaTextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.alpha(alpha)
        )
    }
}
