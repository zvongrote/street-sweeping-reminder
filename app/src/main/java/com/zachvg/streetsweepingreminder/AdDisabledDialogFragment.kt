package com.zachvg.streetsweepingreminder

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/*
Dialog fragment asking the user to consider leaving ads enabled after disabling them.
 */

class AdDisabledDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it).apply {
                setTitle(R.string.adDisabledDialogTitle)
                setMessage(R.string.dialog_ad_disabled)
                setPositiveButton(R.string.ok) { dialog, id -> /* Do nothing */ }
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}