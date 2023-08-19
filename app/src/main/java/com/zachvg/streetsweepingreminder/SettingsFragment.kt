package com.zachvg.streetsweepingreminder

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

/*
Preferences are displayed using the Jetpack settings library:
https://developer.android.com/guide/topics/ui/settings
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}