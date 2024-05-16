package com.cupofcoffee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

const val INIT_ZOOM_LEVEL = 17.0

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    override fun onMapReady(p0: NaverMap) {
        setSymbolClick(p0)
        initCameraZoom(p0)
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

    private fun showDialog(placeName: String, position: LatLng) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.save_title))
            .setMessage(placeName)
            .setNegativeButton(getString(R.string.save_cancle)) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(getString(R.string.save_create)) { _, _ ->
                moveToSaveMeeting(placeName, position)
            }
            .show()
    }

    private fun moveToSaveMeeting(placeName: String, position: LatLng) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToSaveMeetingFragment(placeName, position)
        findNavController().navigate(action)
    }

}