package com.example.noteandschedule.data.model

import java.util.*

data class Item (

    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var isChecked: Boolean = false

)