package com.cupofcoffee0801.ui.meetingdetail

import com.cupofcoffee0801.ui.model.Comment

interface CommentClickListener {

    fun onUpdateClick(commentId: String,meetingId:String)

    fun onDeleteClick(commentId: String)
}