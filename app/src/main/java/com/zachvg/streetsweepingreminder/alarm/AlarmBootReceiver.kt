package com.zachvg.streetsweepingreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zachvg.streetsweepingreminder.ACTION_ALARM_BOOT

/*
Launches the job/service to schedule the alarm after a reboot.
 */
class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            val serviceIntent = Intent(ACTION_ALARM_BOOT)
            context?.let {
                ScheduleNextAlarmService.enqueueWork(it, serviceIntent)
            }
        }
    }
}