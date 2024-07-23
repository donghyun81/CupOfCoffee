package com.cupofcoffee.ui.meetingdetail

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cupofcoffee.R
import com.cupofcoffee.databinding.MeetingDetailItemBinding
import com.cupofcoffee.ui.model.CommentEntry
import com.cupofcoffee.ui.toDateFormat

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
                tvCreatedDate.text = commentEntry.commentModel.createdDate.toDateFormat()
                tvContent.text = commentEntry.commentModel.content
                ivMoreMenu.setOnClickListener {
                    showPopupMenu(commentEntry, commentClickListener)
                }
                Glide.with(binding.root.context)
                    .load(commentEntry.commentModel.profileImageWebUrl)
                    .centerCrop()
                    .into(ivUserProfile)
            }
        }

        private fun showPopupMenu(
            commentEntry: CommentEntry,
            commentClickListener: CommentClickListener
        ) {
            val popupMenu = PopupMenu(binding.root.context, binding.ivMoreMenu)
            popupMenu.menuInflater.inflate(R.menu.edit_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        commentClickListener.onUpdateClick(commentEntry)
                        true
                    }

                    R.id.delete -> {
                        commentClickListener.onDeleteClick(commentEntry.id)
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