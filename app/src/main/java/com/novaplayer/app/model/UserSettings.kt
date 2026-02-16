package com.novaplayer.app.model

data class UserSettings(
    val darkTheme: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val showWaveform: Boolean = false,
    val shuffleMode: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sleepTimerMinutes: Int = 0
)

