package com.example.spotifydownloader.Fragments

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifydownloader.R
import com.example.spotifydownloader.databinding.FragmentSelectSongBinding
import com.example.spotifydownloader.parcels.DataSearch
import com.example.spotifydownloader.rvAdaptors.SongSearchRecyclerViewAdaptor
import com.google.android.material.bottomnavigation.BottomNavigationView


class selectSongFragment : Fragment(R.layout.fragment_select_song) {
    private  lateinit var binding :FragmentSelectSongBinding
    private lateinit var navController: NavController
    private val args by navArgs<selectSongFragmentArgs>()
    private lateinit var bottomNav : BottomNavigationView






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectSongBinding.bind(view)
        navController = Navigation.findNavController(view)
        bottomNav = activity?.findViewById(R.id.bottomNav)!!

        val rV = binding.rvSongSearch

        val from_top : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_top
        ) }
        val from_bottom : Animation by lazy { AnimationUtils.loadAnimation((context as Activity),
            R.anim.from_bottom
        ) }


        val callback = object  : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navController.popBackStack()
                bottomNav.startAnimation(from_bottom)
                bottomNav.menu.forEach {
                    it.isEnabled = true
                }


            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)


        bottomNav.startAnimation(from_top)
        bottomNav.menu.forEach {
            it.isEnabled = false
        }


        val adapter = SongSearchRecyclerViewAdaptor(args.ItemListData.Items)
        rV.adapter = adapter
        rV.layoutManager = LinearLayoutManager(context)
        adapter.setOnItemClickListener(object :SongSearchRecyclerViewAdaptor.onItemClickListener{
            override fun onItemClick(position: Int) {
                val regex = Regex("[^A-Za-z0-9]")
                val songData  = args.ItemListData.Items[position]


                val data = DataSearch(
                    songName = songData.name,
                    imgUrl = songData.album.images[0].url,
                    artistName = songData.artists[0].name,
                    filename = regex.replace(songData.name,""),
                    albumName = songData.album.name,
                    albumArtistName = songData.album.artists[0].name,
                    releaseDate = songData.album.release_date,
                    folderUri = args.ItemListData.folderUri
                )
                val action = selectSongFragmentDirections.actionSelectSongFragmentToSongDownloadFragment2(data)
                navController.navigate(action)



            }

        })





















    }




}