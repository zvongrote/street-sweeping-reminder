package com.zachvg.streetsweepingreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zachvg.streetsweepingreminder.EXTRA_ALARM_CONTENT_TEXT_KEY
import com.zachvg.streetsweepingreminder.EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY
import com.zachvg.streetsweepingreminder.R

/*
Handles the broadcast from the Android Alarm Manager for the alarm.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmDetailString = intent.getStringExtra(EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY) ?: context.getString(R.string.notification_content_text_default)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_ALARM_CONTENT_TEXT_KEY, alarmDetailString)
        }

        // The alarm service is a foreground service, so start it correctly based on the Android version.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}