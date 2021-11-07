package com.example.noteandschedule.adapter.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.note.ItemAdapter
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.databinding.NoteListItemBinding
import java.text.SimpleDateFormat

class CalendarListNoteAdapter(
    private var noteList: List<Note>,
    private val onClicked: (Note) -> Unit)
    : RecyclerView.Adapter<CalendarListNoteAdapter.NoteViewHolder>(){

    class NoteViewHolder(private val binding: NoteListItemBinding): RecyclerView.ViewHolder(binding.root) {
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
        val currentNote = noteList[position]
        holder.onBind(currentNote)
        holder.itemView.setOnClickListener {
            onClicked(currentNote)
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    fun setNoteList(notes: List<Note>) {
        noteList = notes
    }
}