package com.novaplayer.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.novaplayer.app.model.Playlist
import com.novaplayer.app.model.RepeatMode
import com.novaplayer.app.model.TrackStats
import com.novaplayer.app.model.UserSettings

class PersistenceHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nova_player_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Playlists persistence
    fun savePlaylists(playlists: List<Playlist>) {
        val json = gson.toJson(playlists)
        prefs.edit().putString(KEY_PLAYLISTS, json).apply()
    }

    fun loadPlaylists(): List<Playlist> {
        val json = prefs.getString(KEY_PLAYLISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Playlist>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Settings persistence
    fun saveSettings(settings: UserSettings) {
        prefs.edit()
            .putBoolean(KEY_DARK_THEME, settings.darkTheme)
            .putBoolean(KEY_GAPLESS_PLAYBACK, settings.gaplessPlayback)
            .putBoolean(KEY_SHOW_WAVEFORM, settings.showWaveform)
            .putBoolean(KEY_SHUFFLE_MODE, settings.shuffleMode)
            .putInt(KEY_REPEAT_MODE, settings.repeatMode.ordinal)
            .putInt(KEY_SLEEP_TIMER, settings.sleepTimerMinutes)
            .putFloat(KEY_PLAYBACK_SPEED, settings.playbackSpeed)
            .putBoolean(KEY_SKIP_SILENCE, settings.skipSilence)
            .putInt(KEY_CROSSFADE_DURATION, settings.crossfadeDurationMs)
            .putInt(KEY_BASS_BOOST, settings.bassBoostStrength)
            .putInt(KEY_VIRTUALIZER, settings.virtualizerStrength)
            .putInt(KEY_LOUDNESS_GAIN, settings.loudnessGain)
            .apply()
    }

    fun loadSettings(): UserSettings {
        return UserSettings(
            darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
            gaplessPlayback = prefs.getBoolean(KEY_GAPLESS_PLAYBACK, true),
            showWaveform = prefs.getBoolean(KEY_SHOW_WAVEFORM, false),
            shuffleMode = prefs.getBoolean(KEY_SHUFFLE_MODE, false),
            repeatMode = RepeatMode.values()[prefs.getInt(KEY_REPEAT_MODE, 0)],
            sleepTimerMinutes = prefs.getInt(KEY_SLEEP_TIMER, 0),
            playbackSpeed = prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f),
            skipSilence = prefs.getBoolean(KEY_SKIP_SILENCE, false),
            crossfadeDurationMs = prefs.getInt(KEY_CROSSFADE_DURATION, 0),
            bassBoostStrength = prefs.getInt(KEY_BASS_BOOST, 0),
            virtualizerStrength = prefs.getInt(KEY_VIRTUALIZER, 0),
            loudnessGain = prefs.getInt(KEY_LOUDNESS_GAIN, 0)
        )
    }

    // Track stats persistence
    fun saveTrackStats(stats: Map<Long, TrackStats>) {
        val json = gson.toJson(stats)
        prefs.edit().putString(KEY_TRACK_STATS, json).apply()
    }

    fun loadTrackStats(): Map<Long, TrackStats> {
        val json = prefs.getString(KEY_TRACK_STATS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<Long, TrackStats>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Listening streak
    fun saveLastListenDate(dateMillis: Long) {
        prefs.edit().putLong(KEY_LAST_LISTEN_DATE, dateMillis).apply()
    }

    fun getLastListenDate(): Long = prefs.getLong(KEY_LAST_LISTEN_DATE, 0)

    fun saveStreak(days: Int) {
        prefs.edit().putInt(KEY_LISTEN_STREAK, days).apply()
    }

    fun getStreak(): Int = prefs.getInt(KEY_LISTEN_STREAK, 0)

    fun saveTotalListeningTime(timeMs: Long) {
        prefs.edit().putLong(KEY_TOTAL_LISTENING_TIME, timeMs).apply()
    }

    fun getTotalListeningTime(): Long = prefs.getLong(KEY_TOTAL_LISTENING_TIME, 0)

    companion object {
        private const val KEY_PLAYLISTS = "playlists"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_GAPLESS_PLAYBACK = "gapless_playback"
        private const val KEY_SHOW_WAVEFORM = "show_waveform"
        private const val KEY_SHUFFLE_MODE = "shuffle_mode"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_SLEEP_TIMER = "sleep_timer"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
        private const val KEY_SKIP_SILENCE = "skip_silence"
        private const val KEY_CROSSFADE_DURATION = "crossfade_duration"
        private const val KEY_BASS_BOOST = "bass_boost"
        private const val KEY_VIRTUALIZER = "virtualizer"
        private const val KEY_LOUDNESS_GAIN = "loudness_gain"
        private const val KEY_TRACK_STATS = "track_stats"
        private const val KEY_LAST_LISTEN_DATE = "last_listen_date"
        private const val KEY_LISTEN_STREAK = "listen_streak"
        private const val KEY_TOTAL_LISTENING_TIME = "total_listening_time"
    }
}
