package com.example.spotifydownloader.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.spotifydownloader.R
import com.example.spotifydownloader.SharedViewModel
import com.example.spotifydownloader.SpotifyApi.SpotifyApi
import com.example.spotifydownloader.SpotifyApi.model.Search.Item
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_ID
import com.example.spotifydownloader.SpotifyApi.util.Constants.Companion.CLIENT_SECRET
import com.example.spotifydownloader.databinding.FragmentSongBinding
import com.example.spotifydownloader.parcels.ItemListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class SongFragment : Fragment(R.layout.fragment_song) {
    private lateinit var binding: FragmentSongBinding
    private var data: Intent? = null
    private val tag = "MainActivity"
    private lateinit var navController: NavController
    private val sharedViewModel : SharedViewModel by activityViewModels()
    private var folderUri: Uri?=null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSongBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)







        navController = Navigation.findNavController(view)
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    data = result.data
                    sharedViewModel.folderUri.value = data?.data
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

        fun enableDisableUI(enable: Boolean) {

            binding.perms.isEnabled = enable
            binding.songNameTextBox.isEnabled = enable
            binding.download.isEnabled = enable
            binding.artistNameTextBox.isEnabled = enable
        }
        val spotifyApi = sharedViewModel.spotifyApi


        sharedViewModel.folderUri.observe(viewLifecycleOwner , Observer {
            folderUri1 -> folderUri = folderUri1
        })

        binding.download.setOnClickListener {


            lifecycleScope.launch(Dispatchers.IO) {
                runOnUiThread {
                    enableDisableUI(false)
                }
                var succesCode = 0
                val ItemListObject = mutableListOf<Item>()





                try {


                    val textInBox = "${binding.songNameEditText.text.toString()} ${binding.artistNameEditText.text.toString()}"
                    val response =  spotifyApi.getSearch(textInBox)



                    if (response.tracks.items.size !=0) {
                        response.tracks.items.forEach {
                            ItemListObject.add(it)
                        }

                        succesCode = 0
                    }
                    else{
                        succesCode = 2
                    }



                }catch (e:IOException){
                    Log.e(tag,e.toString())

                    succesCode = 1



                }catch (e:IllegalArgumentException){
                    Log.e(tag,e.toString())

                    succesCode = 2


                }





                    if (isDeviceOnline(context as Activity)&&folderUri!=null&& succesCode== 0){

                        val itemListData = ItemListData(Items= ItemListObject.toList(), folderUri = folderUri)
                        val action = SongFragmentDirections.actionSongFragmentToSelectSongFragment(itemListData)
                        println(itemListData.Items[0])
                        runOnUiThread {
                            enableDisableUI(true)
                            navController.navigate(action)
                        }







                    }else if (succesCode == 1){
                        runOnUiThread {
                            Toast.makeText(
                                context as Activity,
                                "Connection Error try again later",
                                Toast.LENGTH_SHORT
                            ).show()


                            enableDisableUI(true)
                        }


                    }
                    else if (succesCode == 2 ) {
                        runOnUiThread    {
                            Toast.makeText(
                                context as Activity, "Invalid Song Name", Toast.LENGTH_SHORT
                            ).show()
                            enableDisableUI(true)
                        }
                    }


                    else if (folderUri == null){
                        runOnUiThread {
                            Toast.makeText(
                                context as Activity, "Please choose a folder", Toast.LENGTH_SHORT
                            ).show()
                            enableDisableUI(true)
                        }
                    }

                    else {
                        runOnUiThread {

                            Toast.makeText(
                                context as Activity,
                                "Connection Error try again later",
                                Toast.LENGTH_SHORT
                            ).show()


                            enableDisableUI(true)
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


