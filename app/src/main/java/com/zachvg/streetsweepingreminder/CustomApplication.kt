package com.zachvg.streetsweepingreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.jakewharton.threetenabp.AndroidThreeTen

/*
This class initializes the Three Ten Android backport and sets up the notification channels.
 */

class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Set up the notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderNotificationName = getString(R.string.reminder_notification_channel_name)
            val reminderNotificationDescriptionText = getString(R.string.reminder_notification_channel_detail)
            val reminderNotificationImportance = NotificationManager.IMPORTANCE_HIGH
            val reminderNotificationChannel = NotificationChannel(REMINDER_NOTIFICATION_CHANNEL_ID, reminderNotificationName, reminderNotificationImportance).apply {
                description = reminderNotificationDescriptionText
            }

            val alarmName = getString(R.string.alarm_notification_channel_name)
            val alarmDescriptionText = getString(R.string.alarm_notification_channel_detail)
            val alarmImportance = NotificationManager.IMPORTANCE_HIGH
            val alarmChannel = NotificationChannel(ALARM_NOTIFICATION_CHANNEL_ID, alarmName, alarmImportance).apply {
                description = alarmDescriptionText

                val soundURI = Uri.parse("android.resource://$packageName/${R.raw.silence}")
                val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

                setSound(soundURI, attributes)
            }

            notificationManager.createNotificationChannel(reminderNotificationChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }
}