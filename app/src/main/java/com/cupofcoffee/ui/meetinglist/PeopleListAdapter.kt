package com.cupofcoffee.ui.meetinglist

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cupofcoffee.databinding.MeetingPeopleItemBinding
import com.cupofcoffee.ui.model.UserEntry

class PeopleListAdapter : ListAdapter<UserEntry, PeopleListAdapter.ViewHolder>(PeopleListAdapter.diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class ViewHolder(private val binding: MeetingPeopleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userEntry: UserEntry) {
            Log.d("12345","이게 된다고?")
            val profileUrl = userEntry.userModel.profileImageWebUrl
            Glide.with(binding.root.context).load(profileUrl).centerCrop()
                .into(binding.personProfile)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    MeetingPeopleItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<UserEntry>() {
            override fun areItemsTheSame(oldItem: UserEntry, newItem: UserEntry): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UserEntry, newItem: UserEntry): Boolean {
                return oldItem == newItem
            }
        }
    }
}