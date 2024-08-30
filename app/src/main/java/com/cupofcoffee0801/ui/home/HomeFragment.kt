package com.cupofcoffee0801.ui.home

import android.Manifest
import android.content.Context
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
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.handle
import com.cupofcoffee0801.ui.graphics.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint

private const val INIT_ZOOM_LEVEL = 17.0
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

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
        val action =
            HomeFragmentDirections.actionHomeFragmentToSaveMeetingFragment(
                placeName,
                position,
                null
            )
        findNavController().navigate(action)
    }

    private fun moveToPlaceMeetings(placeId: String) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToMeetingListFragment(
                placeId
            )
        findNavController().navigate(action)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    fusedLocationSource: FusedLocationSource,
    onPlaceClick: (String, LatLng) -> Unit,
    onMarkerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = rememberMapViewWithLifecycle()
    val naverMapState = remember { mutableStateOf<NaverMap?>(null) }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            val naverMap = naverMapState.value
            if (naverMap != null) {
                naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
            }
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        ) { mapView ->
            mapView.getMapAsync { naverMap ->
                naverMapState.value = naverMap
                naverMap.locationSource = fusedLocationSource
                naverMap.uiSettings.isLocationButtonEnabled = false

                showUserLocation(naverMap)
                initCameraZoom(naverMap)
                setSymbolClick(naverMap, context, onPlaceClick)
                initMarkers(naverMap, viewModel, lifecycleOwner, onMarkerClick)
            }
        }

        IconButton(
            onClick = {
                val naverMap = naverMapState.value!!
                val location = naverMap.locationOverlay.position
                val cameraUpdate = CameraUpdate.scrollTo(location)
                naverMap.moveCamera(cameraUpdate)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_my_location_24),
                contentDescription = "본인 위치로 이동",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

private fun showUserLocation(naverMap: NaverMap) {
    naverMap.locationOverlay.isVisible = true
}

private fun initCameraZoom(naverMap: NaverMap) {
    naverMap.moveCamera(CameraUpdate.zoomTo(INIT_ZOOM_LEVEL))
}

private fun setSymbolClick(
    naverMap: NaverMap, context: Context, onPlaceClick: (String, LatLng) -> Unit
) {
    naverMap.setOnSymbolClickListener { symbol ->
        val placeName = symbol.caption.split("\n").last()
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.save_title))
            .setMessage(placeName)
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(context.getString(R.string.save_create)) { _, _ ->
                onPlaceClick(placeName, symbol.position)
            }
            .show()
        true
    }
}

private fun initMarkers(
    naverMap: NaverMap,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner,
    onMarkerClick: (String) -> Unit
) {
    viewModel.dataResult.observe(lifecycleOwner) { result ->
        result.handle(
            onLoading = {
            },
            onSuccess = { state ->
                state.markers.map { marker ->
                    if (state.showedMakers.contains(marker).not()) {
                        marker.map = naverMap
                        val placeId = marker.tag.toString()
                        marker.setOnClickListener {
                            onMarkerClick(placeId)
                            true
                        }
                    }
                }
                viewModel.updateShowedMarkers(state.markers)
            },
            onError = {
            }
        )
    }
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
