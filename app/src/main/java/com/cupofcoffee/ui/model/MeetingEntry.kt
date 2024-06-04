package com.cupofcoffee.ui.model

import com.cupofcoffee.ui.meetinglist.MeetingListEntry

data class MeetingEntry(
    val id: String,
    val meetingModel: MeetingModel
)

fun MeetingEntry.toMeetingListEntry(peopleList: List<UserEntry>) =
    MeetingListEntry(
        id, meetingModel.toMeetingListModel(peopleList)
    )
