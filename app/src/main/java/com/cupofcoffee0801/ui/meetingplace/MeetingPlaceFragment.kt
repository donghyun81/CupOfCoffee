package com.cupofcoffee0801.ui.meetingplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.component.StateContent
import com.cupofcoffee0801.ui.graphics.AppTheme
import com.cupofcoffee0801.ui.showSnackBar
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
                        applyOnclick(),
                        ::navigateUp
                    )
                }
            }
        }
    }

    private fun applyOnclick() = object : MeetingClickListener {

        override fun onApplyClick(meetingId: String) {
            if (viewModel.isNetworkConnected()) viewModel.applyMeeting(meetingId)
            else view?.showSnackBar(R.string.attended_network_message)
        }

        override fun onCancelClick(isMyMeeting: Boolean, meetingId: String) {
            when {
                isMyMeeting -> view?.showSnackBar(R.string.no_cancel_my_meeting)
                viewModel.isNetworkConnected() -> viewModel.cancelMeeting(meetingId)
                else -> view?.showSnackBar(R.string.attended_network_message)
            }
        }

        override fun onDetailClick(meetingId: String) {
            val action =
                MeetingPlaceFragmentDirections.actionMeetingPlaceFragmentToMeetingDetailFragment(
                    meetingId
                )
            findNavController().navigate(action)
        }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceMeetingsScreen(
    viewModel: MeetingPlaceViewModel = hiltViewModel(),
    meetingClickListener: MeetingClickListener,
    navigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState()

    StateContent(
        isError = uiState?.isError ?: false,
        isLoading = uiState?.isLoading ?: false,
        data = uiState
    ) { data ->
        Column(
            modifier = Modifier
                .fillMaxHeight(0.8f) // BottomSheetDialog에서 적절한 높이 설정
                .padding(16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = data?.placeCaption ?: "",
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
                modifier = Modifier.weight(1f) // 화면 여분을 사용하는 데 기여
            ) {
                items(data!!.meetingsInPlace) { meetingInPlace ->
                    MeetingItem(
                        meetingPlaceMeetingUiModel = meetingInPlace,
                        meetingClickListener
                    )
                }
            }
        }
    }
}

@Composable
fun MeetingItem(
    meetingPlaceMeetingUiModel: MeetingPlaceMeetingUiModel,
    meetingClickListener: MeetingClickListener
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clickable { meetingClickListener.onDetailClick(meetingId = meetingPlaceMeetingUiModel.id) }
            .padding(8.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
    ) {
        Text(
            text = meetingPlaceMeetingUiModel.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3, // 최대 2줄로 표시
            overflow = TextOverflow.Ellipsis, // 내용이 길면 잘라내기
            modifier = Modifier
                .height(100.dp)
                .padding(bottom = 8.dp)
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
            items(meetingPlaceMeetingUiModel.meetingPlaceUserUiModel) { userInMeeting ->
                UserItem(meetingPlaceUserUiModel = userInMeeting)
            }
        }

        Button(
            onClick = {
                if (meetingPlaceMeetingUiModel.isAttendedMeeting) meetingClickListener.onCancelClick(
                    meetingPlaceMeetingUiModel.isMyMeeting,
                    meetingPlaceMeetingUiModel.id
                )
                else meetingClickListener.onApplyClick(meetingId = meetingPlaceMeetingUiModel.id)
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