package com.example.spotifydownloader.Fragments

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
import coil.load
import com.example.spotifydownloader.R
import com.example.spotifydownloader.databinding.FragmentSongDownloadBinding
import com.example.spotifydownloader.model.songItemData
import com.example.spotifydownloader.mp3agic.Mp3File
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL


class SongDownloadFragment : Fragment(R.layout.fragment_song_download) {
    private var fragmentSongDownload: FragmentSongDownloadBinding? = null
    private val tag = "MainActivity"
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var navController: NavController
    private val args by navArgs<SongDownloadFragmentArgs>()
    private var stop = false
    private var songItems = mutableListOf<songItemData>()
    private lateinit var binding:FragmentSongDownloadBinding




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         binding = FragmentSongDownloadBinding.bind(view)

        fragmentSongDownload = binding

        navController = Navigation.findNavController(view)

        bottomNav = activity?.findViewById(R.id.bottomNav)!!


        val from_top : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_top
        ) }
        val from_bottom : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_bottom
        ) }
        bottomNav.startAnimation(from_top)
        bottomNav.menu.forEach {
            it.isEnabled = false
        }

        val callback = object  : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Toast.makeText((context as Activity),"Stopping...",Toast.LENGTH_SHORT).show()
                stop = true
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

        binding.albumCover.load(args.DataSearch.imgUrl)
        binding.songName.text = args.DataSearch.songName
        binding.artistName.text = args.DataSearch.artistName


        lifecycleScope.launch (Dispatchers.IO){


            when(download()){
                0->{
                    delay(1000L)
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
                1->{
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

                }
                2->{
                    delay(1000L)
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


        }






















    }
    override fun onDestroyView() {
        // Consider not storing the binding instance in a field
        // if not needed.
        super.onDestroyView()
        stop = true
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
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
    private fun download(): Int {







        val data = args.DataSearch.folderUri
        val tmpFile = File.createTempFile(
            "Spotify downloader",
            null,
            (context as Activity).externalCacheDir
        )
        tmpFile.delete()
        tmpFile.mkdir()
        tmpFile.deleteOnExit()
        val datas = args.DataSearch

        try {
            val fileLocation = "${tmpFile.absolutePath}/${datas.filename}"
            val request =   YoutubeDLRequest("ytsearch:${datas.songName} ${datas.artistName}")


            request.addOption("--output", "${fileLocation}TMP.mp3")
            request.addOption("-x")
            request.addOption("--audio-format", "mp3")
            request.addOption("-R", "2")
            request.addOption("--socket-timeout", "40")
            println(request.buildCommand())


            val documentFIle: DocumentFile =
                DocumentFile.fromTreeUri((context as Activity), data!!)!!
            val dc: DocumentFile? = documentFIle.findFile("${datas.filename}.mp3")
            println("${datas.filename}.mp3")
            if (isDeviceOnline(context as Activity)&& dc?.exists()==null){

                try {
                    YoutubeDL.getInstance().execute(
                        request
                    ) { progress: Float, _: Long, _: String? ->

                        if(progress>0){
                            runOnUiThread {
                                binding.progressBar.progress = progress.toInt()
                            }
                        }
                        //Log.d(tag,"Progress is $progress and the song is $songName")



                    }

                }
                catch (e: YoutubeDLException) {
                    Log.e(tag, e.message!!)
                    tmpFile.deleteRecursively()
                    return 1
                }
                val mp3file = Mp3File("${fileLocation}TMP.mp3")
                val image :ByteArray?
                try {
                    image = getImageBytes(datas.imgUrl)

                    mp3file.id3v2Tag.setAlbumImage(image,"image/jpeg")
                    mp3file.id3v2Tag.title = datas.songName
                    mp3file.id3v2Tag.album = datas.albumName
                    mp3file.id3v2Tag.albumArtist = datas.albumArtistName
                    mp3file.id3v2Tag.date = datas.releaseDate
                    mp3file.id3v2Tag.albumArtist = datas.albumArtistName
                    mp3file.save("$fileLocation.mp3")

                }catch (e: IOException){
                    return 1
                }
                var destUri = data
                val treeUri = Uri.parse(destUri.toString())
                val docId = DocumentsContract.getTreeDocumentId(treeUri)
                val destDir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                val fileTempLocation = File("${fileLocation}.mp3")
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

            }
            else if (!(isDeviceOnline(context as Activity))) {
                Log.e(tag, "Connection Error")
                tmpFile.deleteRecursively()
                return 1
            } else {
                runOnUiThread {
                    binding.progressBar.progress = binding.progressBar.max
                }
                Log.d(tag, "Already exists skipping")
                tmpFile.deleteRecursively()



                return 2
            }
            Log.d(tag, "No errors")


            tmpFile.deleteRecursively()
            return 0
        }catch (e: java.lang.NullPointerException) {
            tmpFile.deleteRecursively()
            return 4
        }








    }

    private fun getImageBytes(imageUrl: String): ByteArray? {
        val url = URL(imageUrl)
        val output = ByteArrayOutputStream()
        url.openStream().use { stream ->
            val buffer = ByteArray(10000)
            while (true) {
                val bytesRead = stream.read(buffer)
                if (bytesRead < 0) {
                    break
                }
                output.write(buffer, 0, bytesRead)
            }
        }
        return output.toByteArray()
    }







}