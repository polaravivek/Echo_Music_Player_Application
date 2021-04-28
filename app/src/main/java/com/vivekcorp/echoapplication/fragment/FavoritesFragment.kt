package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.content.Context
import android.content.EntityIterator
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.TextView
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
import kotlinx.android.synthetic.main.fragment_all_songs.view.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.playPauseButton
import kotlinx.android.synthetic.main.fragment_now_playing.*
import java.util.*
import kotlin.collections.ArrayList

class FavoritesFragment : Fragment() {

    var myActivity: Activity? = null
    lateinit var getSongsList : ArrayList<AudioModel>

    var getEntitiesList = listOf<Entities>()

    lateinit var recyclerAllSongsView: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var hiddenBarMainScreen: RelativeLayout

    var visibleLayout: RelativeLayout? = null
    lateinit var noSongs: RelativeLayout

    var songTitle: TextView? = null
    var playPauseButton: ImageButton? = null
    var trackPosition: Int = 0

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

        getSongsList = ArrayList()
        layoutManager = LinearLayoutManager(activity)

        visibleLayout = view?.findViewById(R.id.visibleLayout)

        noSongs = view.findViewById(R.id.noSongs)

        hiddenBarMainScreen = view.findViewById(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById(R.id.nowPlaying)
        playPauseButton = view.findViewById(R.id.playPauseButton)

        if (NowPlayingFragment.Statified.mediaPlayer?.isPlaying == true) {
            hiddenBarMainScreen.visibility = View.VISIBLE
        } else if(NowPlayingFragment.Statified.mediaPlayer?.isPlaying == false){
            hiddenBarMainScreen.visibility = View.VISIBLE
        }

        recyclerAllSongsView = view.findViewById(R.id.recyclerFavSongsView)

        myActivity = context as Activity

        getEntitiesList = RetrieveFavorites(activity as Context).execute().get()

        if(getEntitiesList.isEmpty()){
            noSongs.visibility = View.VISIBLE
        }else{
            noSongs.visibility = View.INVISIBLE
        }

        println("indices = ${getEntitiesList.indices}")
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

        println("in on create view $getSongsList")

        recyclerAllSongsView.addItemDecoration(
            DividerItemDecoration(
                recyclerAllSongsView.context,
                DividerItemDecoration.VERTICAL
            )
        )

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

            println("get song list = $getSongsList")
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
        bottomBarSetup()
    }

    class RetrieveFavorites(val context: Context): AsyncTask<Void, Void, List<Entities>>() {
        override fun doInBackground(vararg params: Void?): List<Entities> {
            val db = Room.databaseBuilder(context, SongDatabase::class.java,"songsNew-db").build()

            return db.songDao().getAllSong()
        }
    }

    fun bottomBarSetup() {

        try {

            bottomBarClickHandler()
            songTitle?.text = NowPlayingFragment.Statified.currentSongHelper.songTitle
            NowPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener {
                songTitle?.text = NowPlayingFragment.Statified.currentSongHelper.songTitle
                NowPlayingFragment.Staticated.onSongComplete()
            }

            if (NowPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                hiddenBarMainScreen.visibility = View.VISIBLE
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)

            } else if (NowPlayingFragment.Statified.mediaPlayer?.isPlaying == false){
                hiddenBarMainScreen.visibility = View.VISIBLE
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    override fun onResume() {
//        super.onResume()
//        println("here")
//        println("$getEntitiesList")
//    }

    fun bottomBarClickHandler() {
        hiddenBarMainScreen.setOnClickListener {
            AllSongsFragment.Statified.mediaPlayer = NowPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = NowPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", NowPlayingFragment.Statified.currentSongHelper.songArtist)
            args.putString("path", NowPlayingFragment.Statified.currentSongHelper.songPath)
            args.putString("songTitle", NowPlayingFragment.Statified.currentSongHelper.songTitle)
            args.putInt(
                "songId",
                NowPlayingFragment.Statified.currentSongHelper.songId.toInt()
            )

            args.putInt(
                "songPosition",
                NowPlayingFragment.Statified.currentSongHelper.currentPosition
            )
            args.putParcelableArrayList("songData", NowPlayingFragment.Statified.fetchSongs)
            args.putString("favBottomBar", "success")
            songPlayingFragment.arguments = args

            fragmentManager!!.beginTransaction()
                .replace(R.id.frame, songPlayingFragment)
                .addToBackStack("NowPlayingFragment")
                .commit()
        }

        playPauseButton?.setOnClickListener {
            if (NowPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                NowPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition =
                    NowPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                NowPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                NowPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }
}