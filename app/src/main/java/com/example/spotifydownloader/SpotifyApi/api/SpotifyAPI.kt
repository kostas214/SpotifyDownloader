package com.example.spotifydownloader.SpotifyApi.api

import com.example.spotifydownloader.SpotifyApi.model.Album.Album
import com.example.spotifydownloader.SpotifyApi.model.Artist.Artist
import com.example.spotifydownloader.SpotifyApi.model.Search.Search
import com.example.spotifydownloader.SpotifyApi.model.Track.Track
import com.example.spotifydownloader.SpotifyApi.model.playlist.Playlist
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyAPI {
    @GET("artists/{artistID}")
    suspend fun getArtist(
        @Path("artistID") artistId:String,
        @HeaderMap headers: Map<String, String>
    ): Response<Artist>

    @GET("tracks/{trackId}")
    suspend fun getTrack(
        @Path("trackId") trackId:String,
        @HeaderMap headers: Map<String, String>
    ): Response<Track>

    @GET("albums/{albumId}")
    suspend fun getAlbum(
        @Path("albumId") albumId: String,
        @HeaderMap headers: Map<String, String>
    ): Response<Album>

    @GET("playlists/{playlistId}/tracks")
    suspend fun getPlaylist(
        @Path("playlistId") playListId: String,
        @HeaderMap headers: Map<String,String>,
        @Query("offset") offset:Int
    ): Response<Playlist>


    @GET("search")
    suspend fun getSearch(
        @HeaderMap headers: Map<String,String>,
        @Query("q") searchQuery:String,
        @Query("type")type:String = "track",


    ):Response<Search>


}