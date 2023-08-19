package com.zachvg.streetsweepingreminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zachvg.streetsweepingreminder.alarm.AlarmService
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import com.zachvg.streetsweepingreminder.remindernotification.ReminderNotificationReceiver
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjusters


/*
Various functions that don't really belong in a specific class.
 */
fun nextSweepingDateFrom(date: LocalDate, schedules: List<SweepingSchedule>): LocalDate {

    // Initialize to a far future date
    var nextSweepingDate = date.plusDays(100)

    // Used for calculation in case the next sweeping day is in the next month
    val nextMonthDate = date.with(TemporalAdjusters.firstDayOfNextMonth())


    for (schedule in schedules) {
        for (dayNumberOfMonth in schedule.dayNumbersOfMonth) {

            val possibleCurrentMonthDate = date.with(TemporalAdjusters.dayOfWeekInMonth(dayNumberOfMonth, schedule.day))

            val newCurrentMonthDifference = ChronoUnit.DAYS.between(date, possibleCurrentMonthDate)

            val oldDifference = ChronoUnit.DAYS.between(date, nextSweepingDate)

            if (date.month == possibleCurrentMonthDate.month && newCurrentMonthDifference >= 0 && newCurrentMonthDifference < oldDifference) {
                nextSweepingDate = possibleCurrentMonthDate
            } else {

                val possibleNextMonthDate = nextMonthDate.with(TemporalAdjusters.dayOfWeekInMonth(dayNumberOfMonth, schedule.day))

                val newNextMontDifference = ChronoUnit.DAYS.between(date, possibleNextMonthDate)

                if (newNextMontDifference < oldDifference) {
                    nextSweepingDate = possibleNextMonthDate
                }
            }
        }
    }

    return nextSweepingDate
}

// Changed the DateTime to millis
fun localDateTimeToMillis(dateTime: LocalDateTime): Long {
    // Need to get the default time zone offset in order to convert to millis:
    // https://stackoverflow.com/questions/41427384/how-to-get-default-zoneoffset-in-java8
    val zoneId = ZoneId.systemDefault()

    return OffsetDateTime.now(zoneId)
            .withYear(dateTime.year)
            .withMonth(dateTime.monthValue)
            .withDayOfMonth(dateTime.dayOfMonth)
            .withHour(dateTime.hour)
            .withMinute(dateTime.minute)
            .withSecond(0)
            .toInstant()
            .toEpochMilli()
}


// Alarm manager requires the same pending intent to cancel an alarm that it was set with.
// Use this function get a consistent pending intent for the reminder notifications.
fun getReminderNotificationPendingIntent(context: Context, dayOf: Boolean): PendingIntent {
    val notificationDayString = if (dayOf) context.getString(R.string.today_lowercase) else context.getString(R.string.tomorrow_lowercase)

    val notificationDetailString = context.getString(R.string.notification_detail, notificationDayString)

    val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
        putExtra(EXTRA_REMINDER_NOTIFICATION_CONTENT_TEXT_KEY, notificationDetailString)
    }

    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    return PendingIntent.getBroadcast(context, REMINDER_NOTIFICATION_REQUEST_CODE, intent, flags)
}

// Alarm manager requires the same pending intent to cancel an alarm that it was set with.
// Use this function get a consistent pending intent for the alarm.
fun getAlarmPendingIntent(context: Context, dayOf: Boolean): PendingIntent {
    val alarmDayString = if (dayOf) context.getString(R.string.today_lowercase) else context.getString(R.string.tomorrow_lowercase)

    val alarmDetailString = context.getString(R.string.notification_detail, alarmDayString)

    val intent = Intent(context, AlarmService::class.java).apply {
        putExtra(EXTRA_ALARM_CONTENT_TEXT_KEY, alarmDetailString)
    }

    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        PendingIntent.getForegroundService(context, ALARM_PENDING_INTENT_REQUEST_CODE, intent, flags)
    } else {
        PendingIntent.getService(context, ALARM_PENDING_INTENT_REQUEST_CODE, intent, flags)
    }
}