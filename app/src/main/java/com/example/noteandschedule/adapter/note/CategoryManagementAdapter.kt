package com.example.noteandschedule.adapter.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.databinding.AddCategoryItemLayoutBinding

class CategoryManagementAdapter(
    private val showPopupMenu: (View, Category) -> Unit, )
    : ListAdapter<Category, CategoryManagementAdapter.CategoryViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.categoryId == newItem.categoryId
            }

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.categoryName == newItem.categoryName
            }
        }
    }

    class CategoryViewHolder(val binding: AddCategoryItemLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(category: Category) {
            if (category.categoryId == 1 || category.categoryId == 2) {
                binding.iconCircle.visibility = View.GONE
                binding.textViewItem.visibility = View.GONE
                binding.imagePopupMenu.visibility = View.GONE
            }
            else
                binding.textViewItem.text = category.categoryName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = AddCategoryItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.onBind(currentItem)
        holder.binding.imagePopupMenu.setOnClickListener {
            showPopupMenu(holder.binding.imagePopupMenu, currentItem)
        }
    }
}