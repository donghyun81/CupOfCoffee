package com.cupofcoffee.ui.meetinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee.databinding.MeetingListItemBinding
import com.cupofcoffee.ui.model.MeetingEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MeetingListAdapter(
    private val meetingClickListener: MeetingClickListener
) : ListAdapter<MeetingEntry, MeetingListAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], meetingClickListener)
    }

    class ViewHolder(private val binding: MeetingListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meetingEntry: MeetingEntry, meetingClickListener: MeetingClickListener) {
            val uid = Firebase.auth.uid
            with(binding) {
                tvContent.text = meetingEntry.meetingModel.content
                tvDate.text = meetingEntry.meetingModel.date
                tvTime.text = meetingEntry.meetingModel.time
                val hasUserId = meetingEntry.meetingModel.peopleId.contains(uid).not()
                btnApply.isEnabled = hasUserId
                if (hasUserId) {
                    btnApply.setOnClickListener {
                        meetingClickListener.onClick(meetingEntry)
                    }
                    btnApply.isEnabled = false
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    MeetingListItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MeetingEntry>() {
            override fun areItemsTheSame(oldItem: MeetingEntry, newItem: MeetingEntry): Boolean {
                return oldItem.meetingModel.createDate == newItem.meetingModel.createDate
            }

            override fun areContentsTheSame(oldItem: MeetingEntry, newItem: MeetingEntry): Boolean {
                return oldItem == newItem
            }
        }
    }
}