package com.cupofcoffee.ui.meetingdetail

import com.cupofcoffee.ui.model.CommentEntry

interface CommentClickListener {

    fun onUpdateClick(commentEntry: CommentEntry)

    fun onDetailClick(commentId: String)
}