package com.example.spotifydownloader


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.chaquo.python.PyObject
import com.example.spotifydownloader.databinding.FragmentDownloadBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class downloadFragment : Fragment(R.layout.fragment_download) {
    private lateinit var binding: FragmentDownloadBinding
    private val tag = "MainActivity"
    private lateinit var bootomNav :BottomNavigationView
    private lateinit var navController: NavController
    private  var songName:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bootomNav = activity?.findViewById(R.id.bottomNav)!!
        bootomNav?.visibility = View.GONE




    }
    override fun onDetach() {
        super.onDetach()
        bootomNav?.visibility = View.VISIBLE

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



