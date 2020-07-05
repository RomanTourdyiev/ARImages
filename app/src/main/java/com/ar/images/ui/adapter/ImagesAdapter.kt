package com.ar.images.ui.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.ar.images.R
import com.ar.images.dao.ImageRoomDatabase
import com.ar.images.dao.ImageRoomEntity
import kotlinx.android.synthetic.main.fragment_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ImagesAdapter : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    private var imagesList = listOf<ImageRoomEntity>()
    private var editIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    open fun setData(imagesList: List<ImageRoomEntity>) {
        this.imagesList = imagesList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = imagesList[holder.adapterPosition]
        holder.itemView.item_id.text = item.id.toString()
        holder.itemView.item_name.text = item.title
        holder.itemView.item_name_edittext.setText(item.title)
        holder.itemView.item_image.setImageBitmap(
            BitmapFactory.decodeByteArray(
                item.bitmap,
                0,
                item.bitmap.size
            )
        )

        if (holder.adapterPosition == editIndex) {
            holder.itemView.item_name_edit_button.setImageDrawable(
                holder.itemView.context.resources.getDrawable(
                    R.drawable.check
                )
            )
            holder.itemView.item_name.visibility = GONE
            holder.itemView.item_name_edittext.visibility = VISIBLE
        } else {
            holder.itemView.item_name_edit_button.setImageDrawable(
                holder.itemView.context.resources.getDrawable(
                    R.drawable.pencil
                )
            )
            holder.itemView.item_name.visibility = VISIBLE
            holder.itemView.item_name_edittext.visibility = GONE
        }
        holder.itemView.item_name_edit_button.setOnClickListener {
            if (holder.adapterPosition == editIndex) {
                // saving
                editIndex = -1
                item.title = holder.itemView.item_name_edittext.text.toString()
                GlobalScope.launch(Dispatchers.IO) {
                    ImageRoomDatabase.getDB(holder.itemView.context)?.imageRoomDao()?.update(item)
                }
            } else {
                // start editing
                editIndex = holder.adapterPosition
                notifyItemChanged(holder.adapterPosition)
            }
        }

    }

    override fun getItemCount(): Int = imagesList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}