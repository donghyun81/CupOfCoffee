package com.cupofcoffee.data.remote

import com.cupofcoffee.ui.meetinglist.MeetingListModel
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class MeetingDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val peopleId: MutableList<String> = mutableListOf(),
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String
)

fun MeetingDTO.toMeetingEntry(id: String) =
    MeetingEntry(
        id,
        MeetingModel(caption, lat, lng, managerId, peopleId, date, time, createDate, content)
    )
