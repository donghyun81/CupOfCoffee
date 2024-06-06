package com.cupofcoffee.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee.databinding.UserMeetingsItemBinding
import com.cupofcoffee.ui.model.MeetingEntry

class UserMeetingsAdapter : ListAdapter<MeetingEntry, UserMeetingsAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class ViewHolder(private val binding: UserMeetingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meetingEntry: MeetingEntry) {
            val meetingModel = meetingEntry.meetingModel
            with(binding) {
                tvPlace.text = meetingModel.caption
                tvDate.text = meetingModel.date
                tvTime.text = meetingModel.time
                tvContent.text = meetingModel.content
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    UserMeetingsItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MeetingEntry>() {
            override fun areItemsTheSame(
                oldItem: MeetingEntry,
                newItem: MeetingEntry
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MeetingEntry,
                newItem: MeetingEntry
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}