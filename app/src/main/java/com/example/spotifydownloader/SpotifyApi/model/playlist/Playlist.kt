package com.example.spotifydownloader.SpotifyApi.model.playlist

data class Playlist(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: Any,
    val offset: Int,
    val previous: Any,
    val total: Int
)