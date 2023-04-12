package com.example.spotifydownloader.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.spotifydownloader.R

class recyclerViewAdaptor (private val songData: List<songItemData>)
    :RecyclerView.Adapter<recyclerViewAdaptor.ViewHolder>() {




    class ViewHolder(ItemView: View):RecyclerView.ViewHolder(ItemView){
        val songCoverImage: ImageView = itemView.findViewById(R.id.songCoverImage)
        val songTitle : TextView = itemView.findViewById(R.id.songTitle)
        val artistName : TextView = itemView.findViewById(R.id.artistName)
        val progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)


        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  view = LayoutInflater.from(parent.context).inflate(R.layout.song_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  songData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recyclerViewAdaptor = songData[position]

        holder.songCoverImage.load(recyclerViewAdaptor.imageUrl)


        if (recyclerViewAdaptor.songName.length>19) {
            holder.songTitle.text = recyclerViewAdaptor.songName.take(17).plus("...")
        }
        else{
            holder.songTitle.text = recyclerViewAdaptor.songName
        }
        holder.artistName.text = recyclerViewAdaptor.artistName
        holder.progressBar.progress = recyclerViewAdaptor.progress


    }


}