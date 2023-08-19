package com.zachvg.streetsweepingreminder.database

import androidx.lifecycle.LiveData

class SweepingReminderRepository private constructor(private val sweepingDao: SweepingScheduleDao) {

    val schedules = sweepingDao.getAllSchedules()

    suspend fun insert(sweepingSchedule: SweepingSchedule) {
        sweepingDao.insert(sweepingSchedule)
    }

    suspend fun delete(sweepingSchedule: SweepingSchedule) {
        sweepingDao.delete(sweepingSchedule)
    }

    suspend fun update(sweepingSchedule: SweepingSchedule) {
        sweepingDao.update(sweepingSchedule)
    }

    fun getSchedule(scheduleId: Int): LiveData<SweepingSchedule> {
        return sweepingDao.getSchedule(scheduleId)
    }

    fun getAllSchedulesAsList(): List<SweepingSchedule> {
        return sweepingDao.getAllSchedulesNonLiveData()
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: SweepingReminderRepository? = null

        fun getInstance(sweepingScheduleDao: SweepingScheduleDao) =
                instance ?: synchronized(this) {
                    instance
                            ?: SweepingReminderRepository(sweepingScheduleDao).also { instance = it }
                }
    }
}