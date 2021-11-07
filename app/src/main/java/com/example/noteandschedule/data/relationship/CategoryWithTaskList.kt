package com.example.noteandschedule.data.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Task

data class CategoryWithTaskList (
    @Embedded val category: Category,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category"
    )
    val taskList: List<Task>
        )