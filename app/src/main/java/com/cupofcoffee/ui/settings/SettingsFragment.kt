package com.cupofcoffee.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.R
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
                .setTitle(getString(R.string.cancel_membership))
                .setMessage(getString(R.string.cancel_membership_message))
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
                .setPositiveButton(getString(R.string.cancel_membership)) { _, _ ->
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