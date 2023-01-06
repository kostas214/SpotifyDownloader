package com.example.SpotifyDownloader

import android.Manifest
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.SpotifyDownloader.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private var SuccessCode: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("main")
        var songNames = listOf<PyObject>()
        var code: Int?

        binding.perms.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    100
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${applicationContext.packageName}")
                startActivity(intent)
            }


        }

        binding.download2.setOnClickListener {
            binding.progressBar.progress = 0


            val deviceInternetAccess = isDeviceOnline(this.applicationContext)


            val saveDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)


            val task1 = Thread {
                val data = module.callAttr(
                    "songSearchSpotify",
                    binding.PlaylistLinkEditText.text.toString()
                ).asList().toList()
                songNames = data[0].asList()
                SuccessCode = data[1].toInt()
                binding.progressBar.max = songNames.size
                println("Got Data")

            }


            val task2 = Thread {
                code = null
                SuccessCode = null
                runOnUiThread {
                    binding.perms.isEnabled = false
                    binding.PlaylistLinkTextBox.isEnabled = false
                    binding.download2.isEnabled = false
                }
                while (true) {
                    when (code) {
                        1 -> {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "Connection Error try again later",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.perms.isEnabled = true
                                binding.PlaylistLinkTextBox.isEnabled = true
                                binding.download2.isEnabled = true

                            }
                            break
                        }
                        2 -> {
                            runOnUiThread {
                                Toast.makeText(this, "Internal server is down", Toast.LENGTH_SHORT)
                                    .show()
                                binding.perms.isEnabled = true
                                binding.PlaylistLinkTextBox.isEnabled = true
                                binding.download2.isEnabled = true
                            }
                            break
                        }
                        3 -> {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "Storage permission not given",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.perms.isEnabled = true
                                binding.PlaylistLinkTextBox.isEnabled = true
                                binding.download2.isEnabled = true
                            }
                            break
                        }
                    }
                    when (SuccessCode) {
                        1 -> {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "Connection Error try again later",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.perms.isEnabled = true
                                binding.PlaylistLinkTextBox.isEnabled = true
                                binding.download2.isEnabled = true
                            }
                            break
                        }
                        2 -> {
                            runOnUiThread {
                                Toast.makeText(this, "Invalid link", Toast.LENGTH_SHORT).show()
                                binding.perms.isEnabled = true
                                binding.PlaylistLinkTextBox.isEnabled = true
                                binding.download2.isEnabled = true
                            }
                            break
                        }
                    }
                    if (!task1.isAlive) {
                        if (binding.progressBar.progress == binding.progressBar.max) {
                            println(binding.progressBar.progress)
                            println(binding.progressBar.max)
                            runOnUiThread {
                                Toast.makeText(this, "Finished", Toast.LENGTH_SHORT).show()
                                binding.perms.isEnabled = true
                                binding.PlaylistLinkTextBox.isEnabled = true
                                binding.download2.isEnabled = true
                            }
                            break
                        }
                    }
                }

                }
            task1.start()
            task2.start()


            GlobalScope.launch {

                println(SuccessCode)
                var run = true

                while (run) {
                    val isRunning = task1.isAlive
                    if (!isRunning) {
                        run = false

                        val executor = Executors.newFixedThreadPool(4)
                        for (i in songNames) {
                            val worker = Runnable {
                                code = module.callAttr("DownloadSongs", i, saveDirectory).toInt()
                                binding.progressBar.incrementProgressBy(1)
                                if (code == 1 || code == 2 || code == 3) {
                                    executor.shutdownNow()
                                    println("1")
                                }
                            }
                            if (SuccessCode == 0 && deviceInternetAccess) {
                                executor.execute(worker)
                            }
                        }
                    }
                }
            }
        }
    }
}


fun isDeviceOnline(context: Context): Boolean {
    val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connManager.getNetworkCapabilities(connManager.activeNetwork)
        if (networkCapabilities == null) {
            println("Device Offline")
            return false
        } else {
            println("Device Online")
            return true
        }
    } else {
        // below Marshmallow
        val activeNetwork = connManager.activeNetworkInfo
        if (activeNetwork?.isConnectedOrConnecting == true && activeNetwork.isAvailable) {
            println("Device Online")
            return true
        } else {
            println("Device Offline")
            return false
        }
    }
}
