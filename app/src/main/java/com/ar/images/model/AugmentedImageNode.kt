package com.ar.images.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ar.images.R
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.ar_item_label.view.*

/**
 * Created by Tourdyiev Roman on 04.07.2020.
 */
class AugmentedImageNode(var context: Context? = null, var arFragment: ArFragment) : AnchorNode() {

    private var image: AugmentedImage? = null


    fun setImage(image: AugmentedImage) {
        this.image = image

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)

        var view: View = LayoutInflater.from(context).inflate(R.layout.ar_item_label, null)
        view.ar_label.text = image.name

        ViewRenderable.builder()
            .setView(context, view)
            .build()
            .thenAccept { renderable: ViewRenderable ->
                addNodeToScene(
                    arFragment,
                    anchor,
                    renderable
                )
            }
    }

    private fun addNodeToScene(
        arFragment: ArFragment,
        anchor: Anchor?,
        renderable: Renderable
    ) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        arFragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }
}