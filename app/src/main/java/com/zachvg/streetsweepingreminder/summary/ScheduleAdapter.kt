package com.zachvg.streetsweepingreminder.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zachvg.streetsweepingreminder.R
import com.zachvg.streetsweepingreminder.RecyclerViewClickHandler
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import kotlinx.android.synthetic.main.schedule_list_item.view.*
import org.threeten.bp.DayOfWeek

// Used to automatically animate changes in the recycler view
private class DiffCallback : DiffUtil.ItemCallback<SweepingSchedule>() {
    override fun areItemsTheSame(oldSchedule: SweepingSchedule, newSchedule: SweepingSchedule): Boolean {
        return oldSchedule.id == newSchedule.id
    }

    override fun areContentsTheSame(oldSchedule: SweepingSchedule, newSchedule: SweepingSchedule): Boolean {
        return oldSchedule == newSchedule
    }

}

class ScheduleViewHolder(itemView: View, private val clickHandler: RecyclerViewClickHandler) : RecyclerView.ViewHolder(itemView) {

    private var schedule = SweepingSchedule()
    private val context: Context

    // Sets the fragment to handle the deletion of items from the database.
    // Do this in the constructor so that it is only called once when the view holder
    // is created. A lot of the solutions online suggest assigning a the click listener when binding,
    // but this would cause unnecessary work since the listener never needs to change.
    init {
        itemView.imageButton_delete_schedule.setOnClickListener {
            clickHandler.onRecyclerDeleteClicked(schedule)
        }

        itemView.imageButton_edit_schedule.setOnClickListener {
            clickHandler.onRecyclerEditClicked(schedule.id)
        }

        context = itemView.context
    }

    fun bindTo(bindingSchedule: SweepingSchedule) {
        // Keep track of the schedule the view holder is representing
        schedule = bindingSchedule

        val weeksAndDayText = if (schedule.everyWeek) {
            context.getString(R.string.every, getScheduleDayAsString(schedule.day))
        } else {
            schedule.run {
                // Build the string for the weeks of the month
                // Ex) 1st, 3rd
                val stringBuilder = StringBuilder()
                dayNumbersOfMonth.forEachIndexed { index, i ->
                    when (i) {
                        1 -> stringBuilder.append("1st")
                        2 -> stringBuilder.append("2nd")
                        3 -> stringBuilder.append("3rd")
                        4 -> stringBuilder.append("4th")
                        5 -> stringBuilder.append("5th")
                    }

                    // Add a comma for every element except the last
                    if (index != dayNumbersOfMonth.lastIndex) {
                        stringBuilder.append(",")
                    }

                    // Add a space
                    stringBuilder.append(" ")
                }

                // Add the day after the weeks of the month
                // Ex) 1st, 3rd Tuesday
                stringBuilder.append(getScheduleDayAsString(day))

                stringBuilder.toString()
            }
        }

        // Set all the values
        itemView.text_schedule_name.text = schedule.name
        itemView.text_schedule_weeks_and_day.text = weeksAndDayText
    }
    private fun getScheduleDayAsString(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> context.getString(R.string.monday)
            DayOfWeek.TUESDAY-> context.getString(R.string.tuesday)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.wednesday)
            DayOfWeek.THURSDAY -> context.getString(R.string.thursday)
            DayOfWeek.FRIDAY -> context.getString(R.string.friday)
            else -> "ERROR"
        }
    }
}


class ScheduleAdapter(private val clickHandler: RecyclerViewClickHandler)
    : ListAdapter<SweepingSchedule, ScheduleViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val scheduleItem = LayoutInflater.from(parent.context).inflate(R.layout.schedule_list_item, parent, false)

        return ScheduleViewHolder(scheduleItem, clickHandler)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }
}