package com.vivekcorp.echoapplication.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Entities(
    @PrimaryKey val song_id: Long,
    @ColumnInfo(name = "song_title") val songTitle: String,
    @ColumnInfo(name = "song_artist") val artist: String,
    @ColumnInfo(name = "song_path") val songData: String,
    @ColumnInfo(name = "song_position") val songPosition: Int,
    @ColumnInfo(name = "song_date") val dateAdded: Long
)