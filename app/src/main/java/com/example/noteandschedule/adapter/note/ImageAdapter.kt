package com.example.noteandschedule.adapter.note

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.noteandschedule.R
import com.example.noteandschedule.databinding.ImageLayoutBinding

class ImageAdapter(private val selectedItem: OnSelectedItem)
    : ListAdapter<String, ImageAdapter.ImageViewHolder>(DiffCallback) {

    interface OnSelectedItem {
        fun onImageClicked(position: Int)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

        }
    }

    class ImageViewHolder(private val binding: ImageLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(image: String) {
            Glide.with(itemView)
                .load(Uri.parse(image))
                .placeholder(R.drawable.progress_loading_image)
                .into(binding.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentImage = getItem(position)
        holder.onBind(currentImage)
        holder.itemView.setOnClickListener { selectedItem.onImageClicked(position) }
    }
}