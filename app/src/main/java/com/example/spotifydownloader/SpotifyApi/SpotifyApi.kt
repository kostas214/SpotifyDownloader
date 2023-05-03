package com.example.spotifydownloader.SpotifyApi

import android.util.Log
import com.example.spotifydownloader.SpotifyApi.api.RetrofitApiInstance
import com.example.spotifydownloader.SpotifyApi.api.RetrofitAuthInstance
import com.example.spotifydownloader.SpotifyApi.model.Album.Album
import com.example.spotifydownloader.SpotifyApi.model.Artist.Artist
import com.example.spotifydownloader.SpotifyApi.model.ExtractIdResponse
import com.example.spotifydownloader.SpotifyApi.model.Search.Search
import com.example.spotifydownloader.SpotifyApi.model.Track.Track
import com.example.spotifydownloader.SpotifyApi.model.playlist.Playlist
import retrofit2.HttpException
import java.io.IOException

class SpotifyApi(private val clientId: String, private val clientSecret: String, var authToken:String? = null){

    private val tag = "MainActivity"
    private suspend fun getAuthTokenOrRefresh(clientId: String,clientSecret: String){



        try {
            val response = RetrofitAuthInstance.apiAuth.getAuthenticationToken(
                grant_type = "client_credentials",
                client_id = clientId,
                client_secret = clientSecret
            )
            if (response.isSuccessful && response.body() != null ){
                Log.d(tag,response.body()!!.access_token)
                authToken = response.body()!!.access_token
            }
            else{
                throw error("Unexpected Response")

            }

        }catch (e: IOException){
            throw IOException("No Internet Connection")


        }catch (e: HttpException){
            throw error("Unexpected Response")
        }

    }

    suspend fun getAlbum(albumLink:String): Album {



        if (authToken == null){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
        }

        val link = extractId(albumLink)
        if ((link.type != "album" )){
            throw IllegalArgumentException("The link isn't an album type ")
        }
        try {


            val response = RetrofitApiInstance.api.getAlbum(
                albumId = link.id,
                headers = mapOf("Authorization" to "Bearer  $authToken")
            )
            return response.body()!!
        }
        catch (e: IOException){
            throw IOException("No Internet Connection")
        }
        catch (e:NullPointerException){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
            try {
                val response = RetrofitApiInstance.api.getAlbum(
                    albumId = link.id,
                    headers = mapOf("Authorization" to "Bearer  $authToken")
                )
                return response.body()!!
            }
            catch (e: IOException){
                throw IOException("No Internet Connection")
            }
            catch (e:NullPointerException){
                throw error("Invalid Id")
            }
        }
    }
    suspend fun getArtist(artistLink:String):Artist{



        if (authToken == null){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
        }

        val link = extractId(artistLink)
        if ((link.type != "artist" )){
            throw IllegalArgumentException("The link isn't an artist type ")
        }
        try {


            val response = RetrofitApiInstance.api.getArtist(
                artistId = link.id,
                headers = mapOf("Authorization" to "Bearer  $authToken")
            )
            return response.body()!!
        }
        catch (e: IOException){
            throw IOException("No Internet Connection")
        }
        catch (e:NullPointerException){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
            try {
                val response = RetrofitApiInstance.api.getArtist(
                    artistId = link.id,
                    headers = mapOf("Authorization" to "Bearer  $authToken")
                )
                return response.body()!!
            }
            catch (e: IOException){
                throw IOException("No Internet Connection")
            }
            catch (e:NullPointerException){
                throw error("Invalid Id")
            }
        }
    }
    suspend fun getTrack(trackId:String):Track{



        if (authToken == null){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
        }

        val link = extractId(trackId)
        if ((link.type != "track" )){
            throw IllegalArgumentException("The link isn't an track type ")
        }
        try {


            val response = RetrofitApiInstance.api.getTrack(
                trackId = link.id,
                headers = mapOf("Authorization" to "Bearer  $authToken")
            )
            return response.body()!!
        }
        catch (e: IOException){
            throw IOException("No Internet Connection")
        }
        catch (e:NullPointerException){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
            try {
                val response = RetrofitApiInstance.api.getTrack(
                    trackId = link.id,
                    headers = mapOf("Authorization" to "Bearer  $authToken")
                )
                return response.body()!!
            }
            catch (e: IOException){
                throw IOException("No Internet Connection")
            }
            catch (e:NullPointerException){
                throw error("Invalid Id")
            }
        }
    }
    suspend fun getPlaylist(playlistLink:String,offset:Int):Playlist{



        if (authToken == null){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
        }

        val link = extractId(playlistLink)
        if ((link.type != "playlist" )){
            throw IllegalArgumentException("The link isn't a playlist type ")
        }
        try {


            val response = RetrofitApiInstance.api.getPlaylist(
                playListId = link.id,
                headers = mapOf("Authorization" to "Bearer  $authToken"),
                offset = offset
            )
            return response.body()!!
        }
        catch (e: IOException){
            throw IOException("No Internet Connection")
        }
        catch (e:NullPointerException){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
            try {
                val response = RetrofitApiInstance.api.getPlaylist(
                    playListId = link.id,
                    headers = mapOf("Authorization" to "Bearer  $authToken"),
                    offset = offset
                )





                return response.body()!!
            }
            catch (e: IOException){
                throw IOException("No Internet Connection")
            }
            catch (e:NullPointerException){
                throw IllegalArgumentException("Invalid Id")
            }
        }
    }


    suspend fun getSearch(query:String):Search{
        if (authToken == null){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
        }
        try {


            val response = RetrofitApiInstance.api.getSearch(
                searchQuery = query,
                headers = mapOf("Authorization" to "Bearer  $authToken"),
            )
            return response.body()!!
        }
        catch (e: IOException){
            throw IOException("No Internet Connection")
        }
        catch (e:NullPointerException){
            getAuthTokenOrRefresh(
                clientId = clientId,
                clientSecret = clientSecret
            )
            try {
                val response = RetrofitApiInstance.api.getSearch(
                    searchQuery = query,
                    headers = mapOf("Authorization" to "Bearer  $authToken")
                )
                return response.body()!!
            }
            catch (e: IOException){
                throw IOException("No Internet Connection")
            }
            catch (e:NullPointerException){
                throw IllegalArgumentException("Invalid Search")
            }
        }








    }



    private fun extractId(link:String): ExtractIdResponse {
        if (link.contains("/track/")) {
            val id = link.substringAfter("/track/").substringBefore("?")
            val data = ExtractIdResponse(id,"track")
            return data

        }
        else if(link.contains("/playlist/")) {
            val id = link.substringAfter("/playlist/").substringBefore("?")
            val data = ExtractIdResponse(id,"playlist")
            return data

        }
        else if (link.contains("/artist/")) {
            val id = link.substringAfter("/artist/").substringBefore("?")
            val data = ExtractIdResponse(id,"artist")
            return data

        }
        else if (link.contains("/album/")) {
            val id = link.substringAfter("/album/").substringBefore("?")
            val data = ExtractIdResponse(id,"album")
            return data

        }
        else{
            throw IllegalArgumentException("Link is invalid")

        }

    }
}
