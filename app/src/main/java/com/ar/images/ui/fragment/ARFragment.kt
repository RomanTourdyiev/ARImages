package com.ar.images.ui.fragment

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ar.images.model.ImagesViewModel
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Tourdyiev Roman on 04.07.2020.
 */
class ARFragment : ArFragment() {

    private val MIN_OPENGL_VERSION = 3.0
    private lateinit var imagesViewModel: ImagesViewModel
    private var config:Config? = null
    private var session:Session? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        // Turn off the plane discovery since we're only looking for images
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val openGlVersionString =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e("ARFragment", "Sceneform requires OpenGL ES 3.0 or later")
        }
    }

    override fun getSessionConfiguration(session: Session?): Config? {
        val config = super.getSessionConfiguration(session)
        // Use setFocusMode to configure auto-focus.
        config.focusMode = Config.FocusMode.AUTO
        this.config = config
        this.session = session
        setupAugmentedImageDatabase()
        return config
    }


    private fun setupAugmentedImageDatabase(
    ) {
        var imageDatabase = AugmentedImageDatabase(session)
        imagesViewModel = ViewModelProviders.of(this).get(ImagesViewModel::class.java)
        imagesViewModel.getAllImages()
        imagesViewModel.imagesLiveData?.observe(viewLifecycleOwner, Observer { imagesList ->
            for (image in imagesList) {
                val bitmap: Bitmap =
                    BitmapFactory.decodeByteArray(image.bitmap, 0, image.bitmap.size)
                imageDatabase.addImage(image.title, bitmap)
            }
            config?.augmentedImageDatabase = imageDatabase
            arSceneView.session?.configure(config)
            Log.d("augmentedImageDatabase", config?.augmentedImageDatabase?.numImages.toString())
        })
    }


}