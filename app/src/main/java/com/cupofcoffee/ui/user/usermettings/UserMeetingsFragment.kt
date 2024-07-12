package com.cupofcoffee.ui.user.usermettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentUserMeetingsBinding
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserMeetingsAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
            if (viewModel.isNetworkConnected()) viewModel.deleteMeeting(meetingEntry)
            else view?.showSnackBar(R.string.delete_meeting_network_message)
        }

        override fun onDetailClick(meetingId: String) {
            val action =
                UserMeetingsFragmentDirections.actionUserMeetingsFragmentToMeetingDetailFragment(
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