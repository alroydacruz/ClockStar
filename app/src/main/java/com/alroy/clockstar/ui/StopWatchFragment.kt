package com.alroy.clockstar.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alroy.clockstar.R
import com.alroy.clockstar.services.StopwatchService
import com.alroy.clockstar.util.AppConstants.ACTION_PAUSE_SERVICE
import com.alroy.clockstar.util.AppConstants.ACTION_START_OR_RESUME_SERVICE
import com.alroy.clockstar.util.AppConstants.ACTION_STOP_SERVICE
import com.alroy.clockstar.util.Converters
import kotlinx.android.synthetic.main.fragment_stop_watch.*


class StopWatchFragment : Fragment(R.layout.fragment_stop_watch) {
    private var isTracking = false
    private var curTimeInMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnToggleStartPause.setOnClickListener {
            toggleRun()
        }
        btnStopTimer.setOnClickListener{
            sendCommandToService(ACTION_STOP_SERVICE)
        }
        subscribeToObservers()
    }

    private fun toggleRun() {
        if(isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), StopwatchService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }


    private fun subscribeToObservers() {
        StopwatchService.isTracking.observe(viewLifecycleOwner, Observer {
         isTracking = it
            updateUi(it)
        })

        StopwatchService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = Converters.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun updateUi(isTracking: Boolean) {
        if(!isTracking) {
            btnToggleStartPause.setBackgroundResource(R.drawable.play_button)
            btnStopTimer.visibility = View.VISIBLE
        } else {
            btnToggleStartPause.setBackgroundResource(R.drawable.pause_button)
            btnStopTimer.visibility = View.GONE
        }
    }
}
