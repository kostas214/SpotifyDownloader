package com.example.spotifydownloader.parcels


import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Data(
    val concurrentDownloads: Int,
    val folderURI: Uri?,
    val authToken:String,
    val songNames:List<String>,
    val imgUrls:List<String>,
    val artistNames:List<String>,
    val filenames:List<String>,
    val albumNames:List<String>,
    val albumArtistNames:List<String>,
    val releaseDates:List<String>,
    val songCount:Int


):Parcelable

