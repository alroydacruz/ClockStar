package com.alroy.clockstar.ui

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alroy.clockstar.R
import kotlinx.android.synthetic.main.activity_timer_wake_up.*

class TimerWakeUpActivity : AppCompatActivity() {
    companion object{
        var mp3 = MediaPlayer()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_wake_up)

        mp3 =  MediaPlayer.create(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        mp3.start()

        btnStop.setOnClickListener {
            mp3.stop()
            finish()
        }
    }
}