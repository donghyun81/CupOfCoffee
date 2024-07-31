package com.cupofcoffee.ui.meetingdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentMeetingDetailBinding
import com.cupofcoffee.ui.model.CommentEntry
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar

class MeetingDetailFragment : Fragment() {

    private var _binding: FragmentMeetingDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MeetingDetailViewModel by viewModels { MeetingDetailViewModel.Factory }

    private val adapter: MeetingDetailAdapter = MeetingDetailAdapter(getCommentClickListener())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        setCommentClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.currentJob?.cancel()
        _binding = null
    }

    private fun initUi() {
        viewModel.meetingDetailUiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    setEdit(uiState.isMyMeeting, uiState.meeting)
                    setMeeting(uiState.meeting)
                    setCommentAdapter(uiState.comments)
                    setUserProfile(uiState.userEntry.userModel.profileImageWebUrl)
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setMeeting(meetingEntry: MeetingEntry) {
        with(binding) {
            tvContent.text = meetingEntry.meetingModel.content
            tvDate.text = meetingEntry.meetingModel.date
            tvPlace.text = meetingEntry.meetingModel.caption
            tvTime.text = meetingEntry.meetingModel.time
            tvAddComment.visibility
        }
    }

    private fun setEdit(isMyMeeting: Boolean, meetingEntry: MeetingEntry) {
        binding.ivMoreMenu.isVisible = isMyMeeting
        binding.ivMoreMenu.setOnClickListener {
            showPopupMenu(meetingEntry)
        }
    }

    private fun showPopupMenu(meetingEntry: MeetingEntry) {
        val popupMenu = PopupMenu(requireContext(), binding.ivMoreMenu)
        popupMenu.menuInflater.inflate(R.menu.edit_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    val action =
                        MeetingDetailFragmentDirections.actionMeetingDetailFragmentToMakeMeetingFragment(
                            null,
                            null,
                            meetingEntry.id
                        )
                    findNavController().navigate(action)
                    true
                }

                R.id.delete -> {
                    val deleteMeetingWorker = viewModel.getDeleteMeetingWorker(meetingEntry)
                    WorkManager.getInstance(requireContext()).enqueue(deleteMeetingWorker)
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun setCommentClick() {
        binding.tvAddComment.setOnClickListener {
            val action =
                MeetingDetailFragmentDirections.actionMeetingDetailFragmentToCommentEditFragment(
                    null,
                    viewModel.meetingId
                )
            findNavController().navigate(action)
        }
    }

    private fun setCommentAdapter(comments: List<CommentEntry>) {
        binding.rvComments.adapter = adapter
        adapter.submitList(comments)
    }

    private fun setUserProfile(profileImageWebUrl: String?) {
        Glide.with(binding.root.context)
            .load(profileImageWebUrl)
            .centerCrop()
            .into(binding.ivUserProfile)
    }

    private fun getCommentClickListener() = object : CommentClickListener {

        override fun onUpdateClick(commentEntry: CommentEntry) {
            val action =
                MeetingDetailFragmentDirections.actionMeetingDetailFragmentToCommentEditFragment(
                    commentEntry.id,
                    viewModel.meetingId
                )
            findNavController().navigate(action)
        }

        override fun onDeleteClick(commentId: String) {
            viewModel.deleteComment(commentId)
        }

    }
}