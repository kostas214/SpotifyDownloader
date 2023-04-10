package com.example.spotifydownloader

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.spotifydownloader.databinding.FragmentPlayListBinding
import com.example.spotifydownloader.model.Data
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class playListFragment : Fragment(R.layout.fragment_play_list) {
    private lateinit var binding: FragmentPlayListBinding
    private val tag = "MainActivity"
    private var  data: Intent? = null
    private lateinit var navController:NavController



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = Navigation.findNavController(view)



        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(context as Activity));
        }
        val py = Python.getInstance()
        val module = py.getModule("main")

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
            var songNames: List<PyObject>?

            lifecycleScope.launch(Dispatchers.IO) {

                val playlistLink = binding.PlaylistLinkEditTextPLF.text.toString()
                val response = async { module.callAttr("songSearchSpotifyPlaylist", playlistLink) }
                val songs = response.await().asList().toList()
                songNames = songs[0].asList()
                val successCode = songs[1].toInt()
                Log.d(tag, "Songs are $songNames")
                Log.d(tag, "Code is $successCode")

                if (isDeviceOnline(context as Activity)  && data !=null && successCode == 0) {

                    val data = Data(songNames as MutableList<PyObject>, radioButtonSelection(), data!!.data)
                    val action = playListFragmentDirections.actionPlayListFragmentToDownloadFragment(data)
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