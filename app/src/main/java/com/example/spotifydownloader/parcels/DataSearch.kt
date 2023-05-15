package com.example.spotifydownloader.parcels

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class DataSearch(

    val songName:String,
    val imgUrl:String,
    val artistName:String,
    val filename:String,
    val albumName:String,
    val albumArtistName:String,
    val releaseDate:String,
    val folderUri: Uri?



) : Parcelable
