package com.ar.images.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ar.images.dao.ImageRoomDatabase
import com.ar.images.dao.ImageRoomEntity


/**
 * Created by Tourdyiev Roman on 03.07.2020.
 */
class ImagesViewModel(application: Application) : AndroidViewModel(application) {
    var imagesLiveData: LiveData<List<ImageRoomEntity>>? = null

    fun getAllImages() {
        imagesLiveData = ImageRoomDatabase.getDB(getApplication())?.imageRoomDao()?.getAllImages()
    }

}