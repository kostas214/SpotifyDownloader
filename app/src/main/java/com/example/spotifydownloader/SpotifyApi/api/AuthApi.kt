package com.example.spotifydownloader.SpotifyApi.api

import com.example.spotifydownloader.SpotifyApi.model.ApiToken
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/token")
    @FormUrlEncoded
    suspend fun getAuthenticationToken(@Field("grant_type") grant_type :String,
                                       @Field("client_id")client_id:String,
                                       @Field("client_secret") client_secret: String) :
            Response<ApiToken>
}