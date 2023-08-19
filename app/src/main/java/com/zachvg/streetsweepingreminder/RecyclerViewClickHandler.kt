package com.zachvg.streetsweepingreminder

import com.zachvg.streetsweepingreminder.database.SweepingSchedule

/*
Used for letting the fragment handle click events for views inside the
view holders of the recycler view.
 */
interface RecyclerViewClickHandler {
    fun onRecyclerDeleteClicked(schedule: SweepingSchedule)

    fun onRecyclerEditClicked(scheduleId: Int)
}