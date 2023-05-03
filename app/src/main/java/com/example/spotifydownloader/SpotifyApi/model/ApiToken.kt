package com.example.spotifydownloader.SpotifyApi.model


data class ApiToken (
        val access_token :String,
        val token_type :String,
        val expires_in : Int,

        )

