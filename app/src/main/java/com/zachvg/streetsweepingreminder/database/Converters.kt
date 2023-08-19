package com.zachvg.streetsweepingreminder.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.DayOfWeek

/*
Uses Gson to convert the data in a sweeping schedule so it can be stored with Room.
 */
class Converters {

    @TypeConverter
    fun mutableListToJson(list: MutableList<SweepingSchedule>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToMutableList(json: String): MutableList<SweepingSchedule> {
        return Gson().fromJson(json, mutableListOf<SweepingSchedule>()::class.java)
    }

    @TypeConverter
    fun dayOfWeeksToJson(day: DayOfWeek): String {
        return Gson().toJson(day)
    }

    @TypeConverter
    fun jsonToDayOfWeek(json: String): DayOfWeek {
        return Gson().fromJson(json, DayOfWeek::class.java)
    }

    @TypeConverter
    fun mutableIntListToJson(list: MutableList<Int>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToMutableIntList(json: String): MutableList<Int> {
        val collectionType = object : TypeToken<MutableList<Int>>() {}.type
        return Gson().fromJson(json, collectionType)
    }
}