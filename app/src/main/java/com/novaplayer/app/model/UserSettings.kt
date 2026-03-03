package com.novaplayer.app.model

data class UserSettings(
    val darkTheme: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val showWaveform: Boolean = false,
    val shuffleMode: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sleepTimerMinutes: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val skipSilence: Boolean = false,
    val crossfadeDurationMs: Int = 0,
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0
)
