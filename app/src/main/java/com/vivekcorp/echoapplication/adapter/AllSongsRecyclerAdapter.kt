package com.vivekcorp.echoapplication.adapter

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.fragment.AllSongsFragment
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment
import com.vivekcorp.echoapplication.model.AudioModel

class AllSongsRecyclerAdapter(val context: Context, private val itemList: ArrayList<AudioModel>)
    :RecyclerView.Adapter<AllSongsRecyclerAdapter.AllSongsRecyclerViewHolder>(){

        class AllSongsRecyclerViewHolder(view: View):RecyclerView.ViewHolder(view){
            val songTitle: TextView = view.findViewById(R.id.txtSongTitle)
            val songArtist: TextView = view.findViewById(R.id.txtSongArtist)
            val contentHolder: LinearLayout = view.findViewById(R.id.contentHolder)
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
        holder.contentHolder.setOnClickListener{

            NowPlayingFragment.Statified.mediaPlayer?.pause()
                val nowPlayingFragment = NowPlayingFragment()
                val args = Bundle()
                args.putString("songArtist", songs.artist)
                args.putString("path", songs.songData)
                args.putString("songTitle", songs.songTitle)
                args.putInt("songId", songs.songID.toInt() as Int)
                args.putInt("songPosition", position)
                args.putParcelableArrayList("songData", itemList)
                nowPlayingFragment.arguments = args
                (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame, nowPlayingFragment)
                    .addToBackStack("NowPlayingFragment")
                    .commit()

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}