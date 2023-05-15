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
import com.example.spotifydownloader.databinding.FragmentPlayListBinding
import com.example.spotifydownloader.parcels.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class playListFragment : Fragment(R.layout.fragment_play_list) {
    private lateinit var binding: FragmentPlayListBinding
    private val tag = "MainActivity"
    private var  data: Intent? = null
    private lateinit var navController:NavController



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = Navigation.findNavController(view)






        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                data = result.data
                if (data != null) {
                    Log.d(tag,data?.data.toString())
                    Log.d(tag,(context as Activity).externalCacheDir.toString())
                }
            }
        }
        binding = FragmentPlayListBinding.bind(view)
        //Permissions Button
        binding.permsPLF.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
            Log.d(tag,resultLauncher.toString())
        }

        //Check for internet access
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
        //Get radio button selection
        fun radioButtonSelection(): Int {
            var toBeReturned = 0
            if (binding.selection1PLF.isChecked) {
                toBeReturned = 1
            } else if (binding.selection2PLF.isChecked) {
                toBeReturned = 2
            } else if (binding.selection3PLF.isChecked) {
                toBeReturned = 3
            } else if (binding.selection4PLF.isChecked) {
                toBeReturned = 4
            }else if (binding.selection5PLF.isChecked) {
                toBeReturned = 5
            }
            return toBeReturned
        }
        //Download Logic



        //Enabling and disabling the ui
        fun enableDisableUI(enable: Boolean) {

            binding.permsPLF.isEnabled = enable
            binding.PlaylistLinkTextBoxPLF.isEnabled = enable
            binding.downloadPLF.isEnabled = enable
            binding.selection1PLF.isEnabled = enable
            binding.selection2PLF.isEnabled = enable
            binding.selection3PLF.isEnabled = enable
            binding.selection4PLF.isEnabled = enable
            binding.selection5PLF.isEnabled = enable

        }




        //Main part (Download button)
        binding.downloadPLF.setOnClickListener {
            runOnUiThread {
                enableDisableUI(false)
            }
            //define variables
            val songNames = mutableListOf<String>()
            val imgUrls= mutableListOf<String>()
            val artistNames=mutableListOf<String>()
            val filenames= mutableListOf<String>()
            val albumNameS= mutableListOf<String>()
            val albumArtistNames= mutableListOf<String>()
            val releaseDates= mutableListOf<String>()

            val regex = Regex("[^A-Za-z0-9]")

            lifecycleScope.launch(Dispatchers.IO) {

                val spotifyApi = SpotifyApi(clientId = CLIENT_ID, clientSecret = CLIENT_SECRET)
                val playlistLink = binding.PlaylistLinkEditTextPLF.text.toString()
                var successCode: Int

                try {
                    var offset = 0
                    var done = true

                    val playlistResponse = spotifyApi.getPlaylist(playlistLink = playlistLink,offset)
                    playlistResponse.items.forEach {
                        songNames.add(it.track.name)
                        imgUrls.add(it.track.album.images[0].url)
                        artistNames.add(it.track.artists[0].name)
                        filenames.add ( regex.replace(it.track.name, ""))
                        albumNameS.add(it.track.album.name)
                        albumArtistNames.add(it.track.album.artists[0].name)
                        releaseDates.add(it.track.album.release_date)

                    }


                    while (done) {
                        if (songNames.size >= offset + 100) {
                            offset +=100
                            val playlistResponse2 = spotifyApi.getPlaylist(playlistLink = playlistLink,offset)
                            playlistResponse2.items.forEach {
                                songNames.add(it.track.name)
                                imgUrls.add(it.track.album.images[0].url)
                                artistNames.add(it.track.artists[0].name)
                                filenames.add ( regex.replace(it.track.name, ""))
                                albumNameS.add(it.track.album.name)
                                albumArtistNames.add(it.track.album.artists[0].name)
                                releaseDates.add(it.track.album.release_date)

                            }


                        }
                        else{
                            done = false
                        }
                    }
                    successCode = 0

                }catch (e:IOException){
                    Log.e(tag,e.toString())
                    successCode = 1


                }catch (e:IllegalArgumentException) {
                    Log.e(tag, e.toString())
                    successCode = 2
                }







                if (isDeviceOnline(context as Activity)  && data !=null && successCode == 0) {

                    val data = Data(
                        concurrentDownloads =  radioButtonSelection(),
                        folderURI = data!!.data,
                        authToken = spotifyApi.authToken!!,
                        songNames = songNames,
                        imgUrls= imgUrls,
                        artistNames = artistNames,
                        filenames = filenames,
                        albumNames = albumNameS,
                        albumArtistNames = albumArtistNames,
                        releaseDates = releaseDates,
                        songCount = songNames.size





                    )
                    val action =
                        playListFragmentDirections.actionPlayListFragmentToDownloadFragment(data)
                    runOnUiThread {

                        navController.navigate(action)


                    }







                } else if (successCode == 1 ) {
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity,
                            "Connection Error try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }

                } else if (successCode == 2 ) {
                    runOnUiThread    {
                        Toast.makeText(
                            context as Activity, "Invalid Link", Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                }
                else if (data ==null) {
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity, "Please choose a folder", Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                }
                else{
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
            Log.d(tag, "END")
        }
    }
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
}