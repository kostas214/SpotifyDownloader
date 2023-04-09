package com.example.spotifydownloader


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.spotifydownloader.databinding.FragmentDownloadBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class downloadFragment : Fragment(R.layout.fragment_download) {
    private lateinit var binding: FragmentDownloadBinding
    private val tag = "MainActivity"
    private lateinit var bootomNav :BottomNavigationView
    private lateinit var navController: NavController
    private val args by navArgs<downloadFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bootomNav = activity?.findViewById(R.id.bottomNav)!!
        bootomNav.visibility = View.GONE




    }
    override fun onDetach() {
        super.onDetach()
        bootomNav.visibility = View.VISIBLE

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController =Navigation.findNavController(view)









    }
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

}



