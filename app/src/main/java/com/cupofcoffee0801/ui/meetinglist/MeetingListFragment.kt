package com.cupofcoffee0801.ui.meetinglist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.graphics.Black
import com.cupofcoffee0801.ui.graphics.Brown
import com.cupofcoffee0801.ui.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetingListFragment : BottomSheetDialogFragment() {

    private val viewModel: MeetingListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PlaceMeetingsScreen(
                    viewModel,
                    applyOnclick(),
                    ::navigateUp
                )
            }
        }
    }

    private fun applyOnclick() = object : MeetingClickListener {

        override fun onApplyClick(meetingId: String) {
            if (viewModel.isNetworkConnected()) viewModel.applyMeeting(meetingId)
            else view?.showSnackBar(R.string.attended_network_message)
        }

        override fun onCancelClick(meetingId: String) {
            if (viewModel.isNetworkConnected()) viewModel.cancelMeeting(meetingId)
            else view?.showSnackBar(R.string.attended_network_message)
        }

        override fun onDetailClick(meetingId: String) {
            val action =
                MeetingListFragmentDirections.actionMeetingListFragmentToMeetingDetailFragment(
                    meetingId
                )
            findNavController().navigate(action)
        }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

@Composable
fun PlaceMeetingsScreen(
    viewModel: MeetingListViewModel = hiltViewModel(),
    meetingClickListener: MeetingClickListener,
    navigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState!!.isLoading)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState?.placeCaption ?: "",
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Image(
                        painter = painterResource(id = R.drawable.baseline_cancel_24),
                        contentDescription = "취소",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navigateUp() }
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState!!.meetingsInPlace) { userInMeeting ->
                        MeetingItem(meetingInPlace = userInMeeting, meetingClickListener)
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingItem(meetingInPlace: MeetingInPlace, meetingClickListener: MeetingClickListener) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable {
                meetingClickListener.onDetailClick(meetingId = meetingInPlace.id)
            }
            .padding(8.dp)
    ) {
        Text(text = meetingInPlace.content)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = meetingInPlace.date,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = meetingInPlace.time,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(meetingInPlace.userInMeeting) { userInMeeting ->
                UserItem(userInMeeting = userInMeeting)
            }
        }
        Button(
            onClick = {
                if (meetingInPlace.isAttendedMeeting) meetingClickListener.onCancelClick(
                    meetingInPlace.id
                )
                else meetingClickListener.onApplyClick(meetingId = meetingInPlace.id)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown,
                contentColor = Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = if (meetingInPlace.isAttendedMeeting) stringResource(R.string.cancel)
                else stringResource(R.string.apply)
            )
        }
    }
}

@Composable
fun UserItem(userInMeeting: UserInMeeting) {
    Column(
        modifier = Modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = userInMeeting.profilesUrl,
            contentDescription = "userProfile",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, Brown, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userInMeeting.nickName ?: "Unknown User",
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}