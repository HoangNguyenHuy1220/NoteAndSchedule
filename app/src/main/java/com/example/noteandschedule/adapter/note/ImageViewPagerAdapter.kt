package com.example.noteandschedule.adapter.note

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.databinding.ItemImageLayoutBinding

class ImageViewPagerAdapter(private val listImage: Array<String>)
    : RecyclerView.Adapter<ImageViewPagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(private val binding: ItemImageLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(image: String) {
            binding.image.setImageURI(Uri.parse(image))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.onBind(listImage[position])
    }

    override fun getItemCount(): Int {
        return listImage.size
    }
}