package com.zachvg.streetsweepingreminder.editSchedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zachvg.streetsweepingreminder.database.SweepingReminderDatabase
import com.zachvg.streetsweepingreminder.database.SweepingReminderRepository
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import kotlinx.coroutines.launch

class EditScheduleViewModel(application: Application, scheduleId: Int) : AndroidViewModel(application) {

    private val sweepingDao = SweepingReminderDatabase.getDatabase(application).sweepingScheduleDao()
    private val repository = SweepingReminderRepository.getInstance(sweepingDao)
    val schedule = repository.getSchedule(scheduleId)
    val weeksOfMonth = mutableListOf<Boolean>()

    fun setWeeks(weeks: MutableList<Int>) {
        for (i in 1..5) {
            weeksOfMonth.add(weeks.contains(i))
        }
    }

    fun updateSchedule(sweepingSchedule: SweepingSchedule) = viewModelScope.launch {
        repository.update(sweepingSchedule)
    }

    fun selectEveryWeek() {
        weeksOfMonth.clear()
        weeksOfMonth.addAll(listOf(true, true, true, true, true))
    }
}