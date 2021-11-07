package com.example.noteandschedule.adapter.note

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.R
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.databinding.NoteListItemBinding
import com.example.noteandschedule.ui.note.NoteFragment
import java.text.SimpleDateFormat

class NoteAdapter(private var fragment: NoteFragment ,
                  private val onItemClicked: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.noteId == newItem.noteId
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }

        }
    }

    class NoteViewHolder(val binding: NoteListItemBinding): RecyclerView.ViewHolder(binding.root) {
        var isSelected = false
        @SuppressLint("SimpleDateFormat")
        fun onBind(currentNote: Note) {
            binding.apply {
                /*if (currentNote.noteImage.isNotEmpty())
                    imageNote.setImageURI(Uri.parse(currentNote.noteImage[0]))*/
                parentLayout.setBackgroundResource(R.drawable.normal_note_shape)

                imagePinNote.visibility =
                    if (currentNote.isPinned) View.VISIBLE
                    else View.GONE
                textViewTitle.text = currentNote.noteTitle
                textViewDescription.text = currentNote.noteDescription
                if(currentNote.noteReminder != null) {
                    imageReminder.visibility = View.VISIBLE
                }
                textViewDescription.text = currentNote.noteDescription
                // set up list item
                val adapter = ItemAdapter()
                listItem.adapter = adapter
                listItem.layoutManager = LinearLayoutManager(itemView.context)
                adapter.submitList(currentNote.itemList)
                textViewDate.text = SimpleDateFormat("MMM dd, yyyy").format(currentNote.dueDate)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = getItem(position)
        holder.onBind(currentNote)
        holder.itemView.setOnClickListener {
            onItemClicked(currentNote)
            setBackgroundItem(holder)
        }
    }

    private fun setBackgroundItem(currentHolder: NoteViewHolder) {
        if (fragment.isMultiSelectMode) {
            currentHolder.binding.parentLayout.setBackgroundResource(
                if (!currentHolder.isSelected) R.drawable.multi_select_background
                else R.drawable.normal_note_shape
            )
            currentHolder.isSelected = !currentHolder.isSelected
        }
    }
}