package com.vivekcorp.echoapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.model.AudioModel

class AllSongsRecyclerAdapter(val context: Context, private val itemList: ArrayList<AudioModel>)
    :RecyclerView.Adapter<AllSongsRecyclerAdapter.AllSongsRecyclerViewHolder>(){

        class AllSongsRecyclerViewHolder(view: View):RecyclerView.ViewHolder(view){
            val songTitle: TextView = view.findViewById(R.id.txtSongTitle)
            val songArtist: TextView = view.findViewById(R.id.txtSongArtist)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSongsRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_all_songs_single_row, parent, false)

        return AllSongsRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllSongsRecyclerViewHolder, position: Int) {
        val songs = itemList[position]
        holder.songTitle.text = songs.songTitle
        holder.songArtist.text = songs.artist
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}