package com.cupofcoffee0801.ui.meetinglist

import com.cupofcoffee0801.ui.model.MeetingEntry

data class MeetingEntryWithPeople(
    val id: String,
    val meetingListModel: MeetingModelWithPeople
)

fun MeetingEntryWithPeople.asMeetingEntry() = MeetingEntry(id, meetingListModel.asMeetingModel())