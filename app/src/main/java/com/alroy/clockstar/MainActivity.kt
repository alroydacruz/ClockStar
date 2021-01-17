package com.alroy.clockstar

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alroy.clockstar.adapters.MainActivityViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setSupportActionBar(toolbar) //custom
        val mainActivityViewPagerAdapter =
            MainActivityViewPagerAdapter(this)
        view_pager.adapter = mainActivityViewPagerAdapter

        TabLayoutMediator(tab_layout,view_pager){tab, position ->
            when (position) {
                0 -> tab.icon = getDrawable(R.drawable.avd_anim_settings)
                1 -> tab.icon = getDrawable(R.drawable.ic_launcher_background)
                2 -> tab.icon = getDrawable(R.drawable.ic_launcher_background)
            }
        }.attach()
}
}
