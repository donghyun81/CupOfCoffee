package com.cupofcoffee0801.ui.meetingdetail

interface CommentClickListener {

    fun onUpdateClick(commentId: String,meetingId:String)

    fun onDeleteClick(commentId: String)
}