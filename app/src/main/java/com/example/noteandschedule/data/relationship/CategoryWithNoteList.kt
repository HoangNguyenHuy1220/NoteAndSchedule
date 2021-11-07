package com.example.noteandschedule.data.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Note

data class CategoryWithNoteList (
    @Embedded val category: Category,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category"
    )
    val noteList: List<Note>
        )