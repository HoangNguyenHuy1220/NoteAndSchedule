package com.example.noteandschedule.data

import android.content.Context
import androidx.room.*
import com.example.noteandschedule.data.entity.*
import com.example.noteandschedule.data.model.Converters

@Database(entities = [
    Category::class,
    Note::class,
    Task::class
], version = 1, exportSchema = true)
@TypeConverters( Converters::class )
abstract class TodoAppDatabase: RoomDatabase() {
    abstract val todoDao: TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoAppDatabase? = null

        fun getInstance(context: Context): TodoAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoAppDatabase::class.java,
                    "Todo_database"
                )
                    .createFromAsset("database/Todo.db")
                    //.fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}