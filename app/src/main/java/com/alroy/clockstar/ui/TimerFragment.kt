package com.alroy.clockstar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alroy.clockstar.R
import com.alroy.clockstar.services.CountdownTimerService
import com.alroy.clockstar.services.CountdownTimerService.Companion.hrs
import com.alroy.clockstar.services.CountdownTimerService.Companion.isdef
import com.alroy.clockstar.services.CountdownTimerService.Companion.mins
import com.alroy.clockstar.services.CountdownTimerService.Companion.secs
import com.alroy.clockstar.util.AppConstants
import com.alroy.clockstar.util.Converters
import kotlinx.android.synthetic.main.fragment_timer.*

class TimerFragment : Fragment(R.layout.fragment_timer) {

    private var progr = 0

    companion object{
        var hr = "0"
        var min = "0"
        var sec = "0"
        var isDefault= true
    }


    private var isCountingDown= false
    private var timeLeftInSeconds = 0L


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        isDefault = isdef

        hr = hrs.toString()
        min= mins.toString()
        sec=secs.toString()

        subscribeToObservers()

        defaultUi(isDefault)


        ct_progress_bar.max = Converters.getNumberOfSeconds(hr.toLong(),min.toLong(),sec.toLong()).toInt()

        Toast.makeText(requireContext(),"started ",Toast.LENGTH_SHORT).show()

        btnToggleStartPauseCT.setOnClickListener {

            if ((et_sec.text.isNotEmpty() || et_min.text.isNotEmpty() || et_hr.text.isNotEmpty())&&
                (((et_sec.text?.toString()?.toLongOrNull()?:0L!=0L) || (et_min.text?.toString()?.toLongOrNull()?:0L!=0L)||( et_hr.text?.toString()?.toLongOrNull()?:0L!=0L)))
                ||isCountingDown || !isDefault) {


                view.let { activity?.hideKeyboard() }


//                Toast.makeText(requireContext(),sec.toString(),Toast.LENGTH_SHORT).show()


                hr = if (et_hr.text.toString().isNotEmpty()) et_hr.text.toString() else hr
                min = if (et_min.text.toString().isNotEmpty()) et_min.text.toString() else min
                sec = if (et_sec.text.toString().isNotEmpty()) et_sec.text.toString() else sec



                group.visibility = View.INVISIBLE
                toggleCountdown()

            }
        }
        btnResetCT.setOnClickListener{
            Toast.makeText(requireContext(),sec.toLong().toString(),Toast.LENGTH_SHORT).show()

//            ct_progress_bar.max = Converters.getNumberOfSeconds(hr.toLong(),min.toLong(),sec.toLong()).toInt()
            sendCommandToService(AppConstants.ACTION_RESET_COUNTDOWN_TIMER_SERVICE)
        }
        btnDeleteCT.setOnClickListener{
            ct_progress_bar.progress = 0

            sendCommandToService(AppConstants.ACTION_STOP_SERVICE)

        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun subscribeToObservers() {
        CountdownTimerService.isCountingDown.observe(viewLifecycleOwner, Observer {
            isCountingDown = it
            updateUi(it)
        })

        CountdownTimerService.isDefault.observe(viewLifecycleOwner, Observer {
            isDefault = it
            ct_progress_bar.progress = 0
            defaultUi(it)
        })

        CountdownTimerService.timeLeftInSeconds.observe(viewLifecycleOwner, Observer {
            timeLeftInSeconds = it
            val formattedTime = Converters.getFormattedStopWatchTime(timeLeftInSeconds* 1000L)
            if(!isDefault) {
                ct_progress_bar.progress = it.toInt()
                tvCountdownTimer.text = formattedTime
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defaultUi(isDefault: Boolean) {
        if (isDefault){
            et_hr.text.clear()
            et_min.text.clear()
            et_sec.text.clear()
            group.visibility = View.VISIBLE

            hr = "0"
            min = "0"
            sec = "0"

            btnResetCT.isEnabled = false
            btnDeleteCT.isEnabled = false
            tvCountdownTimer.text = ""
        }else{

//            Toast.makeText(requireContext(),sec.toLong().toString(),Toast.LENGTH_SHORT).show()

            ct_progress_bar.max = Converters.getNumberOfSeconds(hr.toLong(),min.toLong(),sec.toLong()).toInt()
            group.visibility = View.INVISIBLE
        }
    }
    private fun Activity.hideKeyboard(){
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view :View){
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken , 0)
    }

    private fun toggleCountdown() {
        if(isCountingDown) {
            sendCommandToService(AppConstants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(AppConstants.ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateUi(isCountingDown: Boolean) {
        if(!isCountingDown) {
                btnToggleStartPauseCT.setBackgroundResource(R.drawable.play_button)
            if(!isDefault) {
                btnResetCT.isEnabled = true
                btnDeleteCT.isEnabled = true
                ct_progress_bar.progress = if(timeLeftInSeconds.toInt()!=0) timeLeftInSeconds.toInt() else return
            }
        } else {
            ct_progress_bar.progress = timeLeftInSeconds.toInt()
            btnToggleStartPauseCT.setBackgroundResource(R.drawable.pause_button)
                btnResetCT.isEnabled = false
                btnDeleteCT.isEnabled = false
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), CountdownTimerService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

//    override fun onSaveInstanceState(outState: Bundle) {
//
//        super.onSaveInstanceState(outState)
//    }
}