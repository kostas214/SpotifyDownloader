package com.example.spotifydownloader.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.spotifydownloader.R
import com.example.spotifydownloader.SharedViewModel
import com.example.spotifydownloader.SpotifyApi.model.Search.Item
import com.example.spotifydownloader.databinding.FragmentTrackBinding
import com.example.spotifydownloader.parcels.DataSearch
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class trackFragment : Fragment(R.layout.fragment_track) {
    private var _binding: FragmentTrackBinding? = null
    private var data: Intent? = null
    private val tag = "MainActivity"
    private lateinit var navController: NavController
    private val sharedViewModel : SharedViewModel by activityViewModels()
    private var folderUri: Uri?=null
    private lateinit var bottomNav : BottomNavigationView

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    data = result.data
                    sharedViewModel.folderUri.value = data?.data
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
        bottomNav = activity?.findViewById(R.id.bottomNav)!!
        val from_top : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_top
        ) }





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
            binding.PlaylistLinkTextBox.isEnabled = enable
            binding.download.isEnabled = enable
        }
        val spotifyApi = sharedViewModel.spotifyApi
        sharedViewModel.trackLink.observe(viewLifecycleOwner, Observer {
            trackLink ->
            binding.PlaylistLinkEditText.setText(trackLink)
        })
        sharedViewModel.folderUri.observe(viewLifecycleOwner, Observer {
            folderUri1 ->
            folderUri = folderUri1

        })
        binding.download.setOnClickListener {



            lifecycleScope.launch(Dispatchers.IO) {
                runOnUiThread {
                    enableDisableUI(false)
                }


            var succesCode = 0

            var songName  = ""
            var imgUrl = ""
            var artistName = ""
            var filename = ""
            var albumName = ""
            var albumArtistName = ""
            var releaseDate = ""
            val regex = Regex("[^A-Za-z0-9]")

            try {


                val textInBox = binding.PlaylistLinkEditText.text.toString()
                println(textInBox)
                val response = spotifyApi.getTrack(textInBox)
                songName = response.name
                imgUrl = response.album.images[0].url
                artistName = response.artists[0].name
                filename = regex.replace(response.name,"")
                albumName = response.album.name
                albumArtistName = response.album.artists[0].name
                releaseDate = response.album.release_date




            }catch (e:IOException) {
                Log.e(tag, e.toString())

                succesCode = 1
            }catch (e:IllegalArgumentException){
                Log.e(tag,e.toString())

                succesCode = 2


            }
                if (isDeviceOnline(context as Activity)&&folderUri!=null&& succesCode== 0){
                    val data = DataSearch(
                        songName=songName,
                        imgUrl = imgUrl,
                        artistName=artistName,
                        filename = filename,
                        albumName = albumName,
                        albumArtistName = albumArtistName,
                        releaseDate = releaseDate,
                        folderUri = folderUri,
                        isSearch = false
                    )
                    val action = trackFragmentDirections.actionTrackFragmentToSongDownloadFragment(data)
                    runOnUiThread {
                        bottomNav.startAnimation(from_top)
                        bottomNav.menu.forEach {
                            it.isEnabled = false
                        }
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
                            context as Activity, "Invalid track link", Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                }


                else if (folderUri == null){
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity, "Please choose a folder", Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                }

                else {
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

