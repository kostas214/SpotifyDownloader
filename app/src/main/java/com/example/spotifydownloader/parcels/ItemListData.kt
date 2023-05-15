package com.example.spotifydownloader.parcels

import android.net.Uri
import android.os.Parcelable
import com.example.spotifydownloader.SpotifyApi.model.Search.Item
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ItemListData(
     var Items : @RawValue List<Item>,
     var folderUri: Uri?
):Parcelable
