package com.example.spotifydownloader

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.app.ActivityCompat
import com.chaquo.python.Python
import com.example.spotifydownloader.databinding.FragmentSongBinding
import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class SongFragment : Fragment(R.layout.fragment_song) {
    private lateinit var binding: FragmentSongBinding
    private val tag = "MainActivity"



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSongBinding.bind(view)
        val py = Python.getInstance()
        val module = py.getModule("main")

        super.onViewCreated(view, savedInstanceState)

        binding.perms.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    context as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${(context as Activity).packageName}")
                startActivity(intent)
            }
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

        fun download(songName: String): Int {


            val videoInfo = module.callAttr("getDownloadPath", songName).asList().toList()

            val filename = videoInfo[0].toString()
            val ytLInk = videoInfo[1].toString()
            val code = videoInfo[2].toInt()

            if (code == 0) {
                val youtubeDLDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "SpotifyDownloaderTest"
                )
                val fileLocation = "$youtubeDLDir/${filename}"

                val request = YoutubeDLRequest(ytLInk)

                request.addOption("--output", fileLocation)
                request.addOption("--audio-format", "aac")
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("-R", "2")
                request.addOption("--socket-timeout", "40")

                if (isDeviceOnline(context as Activity)) {
                    try {
                        YoutubeDL.getInstance().execute(
                            request
                        ) { _: Float, _: Long, _: String? -> }

                    } catch (e: YoutubeDLException) {
                        Log.e(tag, "Connection Error")
                        return 1
                    }


                    val responseCode =
                        module.callAttr("insertMetaData", songName, fileLocation).toInt()

                    if (responseCode == 1) {
                        Log.e(tag, "Connection Error on insertMetaData")
                        return responseCode
                    }

                } else {
                    Log.e(tag, "Connection Error")
                    return 1
                }
                Log.d(tag, "No errors")
                return 0


            } else {
                Log.e(tag, "Connection error on getDownloadPath")
                return 1
            }

        }
        fun enableDisableUI(enable: Boolean) {

            binding.perms.isEnabled = enable
            binding.songNameTextBox.isEnabled = enable
            binding.download.isEnabled = enable
        }
        fun checkPermission(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                val write = ContextCompat.checkSelfPermission(
                    context as Activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                val read = ContextCompat.checkSelfPermission(
                    context as Activity, Manifest.permission.READ_EXTERNAL_STORAGE
                )

                write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED

            }


        }

        binding.download.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO){

                val deviceInternet = isDeviceOnline(context as Activity)
                val hasPermission = checkPermission()

                if (deviceInternet && hasPermission){
                    runOnUiThread {
                        enableDisableUI(false)
                    }


                    val songName = binding.songNameEditText.text.toString()

                    when(download(songName)){

                        0->{
                            runOnUiThread {
                                Toast.makeText(
                                    context as Activity, "Finished", Toast.LENGTH_SHORT
                                ).show()
                                enableDisableUI(true)
                            }

                        }
                        1 -> {
                            runOnUiThread {
                                Toast.makeText(
                                    context as Activity,
                                    "Internet Connection Error",
                                    Toast.LENGTH_SHORT
                                ).show()
                                enableDisableUI(true)
                            }

                        }
                    }


                }
                else if (!hasPermission){
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity, "Storage Permission Not Given", Toast.LENGTH_SHORT
                        ).show()
                    }

                }



                else if (!deviceInternet){
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity,
                            "Connection Error try again later",
                            Toast.LENGTH_SHORT
                        ).show()
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