package com.example.noteandschedule.data

import androidx.room.*
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.data.entity.Task
import com.example.noteandschedule.data.relationship.CategoryWithNoteList
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface TodoDao {

    // Category
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM Category")
    fun getAllCategory(): Flow<List<Category>>

    // get category name
    @Query("SELECT category_name FROM Category WHERE category_id = :id")
    suspend fun getCategoryName(id: Int): String?

    // get category id by name
    @Query("SELECT category_id from Category WHERE category_name = :name")
    suspend fun getCategoryId(name: String): Int


    // NOTE QUERY
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // get all note
    @Query("SELECT * FROM Note")
    fun getAllNotes(): Flow<List<Note>>

    // get note by category
    @Query("SELECT * FROM Note WHERE Note.category = :id")
    fun getNoteByCategory(id: Int): Flow<List<Note>>

    // get a note detail
    @Query("SELECT * FROM Note WHERE note_id = :id")
    fun getNoteInfo(id: Int): Flow<Note>

    // get newest note
    @Query("SELECT * FROM Note ORDER BY note_id DESC LIMIT 1")
    fun getNewestNote(): Flow<Note>

    // Query on search view
    @Query("SELECT * FROM Note WHERE note_title LIKE :titleQuery " +
            "ORDER BY due_date DESC")
    fun searchDatabase(titleQuery: String): Flow<List<Note>>

    // get note by date
    @Query("SELECT * FROM Note " +
            "WHERE due_date BETWEEN :startTime AND :endTime " +
            "ORDER BY due_date DESC")
    fun getNotesByDate(startTime: Date, endTime: Date): Flow<List<Note>>

    // TASK QUERY
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM Task" +
            " WHERE start_time BETWEEN :startTime AND :endTime " +
            "ORDER BY start_time ASC")
    fun getAllTasks(startTime: Date, endTime: Date): Flow<List<Task>>

    // filter
    @Query("SELECT * FROM Task WHERE start_time BETWEEN :startTime AND :endTime " +
            "AND task_progress = :progress")
    fun getTasksByProgress(progress: String, startTime: Date, endTime: Date): Flow<List<Task>>

    // get new task
    @Query("SELECT * FROM Task ORDER BY task_id DESC LIMIT 1")
    fun getNewestTask(): Flow<Task>

    // get task by id
    @Query("SELECT * FROM Task WHERE task_id = :id")
    fun getTaskInfo(id: Int): Flow<Task>
}