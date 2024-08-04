package com.cupofcoffee0801.ui.meetingdetail

import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cupofcoffee0801.R
import com.cupofcoffee0801.databinding.MeetingDetailItemBinding
import com.cupofcoffee0801.ui.model.CommentEntry
import com.cupofcoffee0801.ui.toDateFormat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
                val isIvMoreMenuVisible = Firebase.auth.uid == commentEntry.commentModel.userId
                ivMoreMenu.isVisible = isIvMoreMenuVisible
                ivMoreMenu.setOnClickListener {
                    showPopupMenu(commentEntry, commentClickListener)
                }
                val uri = Uri.parse(commentEntry.commentModel.profileImageWebUrl)
                Glide.with(binding.root.context)
                    .load(uri)
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