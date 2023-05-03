package com.example.spotifydownloader.SpotifyApi.api

import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.AUTH_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAuthInstance {

    private val retrofitAuth by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    }
    val apiAuth : AuthApi by lazy {

        retrofitAuth.create(AuthApi::class.java)

    }
}