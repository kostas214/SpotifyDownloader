package com.example.spotifydownloader

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifydownloader.SpotifyApi.SpotifyApi
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_ID
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_SECRET

class SharedViewModel:ViewModel() {
    var playlistLink = MutableLiveData<String>()
    var albumLink = MutableLiveData<String>()
    var trackLink = MutableLiveData<String>()
    var folderUri = MutableLiveData<Uri?>()
    val spotifyApi = SpotifyApi(CLIENT_ID, CLIENT_SECRET)
}