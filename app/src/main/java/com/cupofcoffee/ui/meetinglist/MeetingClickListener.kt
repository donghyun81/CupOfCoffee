package com.cupofcoffee.ui.meetinglist

interface MeetingClickListener {

    fun onApplyClick(meetingEntryWithPeople: MeetingEntryWithPeople)

    fun onDetailClick(meetingId: String)
}