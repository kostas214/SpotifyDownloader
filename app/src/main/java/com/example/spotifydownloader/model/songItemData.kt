package com.example.spotifydownloader.model

import android.graphics.Bitmap

data class songItemData(val imageUrl : String,
                        val songName: String,
                        val artistName:String,
                        var progress:Int)
