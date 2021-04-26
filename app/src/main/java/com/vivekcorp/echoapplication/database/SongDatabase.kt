package com.vivekcorp.echoapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Entities::class], version = 1)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
}