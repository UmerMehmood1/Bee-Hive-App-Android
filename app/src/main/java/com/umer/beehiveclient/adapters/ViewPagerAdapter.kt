package com.umer.beehiveclient.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.umer.beehiveclient.fragments.AlertFragment
import com.umer.beehiveclient.fragments.DashboardFragment
import com.umer.beehiveclient.fragments.ProfileFragment
import com.umer.beehiveclient.fragments.SettingFragment
import com.umer.beehiveclient.listeners.SettingFragmentListener

class ViewPagerAdapter(fragmentActivity: FragmentActivity, settingFragmentListener: SettingFragmentListener) : FragmentStateAdapter(fragmentActivity) {
    private val settingFragment = SettingFragment()
    init {
        settingFragment.setListener(settingFragmentListener)
    }
    private val fragments = listOf(
        DashboardFragment(),
        AlertFragment(),
        settingFragment,
//        ProfileFragment()
//        SecondFragment()
        // Add more fragments if needed
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}