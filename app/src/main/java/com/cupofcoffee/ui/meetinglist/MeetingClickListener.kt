package com.cupofcoffee.ui.meetinglist

import com.cupofcoffee.ui.model.MeetingEntry

interface MeetingClickListener {

    fun onClick(meetingListEntry: MeetingListEntry)
}