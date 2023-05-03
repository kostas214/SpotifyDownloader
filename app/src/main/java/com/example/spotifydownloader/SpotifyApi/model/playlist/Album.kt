package com.example.spotifydownloader.SpotifyApi.model.playlist

data class Album(
    val album_group: String,
    val album_type: String,
    val artists: List<ArtistX>,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val total_tracks: Int,
    val type: String,
    val uri: String
)