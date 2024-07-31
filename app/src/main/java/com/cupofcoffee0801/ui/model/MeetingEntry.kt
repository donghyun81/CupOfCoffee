package com.cupofcoffee0801.ui.model

import com.cupofcoffee0801.ui.meetinglist.MeetingEntryWithPeople
import kotlinx.serialization.Serializable

@Serializable
data class MeetingEntry(
    val id: String,
    val meetingModel: MeetingModel
)

fun MeetingEntry.asMeetingListEntry(peopleList: List<UserEntry>) =
    MeetingEntryWithPeople(
        id, meetingModel.asMeetingListModel(peopleList)
    )

fun MeetingEntry.asMeetingEntity() = meetingModel.asMeetingEntity(id)

fun MeetingEntry.asMeetingDTO() = meetingModel.asMeetingDTO()


