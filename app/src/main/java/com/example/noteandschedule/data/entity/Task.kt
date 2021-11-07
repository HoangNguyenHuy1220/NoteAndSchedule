package com.example.noteandschedule.data.entity

import androidx.room.*
import com.example.noteandschedule.data.model.Item
import java.util.*

@Entity
data class Task (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id") val taskId: Int = 0,
    @ColumnInfo(name = "task_name") var taskName: String = "",
    @ColumnInfo(name = "task_description") var taskDescription: String = "",
    @ColumnInfo(name = "category") var categoryId: Int = 2,
    @ColumnInfo(name = "start_time") var startTime: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "end_time") var endTime: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "all_day") var allDay: Boolean = false,
    @ColumnInfo(name = "repeat_task") var taskRepeat: String = "Does not repeat",
    @ColumnInfo(name = "task_reminder") var taskReminder: Date? = null,
    @ColumnInfo(name = "item_list") var itemList: MutableList<Item> = mutableListOf(),
    @ColumnInfo(name = "task_progress") var taskProgress: String = "Pending"

)






