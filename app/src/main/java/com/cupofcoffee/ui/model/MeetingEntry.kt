package com.cupofcoffee.ui.model

import com.cupofcoffee.ui.meetinglist.MeetingEntryWithPeople

data class MeetingEntry(
    val id: String,
    val meetingModel: MeetingModel
)

fun MeetingEntry.toMeetingListEntry(peopleList: List<UserEntry>) =
    MeetingEntryWithPeople(
        id, meetingModel.asMeetingListModel(peopleList)
    )

fun MeetingEntry.asMeetingEntity() = meetingModel.asMeetingEntity(id)

fun MeetingEntry.asMeetingDTO() = meetingModel.asMeetingDTO()


