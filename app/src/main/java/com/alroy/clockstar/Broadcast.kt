package com.alroy.clockstar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.alroy.clockstar.ui.TimerWakeUpActivity

class Broadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context,"inBroadcastReceiveronReceive",Toast.LENGTH_SHORT).show()

       val timerIntent = Intent(context,TimerWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context?.startActivity(timerIntent)
    }
}