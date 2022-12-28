package com.example.SpotifyDownloader

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.SpotifyDownloader.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
            binding.download2.isEnabled= false
            binding.progressBar.setProgress(0)

            val saveDirectory = createDirectory("KOSTAS")

            val task1 = Thread {
                val data = module.callAttr(
                    "songSearchSpotify",
                    binding.PlaylistLinkEditText.text.toString()
                ).asList().toList()
                songNames = data
                println(songNames)

                binding.progressBar.max = songNames.size
                println("got data")

            }
            val task2 = Thread{

                var isCompleted = false
                while (!isCompleted) {
                    if (binding.progressBar.progress == binding.progressBar.max) {
                        runOnUiThread {
                            binding.download2.isEnabled = true
                            Toast.makeText(this,"Finished",Toast.LENGTH_SHORT).show()
                            println("End")
                        }
                        isCompleted = true

                    }
                }
            }
            task1.start()
            task2.start()


            GlobalScope.launch {
                var run = true
                while (run) {
                    val isRunning = task1.isAlive
                    if (!isRunning) {
                        val executor = Executors.newFixedThreadPool(4)
                        for (i in songNames) {
                            val worker = Runnable {
                                module.callAttr("DownloadSongs", i, saveDirectory)
                                binding.progressBar.incrementProgressBy(1) }

                            executor.execute(worker)
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
        subfoldername
    )
    println("Folder Created")
    return newFolder.toString()

}


