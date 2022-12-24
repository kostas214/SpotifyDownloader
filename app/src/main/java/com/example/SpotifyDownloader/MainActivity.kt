package com.example.SpotifyDownloader

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
    @RequiresApi(Build.VERSION_CODES.M)
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
        var songURLS = listOf<PyObject>()







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
            val saveDirectory = createDirectory("KOSTAS")

            val task1 = Thread {
                val data = module.callAttr(
                    "songSearchSpotify",
                    binding.PlaylistLinkEditText.text.toString()
                ).asList().toList()
                songNames = data[0].asList()
                songURLS = data[1].asList()
                binding.progressBar.max=songNames.size
                println("got data")

            }
            task1.start()
            println("task1.start()")
            GlobalScope.launch {


                var run = true
                println("val isRunning = task1.isAlive")
                while (run){
                    val isRunning = task1.isAlive
                    println("while (true){")
                if (!isRunning){
                    println("Starting download")
                    val executor = Executors.newFixedThreadPool(8)

                    for (i in songNames){
                        println(i)
                        var index = 0
                        val worker = Runnable {
                            module.callAttr(
                                "DownloadSongs",
                                i,
                                songURLS[index],
                                saveDirectory,index
                            )
                            println("Downloading $i")

                            binding.progressBar.incrementProgressBy(1)
                            println(index)
                            index++
                        }
                        executor.execute(worker)


                    }
                    executor.shutdown()
                    run = false
                }

            }



            }










            //println(songURLS)
            //println(songURLS)


        }


    }


}


private fun createDirectory(subfoldername: String): String {
    val newFolder = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        subfoldername
    )
    return newFolder.toString()

}


