package com.cupofcoffee.ui.meetingdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee.databinding.MeetingDetailItemBinding
import com.cupofcoffee.ui.model.CommentEntry

class MeetingDetailAdapter(
    private val commentClickListener: CommentClickListener
) : ListAdapter<CommentEntry, MeetingDetailAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], commentClickListener)
    }

    class ViewHolder(private val binding: MeetingDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            commentEntry: CommentEntry, commentClickListener: CommentClickListener
        ) {
            with(binding) {
                tvUserNickname.text = commentEntry.commentModel.nickname
                tvContent.text = commentEntry.commentModel.content
                ivUpdate.setOnClickListener {
                    commentClickListener.onUpdateClick(commentEntry)
                }
                ivDelete.setOnClickListener {
                    commentClickListener.onDetailClick(commentEntry.id)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    MeetingDetailItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CommentEntry>() {
            override fun areItemsTheSame(
                oldItem: CommentEntry,
                newItem: CommentEntry
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CommentEntry,
                newItem: CommentEntry
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}