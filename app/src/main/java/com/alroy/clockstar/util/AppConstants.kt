package com.alroy.clockstar.util

import android.graphics.Color

object AppConstants {

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_RESET_COUNTDOWN_TIMER_SERVICE = "ACTION_RESET_COUNTDOWN_TIMER_SERVICE"
    const val ACTION_SHOW_STOPWATCH_FRAGMENT = "ACTION_SHOW_STOPWATCH_FRAGMENT"

    const val ACTION_SHOW_COUNTDOWN_FRAGMENT = "ACTION_SHOW_COUNTDOWN_FRAGMENT"


    const val TIMER_UPDATE_INTERVAL = 1L

    const val NOTIFICATION_CHANNEL_SW_ID = "stopwatch_channel"
    const val NOTIFICATION_CHANNEL_SW_NAME = "stopwatch"
    const val NOTIFICATION_SW_ID = 1


    const val NOTIFICATION_CHANNEL_CT_ID = "countdown_channel"
    const val NOTIFICATION_CHANNEL_CT_NAME = "countdown"
    const val NOTIFICATION_CT_ID = 2
}