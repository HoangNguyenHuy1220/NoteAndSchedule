package com.example.noteandschedule.repository

import android.app.Application
import com.example.noteandschedule.data.TodoAppDatabase
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.data.entity.Task
import kotlinx.coroutines.flow.Flow
import java.util.*

class TodoAppRepository(private val database: TodoAppDatabase) {

    companion object {
        @Volatile
        private var INSTANCE: TodoAppRepository? = null

        fun getInstance(application: Application): TodoAppRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TodoAppRepository(TodoAppDatabase.getInstance(application))
                INSTANCE = instance
                instance
            }
        }
    }

    // Category
    suspend fun insertCategory(category: Category) = database.todoDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = database.todoDao.deleteCategory(category)
    suspend fun updateCategory(category: Category) = database.todoDao.updateCategory(category)
    fun getAllCategory() = database.todoDao.getAllCategory()

    // Note
    suspend fun insertNote(note: Note) = database.todoDao.insertNote(note)
    suspend fun updateNote(note: Note) = database.todoDao.updateNote(note)
    suspend fun deleteNote(note: Note) = database.todoDao.deleteNote(note)
    fun getAllNotes() = database.todoDao.getAllNotes()
    fun getNoteByCategory(category_id: Int) = database.todoDao.getNoteByCategory(category_id)
    fun getNoteInfo(note_id: Int) = database.todoDao.getNoteInfo(note_id)
    fun getNewestNote() = database.todoDao.getNewestNote()
    fun searchDatabase(nameQuery: String) = database.todoDao.searchDatabase(nameQuery)
    fun getNotesByDate(startTime: Date, endTime: Date) = database.todoDao.getNotesByDate(startTime, endTime)

    // Task
    suspend fun insertTask(task: Task) = database.todoDao.insertTask(task)
    suspend fun updateTask(task: Task) = database.todoDao.updateTask(task)
    suspend fun deleteTask(task: Task) = database.todoDao.deleteTask(task)
    fun getAllTask(startTime: Date, endTime: Date) = database.todoDao.getAllTasks(startTime, endTime)
    fun getNewestTask() = database.todoDao.getNewestTask()
    fun getTaskInfo(task_id: Int) = database.todoDao.getTaskInfo(task_id)
    fun getTasksByProgress(progress: String, startTime: Date, endTime: Date) =
        database.todoDao.getTasksByProgress(progress, startTime, endTime)
}