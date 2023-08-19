package com.zachvg.streetsweepingreminder

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/*
Shows an error message if the user forgets to pick at least one week when
adding or editing a schedule.
 */
class WeekRequiredDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.weeks_dialog_error_message)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        // Do nothing
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}