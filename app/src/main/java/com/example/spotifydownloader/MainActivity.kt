package com.example.spotifydownloader

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.spotifydownloader.databinding.ActivityMainBinding
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class MainActivity : AppCompatActivity() {


    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        toggle = ActionBarDrawerToggle(
            this@MainActivity,
            binding.drawerLayout,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val playListFragment = playListFragment()
        val albumFragment = albumFragment()
        val songFragment = SongFragment()


        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, playListFragment)
            commit()


        }



        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {






                R.id.playlistMode -> {



                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, playListFragment)
                        addToBackStack(null)
                        commit()


                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)


                }
                R.id.albumMode -> {



                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, albumFragment)
                        addToBackStack(null)
                        commit()

                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }
                R.id.songNameMode -> {




                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, songFragment)
                        addToBackStack(null)
                        commit()

                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }


            }
            true


        }


        //Init youtubedl-android

        try {
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e("error", "failed to initialize youtubedl-android", e)


        }
        //Init chaquopy
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }


        return super.onOptionsItemSelected(item)
    }


}