package com.example.spotifydownloader

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.spotifydownloader.databinding.FragmentAlbumBinding

class albumFragment : Fragment(R.layout.fragment_album) {
    private  var binding: FragmentAlbumBinding? = null




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAlbumBinding.bind(view)






        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }



}