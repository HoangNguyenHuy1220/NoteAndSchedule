/*
package com.example.noteandschedule.data.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.noteandschedule.data.model.Item
import com.example.noteandschedule.data.entity.Note

data class NoteWithItemList (

    @Embedded val note: Note,
    @Relation(
        parentColumn = "note_id",
        entityColumn = "note"
    )
    val itemList: List<Item>

)
*/
