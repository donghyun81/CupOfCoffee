package com.cupofcoffee.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentHomeBinding
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.util.FusedLocationSource

private const val INIT_ZOOM_LEVEL = 17.0
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMap()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun loadMap() {
        val mapFragment = binding.mapFragment.getFragment<MapFragment>()
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.locationSource = locationSource

        setLocationPermission()
        showUserLocation()
        setSymbolClick(naverMap)
        initCameraZoom(naverMap)
        initMarkers(naverMap)
    }

    private fun setLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            naverMap.locationTrackingMode = LocationTrackingMode.Face
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showUserLocation() {
        val locationOverlay: LocationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true
    }

    private fun initCameraZoom(naverMap: NaverMap) {
        naverMap.moveCamera(CameraUpdate.zoomTo(INIT_ZOOM_LEVEL))
    }

    private fun setSymbolClick(naverMap: NaverMap) {
        naverMap.setOnSymbolClickListener { symbol ->
            val placeName = symbol.caption.split("\n").last()
            showDialog(placeName, symbol.position)
            true
        }
    }

    private fun initMarkers(naverMap: NaverMap) {
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = {
                    binding.cpiLoading.showLoading(result)
                },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    uiState.markers.map { marker ->
                        if (uiState.showedMakers.contains(marker).not()) {
                            marker.map = naverMap
                            val placeId = marker.tag.toString()
                            marker.setOnClickListener {
                                val action =
                                    HomeFragmentDirections.actionHomeFragmentToMeetingListFragment(
                                        placeId
                                    )
                                findNavController().navigate(action)
                                true
                            }
                        }
                    }
                    viewModel.updateShowedMarkers(uiState.markers)
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun showDialog(placeName: String, position: LatLng) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.save_title))
            .setMessage(placeName)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(getString(R.string.save_create)) { _, _ ->
                moveToSaveMeeting(placeName, position)
            }
            .show()
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
}