package com.example.SpotifyDownloader

import android.Manifest
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.SpotifyDownloader.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private  var SuccessCode : Int = 0
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
        var code: Int? = null



        println("songs : ${songNames}")

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
            SuccessCode = 0
            code = 0


            var deviceInternetAccess = isDeviceOnline(this.applicationContext)


            val saveDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

            val task1 = Thread {
                val data = module.callAttr(
                    "songSearchSpotify",
                    binding.PlaylistLinkEditText.text.toString()
                ).asList().toList()

                songNames = data[0].asList()
                SuccessCode = data[1].toInt()
                println("Succes code $SuccessCode")


                binding.progressBar.max = songNames.size
                println("got data")

            }
            val task2 = Thread {

                var isCompleted = false
                var ToastMessage : String
                runOnUiThread {
                    binding.download2.isEnabled = false
                    binding.progressBar.progress = 0
                    binding.PlaylistLinkEditText.isEnabled=false
                    binding.perms.isEnabled=false
                }
                while (!isCompleted) {

                    if (binding.progressBar.progress == binding.progressBar.max) {

                        if (SuccessCode == 2 )
                        {
                            ToastMessage = "Invalid Spotify Playlist Share Link"
                        }
                        else if (SuccessCode == 1){
                             ToastMessage = "No internet Connection"
                        }
                        else {
                            ToastMessage = "Other error"
                        }
                        runOnUiThread {
                            binding.download2.isEnabled = true
                            binding.PlaylistLinkEditText.isEnabled=true
                            binding.perms.isEnabled=true
                            Toast.makeText(this,ToastMessage , Toast.LENGTH_SHORT).show()
                            println("End")
                        }
                        isCompleted = true

                    }
                    if(code == 1){
                        runOnUiThread {
                            Toast.makeText(this,"Connection Error",Toast.LENGTH_SHORT).show()
                            binding.download2.isEnabled = true
                            binding.PlaylistLinkEditText.isEnabled=true
                            binding.perms.isEnabled=true
                            binding.progressBar.progress = 0



                        }
                        isCompleted = true


                    }
                    else if (code == 2){
                        runOnUiThread {
                            Toast.makeText(this,"Internal Server is not working",Toast.LENGTH_SHORT).show()
                            binding.download2.isEnabled = true
                            binding.PlaylistLinkEditText.isEnabled=true
                            binding.perms.isEnabled=true
                            binding.progressBar.progress = 0


                        }
                        isCompleted = true

                    }
                    else if (code == 3){
                        runOnUiThread {
                            Toast.makeText(this,"Permission not given",Toast.LENGTH_SHORT).show()
                            binding.download2.isEnabled = true
                            binding.PlaylistLinkEditText.isEnabled=true
                            binding.perms.isEnabled=true
                            binding.progressBar.progress = 0



                        }
                        isCompleted = true
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
                        val executor = Executors.newFixedThreadPool(4)
                        for (i in songNames) {
                            val worker = Runnable {
                                 code = module.callAttr("DownloadSongs", i, saveDirectory).toInt()
                                 binding.progressBar.incrementProgressBy(1)
                                if (code == 1 || code == 2 || code == 3){
                                    executor.shutdownNow()


                                }

                            }

                            if (SuccessCode == 0 && deviceInternetAccess) {
                                executor.execute(worker)
                            }
                            run = false
                        }
                    }


                }
            }


        }
    }
}

private fun createDirectory(subfoldername: String): String {
    val newFolder = File(

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        "$subfoldername"
    )
    println("Folder Created")
    return newFolder.toString()

}

fun isDeviceOnline(context: Context ): Boolean {
    val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities =  connManager.getNetworkCapabilities(connManager.activeNetwork)
        if (networkCapabilities == null) {
            println( "Device Offline")
            return false
        }
        else {
            println( "Device Online")
            return true
        }
    } else {
        // below Marshmallow
        val activeNetwork = connManager.activeNetworkInfo
        if (activeNetwork?.isConnectedOrConnecting == true && activeNetwork.isAvailable) {
            println( "Device Online")
            return true
        }
        else {
            println( "Device Offline")
            return false
        }
    }
}
