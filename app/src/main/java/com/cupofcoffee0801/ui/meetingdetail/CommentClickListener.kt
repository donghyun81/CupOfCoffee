package com.cupofcoffee0801.ui.meetingdetail

import com.cupofcoffee0801.ui.model.CommentEntry

interface CommentClickListener {

    fun onUpdateClick(commentEntry: CommentEntry)

    fun onDeleteClick(commentId: String)
}