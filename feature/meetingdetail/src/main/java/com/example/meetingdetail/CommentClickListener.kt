package com.example.meetingdetail

interface CommentClickListener {

    fun onUpdateClick(commentId: String,meetingId:String)

    fun onDeleteClick(commentId: String)
}