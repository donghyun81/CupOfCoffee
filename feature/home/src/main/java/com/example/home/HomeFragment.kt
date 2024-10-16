package com.example.home

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.example.common.component.StateContent
import com.example.common.graphics.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint

private const val INIT_ZOOM_LEVEL = 17.0
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val POSITION_COUNT = 10

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()
    private val fusedLocationSource: FusedLocationSource =
        FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    HomeScreen(
                        viewModel,
                        fusedLocationSource,
                        ::moveToSaveMeeting,
                        ::moveToPlaceMeetings
                    )
                }
            }
        }
    }

    private fun moveToSaveMeeting(placeName: String, position: LatLng) {
        val lat = position.latitude.toString().take(POSITION_COUNT)
        val lng = position.longitude.toString().take(POSITION_COUNT)
        val uri =
            Uri.parse("cupofcoffee://make_meeting?placeName=${placeName}&lat=${lat}&lng=${lng}&meetingId=${null}")
        findNavController().navigate(uri)
    }

    private fun moveToPlaceMeetings(placeId: String) {
        val uri = Uri.parse("cupofcoffee://meeting_place?placeId=${placeId}")
        findNavController().navigate(uri)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    fusedLocationSource: FusedLocationSource,
    moveToSaveMeeting: (String, LatLng) -> Unit,
    moveToPlaceMeetings: (String) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val naverMapState = remember { mutableStateOf<NaverMap?>(null) }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val uiState by viewModel.uiState.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(HomeIntent.InitData)
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateMakeMeeting -> {
                    moveToSaveMeeting(effect.caption, effect.latLng)
                }

                is HomeSideEffect.NavigateMeetingPlace -> {
                    moveToPlaceMeetings(effect.placeId)
                }
            }
        }
    }

    StateContent(
        isError = uiState?.isError ?: false,
        isLoading = uiState?.isLoading ?: false,
        data = uiState
    ) {
        HandlePermissions(permissionsState, naverMapState)

        Box(modifier = Modifier.fillMaxSize()) {
            MapViewWithMarkers(
                mapView = mapView,
                naverMapState = naverMapState,
                fusedLocationSource = fusedLocationSource,
                uiState = uiState,
                context = context,
                viewModel = viewModel
            )

            MyLocationButton(
                naverMapState = naverMapState,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HandlePermissions(
    permissionsState: MultiplePermissionsState,
    naverMapState: MutableState<NaverMap?>
) {
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            naverMapState.value?.locationTrackingMode = LocationTrackingMode.NoFollow
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
}

@Composable
private fun MapViewWithMarkers(
    mapView: MapView,
    naverMapState: MutableState<NaverMap?>,
    fusedLocationSource: FusedLocationSource,
    uiState: HomeUiState?,
    context: Context,
    viewModel: HomeViewModel
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                onCreate(null)
                onStart()
                viewModel.initMarkersJob()
            }
        }
    ) { mapView ->
        mapView.getMapAsync { naverMap ->
            naverMapState.value = naverMap
            naverMap.locationSource = fusedLocationSource
            naverMap.uiSettings.isLocationButtonEnabled = false
            showUserLocation(naverMap)
            initCameraZoom(naverMap)
            setSymbolClick(naverMap, context, viewModel)
            showMarkers(
                naverMap = naverMap,
                markers = uiState?.markers.orEmpty(),
                showedMarkers = uiState?.showedMarkers.orEmpty(),
                viewModel = viewModel,
                updateShowedMarkers = viewModel::updateShowedMarkers
            )
        }
    }
}

@Composable
private fun MyLocationButton(
    naverMapState: MutableState<NaverMap?>,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            naverMapState.value?.let { naverMap ->
                val location = naverMap.locationOverlay.position
                val cameraUpdate = CameraUpdate.scrollTo(location)
                naverMap.moveCamera(cameraUpdate)
            }
        },
        modifier = modifier
            .padding(16.dp)
            .size(48.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_my_location_24),
            contentDescription = "본인 위치로 이동"
        )
    }
}

private fun showUserLocation(naverMap: NaverMap) {
    naverMap.locationOverlay.isVisible = true
}

private fun initCameraZoom(naverMap: NaverMap) {
    naverMap.moveCamera(CameraUpdate.zoomTo(INIT_ZOOM_LEVEL))
}

private fun setSymbolClick(
    naverMap: NaverMap, context: Context, viewModel: HomeViewModel
) {
    naverMap.setOnSymbolClickListener { symbol ->
        val placeName = symbol.caption.split("\n").last()
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.save_title))
            .setMessage(placeName)
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(context.getString(R.string.save_create)) { _, _ ->
                viewModel.handleIntent(HomeIntent.SymbolClick(placeName, symbol.position))
            }
            .show()
        true
    }
}

private fun showMarkers(
    naverMap: NaverMap,
    markers: List<Marker>,
    showedMarkers: List<Marker>,
    viewModel: HomeViewModel,
    updateShowedMarkers: (List<Marker>) -> Unit
) {
    markers.filterNot { it in showedMarkers }.forEach { marker ->
        marker.map = naverMap
        val placeId = marker.tag.toString()
        marker.setOnClickListener {
            viewModel.handleIntent(HomeIntent.MarkerClick(placeId))
            true
        }
    }

    showedMarkers.filterNot { it in markers }.forEach { marker ->
        marker.map = null
    }
    updateShowedMarkers(markers)
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(null)
            }

            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }
        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return mapView
}