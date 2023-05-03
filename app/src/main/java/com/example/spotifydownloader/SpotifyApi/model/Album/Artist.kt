package com.example.spotifydownloader.SpotifyApi.model.Album

data class Artist(
    val external_urls: ExternalUrlsX,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)