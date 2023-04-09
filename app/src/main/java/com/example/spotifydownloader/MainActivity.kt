package com.example.spotifydownloader

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.spotifydownloader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavView = binding.bottomNav
        val navController = findNavController(R.id.fragment)

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






                R.id.playListFragment -> {




                    binding.drawerLayout.closeDrawer(GravityCompat.START)


                }
                R.id.albumFragment -> {




                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }
                R.id.songFragment -> {





                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }


            }
            true


        }





    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }


        return super.onOptionsItemSelected(item)
    }


}