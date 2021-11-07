package com.example.noteandschedule.data.entity

import androidx.room.*
import com.example.noteandschedule.data.model.Item
import java.util.*

@Entity
data class Note (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id") val noteId: Int = 0,
    @ColumnInfo(name = "note_title") var noteTitle: String = "",
    @ColumnInfo(name = "note_description") var noteDescription: String = "",
    @ColumnInfo(name = "category") var categoryId: Int = 2,
    @ColumnInfo(name = "due_date") var dueDate: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "note_reminder") var noteReminder: Date? = null,
    @ColumnInfo(name = "repeat_reminder") var notifyRepeat: String = "No repeat",
    @ColumnInfo(name = "note_pinned") var isPinned: Boolean = false,
    @ColumnInfo(name = "item_list") var itemList: MutableList<Item> = mutableListOf(),
    @ColumnInfo(name = "note_image") var noteImage: MutableList<String> = mutableListOf()

)



