package com.example.spotifydownloader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.get
import androidx.navigation.ui.setupWithNavController
import com.example.spotifydownloader.SpotifyApi.SpotifyApi
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_ID
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_SECRET
import com.example.spotifydownloader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    private val tag = "MainActivity"
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)






        val bottomNavView = binding.bottomNav
        navController = findNavController(R.id.fragment)



        bottomNavView.setupWithNavController(navController)


        toggle = ActionBarDrawerToggle(
            this@MainActivity,
            binding.drawerLayout,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.updateYtDlp -> {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {


            val apiInstance = SpotifyApi(CLIENT_ID, CLIENT_SECRET)
            try {
                when (apiInstance.extractId(it).type){
                    "playlist"->{
                        sharedViewModel.playlistLink.value = it
                        navController.navigate(R.id.playListFragment)
                    }
                    "album"->{
                        sharedViewModel.albumLink.value = it
                        navController.navigate(R.id.albumFragment)



                    }

                    else -> {
                        Toast.makeText(this,"Invalid Link",Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e:IllegalArgumentException){
                Toast.makeText(this,"Invalid Link",Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}