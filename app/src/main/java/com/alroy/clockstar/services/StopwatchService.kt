package com.alroy.clockstar.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alroy.clockstar.MainActivity
import com.alroy.clockstar.R
import com.alroy.clockstar.util.AppConstants.ACTION_PAUSE_SERVICE
import com.alroy.clockstar.util.AppConstants.ACTION_SHOW_STOPWATCH_FRAGMENT
import com.alroy.clockstar.util.AppConstants.ACTION_START_OR_RESUME_SERVICE
import com.alroy.clockstar.util.AppConstants.ACTION_STOP_SERVICE
import com.alroy.clockstar.util.AppConstants.NOTIFICATION_CHANNEL_SW_ID
import com.alroy.clockstar.util.AppConstants.NOTIFICATION_CHANNEL_SW_NAME
import com.alroy.clockstar.util.AppConstants.NOTIFICATION_SW_ID
import com.alroy.clockstar.util.AppConstants.TIMER_UPDATE_INTERVAL
import com.alroy.clockstar.util.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class StopwatchService: LifecycleService() {

    private var isFirstRun = true
    var serviceKilled = false

    private lateinit var curNotificationBuilder: NotificationCompat.Builder
    private lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder =  NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SW_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.play_arrow)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())
        postInitialValues()

//        val t = timeRunInSeconds.value!!


        isTracking.observe(this, Observer {
            updateNotificationTrackingState(it)
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startTimer()
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }
            }
        }

       return super.onStartCommand(intent, flags, startId)

    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }


    private fun startForegroundService() {
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SW_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.play_arrow)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())


        baseNotificationBuilder =notificationBuilder

        startForeground(NOTIFICATION_SW_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(Converters.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_SW_ID, notification.build())
            }
        })
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            action = ACTION_SHOW_STOPWATCH_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, StopwatchService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, StopwatchService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val stopIntent = Intent(this, StopwatchService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
       val  pendingStopIntent =  PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled) {
            if(isTracking) {
                curNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.pause, notificationActionText, pendingIntent)
                notificationManager.notify(NOTIFICATION_SW_ID, curNotificationBuilder.build())
            }else{
                curNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.pause, notificationActionText, pendingIntent)
                    .addAction(R.drawable.pause, "Stop", pendingStopIntent)
                notificationManager.notify(NOTIFICATION_SW_ID, curNotificationBuilder.build())
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_SW_ID,
            NOTIFICATION_CHANNEL_SW_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L


    private fun startTimer() {


        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }


}