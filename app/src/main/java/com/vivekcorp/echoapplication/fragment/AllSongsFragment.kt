package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.adapter.AllSongsRecyclerAdapter
import com.vivekcorp.echoapplication.model.AudioModel
import java.util.*
import kotlin.collections.ArrayList

class AllSongsFragment : Fragment() {

    var myActivity: Activity? = null
    lateinit var getSongsList: ArrayList<AudioModel>

    lateinit var recyclerAllSongsView: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null

    lateinit var recyclerAdapter: AllSongsRecyclerAdapter

    var _mainScreenAdapter: AllSongsRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)

        setHasOptionsMenu(true)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

        layoutManager = LinearLayoutManager(activity)

        visibleLayout = view?.findViewById(R.id.visibleLayout)

        noSongs = view?.findViewById(R.id.noSongs)

        recyclerAllSongsView = view.findViewById(R.id.recyclerAllSongsView)

        myActivity = context as Activity

        getSongsList = getAllAudioFromDevice(this@AllSongsFragment)

        recyclerAllSongsView.addItemDecoration(
            DividerItemDecoration(
                recyclerAllSongsView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        recyclerAdapter =
            AllSongsRecyclerAdapter(activity as Context, getSongsList)

        recyclerAllSongsView.adapter = recyclerAdapter

        recyclerAllSongsView.layoutManager = layoutManager

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main,menu)
        return
    }

    @RequiresApi(Build.VERSION_CODES.R)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = getAllAudioFromDevice(this)

        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs?.getString("action_sort_recent", "false")

        _mainScreenAdapter = AllSongsRecyclerAdapter(myActivity as Context,getSongsList)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val switcher = item.itemId
        if (switcher == R.id.action_sort_ascending) {

            val editorOne = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editorOne?.putString("action_sort_recent", "false")
            editorOne?.putString("action_sort_ascending", "true")
            editorOne?.apply()

            Collections.sort(getSongsList, AudioModel.Statified.nameComparator)
            _mainScreenAdapter?.notifyDataSetChanged()
            return false

        } else if (switcher == R.id.action_sort_recent) {

            val editorTwo = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editorTwo?.putString("action_sort_recent", "true")
            editorTwo?.putString("action_sort_ascending", "false")
            editorTwo?.apply()

            Collections.sort(getSongsList, AudioModel.Statified.dateComparator)
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudioFromDevice(context: AllSongsFragment): ArrayList<AudioModel> {
        var tempAudioList = ArrayList<AudioModel>()
        val contentResolver = myActivity?.contentResolver
        val uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver?.query(uri,null,null,null,null)

        if (cursor != null && cursor.moveToFirst()) {

            val songId = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {

                // Create a model object.
                val currentId = cursor.getLong(songId)
                val currentTitle = cursor.getString(songTitle)
                val currentArtist = cursor.getString(songArtist)
                val currentData = cursor.getString(songData)
                val currentDate = cursor.getLong(dateIndex) // Retrieve artist name.

                // Add the model object to the list .
                tempAudioList.add(AudioModel(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
            cursor.close()
        }
        return tempAudioList
    }

}