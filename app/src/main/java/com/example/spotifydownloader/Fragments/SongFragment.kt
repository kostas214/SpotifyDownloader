package com.example.spotifydownloader.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.spotifydownloader.R
import com.example.spotifydownloader.SpotifyApi.SpotifyApi
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_ID
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_SECRET
import com.example.spotifydownloader.databinding.FragmentSongBinding
import com.example.spotifydownloader.model.DataSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.NullPointerException



class SongFragment : Fragment(R.layout.fragment_song) {
    private lateinit var binding: FragmentSongBinding
    private var data: Intent? = null
    private val tag = "MainActivity"
    private lateinit var navController: NavController


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSongBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)


        navController = Navigation.findNavController(view)
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    data = result.data
                    if (data != null) {
                        Log.d(tag, data?.data.toString())
                        Log.d(tag, (context as Activity).externalCacheDir.toString())

                    }

                }

            }

        binding.perms.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
            Log.d(tag, resultLauncher.toString())


        }

        fun isDeviceOnline(context: Context): Boolean {
            val connManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connManager.getNetworkCapabilities(connManager.activeNetwork)
            return if (networkCapabilities == null) {
                println("Device Offline")
                false
            } else {
                true
            }
        }

        fun enableDisableUI(enable: Boolean) {

            binding.perms.isEnabled = enable
            binding.songNameTextBox.isEnabled = enable
            binding.download.isEnabled = enable
        }
        val spotifyApi = SpotifyApi(clientId = CLIENT_ID, clientSecret = CLIENT_SECRET)



        binding.download.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {
                runOnUiThread {
                    enableDisableUI(false)
                }
                var songName =""
                var imgUrl =""
                var artistName = ""
                var filename = ""
                var albumName = ""
                var albumArtistName = ""
                var releaseDate = ""
                var succesCode :Int




                try {


                    val textInBox = binding.songNameEditText.text.toString()
                    val response =  spotifyApi.getSearch(textInBox)

                    val searchResult = response.tracks.items[0]
                    val regex = Regex("[^A-Za-z0-9]")


                    songName = searchResult.name
                    imgUrl = searchResult.album.images[0].url
                    artistName = searchResult.artists[0].name
                    filename = regex.replace(searchResult.name, "")
                    albumName = searchResult.album.name
                    albumArtistName = searchResult.album.artists[0].name
                    releaseDate = searchResult.album.release_date
                    succesCode = 0


                }catch (e:IOException){
                    Log.e(tag,e.toString())

                    succesCode = 1



                }catch (e:IllegalArgumentException){
                    Log.e(tag,e.toString())

                    succesCode = 2


                }





                    if (isDeviceOnline(context as Activity)&&data!=null&& succesCode== 0){
                        val dataToBeSent = DataSearch(
                            songName = songName,
                            imgUrl = imgUrl,
                            artistName = artistName,
                            filename = filename,
                            albumName = albumName,
                            albumArtistName = albumArtistName,
                            releaseDate = releaseDate,
                            folderUri = data!!.data
                        )
                        val action = SongFragmentDirections.actionSongFragmentToSongDownloadFragment(dataToBeSent)
                        runOnUiThread {
                            enableDisableUI(true)
                            navController.navigate(action)
                        }







                    }else if (succesCode == 1){
                        runOnUiThread {
                            Toast.makeText(
                                context as Activity,
                                "Connection Error try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                            enableDisableUI(true)
                        }


                    }
                    else if (succesCode == 2 ) {
                        runOnUiThread    {
                            Toast.makeText(
                                context as Activity, "Invalid Song Name", Toast.LENGTH_SHORT
                            ).show()
                            enableDisableUI(true)
                        }
                    }


                    else if (data == null){
                        runOnUiThread {
                            Toast.makeText(
                                context as Activity, "Please choose a folder", Toast.LENGTH_SHORT
                            ).show()
                            enableDisableUI(true)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                context as Activity,
                                "Connection Error try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                            enableDisableUI(true)
                        }
                    }












            }
        }
    }
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

}


