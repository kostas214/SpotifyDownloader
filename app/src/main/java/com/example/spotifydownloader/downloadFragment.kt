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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.example.spotifydownloader.databinding.FragmentDownloadBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
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


    override fun onDestroy() {
        super.onDestroy()
        stop = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDownloadBinding.bind(view)
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


        binding.cancelBtn.setOnClickListener {
            stop = true
            Toast.makeText((context as Activity),"Stopping...",Toast.LENGTH_SHORT).show()
            binding.cancelBtn.isEnabled = false


        }
        val callback = object  : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Toast.makeText((context as Activity),"Stopping...",Toast.LENGTH_SHORT).show()
                stop = true
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)







        binding.progressBar.progress = 0

        if (isDeviceOnline(context as Activity)){

            val concurrentThreads = args.Data.concurrentDownloads
            val executors = Executors.newFixedThreadPool(concurrentThreads)

            val songNamesSize = args.Data.songNames.size

            binding.progressBar.max = songNamesSize

            for (i in args.Data.songNames as MutableList<PyObject>) {

                val worker = Runnable {

                    if (!executors.isShutdown) {

                        val songName = i.toString()


                        if (!stop) {


                                when (download(songName, args.Data.folderURI)) {
                                    0 -> {

                                        if (songNamesSize == binding.progressBar.progress) {
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
                                        if (songNamesSize == binding.progressBar.progress) {
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
        val videoInfo = module.callAttr("getDownloadPath", songName).asList().toList()

        val tmpFile = File.createTempFile("Spotify downloader", null, (context as Activity).externalCacheDir)
        tmpFile.delete()
        tmpFile.mkdir()
        tmpFile.deleteOnExit()

        val filename = videoInfo[0].toString()
        val ytLInk = videoInfo[1].toString()
        val code = videoInfo[2].toInt()

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
                        ) { _: Float, _: Long, _: String? -> }

                    } catch (e: YoutubeDLException) {
                        Log.e(tag, "Connection Error")
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
                    binding.progressBar.incrementProgressBy(1)
                    return 2
                }
                Log.d(tag, "No errors")
                binding.progressBar.incrementProgressBy(1)
                tmpFile.deleteRecursively()
                return 0
            }catch (e:java.lang.NullPointerException){
                tmpFile.deleteRecursively()
                return 4

            }
        } else if (code == 1){
            Log.e(tag, "Connection error on getDownloadPath")
            tmpFile.deleteRecursively()
            return 1
        }
        else {
            Log.e(tag, "Not specified folder")
            tmpFile.deleteRecursively()
            return 3
        }
    }

}



