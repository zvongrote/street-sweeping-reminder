package com.zachvg.streetsweepingreminder.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SweepingScheduleDao {

    @Insert
    suspend fun insert(schedule: SweepingSchedule)

    @Delete
    suspend fun delete(schedule: SweepingSchedule)

    @Update
    suspend fun update(schedule: SweepingSchedule)

    @Query("SELECT * FROM schedule_table ORDER BY name")
    fun getAllSchedules(): LiveData<List<SweepingSchedule>>

    @Query("SELECT * FROM schedule_table")
    fun getAllSchedulesNonLiveData(): List<SweepingSchedule>

    @Query("SELECT * FROM schedule_table WHERE id = :scheduleId")
    fun getSchedule(scheduleId: Int): LiveData<SweepingSchedule>
}