package com.zachvg.streetsweepingreminder.remindernotification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zachvg.streetsweepingreminder.*
import com.zachvg.streetsweepingreminder.activity.MainActivity
import com.zachvg.streetsweepingreminder.database.SweepingReminderDatabase
import com.zachvg.streetsweepingreminder.database.SweepingReminderRepository
import org.threeten.bp.LocalDate

/*
Shows the reminder notification. This might not go off at exact time set by the user
since it could be scheduled as a Job on some platforms. This is intended behavior since
the reminder doesn't really need to be a precise as an alarm.
 */
class ReminderNotificationService : JobIntentService() {

    companion object {
        private const val JOB_ID = 2

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, ReminderNotificationService::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        // Send the notification
        val notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(this, 0, tapIntent, flags)

        val notificationContentText = intent.getStringExtra(EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY) ?: this.getString(R.string.notification_content_text_default)


        val builder = NotificationCompat.Builder(this, REMINDER_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_broom)
                .setContentTitle(this.getString(R.string.street_sweeping_reminder))
                .setContentText(notificationContentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(notificationSoundURI) // Need to set a sound or a vibration pattern for a heads-up notification to work
                .setVibrate(longArrayOf(100, 100))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(REMINDER_NOTIFICATION_ID, builder.build())

        // Schedule the next notification
        val sweepingDao = SweepingReminderDatabase.getDatabase(this).sweepingScheduleDao()
        val repository = SweepingReminderRepository.getInstance(sweepingDao)

        repository.schedules.value?.let { schedules ->
            // Find when the next sweeping is after the one this notification is for
            val currentSweepingDate = nextSweepingDateFrom(LocalDate.now(), schedules)
            val nextBaseDate = currentSweepingDate.plusDays(1)
            val nextSweepingDate = nextSweepingDateFrom(nextBaseDate, schedules)

            val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
            val notificationRBId = sharedPreferences.getInt(NOTIFICATION_RADIO_BUTTON_CHECKED_KEY, NOTIFICATION_RADIO_BUTTON_DAY_BEFORE)
            val notificationHour = sharedPreferences.getInt(NOTIFICATION_HOUR_KEY, 9)
            val notificationMinute = sharedPreferences.getInt(NOTIFICATION_MINUTE_KEY, 0)

            val dayOf = notificationRBId == NOTIFICATION_RADIO_BUTTON_DAY_OF

            val nextNotificationDate = if (dayOf) nextSweepingDate else nextSweepingDate.minusDays(1)
            val nextNotificationDateTime = nextNotificationDate.atTime(notificationHour, notificationMinute, 0)

            val nextNotificationTriggerMillis = localDateTimeToMillis(nextNotificationDateTime)

            val notificationPendingIntent = getReminderNotificationPendingIntent(this, dayOf)

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
}