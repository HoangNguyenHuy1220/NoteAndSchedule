package com.example.noteandschedule.adapter.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.R
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.databinding.NoteCategoryItemBinding

class NoteCategoryAdapter(private val selectedCategory: (Int) -> Unit)
    : ListAdapter<Category, NoteCategoryAdapter.CategoryViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.categoryId == newItem.categoryId
            }

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem == newItem
            }

        }
    }

    class CategoryViewHolder(val binding: NoteCategoryItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(category: Category) {
            binding.apply {
                if (adapterPosition == 0) {
                    imageAddCategory.visibility = View.VISIBLE
                    textViewNoteCategory.visibility = View.GONE
                }
                textViewNoteCategory.text = category.categoryName
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = NoteCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.onBind(currentItem)
        holder.itemView.setOnClickListener {
            selectedCategory(currentItem.categoryId)
            /*holder.itemView.background =
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.selected_category_selector)*/
        }
    }
}