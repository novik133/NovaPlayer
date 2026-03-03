package com.novaplayer.app.playback

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.novaplayer.app.data.MusicRepository
import com.novaplayer.app.data.PersistenceHelper
import com.novaplayer.app.model.Playlist
import com.novaplayer.app.model.RepeatMode
import com.novaplayer.app.model.Track
import com.novaplayer.app.model.TrackStats
import com.novaplayer.app.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

@UnstableApi
class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val prefs = application.getSharedPreferences("nova_player_prefs", Context.MODE_PRIVATE)
    private val persistenceHelper = PersistenceHelper(application)

    val audioEffects = AudioEffectsManager()

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

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

    private val _audioSessionId = MutableStateFlow<Int?>(null)
    val audioSessionId: StateFlow<Int?> = _audioSessionId

    private val _trackStats = MutableStateFlow<Map<Long, TrackStats>>(emptyMap())
    val trackStats: StateFlow<Map<Long, TrackStats>> = _trackStats

    private val _totalListeningTime = MutableStateFlow(0L)
    val totalListeningTime: StateFlow<Long> = _totalListeningTime

    private val _listeningStreak = MutableStateFlow(0)
    val listeningStreak: StateFlow<Int> = _listeningStreak

    private var controller: MediaController? = null
    private var restoredFromLastState: Boolean = false
    private var sleepTimerJob: Job? = null
    private var listenTimeTracker: Job? = null

    init {
        loadPlaylists()
        loadSettings()
        loadStats()
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

                        // Get audio session ID from service companion
                        val sessionId = PlaybackService.currentAudioSessionId
                        if (sessionId != 0) {
                            _audioSessionId.value = sessionId
                            audioEffects.init(sessionId)
                            applyAudioEffects()
                        }

                        mediaController.addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                _isPlaying.value = isPlaying
                                if (isPlaying) {
                                    startListenTimeTracking()
                                    updateStreak()
                                } else {
                                    stopListenTimeTracking()
                                }
                            }

                            override fun onMediaItemTransition(
                                mediaItem: androidx.media3.common.MediaItem?,
                                reason: Int
                            ) {
                                val index = mediaController.currentMediaItemIndex
                                _allTracks.value.getOrNull(index)?.let { track ->
                                    _currentTrack.value = track
                                    recordTrackPlay(track.id)
                                }
                                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                                    !mediaController.hasNextMediaItem() &&
                                    _settings.value.repeatMode == RepeatMode.OFF
                                ) {
                                    mediaController.pause()
                                }
                            }
                        })

                        // Apply persisted settings
                        applyPlaybackSpeed()
                        applySkipSilence()
                        applyCrossfade()

                        maybeRestoreLastPlayback()
                    } catch (e: Exception) {
                        // Controller not available
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
            _allTracks.value = result
            applySearchFilter()
            _isLoading.value = false
            maybeRestoreLastPlayback()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val query = _searchQuery.value.lowercase()
        val filtered = if (query.isBlank()) {
            _allTracks.value
        } else {
            _allTracks.value.filter {
                it.title.lowercase().contains(query) ||
                    it.artist.lowercase().contains(query) ||
                    it.album.lowercase().contains(query)
            }
        }
        _tracks.value = filtered
    }

    fun playTrack(index: Int) {
        val list = _tracks.value
        if (index !in list.indices) return

        val controller = controller ?: return
        val items = list.map { androidx.media3.common.MediaItem.fromUri(it.uri) }

        if (_settings.value.shuffleMode) {
            val shuffled = items.shuffled()
            controller.setMediaItems(shuffled)
            controller.prepare()
            controller.seekTo(0, 0)
        } else {
            controller.setMediaItems(items)
            controller.prepare()
            controller.seekTo(index, 0)
        }

        applyRepeatMode()
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
        val controller = controller ?: return
        when (_settings.value.repeatMode) {
            RepeatMode.ONE -> {
                controller.seekTo(controller.currentMediaItemIndex, 0)
            }
            else -> {
                if (controller.hasNextMediaItem()) {
                    controller.seekToNext()
                } else if (_settings.value.repeatMode == RepeatMode.ALL) {
                    controller.seekTo(0, 0)
                }
            }
        }
    }

    fun skipPrevious() {
        val controller = controller ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(controller.currentMediaItemIndex, 0)
        } else {
            if (controller.hasPreviousMediaItem()) {
                controller.seekToPrevious()
            } else if (_settings.value.repeatMode == RepeatMode.ALL) {
                controller.seekTo(controller.mediaItemCount - 1, 0)
            }
        }
    }

    // region Playlists

    private fun loadPlaylists() {
        _playlists.value = persistenceHelper.loadPlaylists()
    }

    private fun savePlaylists() {
        persistenceHelper.savePlaylists(_playlists.value)
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        val newId = (_playlists.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _playlists.value = _playlists.value + Playlist(
            id = newId,
            name = name.trim(),
            trackIds = emptyList()
        )
        savePlaylists()
    }

    fun renamePlaylist(id: Long, newName: String) {
        if (newName.isBlank()) return
        _playlists.value = _playlists.value.map {
            if (it.id == id) it.copy(name = newName.trim()) else it
        }
        savePlaylists()
    }

    fun deletePlaylist(id: Long) {
        _playlists.value = _playlists.value.filterNot { it.id == id }
        savePlaylists()
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId && trackId !in playlist.trackIds) {
                playlist.copy(trackIds = playlist.trackIds + trackId)
            } else {
                playlist
            }
        }
        savePlaylists()
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(trackIds = playlist.trackIds.filterNot { it == trackId })
            } else {
                playlist
            }
        }
        savePlaylists()
    }

    fun playPlaylist(playlistId: Long, startIndex: Int = 0) {
        val playlist = _playlists.value.firstOrNull { it.id == playlistId } ?: return
        if (playlist.trackIds.isEmpty()) return

        val allTracks = _allTracks.value.associateBy { it.id }
        val playlistTracks = playlist.trackIds.mapNotNull { allTracks[it] }
        if (playlistTracks.isEmpty() || startIndex !in playlistTracks.indices) return

        val controller = controller ?: return
        val items = playlistTracks.map { androidx.media3.common.MediaItem.fromUri(it.uri) }

        if (_settings.value.shuffleMode) {
            val shuffled = items.shuffled()
            controller.setMediaItems(shuffled)
            controller.prepare()
            controller.seekTo(0, 0)
        } else {
            controller.setMediaItems(items)
            controller.prepare()
            controller.seekTo(startIndex, 0)
        }

        applyRepeatMode()
        controller.play()
        _currentTrack.value = playlistTracks[startIndex]
    }

    // endregion

    // region Settings

    private fun loadSettings() {
        _settings.value = persistenceHelper.loadSettings()
    }

    private fun saveSettings() {
        persistenceHelper.saveSettings(_settings.value)
    }

    fun setDarkTheme(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkTheme = enabled)
        saveSettings()
    }

    fun setGaplessPlayback(enabled: Boolean) {
        _settings.value = _settings.value.copy(gaplessPlayback = enabled)
        saveSettings()
    }

    fun setShowWaveform(enabled: Boolean) {
        _settings.value = _settings.value.copy(showWaveform = enabled)
        saveSettings()
    }

    fun setShuffleMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(shuffleMode = enabled)
        saveSettings()
        val controller = controller ?: return
        if (controller.mediaItemCount > 0) {
            val currentIndex = controller.currentMediaItemIndex
            val items = (0 until controller.mediaItemCount).map { controller.getMediaItemAt(it) }
            if (enabled) {
                val shuffled = items.shuffled()
                controller.setMediaItems(shuffled)
                controller.seekTo(0, 0)
            } else {
                controller.setMediaItems(items)
                controller.seekTo(currentIndex.coerceIn(0, items.size - 1), 0)
            }
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _settings.value = _settings.value.copy(repeatMode = mode)
        saveSettings()
        applyRepeatMode()
    }

    private fun applyRepeatMode() {
        val controller = controller ?: return
        when (_settings.value.repeatMode) {
            RepeatMode.OFF -> controller.repeatMode = Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> controller.repeatMode = Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> controller.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    fun setSleepTimer(minutes: Int) {
        _settings.value = _settings.value.copy(sleepTimerMinutes = minutes)
        saveSettings()
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                controller?.pause()
                _settings.value = _settings.value.copy(sleepTimerMinutes = 0)
                saveSettings()
            }
        }
    }

    // endregion

    // region New Features: Speed, Skip Silence, Crossfade, Audio FX

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        _settings.value = _settings.value.copy(playbackSpeed = clamped)
        saveSettings()
        applyPlaybackSpeed()
    }

    private fun applyPlaybackSpeed() {
        val speed = _settings.value.playbackSpeed
        controller?.setPlaybackParameters(PlaybackParameters(speed))
    }

    fun setSkipSilence(enabled: Boolean) {
        _settings.value = _settings.value.copy(skipSilence = enabled)
        saveSettings()
        applySkipSilence()
    }

    private fun applySkipSilence() {
        PlaybackService.skipSilenceEnabled = _settings.value.skipSilence
    }

    fun setCrossfadeDuration(durationMs: Int) {
        _settings.value = _settings.value.copy(crossfadeDurationMs = durationMs)
        saveSettings()
        applyCrossfade()
    }

    private fun applyCrossfade() {
        PlaybackService.pendingCrossfadeDurationMs = _settings.value.crossfadeDurationMs
    }

    fun setBassBoost(strength: Int) {
        _settings.value = _settings.value.copy(bassBoostStrength = strength)
        saveSettings()
        audioEffects.setBassBoost(strength)
    }

    fun setVirtualizer(strength: Int) {
        _settings.value = _settings.value.copy(virtualizerStrength = strength)
        saveSettings()
        audioEffects.setVirtualizer(strength)
    }

    fun setLoudnessGain(gain: Int) {
        _settings.value = _settings.value.copy(loudnessGain = gain)
        saveSettings()
        audioEffects.setLoudnessGain(gain)
    }

    private fun applyAudioEffects() {
        val s = _settings.value
        audioEffects.setBassBoost(s.bassBoostStrength)
        audioEffects.setVirtualizer(s.virtualizerStrength)
        audioEffects.setLoudnessGain(s.loudnessGain)
    }

    // endregion

    // region Listening Statistics

    private fun loadStats() {
        _trackStats.value = persistenceHelper.loadTrackStats()
        _totalListeningTime.value = persistenceHelper.getTotalListeningTime()
        _listeningStreak.value = persistenceHelper.getStreak()
    }

    private fun recordTrackPlay(trackId: Long) {
        val current = _trackStats.value.toMutableMap()
        val existing = current[trackId] ?: TrackStats(trackId = trackId)
        current[trackId] = existing.copy(
            playCount = existing.playCount + 1,
            lastPlayedTimestamp = System.currentTimeMillis()
        )
        _trackStats.value = current
        persistenceHelper.saveTrackStats(current)
    }

    private fun startListenTimeTracking() {
        listenTimeTracker?.cancel()
        listenTimeTracker = viewModelScope.launch {
            while (true) {
                delay(5000)
                _totalListeningTime.value += 5000
                persistenceHelper.saveTotalListeningTime(_totalListeningTime.value)

                val trackId = _currentTrack.value?.id ?: continue
                val current = _trackStats.value.toMutableMap()
                val existing = current[trackId] ?: TrackStats(trackId = trackId)
                current[trackId] = existing.copy(
                    totalListenTimeMs = existing.totalListenTimeMs + 5000
                )
                _trackStats.value = current
            }
        }
    }

    private fun stopListenTimeTracking() {
        listenTimeTracker?.cancel()
        listenTimeTracker = null
        persistenceHelper.saveTrackStats(_trackStats.value)
        persistenceHelper.saveTotalListeningTime(_totalListeningTime.value)
    }

    private fun updateStreak() {
        val now = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastDate = persistenceHelper.getLastListenDate()
        val yesterday = today - 86_400_000L

        val currentStreak = persistenceHelper.getStreak()
        val newStreak = when {
            lastDate == 0L -> 1
            lastDate >= today -> currentStreak
            lastDate >= yesterday -> currentStreak + 1
            else -> 1
        }

        _listeningStreak.value = newStreak
        persistenceHelper.saveStreak(newStreak)
        persistenceHelper.saveLastListenDate(now.timeInMillis)
    }

    fun getMostPlayedTracks(limit: Int = 5): List<Pair<Track, Int>> {
        val stats = _trackStats.value
        val allTracks = _allTracks.value.associateBy { it.id }
        return stats.values
            .filter { it.playCount > 0 }
            .sortedByDescending { it.playCount }
            .take(limit)
            .mapNotNull { stat ->
                allTracks[stat.trackId]?.let { track -> track to stat.playCount }
            }
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
        val list = _allTracks.value
        if (list.isEmpty()) return

        val index = prefs.getInt(KEY_LAST_INDEX, -1)
        val position = prefs.getLong(KEY_LAST_POSITION, -1L)
        if (index !in list.indices || position < 0L) return

        restoredFromLastState = true

        val items = list.map { androidx.media3.common.MediaItem.fromUri(it.uri) }
        controller.setMediaItems(items)
        controller.prepare()
        applyRepeatMode()
        controller.seekTo(index, position)
        controller.play()
        _currentTrack.value = list[index]
    }

    companion object {
        private const val KEY_LAST_INDEX = "last_index"
        private const val KEY_LAST_POSITION = "last_position"
    }

    override fun onCleared() {
        stopListenTimeTracking()
        savePlaybackState()
        savePlaylists()
        saveSettings()
        sleepTimerJob?.cancel()
        audioEffects.release()
        controller?.release()
        controller = null
        super.onCleared()
    }
}
