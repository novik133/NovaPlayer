package com.novaplayer.app.playback

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.novaplayer.app.data.MusicRepository
import com.novaplayer.app.model.Playlist
import com.novaplayer.app.model.Track
import com.novaplayer.app.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val prefs = application.getSharedPreferences("nova_player_prefs", Context.MODE_PRIVATE)

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings

    private var controller: MediaController? = null
    private var restoredFromLastState: Boolean = false

    init {
        connectToService()
        loadTracks()
    }

    private fun connectToService() {
        viewModelScope.launch(Dispatchers.Main) {
            val context = getApplication<Application>()
            val token = SessionToken(
                context,
                ComponentName(context, PlaybackService::class.java)
            )

            val controllerFuture = MediaController.Builder(context, token).buildAsync()
            controllerFuture.addListener(
                {
                    try {
                        val mediaController = controllerFuture.get()
                        controller = mediaController

                        mediaController.addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                _isPlaying.value = isPlaying
                            }

                            override fun onMediaItemTransition(
                                mediaItem: androidx.media3.common.MediaItem?,
                                reason: Int
                            ) {
                                val index = mediaController.currentMediaItemIndex
                                _tracks.value.getOrNull(index)?.let { track ->
                                    _currentTrack.value = track
                                }
                            }
                        })

                        maybeRestoreLastPlayback()
                    } catch (e: Exception) {
                        // Ignore – controller will simply not be available
                    }
                },
                MoreExecutors.directExecutor()
            )
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loadAllTracks()
            _tracks.value = result
            _isLoading.value = false

            maybeRestoreLastPlayback()
        }
    }

    fun playTrack(index: Int) {
        val list = _tracks.value
        if (index !in list.indices) return

        val controller = controller ?: return
        controller.setMediaItems(list.map { androidx.media3.common.MediaItem.fromUri(it.uri) })
        controller.prepare()
        controller.seekTo(index, 0)
        controller.play()
        _currentTrack.value = list[index]
    }

    fun togglePlayPause() {
        val controller = controller ?: return
        if (controller.isPlaying) {
            savePlaybackState()
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun skipNext() {
        controller?.seekToNext()
    }

    fun skipPrevious() {
        controller?.seekToPrevious()
    }

    // region Playlists (in-memory for now)

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        val newId = (_playlists.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _playlists.value = _playlists.value + Playlist(
            id = newId,
            name = name.trim(),
            trackIds = emptyList()
        )
    }

    fun renamePlaylist(id: Long, newName: String) {
        if (newName.isBlank()) return
        _playlists.value = _playlists.value.map {
            if (it.id == id) it.copy(name = newName.trim()) else it
        }
    }

    fun deletePlaylist(id: Long) {
        _playlists.value = _playlists.value.filterNot { it.id == id }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId && trackId !in playlist.trackIds) {
                playlist.copy(trackIds = playlist.trackIds + trackId)
            } else {
                playlist
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(trackIds = playlist.trackIds.filterNot { it == trackId })
            } else {
                playlist
            }
        }
    }

    fun playPlaylist(playlistId: Long, startIndex: Int = 0) {
        val playlist = _playlists.value.firstOrNull { it.id == playlistId } ?: return
        if (playlist.trackIds.isEmpty()) return

        val allTracks = _tracks.value.associateBy { it.id }
        val playlistTracks = playlist.trackIds.mapNotNull { allTracks[it] }
        if (playlistTracks.isEmpty() || startIndex !in playlistTracks.indices) return

        val controller = controller ?: return
        controller.setMediaItems(
            playlistTracks.map { androidx.media3.common.MediaItem.fromUri(it.uri) }
        )
        controller.prepare()
        controller.seekTo(startIndex, 0)
        controller.play()
        _currentTrack.value = playlistTracks[startIndex]
    }

    // endregion

    // region Settings (in-memory, not yet persisted)

    fun setDarkTheme(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkTheme = enabled)
    }

    fun setGaplessPlayback(enabled: Boolean) {
        _settings.value = _settings.value.copy(gaplessPlayback = enabled)
        // Hook into player configuration here in a future version
    }

    fun setShowWaveform(enabled: Boolean) {
        _settings.value = _settings.value.copy(showWaveform = enabled)
    }

    // endregion

    private fun savePlaybackState() {
        val controller = controller ?: return
        val index = controller.currentMediaItemIndex
        val position = controller.currentPosition
        if (index < 0 || _tracks.value.isEmpty()) return

        prefs.edit()
            .putInt(KEY_LAST_INDEX, index)
            .putLong(KEY_LAST_POSITION, position)
            .apply()
    }

    private fun maybeRestoreLastPlayback() {
        if (restoredFromLastState) return
        val controller = controller ?: return
        val list = _tracks.value
        if (list.isEmpty()) return

        val index = prefs.getInt(KEY_LAST_INDEX, -1)
        val position = prefs.getLong(KEY_LAST_POSITION, -1L)
        if (index !in list.indices || position < 0L) return

        restoredFromLastState = true

        controller.setMediaItems(list.map { androidx.media3.common.MediaItem.fromUri(it.uri) })
        controller.prepare()
        controller.seekTo(index, position)
        controller.play()
        _currentTrack.value = list[index]
    }

    companion object {
        private const val KEY_LAST_INDEX = "last_index"
        private const val KEY_LAST_POSITION = "last_position"
    }

    override fun onCleared() {
        savePlaybackState()
        controller?.release()
        controller = null
        super.onCleared()
    }
}

