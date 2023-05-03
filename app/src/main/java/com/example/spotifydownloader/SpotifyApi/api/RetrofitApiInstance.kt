package com.example.spotifydownloader.SpotifyApi.api

import com.example.spotifydownloader.SpotifyApi.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitApiInstance {

    private val retrofitApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_SPOTIFY)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api: SpotifyAPI by lazy {
        retrofitApi.create(SpotifyAPI::class.java)
    }
}