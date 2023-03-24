package com.example.spotifydownloader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.spotifydownloader.databinding.ActivityMainBinding
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val py = Python.getInstance()
        val module = py.getModule("main")


        //Permissions Button
        binding.perms.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${applicationContext.packageName}")
                startActivity(intent)
            }
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
            if (binding.selection1.isChecked) {
                toBeReturned = 1
            } else if (binding.selection2.isChecked) {
                toBeReturned = 2
            } else if (binding.selection3.isChecked) {
                toBeReturned = 3
            } else if (binding.selection4.isChecked) {
                toBeReturned = 4
            }
            return toBeReturned
        }

        //Download Logic
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

                if (isDeviceOnline(applicationContext)) {
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
                binding.progressBar.incrementProgressBy(1)
                return 0


            } else {
                Log.e(tag, "Connection error on getDownloadPath")
                return 1
            }

        }


        //Enabling and disabling the ui
        fun enableDisableUI(enable: Boolean) {

            binding.perms.isEnabled = enable
            binding.PlaylistLinkTextBox.isEnabled = enable
            binding.download.isEnabled = enable
            binding.selection1.isEnabled = enable
            binding.selection2.isEnabled = enable
            binding.selection3.isEnabled = enable
            binding.selection4.isEnabled = enable

        }

        fun checkPermission(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                val write = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                val read = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )

                write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED

            }


        }


        //Main part (Download button)
        binding.download.setOnClickListener {
            binding.progressBar.progress = 0


            val hasPermission = checkPermission()



            enableDisableUI(false)
            var queue = 0

            //define variables
            var songNames: List<PyObject>?


            lifecycleScope.launch(Dispatchers.IO) {


                val playlistLink = binding.PlaylistLinkEditText.text.toString()
                val response = async { module.callAttr("songSearchSpotify", playlistLink) }


                val data = response.await().asList().toList()
                songNames = data[0].asList()
                val successCode = data[1].toInt()
                Log.d(tag, "Songs are $songNames")
                Log.d(tag, "Code is $successCode")
                val songNamesSize = (songNames as MutableList<PyObject>).size
                binding.progressBar.max = songNamesSize
                var count = 1





                if (successCode == 0 && hasPermission) {
                    val concurrentThreads = radioButtonSelection()
                    val executors = Executors.newFixedThreadPool(concurrentThreads)

                    for (i in songNames as MutableList<PyObject>) {
                        val worker = Runnable {
                            val songName = i.toString()
                            count++
                            when (download(songName)) {
                                0 -> {

                                    if (songNamesSize == binding.progressBar.progress) {
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@MainActivity, "Finished", Toast.LENGTH_SHORT
                                            ).show()
                                            enableDisableUI(true)
                                        }
                                    }
                                }
                                1 -> {
                                    if (queue == 0) {
                                        queue += 1
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Internet Connection Error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            enableDisableUI(true)
                                        }
                                        executors.shutdown()
                                    }
                                }
                            }
                        }
                        executors.execute(worker)
                    }
                } else if (successCode == 1 && hasPermission) {
                    launch(Dispatchers.Main.immediate) {
                        Toast.makeText(
                            this@MainActivity,
                            "Connection Error try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                } else if (successCode == 2 && hasPermission) {
                    launch(Dispatchers.Main.immediate) {
                        Toast.makeText(
                            this@MainActivity, "Invalid Link", Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                } else if (!hasPermission) {
                    launch(Dispatchers.Main.immediate) {
                        Toast.makeText(
                            this@MainActivity, "Storage Permission Not Given", Toast.LENGTH_SHORT
                        ).show()
                        enableDisableUI(true)
                    }
                }
            }
            //enableDisableUI(true)
            Log.d(tag, "END")
        }
    }
}