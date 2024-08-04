package com.cupofcoffee0801.ui.meetinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee0801.R
import com.cupofcoffee0801.databinding.MeetingListItemBinding
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
    ) : RecyclerView.ViewHolder(binding.root) {

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
                root.setOnClickListener {
                    meetingClickListener.onDetailClick(meetingEntryWithPeople.id)
                }
                val hasUserId = meetingModel.people.any { it.id == uid }
                val isMyMeeting = meetingModel.managerId == uid
                if (hasUserId && isMyMeeting.not()) {
                    btnApply.setText(R.string.cancel)
                    btnApply.setOnClickListener {
                        btnApply.setText(R.string.apply)
                        meetingClickListener.onCancelClick(meetingEntryWithPeople)
                    }
                } else {
                    btnApply.setText(R.string.apply)
                    btnApply.isEnabled = isMyMeeting.not()
                    btnApply.setOnClickListener {
                        btnApply.setText(R.string.cancel)
                        meetingClickListener.onApplyClick(meetingEntryWithPeople)
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