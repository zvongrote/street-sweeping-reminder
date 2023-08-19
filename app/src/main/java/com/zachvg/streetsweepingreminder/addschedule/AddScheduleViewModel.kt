package com.zachvg.streetsweepingreminder.addschedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zachvg.streetsweepingreminder.database.SweepingReminderDatabase
import com.zachvg.streetsweepingreminder.database.SweepingReminderRepository
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek

class AddScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SweepingReminderRepository
    var dayOfWeek = DayOfWeek.MONDAY
    val weeksOfMonth = mutableListOf(true, false, true, false, false)

    init {
        val sweepingDao = SweepingReminderDatabase.getDatabase(application).sweepingScheduleDao()
        repository = SweepingReminderRepository.getInstance(sweepingDao)
    }

    fun insert(sweepingSchedule: SweepingSchedule) = viewModelScope.launch {
        repository.insert(sweepingSchedule)
    }

    fun selectEveryWeek() {
        weeksOfMonth.clear()
        weeksOfMonth.addAll(listOf(true, true, true, true, true))
    }

}