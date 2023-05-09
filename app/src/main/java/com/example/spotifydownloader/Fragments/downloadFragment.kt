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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifydownloader.R
import com.example.spotifydownloader.databinding.FragmentDownloadBinding
import com.example.spotifydownloader.model.recyclerViewAdaptor
import com.example.spotifydownloader.model.songItemData
import com.example.spotifydownloader.mp3agic.Mp3File
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors


class downloadFragment : Fragment(R.layout.fragment_download) {
    private lateinit var binding: FragmentDownloadBinding
    private val tag = "MainActivity"
    private lateinit var bottomNav :BottomNavigationView
    private lateinit var navController: NavController
    private val args by navArgs<downloadFragmentArgs>()
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


        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)


        navController =Navigation.findNavController(view)
        var queue = 0
        bottomNav = activity?.findViewById(R.id.bottomNav)!!


        val from_top : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_top
        ) }
        val from_bottom :Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_bottom
        ) }
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






       val songNames = args.Data.songNames
        val songNamesSize = args.Data.songCount




        if (isDeviceOnline(context as Activity)){

            val concurrentThreads = args.Data.concurrentDownloads
            val executors = Executors.newFixedThreadPool(concurrentThreads)


            for (i in songNames) {


                val worker = Runnable {

                    if (!executors.isShutdown) {

                        if (!stop) {

                                when (download(args.Data.folderURI,songNames.indexOf(i))) {
                                    0 -> {
                                        Log.d(tag, "$completed")

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

                                        Log.d(tag, "Completed $completed")
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
    private fun download( data:Uri?,arrayPosition:Int): Int {




        val tmpFile = File.createTempFile(
            "Spotify downloader",
            null,
            (context as Activity).externalCacheDir
        )
        tmpFile.delete()
        tmpFile.mkdir()
        tmpFile.deleteOnExit()
        val datas = args.Data

        val songData = songItemData(datas.imgUrls[arrayPosition],datas.songNames[arrayPosition],datas.artistNames[arrayPosition],0)
        songItems.add(songData)
        val position = index
        runOnUiThread {
            adapter.notifyItemInserted(index)
            binding.recyclerView.scrollToPosition(position)
        }
        index += 1



        try {
            val fileLocation = "${tmpFile.absolutePath}/${datas.filenames[arrayPosition]}"
            val request =   YoutubeDLRequest("ytsearch:${datas.songNames[arrayPosition]} ${datas.artistNames[arrayPosition]}")


            request.addOption("--output", "${fileLocation}TMP.mp3")
            request.addOption("-x")
            request.addOption("--audio-format", "mp3")
            request.addOption("-R", "2")
            request.addOption("--socket-timeout", "40")

            request.addOption("--downloader", "libaria2c.so")
            request.addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"");




            println(request.buildCommand())


            val documentFIle: DocumentFile =
                DocumentFile.fromTreeUri((context as Activity), data!!)!!
            val dc: DocumentFile? = documentFIle.findFile("${datas.filenames[arrayPosition]}.mp3")
            println("${datas.filenames[arrayPosition]}.mp3")
            if (isDeviceOnline(context as Activity)&& dc?.exists()==null){

                try {
                    YoutubeDL.getInstance().execute(
                        request
                    ) { _: Float, _: Long, status: String? ->

                        Log.d(tag,status!!)

                        if (songItems[position].progress<=90) {
                            songItems[position].progress += 6
                            runOnUiThread {
                                adapter.notifyItemChanged(position)
                            }
                        }




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
                    image = getImageBytes(datas.imgUrls[arrayPosition])

                    mp3file.id3v2Tag.setAlbumImage(image,"image/jpeg")
                    mp3file.id3v2Tag.title = datas.songNames[arrayPosition]
                    mp3file.id3v2Tag.album = datas.albumNames[arrayPosition]
                    mp3file.id3v2Tag.albumArtist = datas.albumArtistNames[arrayPosition]
                    mp3file.id3v2Tag.date = datas.releaseDates[arrayPosition]
                    mp3file.id3v2Tag.albumArtist = datas.albumArtistNames[arrayPosition]
                    mp3file.save("$fileLocation.mp3")

                }catch (e:IOException){
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
                songItems[position].progress += 10
                runOnUiThread {
                    adapter.notifyItemChanged(position)
                }



                }
            else if (!(isDeviceOnline(context as Activity))) {
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




