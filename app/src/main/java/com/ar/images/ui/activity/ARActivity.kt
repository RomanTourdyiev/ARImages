package com.ar.images.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.ar.images.R
import com.ar.images.model.AugmentedImageNode
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.ar_activity.*
import kotlin.collections.HashMap

/**
 * Created by Tourdyiev Roman on 04.07.2020.
 */
class ARActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    private var augmentedImageMap: HashMap<AugmentedImage, AugmentedImageNode> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ar_activity)
        arFragment = (supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment)
        arFragment.arSceneView.scene.addOnUpdateListener {
            this.onUpdateFrame()
        }
    }

    override fun onResume() {
        super.onResume()
        if (augmentedImageMap.isEmpty()) {
            image_view_fit_to_scan.visibility = VISIBLE
        }
    }

    private fun onUpdateFrame() {
        val frame = arFragment.arSceneView.arFrame

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            return
        }
        val updatedAugmentedImages =
            frame.getUpdatedTrackables(
                AugmentedImage::class.java
            )
        for (augmentedImage in updatedAugmentedImages) {
            when (augmentedImage.trackingState) {
                TrackingState.TRACKING -> {
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        val node = AugmentedImageNode(this, arFragment)

                        node.setImage(augmentedImage)

                        augmentedImageMap[augmentedImage] = node
                        arFragment.arSceneView.scene.addChild(node)
                    }
                }
                TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage)
            }
        }
    }

}