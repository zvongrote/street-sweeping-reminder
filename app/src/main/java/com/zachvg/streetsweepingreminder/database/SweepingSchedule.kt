package com.zachvg.streetsweepingreminder.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.DayOfWeek

/*
Holds a street sweeping schedule which includes:
1) Name
2) Day  (Monday - Friday)
3) List of the weeks of the month (1 - 5)
 */

@Entity(tableName = "schedule_table")
data class SweepingSchedule(var name: String = "No Name",
                            var day: DayOfWeek = DayOfWeek.MONDAY,
                            val dayNumbersOfMonth: MutableList<Int> = mutableListOf(1, 3)) {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    val everyWeek: Boolean
        get() = dayNumbersOfMonth.containsAll(listOf(1, 2, 3, 4, 5))
}