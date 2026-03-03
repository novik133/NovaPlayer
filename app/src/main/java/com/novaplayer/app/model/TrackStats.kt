package com.novaplayer.app.model

data class TrackStats(
    val trackId: Long,
    val playCount: Int = 0,
    val totalListenTimeMs: Long = 0,
    val lastPlayedTimestamp: Long = 0
)
