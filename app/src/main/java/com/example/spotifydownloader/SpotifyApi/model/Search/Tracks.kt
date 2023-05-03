package com.example.spotifydownloader.SpotifyApi.model.Search

data class Tracks(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)