package com.zachvg.streetsweepingreminder.remindernotification

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.JobIntentService
import com.zachvg.streetsweepingreminder.*
import com.zachvg.streetsweepingreminder.database.SweepingReminderDatabase
import com.zachvg.streetsweepingreminder.database.SweepingReminderRepository
import org.threeten.bp.LocalDate

/*
Schedules the reminder notification with Android Alarm Manager.
This will be scheduled as a Job on some platforms, so it might not execute right after booting,
but chances are it won't be long after.
 */
class NotificationBootService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, NotificationBootService::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val sweepingDao = SweepingReminderDatabase.getDatabase(this).sweepingScheduleDao()
        val repository = SweepingReminderRepository.getInstance(sweepingDao)

        val schedules = repository.getAllSchedulesAsList()
        val currentDate = LocalDate.now()
        val nextSweepingDate = nextSweepingDateFrom(currentDate, schedules)

        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
        val notificationRBId = sharedPreferences.getInt(NOTIFICATION_RADIO_BUTTON_CHECKED_KEY, NOTIFICATION_RADIO_BUTTON_DAY_BEFORE)
        val notificationHour = sharedPreferences.getInt(NOTIFICATION_HOUR_KEY, 12)
        val notificationMinute = sharedPreferences.getInt(NOTIFICATION_MINUTE_KEY, 0)

        val dayOf = notificationRBId == NOTIFICATION_RADIO_BUTTON_DAY_OF

        val notificationDate = if (dayOf) nextSweepingDate else nextSweepingDate.minusDays(1)
        val notificationDateTime = notificationDate.atTime(notificationHour, notificationMinute, 0)

        val nextNotificationTriggerMillis = localDateTimeToMillis(notificationDateTime)

        // This check avoids the notification detail text being the wrong value. For example, if the device restarts
        // on the day of sweeping but the notification is set to be shown the day before, this ensures the detail
        // string will read "Street Sweeping Today" instead of "Street Sweeping Tomorrow".
        val notificationPendingIntent = if (notificationDate.isBefore(currentDate)) {
            getReminderNotificationPendingIntent(this, true)
        } else {
            getReminderNotificationPendingIntent(this, dayOf)
        }

        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextNotificationTriggerMillis,
                    notificationPendingIntent)
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextNotificationTriggerMillis,
                    notificationPendingIntent)
        }
    }
}