package com.example.noteandschedule.adapter.note

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.data.model.Item
import com.example.noteandschedule.databinding.AddNoteItemBinding

class ItemAddNewAdapter(private val onRemoveItemClicked: OnRemoveItem)
    : ListAdapter<Item, ItemAddNewAdapter.ViewHolder>(DiffCallback) {

    interface OnRemoveItem {
        fun onDragAction(viewHolder: ViewHolder)
        fun onChecked(position: Int)
        fun onRemoveClicked(position: Int)
        //fun updateItemList()
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }

        }
    }

    class ViewHolder(val binding: AddNoteItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun onBind(item: Item, onRemoveItemClicked: OnRemoveItem) {
            binding.apply {
                checkboxItem.isChecked = item.isChecked
                textItem.paintFlags =
                    if (item.isChecked) Paint.STRIKE_THRU_TEXT_FLAG else Paint.ANTI_ALIAS_FLAG
                textItem.text = item.name
                imageHolder.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_DOWN) {
                        onRemoveItemClicked.onDragAction(this@ViewHolder)
                    }
                    return@setOnTouchListener true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AddNoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.onBind(currentItem, onRemoveItemClicked)
        holder.binding.imageRemoveItem.setOnClickListener {
            onRemoveItemClicked.onRemoveClicked(position)
        }
        holder.binding.checkboxItem.setOnClickListener {
            onRemoveItemClicked.onChecked(position)
        }

    }
}