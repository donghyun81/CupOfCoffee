package com.example.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("meetings")
data class MeetingEntity(
    @PrimaryKey
    val id: String,
    val caption: String,
    val managerId: String,
    val personIds: MutableMap<String, Boolean> = mutableMapOf(),
    val placeId: String,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String,
    val commentIds: MutableMap<String, Boolean> = mutableMapOf()
)