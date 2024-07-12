package com.cupofcoffee.ui.meetinglist

import com.cupofcoffee.ui.model.MeetingEntry

data class MeetingEntryWithPeople(
    val id: String,
    val meetingListModel: MeetingModelWithPeople
)

fun MeetingEntryWithPeople.asMeetingEntry() = MeetingEntry(id, meetingListModel.asMeetingModel())