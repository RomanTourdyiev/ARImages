package com.ar.images.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Created by Tourdyiev Roman on 03.07.2020.
 */
@Database(entities = [(ImageRoomEntity::class)], version = 1, exportSchema = false)
abstract class ImageRoomDatabase : RoomDatabase() {

    abstract fun imageRoomDao(): ImageRoomDao

    companion object {
        private var db: ImageRoomDatabase? = null
        fun getDB(context: Context): ImageRoomDatabase? {
            if (db == null) {
                synchronized(ImageRoomDatabase::class) {
                    db = Room.databaseBuilder(
                        context.applicationContext,
                        ImageRoomDatabase::class.java,
                        "imageRoomDatabase.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return db
        }
    }

}