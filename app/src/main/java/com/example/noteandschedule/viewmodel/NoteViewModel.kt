package com.example.noteandschedule.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.data.entity.Task
import com.example.noteandschedule.repository.TodoAppRepository
import kotlinx.coroutines.*
import java.util.*

class NoteViewModel(private val repository: TodoAppRepository): ViewModel() {

    // Category
    val allCategory: LiveData<List<Category>> = repository.getAllCategory().asLiveData()
    fun insertCategory(category: Category) {
        viewModelScope.launch { repository.insertCategory(category) }
    }
    fun updateCategory(category: Category) {
        viewModelScope.launch { repository.updateCategory(category) }
    }
    fun deleteCategory(category: Category) {
        updateCategoryOfNotes(category.categoryId)
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    // Note
    val allNotes = repository.getAllNotes().asLiveData()
    fun getNoteByCategory(category_id: Int): LiveData<List<Note>> {
        return repository.getNoteByCategory(category_id).asLiveData()
    }
    fun getNoteInfo(note_id: Int): LiveData<Note> {
        return repository.getNoteInfo(note_id).asLiveData()
    }
    fun getNewestNote(): LiveData<Note> {
        return repository.getNewestNote().asLiveData()
    }

    private fun updateCategoryOfNotes(category_id: Int) {
        val listNote = getNoteByCategory(category_id).value
        listNote?.forEach { note ->
            note.categoryId = 2
            updateNote(note)
        }
    }

    fun getPositionCategory(categoryId: Int): Int {
        var position = 1
        val categories = allCategory.value
        for (i in 0 until categories!!.size.minus(1)) {
            if (categories[i].categoryId == categoryId) {
                position = i
            }
        }
        return position
    }

    fun insertNote(note: Note) {
        viewModelScope.launch { repository.insertNote(note) }
    }

    fun updateNote(note: Note)  {
        viewModelScope.launch { repository.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun searchDatabase(nameQuery: String) = repository.searchDatabase(nameQuery).asLiveData()

    fun getNotesByDate(startTime: Date, endTime: Date): LiveData<List<Note>> {
        return repository.getNotesByDate(startTime, endTime).asLiveData()
    }

    // Task
    fun getAllTask(startTime: Date, endTime: Date): LiveData<List<Task>> {
        return repository.getAllTask(startTime, endTime).asLiveData()
    }
    fun getNewestTask() : LiveData<Task> = repository.getNewestTask().asLiveData()
    fun getTaskInfo(task_id: Int) : LiveData<Task> {
        return repository.getTaskInfo(task_id).asLiveData()
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun getTasksByProgress(progress: String, startTime: Date, endTime: Date): LiveData<List<Task>> {
        return repository.getTasksByProgress(progress, startTime, endTime).asLiveData()
    }

}

class NoteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(TodoAppRepository.getInstance(application)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}