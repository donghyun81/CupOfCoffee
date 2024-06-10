package com.cupofcoffee.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.R
import com.cupofcoffee.databinding.FragmentMeetingListBinding
import com.cupofcoffee.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.NaverIdLoginSDK

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLogoutButton()
        setCancelMembership()
    }

    private fun setLogoutButton() {
        binding.tvLogout.setOnClickListener {
            NaverIdLoginSDK.logout()
            Firebase.auth.signOut()
            moveToLogin()
        }
    }

    private fun setCancelMembership() {
        binding.cancelMembership.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("회원 탈퇴")
                .setMessage("기존에 사용한 기록이 전부 제거 됩니다. 그래도 탈퇴 하시겠습니까?")
                .setNegativeButton(getString(R.string.save_cancle)) { dialog, _ -> dialog.cancel() }
                .setPositiveButton(getString(R.string.save_create)) { _, _ ->
                    cancelMembership()
                }
                .show()
        }
    }

    private fun cancelMembership() {
        val user = Firebase.auth.currentUser!!
        viewModel.deleteUserData(user.uid)
        NaverIdLoginSDK.logout()
        user.delete()
            .addOnCompleteListener {
                moveToLogin()
            }
    }

    private fun moveToLogin() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}