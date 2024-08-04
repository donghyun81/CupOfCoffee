package com.cupofcoffee0801.ui.user.usermettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.handle
import com.cupofcoffee0801.databinding.FragmentUserMeetingsBinding
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.MeetingsCategory
import com.cupofcoffee0801.ui.showLoading
import com.cupofcoffee0801.ui.showSnackBar
import com.cupofcoffee0801.ui.user.UserFragmentDirections

class UserMeetingsFragment : Fragment() {

    private var _binding: FragmentUserMeetingsBinding? = null
    private val binding get() = _binding!!

    private val adapter = UserMeetingsAdapter(userMeetingDeleteClick())
    private val viewModel: UserMeetingsViewModel by viewModels { UserMeetingsViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserMeetingsBinding.inflate(inflater)
        viewModel.initUiState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserMeetingsAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.currentJob?.cancel()
        _binding = null
    }

    private fun setUserMeetingsAdapter() {
        binding.rvAttendedMeetings.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = {
                    binding.cpiLoading.showLoading(result)
                },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    adapter.submitList(uiState.meetings)

                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }

    }

    private fun userMeetingDeleteClick() = object : UserMeetingClickListener {
        override fun onDeleteClick(meetingEntry: MeetingEntry) {
            if (viewModel.isNetworkConnected()) {
                val deleteWorker = viewModel.getDeleteMeetingWorker(meetingEntry)
                WorkManager.getInstance(requireContext()).enqueue(deleteWorker)
            } else view?.showSnackBar(R.string.delete_meeting_network_message)
        }

        override fun onDetailClick(meetingId: String) {
            val action =
                UserFragmentDirections.actionUserFragmentToMeetingDetailFragment(
                    meetingId
                )
            findNavController().navigate(action)
        }

        override fun onUpdateClick(meetingId: String) {
            val action =
                UserFragmentDirections.actionUserFragmentToMakeMeetingFragment(
                    null,
                    null,
                    meetingId
                )
            findNavController().navigate(action)
        }
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: MeetingsCategory): UserMeetingsFragment {
            return UserMeetingsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CATEGORY, category)
                }
            }
        }
    }
}