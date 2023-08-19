package com.zachvg.streetsweepingreminder.remindernotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zachvg.streetsweepingreminder.EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY
import com.zachvg.streetsweepingreminder.R

/*
Handles the broadcast send from the Android Alarm Manager to show a reminder.
 */
class ReminderNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationDetailString = intent.getStringExtra(EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY) ?: context.getString(R.string.notification_content_text_default)

        val serviceIntent = Intent(context, ReminderNotificationService::class.java).apply {
            putExtra(EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY, notificationDetailString)
        }

        ReminderNotificationService.enqueueWork(context, serviceIntent)

    }
}