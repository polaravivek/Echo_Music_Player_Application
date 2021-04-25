package com.vivekcorp.echoapplication.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.Comparator

@SuppressLint("ParcelCreator")
class AudioModel(
    var songID: Long,
    var songTitle: String,
    var artist: String,
    var songData: String,
    var dateAdded: Long
): Parcelable{
    override fun writeToParcel(p0: Parcel?, p1: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    object Statified {

        var nameComparator: Comparator<AudioModel> = Comparator<AudioModel> { song1, song2 ->
            val songOne = song1.songTitle.toUpperCase(Locale.ROOT)
            val songTwo = song2.songTitle.toUpperCase(Locale.ROOT)
            songOne.compareTo(songTwo)
        }

        var dateComparator: Comparator<AudioModel> = Comparator<AudioModel> { song1, song2 ->
            val songOne = song1.dateAdded.toDouble()
            val songTwo = song2.dateAdded.toDouble()
            songTwo.compareTo(songOne)
        }
    }
}
