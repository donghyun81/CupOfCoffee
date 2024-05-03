package com.cupofcoffee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cupofcoffee.databinding.FragmentSaveMeetingBinding

class SaveMeetingFragment : Fragment() {

    private var _binding: FragmentSaveMeetingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSaveMeetingBinding.inflate(inflater)
        return binding.root
    }
}