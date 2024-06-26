package com.cupofcoffee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee.data.remote.MeetingDTO
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingModel

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
    val isSynced: Boolean = true,
)

fun MeetingEntity.asMeetingEntry() = MeetingEntry(
    id,
    MeetingModel(caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content)
)

fun MeetingEntity.asMeetingDTO() =
    MeetingDTO(caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content)