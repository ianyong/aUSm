package com.github.iwle.ausm.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(
    fa: FragmentActivity,
    private val fragmentList: List<Fragment>
) : FragmentStateAdapter(fa) {
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}