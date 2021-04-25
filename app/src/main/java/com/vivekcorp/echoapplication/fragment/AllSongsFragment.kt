package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.model.AudioModel


class AllSongsFragment : Fragment() {

    var myActivity: Activity? = null
    lateinit var getSongList: ArrayList<AudioModel>

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
        myActivity = context as Activity

        getSongList = getAllAudioFromDevice(this@AllSongsFragment)
        println(getSongList)
        return view
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