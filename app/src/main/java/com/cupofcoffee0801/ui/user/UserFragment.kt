package com.cupofcoffee0801.ui.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.handle
import com.cupofcoffee0801.databinding.FragmentUserBinding
import com.cupofcoffee0801.ui.model.MeetingsCategory
import com.cupofcoffee0801.ui.showLoading
import com.cupofcoffee0801.ui.showSnackBar
import com.cupofcoffee0801.ui.user.usermettings.UserMeetingsPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()

    private lateinit var userMeetingsPagerAdapter: UserMeetingsPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater)
        viewModel.initUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonEnable()
        setUserUi()
        setMeetingsAdapter()
        setSettings()
        setUserEditOnClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.currentJob?.cancel()
        _binding = null
    }

    private fun setButtonEnable() {
        viewModel.isButtonClicked.observe(viewLifecycleOwner) { isButtonClicked ->
            binding.ivSettings.isEnabled = !isButtonClicked
        }
    }

    private fun setSettings() {
        binding.ivSettings.setOnClickListener {
            viewModel.onButtonClicked()
            val action = UserFragmentDirections.actionUserFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun setUserUi() {
        viewModel.dataResult.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    val userModel = uiState.user?.userModel ?: return@observe
                    setUserProfile(userModel.profileImageWebUrl)
                    with(binding) {
                        tvNickName.text = userModel.nickname
                        tvAttendedMeetingCount.text = uiState.attendedMeetingsCount.toString()
                        binding.tvMadeMeetingCount.text = uiState.madeMeetingsCount.toString()
                    }
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setUserProfile(profileUri: String?) {
        val uri = Uri.parse(profileUri)
        Glide.with(binding.root.context)
            .load(uri)
            .centerCrop()
            .into(binding.ivProfileImage)
    }

    private fun setUserEditOnClick() {
        binding.ivUserEdit.setOnClickListener {
            val action = UserFragmentDirections.actionUserFragmentToUserEditFragment()
            findNavController().navigate(action)
        }
    }

    private fun setMeetingsAdapter() {
        with(binding) {
            val tabCategories = MeetingsCategory.tabCategories
            userMeetingsPagerAdapter = UserMeetingsPagerAdapter(this@UserFragment)
            vpMeetings.adapter = userMeetingsPagerAdapter
            TabLayoutMediator(tlCategory, vpMeetings) { tab, position ->
                tab.text = getString(tabCategories[position].stringResourceId)
            }.attach()
        }
    }
}