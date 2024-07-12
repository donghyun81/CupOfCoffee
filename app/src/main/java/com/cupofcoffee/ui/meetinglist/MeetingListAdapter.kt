package com.cupofcoffee.ui.meetinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee.R
import com.cupofcoffee.databinding.MeetingListItemBinding
import com.cupofcoffee.ui.showSnackBar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MeetingListAdapter(
    private val meetingClickListener: MeetingClickListener,
) : ListAdapter<MeetingEntryWithPeople, MeetingListAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], meetingClickListener)
    }

    class ViewHolder(
        private val binding: MeetingListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            meetingEntryWithPeople: MeetingEntryWithPeople,
            meetingClickListener: MeetingClickListener,
        ) {
            val uid = Firebase.auth.uid
            val adapter = PeopleListAdapter()
            val meetingModel = meetingEntryWithPeople.meetingListModel
            with(binding) {
                tvContent.text = meetingModel.content
                tvDate.text = meetingModel.date
                tvTime.text = meetingModel.time
                rvPeople.adapter = adapter
                val hasUserId = meetingModel.people.none { it.id == uid }
                btnApply.isEnabled = hasUserId
                if (hasUserId) {
                    btnApply.setOnClickListener {
                        meetingClickListener.onClick(meetingEntryWithPeople)
                        btnApply.isEnabled = false
                    }
                }
            }
            adapter.submitList(meetingModel.people)
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
        val diffUtil = object : DiffUtil.ItemCallback<MeetingEntryWithPeople>() {
            override fun areItemsTheSame(
                oldItem: MeetingEntryWithPeople,
                newItem: MeetingEntryWithPeople
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MeetingEntryWithPeople,
                newItem: MeetingEntryWithPeople
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}