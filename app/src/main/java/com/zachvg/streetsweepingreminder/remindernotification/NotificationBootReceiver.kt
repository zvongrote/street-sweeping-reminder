package com.zachvg.streetsweepingreminder.remindernotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/*
Launches the job/schedule to schedules the reminder after a reboot.
 */
class NotificationBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val serviceIntent = Intent(context, NotificationBootService::class.java)

            NotificationBootService.enqueueWork(context, serviceIntent)
        }
    }
}
