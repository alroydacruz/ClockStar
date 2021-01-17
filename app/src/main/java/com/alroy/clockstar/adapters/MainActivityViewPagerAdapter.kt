package com.alroy.clockstar.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alroy.clockstar.ui.AlarmFragment
import com.alroy.clockstar.ui.StopWatchFragment
import com.alroy.clockstar.ui.TimerFragment

class MainActivityViewPagerAdapter(activity: AppCompatActivity):FragmentStateAdapter(activity) {
    private val fragments = arrayOf(
        AlarmFragment(),
        TimerFragment(),
        StopWatchFragment()
    )


    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment =  fragments[position]



}