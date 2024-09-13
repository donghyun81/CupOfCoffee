package com.cupofcoffee0801.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee0801.data.remote.model.MeetingDTO
import com.cupofcoffee0801.ui.model.Meeting

@Entity("meetings")
data class MeetingEntity(
    @PrimaryKey
    val id: String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val personIds: MutableMap<String, Boolean> = mutableMapOf(),
    val placeId: String,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String,
    val commentIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun MeetingEntity.asMeetingEntry() = Meeting(
    id,
    caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content
)

fun MeetingEntity.asMeetingDTO() =
    MeetingDTO(caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content)