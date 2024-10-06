package com.example.meetingplace

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.example.common.component.StateContent
import com.example.common.graphics.AppTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetingPlaceFragment : BottomSheetDialogFragment() {

    private val viewModel: MeetingPlaceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    PlaceMeetingsScreen(
                        viewModel,
                        ::navigateMeetingDetail,
                        ::navigateUp
                    )
                }
            }
        }
    }

    private fun navigateMeetingDetail(meetingId: String) {
        val uri = Uri.parse("cupofcoffee://meeting_detail?meetingId=${meetingId}")
        findNavController().navigate(uri)
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceMeetingsScreen(
    viewModel: MeetingPlaceViewModel,
    navigateMeetingDetail: (String) -> Unit,
    navigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MeetingPlaceSideEffect.NavigateMeetingDetail -> {
                    navigateMeetingDetail(effect.meetingId)
                }

                is MeetingPlaceSideEffect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = uiState.snackBarMessage,
                        duration = SnackbarDuration.Short
                    )
                }

                MeetingPlaceSideEffect.NavigateUp -> {
                    navigateUp()
                }
            }
        }
    }

    StateContent(
        isError = uiState.isError,
        isLoading = uiState.isLoading,
        data = uiState
    ) { uiState ->
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = uiState.placeCaption,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = navigateUp) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_cancel_24),
                                    contentDescription = stringResource(R.string.cancel)
                                )
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.meetingsInPlace) { meetingInPlace ->
                            MeetingItem(
                                meetingPlaceMeetingUiModel = meetingInPlace,
                                viewModel::handleIntent
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MeetingItem(
    meetingPlaceMeetingUiModel: MeetingPlaceMeetingUiModel,
    handleIntent: (MeetingPlaceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .clickable {
                handleIntent(
                    MeetingPlaceIntent.MeetingDetailClick(
                        meetingPlaceMeetingUiModel.id
                    )
                )
            }
            .padding(8.dp)
            .border(
                2.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
    ) {
        Text(
            text = meetingPlaceMeetingUiModel.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .height(100.dp)
                .padding(4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = meetingPlaceMeetingUiModel.date,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = meetingPlaceMeetingUiModel.time,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(meetingPlaceMeetingUiModel.attendees) { userInMeeting ->
                UserItem(meetingPlaceUserUiModel = userInMeeting)
            }
        }

        Button(
            onClick = {
                if (meetingPlaceMeetingUiModel.isAttendedMeeting)
                    handleIntent(
                        MeetingPlaceIntent.AttendedCancelClick(
                            meetingPlaceMeetingUiModel.isMyMeeting,
                            meetingPlaceMeetingUiModel.id
                        )
                    )
                else handleIntent(
                    MeetingPlaceIntent.MeetingApplyClick(
                        meetingPlaceMeetingUiModel.id
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = if (meetingPlaceMeetingUiModel.isAttendedMeeting) stringResource(R.string.cancel)
                else stringResource(R.string.apply)
            )
        }
    }
}

@Composable
fun UserItem(meetingPlaceUserUiModel: MeetingPlaceUserUiModel) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = meetingPlaceUserUiModel.profilesUrl,
            contentDescription = "사용자 프로필",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = meetingPlaceUserUiModel.nickName ?: stringResource(R.string.unknown_user),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}