package com.cupofcoffee0801.ui.user.usermettings

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cupofcoffee0801.R
import com.cupofcoffee0801.databinding.UserMeetingsItemBinding
import com.cupofcoffee0801.ui.model.Meeting
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserMeetingsAdapter(
    private val userMeetingClickListener: UserMeetingClickListener
) : ListAdapter<Meeting, UserMeetingsAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], userMeetingClickListener)
    }

    class ViewHolder(private val binding: UserMeetingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meeting: Meeting, userMeetingClickListener: UserMeetingClickListener) {
            val uid = Firebase.auth.uid!!
            with(binding) {
                tvPlace.text = meeting.caption
                tvDate.text = meeting.date
                tvTime.text = meeting.time
                tvContent.text = meeting.content
                if (meeting.managerId != uid) ivMoreMenu.visibility = View.GONE
                ivMoreMenu.setOnClickListener {
                    showPopupMenu(meeting, userMeetingClickListener)
                }
                root.setOnClickListener {
                    userMeetingClickListener.onDetailClick(meeting.id)
                }
            }
        }

        private fun showPopupMenu(
            meeting: Meeting,
            userMeetingClickListener: UserMeetingClickListener
        ) {
            val popupMenu = PopupMenu(binding.root.context, binding.ivMoreMenu)
            popupMenu.menuInflater.inflate(R.menu.edit_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        userMeetingClickListener.onUpdateClick(meeting.id)
                        true
                    }

                    R.id.delete -> {
                        userMeetingClickListener.onDeleteClick(meeting)
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
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
        val diffUtil = object : DiffUtil.ItemCallback<Meeting>() {
            override fun areItemsTheSame(
                oldItem: Meeting,
                newItem: Meeting
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Meeting,
                newItem: Meeting
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}