package com.example.spotifydownloader.rvAdaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.spotifydownloader.R
import com.example.spotifydownloader.SpotifyApi.model.Search.Item

class SongSearchRecyclerViewAdaptor( private val dataSet:List<Item>):
    RecyclerView.Adapter<SongSearchRecyclerViewAdaptor.ViewHolder>() {


    private lateinit var mListener:onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener:onItemClickListener){
        mListener = listener

    }


        class ViewHolder(view: View,listener:onItemClickListener):RecyclerView.ViewHolder(view) {
            val coverImage: ImageView
            val songName:TextView
            val artistName:TextView

            init {
                coverImage = view.findViewById(R.id.songCoverImage1)
                songName = view.findViewById(R.id.songTitle1)
                artistName = view.findViewById(R.id.artistName1)

                itemView.setOnClickListener {
                    listener.onItemClick(adapterPosition)
                }


            }
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.song_search_item, viewGroup, false)

        return ViewHolder(view,mListener)
    }


    override fun getItemCount(): Int = dataSet.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.coverImage.load(dataSet[position].album.images[0].url)
        holder.songName.text = dataSet[position].name
        holder.artistName.text = dataSet[position].artists[0].name
    }


}