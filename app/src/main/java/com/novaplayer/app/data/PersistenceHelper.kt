package com.novaplayer.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.novaplayer.app.model.Playlist
import com.novaplayer.app.model.RepeatMode
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
            .apply()
    }

    fun loadSettings(): UserSettings {
        return UserSettings(
            darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
            gaplessPlayback = prefs.getBoolean(KEY_GAPLESS_PLAYBACK, true),
            showWaveform = prefs.getBoolean(KEY_SHOW_WAVEFORM, false),
            shuffleMode = prefs.getBoolean(KEY_SHUFFLE_MODE, false),
            repeatMode = RepeatMode.values()[prefs.getInt(KEY_REPEAT_MODE, 0)],
            sleepTimerMinutes = prefs.getInt(KEY_SLEEP_TIMER, 0)
        )
    }

    companion object {
        private const val KEY_PLAYLISTS = "playlists"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_GAPLESS_PLAYBACK = "gapless_playback"
        private const val KEY_SHOW_WAVEFORM = "show_waveform"
        private const val KEY_SHUFFLE_MODE = "shuffle_mode"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_SLEEP_TIMER = "sleep_timer"
    }
}
