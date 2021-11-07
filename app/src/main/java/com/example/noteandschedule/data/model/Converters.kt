package com.example.noteandschedule.data.model

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


class Converters {

    /*// Image
    @TypeConverter
    fun fromString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @TypeConverter
    fun toString(uri: Uri?): String {
        return uri.toString()
    }

    // List Uri
    @TypeConverter
    fun fromUriList(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toUriList(value: String): List<Uri> {
        val listType = object : TypeToken<List<Uri>>(){}.type
        return Gson().fromJson(value, listType)
    }*/

    // Date
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // List item
    @TypeConverter
    fun fromItemList(list: List<Item>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toItemList(value: String): List<Item> {
        val listType = object : TypeToken<List<Item>>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>(){}.type
        return Gson().fromJson(value, listType)
    }

}