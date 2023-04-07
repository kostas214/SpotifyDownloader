package com.example.spotifydownloader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.example.spotifydownloader.databinding.FragmentAlbumBinding
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.apache.commons.io.IOUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


class albumFragment : Fragment(R.layout.fragment_album) {
    private  lateinit var binding: FragmentAlbumBinding
    private val tag = "MainActivity"
    private var  data: Intent? = null





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAlbumBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)


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


        binding.permsAB.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
            Log.d(tag,resultLauncher.toString())
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
        //Get radio button selection
        fun radioButtonSelection(): Int {
            var toBeReturned = 0
            if (binding.selection1AB.isChecked) {
                toBeReturned = 1
            } else if (binding.selection2AB.isChecked) {
                toBeReturned = 2
            } else if (binding.selection3AB.isChecked) {
                toBeReturned = 3
            } else if (binding.selection4AB.isChecked) {
                toBeReturned = 4
            }else if (binding.selection5AB.isChecked) {
                toBeReturned = 5
            }
            return toBeReturned
        }

        fun download(songName: String): Int{
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
                val documentFIle: DocumentFile = DocumentFile.fromTreeUri((context as Activity),uri!!)!!
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
                    binding.progressBarAB.incrementProgressBy(1)
                    return 2
                }
                Log.d(tag, "No errors")
                binding.progressBarAB.incrementProgressBy(1)
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
        fun enableDisableUI(enable: Boolean) {

            binding.permsAB.isEnabled = enable
            binding.PlaylistLinkTextBoxAB.isEnabled = enable
            binding.downloadAB.isEnabled = enable
            binding.selection1AB.isEnabled = enable
            binding.selection2AB.isEnabled = enable
            binding.selection3AB.isEnabled = enable
            binding.selection4AB.isEnabled = enable

        }
        binding.downloadAB.setOnClickListener {
            binding.progressBarAB.progress = 0
            runOnUiThread {
                enableDisableUI(false)
            }
            var queue = 0
            //define variables
            var songNames: List<PyObject>?

            lifecycleScope.launch(Dispatchers.IO) {

                val albumLink = binding.PlaylistLinkEditTextAB.text.toString()
                val response = async { module.callAttr("songSearchSpotifyAlbum", albumLink) }
                val songs = response.await().asList().toList()
                songNames = songs[0].asList()
                val successCode = songs[1].toInt()
                Log.d(tag, "Songs are $songNames")
                Log.d(tag, "Code is $successCode")
                val songNamesSize = (songNames as MutableList<PyObject>).size
                binding.progressBarAB.max = songNamesSize

                if (isDeviceOnline(context as Activity)  && data !=null && successCode == 0) {

                    val concurrentThreads = radioButtonSelection()
                    val executors = Executors.newFixedThreadPool(concurrentThreads)

                    for (i in songNames as MutableList<PyObject>) {
                        val worker = Runnable {

                            if (!executors.isShutdown) {

                                val songName = i.toString()
                                when (download(songName)) {
                                    0 -> {

                                        if (songNamesSize == binding.progressBarAB.progress) {
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
                                        if (songNamesSize == binding.progressBarAB.progress) {
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