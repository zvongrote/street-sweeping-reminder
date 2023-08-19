package com.zachvg.streetsweepingreminder.summary

import android.app.AlarmManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.lifecycle.*
import com.zachvg.streetsweepingreminder.*
import com.zachvg.streetsweepingreminder.R
import com.zachvg.streetsweepingreminder.alarm.AlarmBootReceiver
import com.zachvg.streetsweepingreminder.database.SweepingReminderDatabase
import com.zachvg.streetsweepingreminder.database.SweepingReminderRepository
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import com.zachvg.streetsweepingreminder.remindernotification.NotificationBootReceiver
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

private class DoubleBooleanLiveData(private val first: LiveData<Boolean>, private val second: LiveData<Boolean>) : MediatorLiveData<Boolean>() {
    init {
        super.addSource(first) {
            value = andLiveDataValues(first.value, second.value)
        }
        super.addSource((second)) {
            value = andLiveDataValues(first.value, second.value)
        }
    }

    // Return b1 AND b2 if neither are null, false otherwise
    private fun andLiveDataValues(b1: Boolean?, b2: Boolean?) = b1?.let { value -> b2?.and(value) }
            ?: false
}

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)

    private val repository: SweepingReminderRepository

    val schedules: LiveData<List<SweepingSchedule>>
    val hasSchedules: LiveData<Boolean>

    // Notification
    private val _notificationTime: MutableLiveData<LocalTime>
    val notificationTime: LiveData<LocalTime>
        get() = _notificationTime
    private val _notificationSwitchIsChecked: MutableLiveData<Boolean>
    val notificationSwitchIsChecked: LiveData<Boolean>
        get() = _notificationSwitchIsChecked
    private val _showNotificationViews: DoubleBooleanLiveData
    val showNotificationViews: LiveData<Boolean>
        get() = _showNotificationViews
    private val _checkedNotificationRadioButton: MutableLiveData<Int>
    val checkedNotificationRadioButton: LiveData<Int>
        get() = _checkedNotificationRadioButton
    val notificationTimeString: LiveData<String>

    // Alarm
    private val _alarmTime: MutableLiveData<LocalTime>
    val alarmTime: LiveData<LocalTime>
        get() = _alarmTime
    val alarmTimeString: LiveData<String>
    private val _alarmSwitchIsChecked: MutableLiveData<Boolean>
    val alarmSwitchIsChecked: LiveData<Boolean>
        get() = _alarmSwitchIsChecked
    private val _showAlarmViews: DoubleBooleanLiveData
    val showAlarmViews: LiveData<Boolean>
        get() = _showAlarmViews
    private val _checkedAlarmRadioButton: MutableLiveData<Int>
    val checkedAlarmRadioButton: LiveData<Int>
        get() = _checkedAlarmRadioButton

    val nextSweepingDateString: LiveData<String>

    init {
        // Initialize properties
        val sweepingDao = SweepingReminderDatabase.getDatabase(application).sweepingScheduleDao()
        repository = SweepingReminderRepository.getInstance(sweepingDao)
        schedules = repository.schedules

        // Notification values initialization
        val notificationHour = sharedPreferences.getInt(NOTIFICATION_HOUR_KEY, 12)
        val notificationMinute = sharedPreferences.getInt(NOTIFICATION_MINUTE_KEY, 0)
        val notificationTime: LocalTime = LocalTime.of(notificationHour, notificationMinute, 0)
        _notificationTime = MutableLiveData(notificationTime)

        val notificationSwitchIsChecked = sharedPreferences.getBoolean(NOTIFICATION_SWITCH_IS_CHECKED_KEY, false)
        _notificationSwitchIsChecked = MutableLiveData(notificationSwitchIsChecked)

        val notificationRB = sharedPreferences.getInt(NOTIFICATION_RADIO_BUTTON_CHECKED_KEY, NOTIFICATION_RADIO_BUTTON_DAY_BEFORE)
        _checkedNotificationRadioButton = MutableLiveData(notificationRB)

        val alarmRB = sharedPreferences.getInt(ALARM_RADIO_BUTTON_CHECKED_KEY, ALARM_RADIO_BUTTON_DAY_OF)
        _checkedAlarmRadioButton = MutableLiveData(alarmRB)

        notificationTimeString = Transformations.map(_notificationTime) {
            val format = if (DateFormat.is24HourFormat(application)) "H:mm" else "h:mm a"
            it.format(DateTimeFormatter.ofPattern(format))
        }

        // Alarm values initialization
        val alarmHour = sharedPreferences.getInt(ALARM_HOUR_KEY, 12)
        val alarmMinute = sharedPreferences.getInt(ALARM_MINUTE_KEY, 0)
        val alarmTime = LocalTime.of(alarmHour, alarmMinute, 0)
        _alarmTime = MutableLiveData(alarmTime)

        val alarmSwitchIsChecked = sharedPreferences.getBoolean(ALARM_SWITCH_IS_CHECKED_KEY, false)
        _alarmSwitchIsChecked = MutableLiveData(alarmSwitchIsChecked)

        alarmTimeString = Transformations.map(_alarmTime) {
            val format = if (DateFormat.is24HourFormat(application)) "H:mm" else "h:mm a"
            it.format(DateTimeFormatter.ofPattern(format))
        }

        nextSweepingDateString = Transformations.map(schedules) { schedules ->
            generateNextSweepDateString(application.applicationContext, schedules)
        }

        hasSchedules = Transformations.map(schedules) { schedules ->
            // When the schedules change, check to see if notifications or alarms need to be rescheduled
            if (schedules.isNotEmpty()) {
                if (_notificationSwitchIsChecked.value == true) {
                    rescheduleNotification()
                }
                if (_alarmSwitchIsChecked.value == true) {
                    rescheduleAlarm()
                }
            } else {
                cancelNotification()
                cancelAlarm()
            }

            schedules.isNotEmpty()
        }

        _showNotificationViews = DoubleBooleanLiveData(hasSchedules, _notificationSwitchIsChecked)

        _showAlarmViews = DoubleBooleanLiveData(hasSchedules, _alarmSwitchIsChecked)
    }


    private fun generateNextSweepDateString(context: Context, schedules: List<SweepingSchedule>): String {

        val application = getApplication<CustomApplication>()


        return if (schedules.isEmpty()) {
            application.getString(R.string.not_available)
        } else {
            val today = LocalDate.now()
            val nextSweepingDate = nextSweepingDateFrom(today, schedules)
            val daysUntilNextSweepingDate = ChronoUnit.DAYS.between(today, nextSweepingDate).toInt()

            val daysUntil = if (daysUntilNextSweepingDate == 0) {
                application.getString(R.string.today_uppercase)
            } else {
                application.resources.getQuantityString(R.plurals.numberOfDays, daysUntilNextSweepingDate, daysUntilNextSweepingDate)
            }

            val month = when (nextSweepingDate.month) {
                Month.JANUARY -> context.getString(R.string.january)
                Month.FEBRUARY -> context.getString(R.string.february)
                Month.MARCH -> context.getString(R.string.march)
                Month.APRIL -> context.getString(R.string.april)
                Month.MAY -> context.getString(R.string.may)
                Month.JUNE -> context.getString(R.string.june)
                Month.JULY -> context.getString(R.string.july)
                Month.AUGUST -> context.getString(R.string.august)
                Month.SEPTEMBER -> context.getString(R.string.september)
                Month.OCTOBER -> context.getString(R.string.october)
                Month.NOVEMBER -> context.getString(R.string.november)
                Month.DECEMBER -> context.getString(R.string.december)
                else -> "ERROR"
            }

            "$month ${nextSweepingDate.dayOfMonth} ($daysUntil)"
        }
    }

    private fun scheduleNotification() {

        schedules.value?.let { schedules ->

            notificationTime.value?.let { notificationTime ->

                // Have to check to see if the notification date and time are before the current date and time.
                // For example, if the reminder is set to the "day of" at 12:00 PM and this method is called
                // on the day of the sweeping after 12:00 PM then the Alarm Manager will instantly show the
                // notification.

                val nextSweepingDate = nextSweepingDateFrom(LocalDate.now(), schedules)

                val dayOf = checkedNotificationRadioButton.value == NOTIFICATION_RADIO_BUTTON_DAY_OF
                val notificationDate = if (dayOf) nextSweepingDate else nextSweepingDate.minusDays(1)

                val possibleNotificationDateTime = notificationDate.atTime(notificationTime)
                val now = LocalDateTime.now()

                val actualNotificationDateTime = if (possibleNotificationDateTime.isAfter(now)) {
                    possibleNotificationDateTime
                } else {
                    // Calculate the sweeping date after the current one and calculate a new notification date/time.
                    val tempBaseDate = nextSweepingDate.plusDays(1)
                    val tempSweepingDate = nextSweepingDateFrom(tempBaseDate, schedules)
                    val tempNotificationDate = if (dayOf) tempSweepingDate else tempSweepingDate.minusDays(1)
                    tempNotificationDate.atTime(notificationTime)
                }


                val appContext = getApplication<CustomApplication>().applicationContext
                val notificationPendingIntent = getReminderNotificationPendingIntent(appContext, dayOf)

                val notificationTriggerMillis = localDateTimeToMillis(actualNotificationDateTime)

                val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTriggerMillis,
                            notificationPendingIntent)
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            notificationTriggerMillis,
                            notificationPendingIntent)
                }
            }
        }
    }

    private fun scheduleAlarm() {

        schedules.value?.let { schedules ->

            alarmTime.value?.let { alarmTime ->

                // Have to check to see if the alarm date and time are before the current date and time.
                // For example, if the reminder is set to the "day of" at 12:00 PM and this method is called
                // on the day of the sweeping after 12:00 PM then the Alarm Manager will instantly show the
                // notification.

                val nextSweepingDate = nextSweepingDateFrom(LocalDate.now(), schedules)

                val dayOf = checkedAlarmRadioButton.value == ALARM_RADIO_BUTTON_DAY_OF
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

                val appContext = getApplication<CustomApplication>().applicationContext
                val alarmPendingIntent = getAlarmPendingIntent(appContext, dayOf)

                val alarmTriggerMillis = localDateTimeToMillis(actualAlarmDateTime)

                val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
    }

    private fun cancelNotification() {
        // How to cancel:
        // https://stackoverflow.com/questions/11681095/cancel-an-alarmmanager-pendingintent-in-another-pendingintent

        val appContext = getApplication<CustomApplication>().applicationContext

        val dayOf = checkedNotificationRadioButton.value == NOTIFICATION_RADIO_BUTTON_DAY_OF

        val notificationPendingIntent = getReminderNotificationPendingIntent(appContext, dayOf).apply {
            cancel()
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(notificationPendingIntent)

    }

    private fun cancelAlarm() {
        // How to cancel:
        // https://stackoverflow.com/questions/11681095/cancel-an-alarmmanager-pendingintent-in-another-pendingintent

        val appContext = getApplication<CustomApplication>().applicationContext

        val dayOf = checkedNotificationRadioButton.value == NOTIFICATION_RADIO_BUTTON_DAY_OF

        val alarmPendingIntent = getAlarmPendingIntent(appContext, dayOf).apply {
            cancel()
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent)
    }

    private fun rescheduleNotification() {
        cancelNotification()
        scheduleNotification()
    }

    private fun rescheduleAlarm() {
        cancelAlarm()
        scheduleAlarm()
    }

    fun delete(sweepingSchedule: SweepingSchedule) = viewModelScope.launch {
        repository.delete(sweepingSchedule)
    }

    fun notificationSwitchChanged(isChecked: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(NOTIFICATION_SWITCH_IS_CHECKED_KEY, isChecked)
            apply()
        }
        _notificationSwitchIsChecked.value = isChecked

        if (isChecked) {
            scheduleNotification()

            // Enabled the boot broadcast receiver
            val context = getApplication<CustomApplication>()
            val receiver = ComponentName(context, NotificationBootReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            )

        } else {
            cancelNotification()

            // Disable the reboot broadcast receiver
            val context = getApplication<CustomApplication>()
            val receiver = ComponentName(context, NotificationBootReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            )
        }
    }

    fun notificationTimeChanged(newHour: Int, newMinute: Int) {
        with(sharedPreferences.edit()) {
            putInt(NOTIFICATION_HOUR_KEY, newHour)
            putInt(NOTIFICATION_MINUTE_KEY, newMinute)
            apply()
        }

        _notificationTime.value = LocalTime.of(newHour, newMinute, 0)

        rescheduleNotification()
    }

    fun alarmTimeChanged(newHour: Int, newMinute: Int) {
        with(sharedPreferences.edit()) {
            putInt(ALARM_HOUR_KEY, newHour)
            putInt(ALARM_MINUTE_KEY, newMinute)
            apply()
        }

        _alarmTime.value = LocalTime.of(newHour, newMinute, 0)

        rescheduleAlarm()
    }

    fun notificationRadioButtonChanged(checkedRadioButton: Int) {
        with(sharedPreferences.edit()) {
            putInt(NOTIFICATION_RADIO_BUTTON_CHECKED_KEY, checkedRadioButton)
            apply()
        }

        _checkedNotificationRadioButton.value = checkedRadioButton

        rescheduleNotification()
    }

    fun alarmRadioButtonChanged(checkedRadioButton: Int) {
        with(sharedPreferences.edit()) {
            putInt(ALARM_RADIO_BUTTON_CHECKED_KEY, checkedRadioButton)
            apply()
        }

        _checkedAlarmRadioButton.value = checkedRadioButton

        rescheduleAlarm()
    }

    fun alarmSwitchChanged(isChecked: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(ALARM_SWITCH_IS_CHECKED_KEY, isChecked)
            apply()
        }

        _alarmSwitchIsChecked.value = isChecked

        if (isChecked) {
            scheduleAlarm()

            // Enabled the boot broadcast receiver
            val context = getApplication<CustomApplication>()
            val receiver = ComponentName(context, AlarmBootReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            )
        } else {
            cancelAlarm()

            // Disable the reboot broadcast receiver
            val context = getApplication<CustomApplication>()
            val receiver = ComponentName(context, AlarmBootReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            )
        }
    }
}