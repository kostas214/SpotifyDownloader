package com.example.spotifydownloader


import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.spotifydownloader.databinding.FragmentDownloadBinding
import com.example.spotifydownloader.model.recyclerViewAdaptor
import com.example.spotifydownloader.model.songItemData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executors


class downloadFragment : Fragment(R.layout.fragment_download) {
    private lateinit var binding: FragmentDownloadBinding
    private val tag = "MainActivity"
    private lateinit var bottomNav :BottomNavigationView
    private lateinit var navController: NavController
    private val args by navArgs<downloadFragmentArgs>()
    private lateinit var module:PyObject
    private var stop = false
    private var songItems = mutableListOf<songItemData>()
    private var completed = 0
    private var index = 0

    private val adapter = recyclerViewAdaptor(songItems)

    override fun onDestroy() {
        super.onDestroy()
        stop = true
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(tag,completed.toString())
        binding = FragmentDownloadBinding.bind(view)


        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(context as Activity))
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)


        navController =Navigation.findNavController(view)
        val py = Python.getInstance()
        module = py.getModule("main")
        var queue = 0
        bottomNav = activity?.findViewById(R.id.bottomNav)!!


        val from_top : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),R.anim.from_top) }
        val from_bottom :Animation by lazy { AnimationUtils.loadAnimation((context as Activity),R.anim.from_bottom) }
        bottomNav.startAnimation(from_top)
        bottomNav.menu.forEach {
            it.isEnabled = false
        }







        binding.fabButton.setOnClickListener {
            stop = true
            Toast.makeText((context as Activity),"Stopping...",Toast.LENGTH_SHORT).show()
            binding.fabButton.isEnabled = false


        }
        val callback = object  : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Toast.makeText((context as Activity),"Stopping...",Toast.LENGTH_SHORT).show()
                stop = true
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)






        if (isDeviceOnline(context as Activity)){

            val concurrentThreads = args.Data.concurrentDownloads
            val executors = Executors.newFixedThreadPool(concurrentThreads)

            val songNamesSize = args.Data.songNames.size





            for (i in args.Data.songNames as MutableList<PyObject>) {

                val worker = Runnable {

                    if (!executors.isShutdown) {

                        val songName = i.toString()


                        if (!stop) {


                                when (download(songName, args.Data.folderURI)) {
                                    0 -> {
                                        Log.d(tag,"$completed")

                                        if (songNamesSize == completed) {
                                            runOnUiThread {
                                                Toast.makeText(
                                                    context as Activity,
                                                    "Finished",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                                bottomNav.startAnimation(from_bottom)
                                                bottomNav.menu.forEach {
                                                    it.isEnabled = true
                                                }


                                            }


                                        }
                                    }
                                    1 -> {
                                        if (queue == 0) {
                                            queue += 1
                                            runOnUiThread {
                                                Toast.makeText(
                                                    context as Activity,
                                                    "Internet Connection Error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                                bottomNav.startAnimation(from_bottom)
                                                bottomNav.menu.forEach {
                                                    it.isEnabled = true
                                                }


                                            }
                                            executors.shutdown()
                                        }
                                    }
                                    2 -> {
                                        Log.d(tag,"Completed $completed")
                                        if (songNamesSize == completed) {
                                            runOnUiThread {
                                                Toast.makeText(
                                                    context as Activity,
                                                    "Finished",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                                bottomNav.startAnimation(from_bottom)
                                                bottomNav.menu.forEach {
                                                    it.isEnabled = true
                                                }


                                            }
                                        }
                                    }
                                    3 -> {
                                        if (queue == 0) {
                                            queue += 1
                                            runOnUiThread {
                                                Toast.makeText(
                                                    context as Activity,
                                                    "Please choose a folder",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                                bottomNav.startAnimation(from_bottom)
                                                bottomNav.menu.forEach {
                                                    it.isEnabled = true
                                                }


                                            }
                                            executors.shutdown()
                                        }
                                    }
                                }


                        }
                        else{
                            runOnUiThread {
                                Toast.makeText(
                                    context as Activity,
                                    "Stopped",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                                bottomNav.startAnimation(from_bottom)
                                bottomNav.menu.forEach {
                                    it.isEnabled = true
                                }
                            }
                            executors.shutdown()







                        }

                    }
                }
                executors.execute(worker)
            }
        }
    }
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
    private fun isDeviceOnline(context: Context): Boolean {
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
    private fun download(songName: String, data:Uri?): Int {

        try {


            val videoInfo = module.callAttr("getDownloadPath", songName).asList().toList()

            val tmpFile = File.createTempFile(
                "Spotify downloader",
                null,
                (context as Activity).externalCacheDir
            )
            tmpFile.delete()
            tmpFile.mkdir()
            tmpFile.deleteOnExit()

            val filename = videoInfo[0].toString()
            val ytLInk = videoInfo[1].toString()
            val imgUrl = videoInfo[2].toString()
            val code = videoInfo[3].toInt()
            val artistName = videoInfo[4].toString()
            val trackName = videoInfo[5].toString()

            val songData = songItemData(imgUrl,trackName,artistName,0)
            songItems.add(songData)
            val position = index
            runOnUiThread {
                adapter.notifyItemInserted(index)
                binding.recyclerView.scrollToPosition(position)
            }
            index += 1




            if (code == 0) {
                try {


                    val fileLocation = "${tmpFile.absolutePath}/${filename}"
                    val request = YoutubeDLRequest(ytLInk)

                    request.addOption("--output", fileLocation)
                    request.addOption("--audio-format", "aac")
                    request.addOption("-x")
                    request.addOption("--audio-format", "mp3")
                    request.addOption("-R", "2")
                    request.addOption("--socket-timeout", "40")

                    val documentFIle: DocumentFile =
                        DocumentFile.fromTreeUri((context as Activity), data!!)!!
                    val dc: DocumentFile? = documentFIle.findFile(filename)









                    if (isDeviceOnline(context as Activity) && dc?.exists() == null) {
                        try {






                            YoutubeDL.getInstance().execute(
                                request
                            ) { progress: Float, _: Long, _: String? ->
                                //Log.d(tag,"Progress is $progress and the song is $songName")
                                if (progress >0){
                                    songItems[position].progress = progress.toInt()
                                    runOnUiThread {
                                        adapter.notifyItemChanged(position)
                                    }

                                }



                            }

                        } catch (e: YoutubeDLException) {
                            Log.e(tag, e.message!!)
                            tmpFile.deleteRecursively()
                            return 1
                        }
                        val responseCode =
                            module.callAttr("insertMetaData", songName, fileLocation).toInt()
                        var destUri = data
                        val treeUri = Uri.parse(destUri.toString())
                        val docId = DocumentsContract.getTreeDocumentId(treeUri)
                        val destDir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        val fileTempLocation = File(fileLocation)
                        val mimeType =
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(fileTempLocation.extension) ?: "*/*"
                        destUri = DocumentsContract.createDocument(
                            (context as Activity).contentResolver,
                            destDir,
                            mimeType,
                            fileTempLocation.name.toString()
                        )
                        val ins = fileTempLocation.inputStream()
                        val ops = (context as Activity).contentResolver.openOutputStream(destUri!!)
                        IOUtils.copy(ins, ops)
                        IOUtils.closeQuietly(ops)
                        IOUtils.closeQuietly(ins)

                        if (responseCode == 1) {
                            Log.e(tag, "Connection Error on insertMetaData")
                            tmpFile.deleteRecursively()
                            return responseCode
                        } else if (responseCode == 2) {
                            Log.e(tag, "Spotipy name error")
                            tmpFile.deleteRecursively()
                            return 3
                        }
                    } else if (!(isDeviceOnline(context as Activity))) {
                        Log.e(tag, "Connection Error")
                        tmpFile.deleteRecursively()
                        return 1
                    } else {
                        Log.d(tag, "Already exists skipping")
                        tmpFile.deleteRecursively()
                        songItems[position].progress = 100
                        runOnUiThread {
                            adapter.notifyItemChanged(position)
                        }
                        //binding.progressBar.incrementProgressBy(1)
                        completed += 1
                        return 2
                    }
                    Log.d(tag, "No errors")
                    //binding.progressBar.incrementProgressBy(1)
                    completed += 1

                    tmpFile.deleteRecursively()
                    return 0
                } catch (e: java.lang.NullPointerException) {
                    tmpFile.deleteRecursively()
                    return 4
                }
            } else if (code == 1) {
                Log.e(tag, "Connection error on getDownloadPath")
                tmpFile.deleteRecursively()
                return 1
            } else {
                Log.e(tag, "Not specified folder")
                tmpFile.deleteRecursively()
                return 3
            }
        }catch (e: java.lang.NullPointerException){
            return 3
        }
    }
}




