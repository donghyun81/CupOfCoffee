package com.cupofcoffee0801.ui.user.useredit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.component.StateContent
import com.cupofcoffee0801.ui.graphics.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserEditFragment : DialogFragment() {

    private val viewModel: UserEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    UserEditScreen(
                        viewModel = viewModel,
                        navigateUp = ::navigateUp
                    )
                }
            }
        }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

@Composable
fun UserEditScreen(
    viewModel: UserEditViewModel,
    navigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.handleImagePickerResult(uri)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImagesLauncher.launch("image/*")
        }
    }

    StateContent(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        isError = uiState?.isError ?: false,
        isLoading = uiState?.isLoading ?: false,
        isComplete = uiState?.isCompleted ?: false,
        navigateUp = navigateUp,
        data = uiState
    ) { data ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = data?.contentUri,
                contentDescription = "사용자 프로필",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        viewModel.requestAlbumAccessPermission(requestPermissionLauncher)
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "별명",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = data?.nickname ?: "익명",
                    onValueChange = { viewModel.updateNickname(it) },
                    placeholder = { Text(text = "이름을 작성해주세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = { navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.editUser() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}
