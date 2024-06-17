package com.cupofcoffee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee.ui.model.MeetingEntry

@Entity("meetings")
data class MeetingEntity(
    @PrimaryKey
    val id:String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val peopleId: MutableList<String> = mutableListOf(),
    val placeId: String,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String
)