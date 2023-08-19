package com.zachvg.streetsweepingreminder.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.JobIntentService
import com.zachvg.streetsweepingreminder.*
import com.zachvg.streetsweepingreminder.database.SweepingReminderDatabase
import com.zachvg.streetsweepingreminder.database.SweepingReminderRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/*
Schedules the next alarm.
 */
class ScheduleNextAlarmService : JobIntentService() {

    companion object {
        private const val JOB_ID = 2

        fun enqueueWork(context:Context, intent: Intent) {
            enqueueWork(context, ScheduleNextAlarmService::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val sweepingDao = SweepingReminderDatabase.getDatabase(this).sweepingScheduleDao()
        val repository = SweepingReminderRepository.getInstance(sweepingDao)
        val schedules = repository.getAllSchedulesAsList()

        val nextSweepingDate = nextSweepingDateFrom(LocalDate.now(), schedules)

        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
        val alarmRBId = sharedPreferences.getInt(ALARM_RADIO_BUTTON_CHECKED_KEY, ALARM_RADIO_BUTTON_DAY_OF)
        val alarmHour = sharedPreferences.getInt(ALARM_HOUR_KEY, 12)
        val alarmMinute = sharedPreferences.getInt(ALARM_MINUTE_KEY, 0)
        val alarmTime = LocalTime.of(alarmHour, alarmMinute, 0)

        val dayOf = alarmRBId == ALARM_RADIO_BUTTON_DAY_OF

        val alarmDate = if (dayOf) nextSweepingDate else nextSweepingDate.minusDays(1)

        val possibleAlarmDateTime = alarmDate.atTime(alarmTime)
        val now = LocalDateTime.now()

        val actualAlarmDateTime = if (possibleAlarmDateTime.isAfter(now)) {
            possibleAlarmDateTime
        } else {
            // Calculate the sweeping date after the current one and calculate a new notification date/time.
            val tempBaseDate = nextSweepingDate.plusDays(1)
            val tempSweepingDate = nextSweepingDateFrom(tempBaseDate, schedules)
            val tempAlarmDate = if (dayOf) tempSweepingDate else tempSweepingDate.minusDays(1)
            tempAlarmDate.atTime(alarmTime)
        }

        val alarmPendingIntent = getAlarmPendingIntent(this, dayOf)

        val alarmTriggerMillis = localDateTimeToMillis(actualAlarmDateTime)

        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTriggerMillis,
                    alarmPendingIntent)
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTriggerMillis,
                    alarmPendingIntent)
        }
    }
}