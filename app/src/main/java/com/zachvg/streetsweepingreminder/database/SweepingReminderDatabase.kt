package com.zachvg.streetsweepingreminder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
Make sure that the schemas get correctly exported when the version number changes.
See the "export schemas" section here:
https://developer.android.com/training/data-storage/room/migrating-db-versions#export-schema
 */
@Database(entities = [SweepingSchedule::class], version = 1)
@TypeConverters(Converters::class)
abstract class SweepingReminderDatabase : RoomDatabase() {

    abstract fun sweepingScheduleDao(): SweepingScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: SweepingReminderDatabase? = null

        fun getDatabase(context: Context): SweepingReminderDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        SweepingReminderDatabase::class.java,
                        "schedule_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}