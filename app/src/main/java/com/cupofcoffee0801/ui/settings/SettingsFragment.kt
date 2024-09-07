package com.cupofcoffee0801.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.handle
import com.cupofcoffee0801.databinding.FragmentSettingsBinding
import com.cupofcoffee0801.ui.graphics.AppTheme
import com.cupofcoffee0801.ui.showLoading
import com.cupofcoffee0801.ui.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    SettingsScreen(
                        viewModel = viewModel,
                        onLogoutClick = ::logout,
                        onCancelMembershipClick = ::cancelMembership
                    )
                }
            }
        }
    }

    private fun logout() {
        NaverIdLoginSDK.logout()
        Firebase.auth.signOut()
        moveToLogin()
    }

    private fun cancelMembership() {
        val user = Firebase.auth.currentUser!!
        viewLifecycleOwner.lifecycleScope.launch {
            val deleteUserWorker = viewModel.getDeleteUserWorker()
            NaverIdLoginSDK.logout()
            WorkManager.getInstance(requireContext()).enqueue(deleteUserWorker)
            delay(2000)
            user.delete().addOnCompleteListener {
                moveToLogin()
            }
        }
    }

    private fun moveToLogin() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        findNavController().navigate(action)
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogoutClick: () -> Unit,
    onCancelMembershipClick: () -> Unit,
) {

    val uiState by viewModel.uiState.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        CancelMembershipDialog(
            onCancel = {
                showDialog = false
                onCancelMembershipClick()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState!!.isLoading)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "자동 로그인 설정")
                    Switch(
                        checked = uiState!!.isAutoLogin,
                        onCheckedChange = { viewModel.convertIsAutoLogin() }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onLogoutClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "로그아웃",
                        textAlign = TextAlign.Center
                    )
                }

                // 회원탈퇴 버튼
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(
                        text = "회원탈퇴",
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CancelMembershipDialog(
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel_membership))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.cancel_membership))
        },
        text = {
            Text(text = stringResource(id = R.string.cancel_membership_message))
        }
    )
}
