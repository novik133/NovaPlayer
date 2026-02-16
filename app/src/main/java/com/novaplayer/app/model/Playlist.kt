package com.novaplayer.app.model

data class Playlist(
    val id: Long,
    val name: String,
    val trackIds: List<Long>
)

