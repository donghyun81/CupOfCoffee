package com.cupofcoffee.ui.meetinglist

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee.databinding.MeetingListItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MeetingListAdapter(
    private val meetingClickListener: MeetingClickListener
) : ListAdapter<MeetingListEntry, MeetingListAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], meetingClickListener)
    }

    class ViewHolder(private val binding: MeetingListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val peopleRecyclerView: RecyclerView = binding.rvPeople

        fun bind(
            meetingListEntry: MeetingListEntry,
            meetingClickListener: MeetingClickListener
        ) {
            val uid = Firebase.auth.uid
            val adapter = PeopleListAdapter()
            val meetingModel = meetingListEntry.meetingListModel
            with(binding) {
                tvContent.text = meetingModel.content
                tvDate.text = meetingModel.date
                tvTime.text = meetingModel.time
                rvPeople.adapter = adapter
                val hasUserId = meetingModel.people.filter { it.id == uid }.isEmpty()
                Log.d("12345",hasUserId.toString())
                btnApply.isEnabled = hasUserId
                if (hasUserId) {
                    btnApply.setOnClickListener {
                        meetingClickListener.onClick(meetingListEntry)
                        btnApply.isEnabled = false
                    }
                }
                adapter.submitList(meetingModel.people)
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
        val diffUtil = object : DiffUtil.ItemCallback<MeetingListEntry>() {
            override fun areItemsTheSame(
                oldItem: MeetingListEntry,
                newItem: MeetingListEntry
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MeetingListEntry,
                newItem: MeetingListEntry
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}