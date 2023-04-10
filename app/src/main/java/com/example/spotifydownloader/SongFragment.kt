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
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.spotifydownloader.databinding.FragmentSongBinding
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.File


class SongFragment : Fragment(R.layout.fragment_song) {
    private lateinit var binding: FragmentSongBinding
    private var data: Intent? = null
    private val tag = "MainActivity"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSongBinding.bind(view)
        val py = Python.getInstance()
        val module = py.getModule("main")
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(context as Activity));
        }

        super.onViewCreated(view, savedInstanceState)
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    data = result.data
                    if (data != null) {
                        Log.d(tag, data?.data.toString())
                        Log.d(tag, (context as Activity).externalCacheDir.toString())

                    }

                }

            }

        binding.perms.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
            Log.d(tag, resultLauncher.toString())


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

                val tmpFile = File.createTempFile(
                    "Spotify downloader", null, (context as Activity).externalCacheDir
                )
                tmpFile.delete()
                tmpFile.mkdir()
                tmpFile.deleteOnExit()
                val fileLocation = "${tmpFile.absolutePath}/${filename}"


                val request = YoutubeDLRequest(ytLInk)

                request.addOption("--output", fileLocation)
                request.addOption("--audio-format", "aac")
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("-R", "2")
                request.addOption("--socket-timeout", "40")


                val uri = data?.data

                val documentFIle: DocumentFile =
                    DocumentFile.fromTreeUri((context as Activity), uri!!)!!
                val dc: DocumentFile? = documentFIle.findFile(filename)
                Log.d(tag, dc?.exists().toString())

                if (isDeviceOnline(context as Activity) && dc?.exists() == null) {
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

                    var destUri = data?.data
                    val treeUri = Uri.parse(destUri.toString())
                    val docId = DocumentsContract.getTreeDocumentId(treeUri)
                    val destDir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)


                    val fileTempLocation = File(fileLocation)


                    val mimeType = MimeTypeMap.getSingleton()
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

                    return 2
                }
                Log.d(tag, "No errors")
                tmpFile.deleteRecursively()

                return 0


            } else if (code == 1) {
                Log.e(tag, "Connection error on getDownloadPath")

                return 1
            } else {
                Log.e(tag, "Index error")
                return 3
            }

        }
        fun enableDisableUI(enable: Boolean) {

            binding.perms.isEnabled = enable
            binding.songNameTextBox.isEnabled = enable
            binding.download.isEnabled = enable
        }


        binding.download.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {

                val deviceInternet = isDeviceOnline(context as Activity)

                val songName = binding.songNameEditText.text.toString()
                if (deviceInternet && data != null && songName != "") {
                    runOnUiThread {
                        enableDisableUI(false)
                    }
                    when (download(songName)) {

                        0 -> {
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
                        2 -> {
                            runOnUiThread {
                                Toast.makeText(
                                    context as Activity, "Already downloaded", Toast.LENGTH_SHORT
                                ).show()
                                enableDisableUI(true)
                            }

                        }
                        3 -> {
                            runOnUiThread {
                                Toast.makeText(
                                    context as Activity,
                                    "Please Insert a valid song name",
                                    Toast.LENGTH_SHORT
                                ).show()
                                enableDisableUI(true)
                            }

                        }

                    }


                } else if (data == null) {
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity, "Please choose a folder", Toast.LENGTH_SHORT
                        ).show()
                    }

                } else if (songName == "") {
                    runOnUiThread {
                        Toast.makeText(
                            context as Activity,
                            "Please Insert a valid song name",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                } else {
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