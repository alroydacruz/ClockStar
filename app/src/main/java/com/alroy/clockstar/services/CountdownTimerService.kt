package com.alroy.clockstar.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.CountDownTimer
import android.support.v4.media.MediaBrowserCompat
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alroy.clockstar.Broadcast
import com.alroy.clockstar.MainActivity
import com.alroy.clockstar.R
import com.alroy.clockstar.ui.TimerFragment
import com.alroy.clockstar.ui.TimerFragment.Companion.hr
import com.alroy.clockstar.ui.TimerFragment.Companion.isDefault
import com.alroy.clockstar.ui.TimerFragment.Companion.min
import com.alroy.clockstar.ui.TimerFragment.Companion.sec
import com.alroy.clockstar.ui.TimerWakeUpActivity
import com.alroy.clockstar.util.AppConstants
import com.alroy.clockstar.util.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class CountdownTimerService :LifecycleService() {

    var serviceKilled = false
    var isReset = true
    var once  = true

    lateinit var timer :CountDownTimer
    var timeLeft = 0L




    private lateinit var curNotificationBuilder: NotificationCompat.Builder
    private lateinit var baseNotificationBuilder: NotificationCompat.Builder

    companion object {
        var isdef = true
        var hrs = 0L
        var mins =0L
        var secs =0L
        var once  = true

        val isDefault = MutableLiveData<Boolean>()
        val timeLeftInSeconds = MutableLiveData<Long>()
        val isCountingDown = MutableLiveData<Boolean>()
    }

    private fun postInitialValues() {

        isDefault.postValue(true)
        isCountingDown.postValue(false)
        timeLeftInSeconds.postValue(0L)

    }

    override fun onCreate() {
        super.onCreate()

        postInitialValues()

        CoroutineScope(Dispatchers.Main).launch {
            timeLeft = timeLeftInSeconds.value!!
        }

        baseNotificationBuilder =  NotificationCompat.Builder(this, AppConstants.NOTIFICATION_CHANNEL_CT_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.play_arrow)
            .setContentTitle("Running App")
            .setContentText(Converters.getFormattedStopWatchTime(timeLeft * 1000))
            .setContentIntent(getMainActivityPendingIntent())

       curNotificationBuilder = baseNotificationBuilder


        isCountingDown.observe(this, Observer {
            updateNotificationCountdownState(it)
        })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                AppConstants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isReset) {
                        isDefault.postValue(false)
                        startCountdownTimer()
                        startForegroundService()
                        isReset = false
                    } else {
                        isDefault.postValue(false)
                        startCountdownTimer()
                        isReset = false
                    }
                }
                AppConstants.ACTION_PAUSE_SERVICE -> {
                    isDefault.postValue(false)
                    pauseService()
                }
                AppConstants.ACTION_RESET_COUNTDOWN_TIMER_SERVICE -> {
                     isReset = true
                    isCountingDown.postValue(false)
                    timeLeftInSeconds.postValue(Converters.getNumberOfSeconds(hrs,mins,secs))
                }
                AppConstants.ACTION_STOP_SERVICE -> {
                    isDefault.postValue(true)
                    TimerWakeUpActivity.mp3.stop()
                    once = true
                     isReset = true
                    isCountingDown.postValue(false)
                    timeLeftInSeconds.postValue(Converters.getNumberOfSeconds(0,0,0))
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        serviceKilled = true
        isReset = true
        isdef = true
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }


    private fun pauseService() {
        timer.cancel()
        isCountingDown.postValue(false)
    }

    private fun startCountdownTimer() {
        isCountingDown.postValue(true)

        CoroutineScope(Dispatchers.Main).launch {
            timer = object : CountDownTimer(timeLeftInSeconds.value!! * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInSeconds.postValue(millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    val i = Intent(applicationContext,TimerWakeUpActivity::class.java).apply{
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(i)
//                    Toast.makeText(applicationContext,"Done",Toast.LENGTH_SHORT).show()
                    isDefault.postValue(false)
                    isCountingDown.postValue(false)
                    timeLeftInSeconds.postValue(Converters.getNumberOfSeconds(hrs,mins,secs))



                }
            }.start()
        }
    }

    private fun onTimerFinished() {

    }

    private fun startForegroundService() {
        isCountingDown.postValue(true)
        isdef = false

        if(once) {
            hrs = hr.toLong()
            mins = min.toLong()
            secs = sec.toLong()
            once = false
        }
        timeLeftInSeconds.postValue(Converters.getNumberOfSeconds(hrs,mins,secs))

        CoroutineScope(Dispatchers.Main).launch {
            timeLeft = timeLeftInSeconds.value!!
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, AppConstants.NOTIFICATION_CHANNEL_CT_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.play_arrow)
            .setContentTitle("Running App")
            .setContentText(Converters.getFormattedStopWatchTime(timeLeft* 1000))
            .setContentIntent(getMainActivityPendingIntent())


        baseNotificationBuilder = notificationBuilder

        startForeground(AppConstants.NOTIFICATION_CT_ID, baseNotificationBuilder.build())

        timeLeftInSeconds.observe(this, Observer {
            if(!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(Converters.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(AppConstants.NOTIFICATION_CT_ID, notification.build())
            }
        })
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = AppConstants.ACTION_SHOW_COUNTDOWN_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun updateNotificationCountdownState(isCountingDown: Boolean) {
        val notificationActionText = if(isCountingDown) "Pause" else "Play"
        val pendingIntent = if(isCountingDown) {
            val pauseIntent = Intent(this, CountdownTimerService::class.java).apply {
                action = AppConstants.ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, CountdownTimerService::class.java).apply {
                action = AppConstants.ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val resetIntent = Intent(this, CountdownTimerService::class.java).apply {
            action = AppConstants.ACTION_RESET_COUNTDOWN_TIMER_SERVICE
        }
        val  pendingResetIntent =  PendingIntent.getService(this, 3, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val stopIntent = Intent(this, CountdownTimerService::class.java).apply {
            action = AppConstants.ACTION_STOP_SERVICE
        }
        val  pendingStopIntent =  PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled) {
            if(isCountingDown) {
                curNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.pause, notificationActionText, pendingIntent)
                notificationManager.notify(AppConstants.NOTIFICATION_CT_ID, curNotificationBuilder.build())
            }else{
                curNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.pause, notificationActionText, pendingIntent)
                    .addAction(R.drawable.pause, "Reset", pendingResetIntent)
                    .addAction(R.drawable.pause, "Stop", pendingStopIntent)

                notificationManager.notify(AppConstants.NOTIFICATION_CT_ID, curNotificationBuilder.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            AppConstants.NOTIFICATION_CHANNEL_CT_ID,
            AppConstants.NOTIFICATION_CHANNEL_CT_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


}