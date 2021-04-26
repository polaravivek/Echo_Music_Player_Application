package com.vivekcorp.echoapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SongDao {

    @Insert
    fun insertSong(songEntity: Entities)

    @Delete
    fun deleteSong(songEntity: Entities)

    @Query("SELECT * FROM songs")
    fun getAllSong() : List<Entities>

    @Query("SELECT * FROM songs WHERE song_id= :songId")
    fun getSongById(songId: String): Entities
}