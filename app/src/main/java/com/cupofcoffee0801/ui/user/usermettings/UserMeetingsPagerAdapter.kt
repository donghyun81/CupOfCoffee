package com.cupofcoffee0801.ui.user.usermettings

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cupofcoffee0801.ui.model.MeetingsCategory

class UserMeetingsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val meetingsCategory = MeetingsCategory.entries
    override fun getItemCount(): Int = meetingsCategory.size

    override fun createFragment(position: Int): Fragment {
        val category = meetingsCategory[position]
        return UserMeetingsFragment.newInstance(category)
    }
}