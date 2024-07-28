package com.cupofcoffee.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentSettingsBinding
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonEnable()
        setLogoutButton()
        setCancelMembership()
        setAutoLogin()
    }

    private fun setAutoLogin() {
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    binding.sAutoLogin.isChecked = uiState.isAutoLogin
                    binding.sAutoLogin.setOnCheckedChangeListener { _, _ ->
                        viewModel.convertIsAutoLogin()
                    }
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setButtonEnable() {
        viewModel.isButtonClicked.observe(viewLifecycleOwner) { isButtonClicked ->
            binding.tvLogout.isEnabled = !isButtonClicked
            binding.cancelMembership.isEnabled = !isButtonClicked
        }
    }

    private fun setLogoutButton() {
        binding.tvLogout.setOnClickListener {
            viewModel.onButtonClicked()
            NaverIdLoginSDK.logout()
            Firebase.auth.signOut()
            moveToLogin()
        }
    }

    private fun setCancelMembership() {
        binding.cancelMembership.setOnClickListener {
            viewModel.onButtonClicked()
            if (viewModel.isConnected())
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.cancel_membership))
                    .setMessage(getString(R.string.cancel_membership_message))
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
                    .setPositiveButton(getString(R.string.cancel_membership)) { _, _ ->
                        cancelMembership()
                    }
                    .show()
            else requireView().showSnackBar(R.string.cancel_membership_internet)
        }
    }

    private fun cancelMembership() {
        val user = Firebase.auth.currentUser!!
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteUserData(user.uid)
            NaverIdLoginSDK.logout()
            withContext(Dispatchers.Main) {
                user.delete()
                    .addOnCompleteListener {
                        moveToLogin()
                    }
            }
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