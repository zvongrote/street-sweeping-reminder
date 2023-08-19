package com.zachvg.streetsweepingreminder

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import org.threeten.bp.LocalTime

/*
Displays a time picker when setting up the reminder and alarm.
 */

class TimePickerFragment(private val initialTime: LocalTime, private val timeSetListener: TimePickerDialog.OnTimeSetListener) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
                activity,
                timeSetListener,
                initialTime.hour,
                initialTime.minute,
                DateFormat.is24HourFormat(activity)
        )
    }
}