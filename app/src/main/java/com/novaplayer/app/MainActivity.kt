package com.novaplayer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.novaplayer.app.ui.theme.NovaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Destination { LIBRARY, PLAYLISTS, SETTINGS }

class MainActivity : ComponentActivity() {

    private val viewModel: PlaybackViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // When result comes back, ViewModel will attempt to (re)load tracks
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
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
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
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
        val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
        val playlists by viewModel.playlists.collectAsStateWithLifecycle()
        val settings by viewModel.settings.collectAsStateWithLifecycle()

        var isNowPlayingExpanded by remember { mutableStateOf(false) }
        var showAbout by remember { mutableStateOf(false) }
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(1200)
            showSplash = false
        }

        var currentDestination by remember { mutableStateOf(Destination.LIBRARY) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "NovaPlayer") },
                    actions = {
                        IconButton(onClick = { showAbout = true }) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "About NovaPlayer"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!showSplash) {
                    Column {
                        NowPlayingBar(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            showWaveform = settings.showWaveform,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.skipNext() },
                            onPrevious = { viewModel.skipPrevious() },
                            onClick = { isNowPlayingExpanded = !isNowPlayingExpanded }
                        )
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentDestination == Destination.LIBRARY,
                                onClick = { currentDestination = Destination.LIBRARY },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.LibraryMusic,
                                        contentDescription = "Library"
                                    )
                                },
                                label = { Text("Library") }
                            )
                            NavigationBarItem(
                                selected = currentDestination == Destination.PLAYLISTS,
                                onClick = { currentDestination = Destination.PLAYLISTS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.PlaylistPlay,
                                        contentDescription = "Playlists"
                                    )
                                },
                                label = { Text("Playlists") }
                            )
                            NavigationBarItem(
                                selected = currentDestination == Destination.SETTINGS,
                                onClick = { currentDestination = Destination.SETTINGS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings") }
                            )
                        }
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_nova_player),
                                contentDescription = "NovaPlayer logo"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NovaPlayer",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    when (currentDestination) {
                        Destination.LIBRARY -> {
                            TrackListScreen(
                                tracks = tracks,
                                isLoading = isLoading,
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
                                onDarkThemeChanged = { viewModel.setDarkTheme(it) },
                                onGaplessChanged = { viewModel.setGaplessPlayback(it) },
                                onWaveformChanged = { viewModel.setShowWaveform(it) }
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
