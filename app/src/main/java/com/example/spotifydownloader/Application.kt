package com.example.spotifydownloader

import android.app.Application
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.color.DynamicColors
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
                    YoutubeDL.getInstance().updateYoutubeDL(applicationContext)
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Download Library Initialization Failed", Toast.LENGTH_LONG).show()
            }
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this@SpotifyDownloaderApplication))
            }

        }
    }






    }

