package com.example.spotifydownloader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.spotifydownloader.databinding.FragmentPlayListBinding
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executors


class playListFragment : Fragment(R.layout.fragment_play_list) {
    private lateinit var binding: FragmentPlayListBinding
    private val tag = "MainActivity"
    private var  data: Intent? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        try {
            YoutubeDL.getInstance().init(context as Activity )
            FFmpeg.getInstance().init(context as Activity)
        } catch (e: YoutubeDLException) {
            Log.e("error", "failed to initialize youtubedl-android", e)


        }
        //Init chaquopy
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context as Activity))
        }

        val py = Python.getInstance()
        val module = py.getModule("main")




        //Init youtubedl-android






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
        fun download(songName: String): Int {
        val videoInfo = module.callAttr("getDownloadPath", songName).asList().toList()

            val tmpFile = File.createTempFile("Spotify downloader", null, (context as Activity).externalCacheDir)
            tmpFile.delete()
            tmpFile.mkdir()
            tmpFile.deleteOnExit()

            val filename = videoInfo[0].toString()
            val ytLInk = videoInfo[1].toString()
            val code = videoInfo[2].toInt()

            if (code == 0 && data!=null ) {

                val fileLocation = "${tmpFile.absolutePath}/${filename}"
                val request = YoutubeDLRequest(ytLInk)

                request.addOption("--output", fileLocation)
                request.addOption("--audio-format", "aac")
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("-R", "2")
                request.addOption("--socket-timeout", "40")

                val uri = data?.data
                val documentFIle:DocumentFile = DocumentFile.fromTreeUri((context as Activity),uri!!)!!
                val dc: DocumentFile? = documentFIle.findFile(filename)
                if (isDeviceOnline(context as Activity) && dc?.exists()==null) {
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
                    var destUri = data?.data
                    val treeUri = Uri.parse(destUri.toString())
                    val docId = DocumentsContract.getTreeDocumentId(treeUri)
                    val destDir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    val fileTempLocation = File(fileLocation)
                    val mimeType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileTempLocation.extension) ?: "*/*"
                    destUri = DocumentsContract.createDocument(
                        (context as Activity).contentResolver,
                        destDir,
                        mimeType,
                        fileTempLocation.name.toString())
                    val ins = fileTempLocation.inputStream()
                    val ops = (context as Activity).contentResolver.openOutputStream(destUri!!)
                    IOUtils.copy(ins, ops)
                    IOUtils.closeQuietly(ops)
                    IOUtils.closeQuietly(ins)

                    if (responseCode == 1) {
                        Log.e(tag, "Connection Error on insertMetaData")
                        tmpFile.deleteRecursively()
                        return responseCode
                    }
                    else if (responseCode == 2){
                        Log.e(tag, "Spotipy name error")
                        tmpFile.deleteRecursively()
                        return 3
                    }
                } else if (!(isDeviceOnline(context as Activity))) {
                    Log.e(tag, "Connection Error")
                    tmpFile.deleteRecursively()
                    return 1
                }
                else {
                    Log.d(tag, "Already exists skipping")
                    tmpFile.deleteRecursively()
                    binding.progressBarPLF.incrementProgressBy(1)
                    return 2
                }
                Log.d(tag, "No errors")
                binding.progressBarPLF.incrementProgressBy(1)
                tmpFile.deleteRecursively()
                return 0

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


        //Enabling and disabling the ui
        fun enableDisableUI(enable: Boolean) {

            binding.permsPLF.isEnabled = enable
            binding.PlaylistLinkTextBoxPLF.isEnabled = enable
            binding.downloadPLF.isEnabled = enable
            binding.selection1PLF.isEnabled = enable
            binding.selection2PLF.isEnabled = enable
            binding.selection3PLF.isEnabled = enable
            binding.selection4PLF.isEnabled = enable

        }




        //Main part (Download button)
        binding.downloadPLF.setOnClickListener {
            binding.progressBarPLF.progress = 0
            runOnUiThread {
                enableDisableUI(false)
            }
            var queue = 0
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
                val songNamesSize = (songNames as MutableList<PyObject>).size
                binding.progressBarPLF.max = songNamesSize

                if (isDeviceOnline(context as Activity)  && data !=null && successCode == 0) {

                    val concurrentThreads = radioButtonSelection()
                    val executors = Executors.newFixedThreadPool(concurrentThreads)

                    for (i in songNames as MutableList<PyObject>) {
                        val worker = Runnable {

                            if (!executors.isShutdown) {

                                val songName = i.toString()
                                when (download(songName)) {
                                    0 -> {

                                        if (songNamesSize == binding.progressBarPLF.progress) {
                                            runOnUiThread {
                                                Toast.makeText(
                                                    context as Activity,
                                                    "Finished",
                                                    Toast.LENGTH_SHORT
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
                                                    context as Activity,
                                                    "Internet Connection Error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                enableDisableUI(true)
                                            }
                                            executors.shutdown()
                                        }
                                    }
                                    2 -> {
                                        if (songNamesSize == binding.progressBarPLF.progress) {
                                            runOnUiThread {
                                                Toast.makeText(
                                                    context as Activity,
                                                    "Finished",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                enableDisableUI(true)
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
                                                enableDisableUI(true)
                                            }
                                            executors.shutdown()
                                        }
                                    }
                                }
                            }
                        }
                        executors.execute(worker)
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