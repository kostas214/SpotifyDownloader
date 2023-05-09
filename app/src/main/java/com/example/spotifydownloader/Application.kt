package com.example.spotifydownloader

import android.app.Application
import android.widget.Toast
import com.google.android.material.color.DynamicColors
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpotifyDownloaderApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)


        GlobalScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    YoutubeDL.getInstance().init(this@SpotifyDownloaderApplication)
                    FFmpeg.getInstance().init(this@SpotifyDownloaderApplication)
                    Aria2c.getInstance().init(this@SpotifyDownloaderApplication);

                    YoutubeDL.getInstance().updateYoutubeDL(this@SpotifyDownloaderApplication)
                    println(YoutubeDL.getInstance().version(this@SpotifyDownloaderApplication))
                    //YoutubeDL.getInstance().updateYoutubeDL(applicationContext)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Download Library Initialization Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


        }
    }






    }

