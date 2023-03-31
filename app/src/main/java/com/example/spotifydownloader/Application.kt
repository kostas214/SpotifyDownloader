package com.example.spotifydownloader

import android.app.Application
import com.google.android.material.color.DynamicColors

class SpotifyDownloaderApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}