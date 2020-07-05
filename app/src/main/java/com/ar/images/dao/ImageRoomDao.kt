package com.ar.images.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Created by Tourdyiev Roman on 03.07.2020.
 */
@Dao
open interface ImageRoomDao{

    @Query("SELECT * FROM images")
    fun getAllImages():LiveData<List<ImageRoomEntity>>

    @Insert
    fun insert(image: ImageRoomEntity):Long

    @Update
    fun update(image: ImageRoomEntity)

}
