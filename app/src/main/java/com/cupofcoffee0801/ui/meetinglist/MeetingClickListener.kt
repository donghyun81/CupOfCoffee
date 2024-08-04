package com.cupofcoffee0801.ui.meetinglist

interface MeetingClickListener {

    fun onApplyClick(meetingEntryWithPeople: MeetingEntryWithPeople)

    fun onCancelClick(meetingEntryWithPeople: MeetingEntryWithPeople)

    fun onDetailClick(meetingId: String)
}