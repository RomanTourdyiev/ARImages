package com.ar.images.dao

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Tourdyiev Roman on 03.07.2020.
 */
@Entity(tableName = "images")
class ImageRoomEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var title: String = ""
    var bitmap: ByteArray = byteArrayOf()
    var width: Float = 0.0F
}