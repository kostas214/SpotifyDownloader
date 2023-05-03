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
import com.example.spotifydownloader.SpotifyApi.util.Constants
import com.example.spotifydownloader.databinding.FragmentAlbumBinding
import com.example.spotifydownloader.model.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class albumFragment : Fragment(R.layout.fragment_album) {
    private  lateinit var binding: FragmentAlbumBinding
    private val tag = "MainActivity"
    private var  data: Intent? = null
    private lateinit var navController: NavController






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAlbumBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
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


        binding.permsAB.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
            Log.d(tag,resultLauncher.toString())
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
        //Get radio button selection
        fun radioButtonSelection(): Int {
            var toBeReturned = 0
            if (binding.selection1AB.isChecked) {
                toBeReturned = 1
            } else if (binding.selection2AB.isChecked) {
                toBeReturned = 2
            } else if (binding.selection3AB.isChecked) {
                toBeReturned = 3
            } else if (binding.selection4AB.isChecked) {
                toBeReturned = 4
            }else if (binding.selection5AB.isChecked) {
                toBeReturned = 5
            }
            return toBeReturned
        }


        fun enableDisableUI(enable: Boolean) {

            binding.permsAB.isEnabled = enable
            binding.PlaylistLinkTextBoxAB.isEnabled = enable
            binding.downloadAB.isEnabled = enable
            binding.selection1AB.isEnabled = enable
            binding.selection2AB.isEnabled = enable
            binding.selection3AB.isEnabled = enable
            binding.selection4AB.isEnabled = enable
            binding.selection5AB.isEnabled = enable

        }
        binding.downloadAB.setOnClickListener {
            runOnUiThread {
                enableDisableUI(false)
            }

            val songNames = mutableListOf<String>()
            val imgUrls= mutableListOf<String>()
            val artistNames=mutableListOf<String>()
            val filenames= mutableListOf<String>()
            val albumNameS= mutableListOf<String>()
            val albumArtistNames= mutableListOf<String>()
            val releaseDates= mutableListOf<String>()

            val regex = Regex("[^A-Za-z0-9]")



            lifecycleScope.launch(Dispatchers.IO) {


                val spotifyApi = SpotifyApi(clientId = Constants.CLIENT_ID, clientSecret = Constants.CLIENT_SECRET)
                val albumLink = binding.PlaylistLinkEditTextAB.text.toString()
                var successCode: Int



                try {

                    val albumResponse = spotifyApi.getAlbum(albumLink = albumLink)

                    albumResponse.tracks.items.forEach {
                        songNames.add(it.name)
                        imgUrls.add(albumResponse.images[0].url)
                        artistNames.add(albumResponse.artists[0].name)
                        filenames.add ( regex.replace(it.name, ""))
                        albumNameS.add(albumResponse.name)
                        albumArtistNames.add(albumResponse.artists[0].name)
                        releaseDates.add(albumResponse.release_date)

                    }
                    successCode = 0


                }catch (e: IOException){
                    Log.e(tag,e.toString())
                    successCode = 1


                }catch (e: NullPointerException) {
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
                    val action = albumFragmentDirections.actionAlbumFragmentToDownloadFragment(data)
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