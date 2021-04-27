package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.content.Context
import android.content.EntityIterator
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.adapter.AllSongsRecyclerAdapter
import com.vivekcorp.echoapplication.adapter.FavoriteRecyclerAdapter
import com.vivekcorp.echoapplication.database.Entities
import com.vivekcorp.echoapplication.database.SongDatabase
import com.vivekcorp.echoapplication.model.AudioModel
import java.util.*
import kotlin.collections.ArrayList

class FavoritesFragment : Fragment() {

    var myActivity: Activity? = null
    var getSongsList = ArrayList<AudioModel>()

    var getEntitiesList = listOf<Entities>()

    lateinit var recyclerAllSongsView: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    var visibleLayout: RelativeLayout? = null
    lateinit var noSongs: RelativeLayout

    lateinit var recyclerAdapter: FavoriteRecyclerAdapter

    var _mainScreenAdapter: FavoriteRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        setHasOptionsMenu(true)
        activity?.title = "Favorites"

        layoutManager = LinearLayoutManager(activity)

        visibleLayout = view?.findViewById(R.id.visibleLayout)

        noSongs = view.findViewById(R.id.noSongs)

        recyclerAllSongsView = view.findViewById(R.id.recyclerFavSongsView)

        myActivity = context as Activity

        getEntitiesList = RetrieveFavorites(activity as Context).execute().get()

        if(getEntitiesList.isEmpty()){
            noSongs.visibility = View.VISIBLE
        }else{
            noSongs.visibility = View.INVISIBLE
        }

        for (i in getEntitiesList.indices){
            val songListObject = getEntitiesList[i]

            val wantAddSongObject = AudioModel(
                songListObject.song_id,
                songListObject.songTitle,
                songListObject.artist,
                songListObject.songData,
                songListObject.dateAdded
            )
            getSongsList.add(wantAddSongObject)
        }

        recyclerAllSongsView.addItemDecoration(
            DividerItemDecoration(
                recyclerAllSongsView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        if (activity != null){
            recyclerAdapter =
                FavoriteRecyclerAdapter(activity as Context, getSongsList)

            recyclerAllSongsView.adapter = recyclerAdapter

            recyclerAllSongsView.layoutManager = layoutManager
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main,menu)
        return
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getEntitiesList = RetrieveFavorites(activity as Context).execute().get()

        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs?.getString("action_sort_recent", "false")

        if (activity != null){
            _mainScreenAdapter = FavoriteRecyclerAdapter(myActivity as Context,getSongsList)
            val mLayoutManager = LinearLayoutManager(myActivity)
            recyclerAllSongsView.layoutManager = mLayoutManager
            recyclerAllSongsView.itemAnimator = DefaultItemAnimator()
            recyclerAllSongsView.adapter = _mainScreenAdapter

            if (action_sort_ascending!!.equals("true", true)) {

                Collections.sort(getSongsList, AudioModel.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()

            }
            else if (action_sort_recent!!.equals("true", true)) {

                Collections.sort(getSongsList, AudioModel.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()

            }
        }

    }

    class RetrieveFavorites(val context: Context): AsyncTask<Void, Void, List<Entities>>() {
        override fun doInBackground(vararg params: Void?): List<Entities> {
            val db = Room.databaseBuilder(context, SongDatabase::class.java,"songsNew-db").build()

            return db.songDao().getAllSong()
        }

    }

}