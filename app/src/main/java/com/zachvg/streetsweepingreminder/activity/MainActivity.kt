package com.zachvg.streetsweepingreminder.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.zachvg.streetsweepingreminder.AD_DISABLED_DIALOG_TAG
import com.zachvg.streetsweepingreminder.AdDisabledDialogFragment
import com.zachvg.streetsweepingreminder.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

/*
This activity holds the navigation fragment and the ad.
 */
class MainActivity : AppCompatActivity() {

    private var adIsInitialized = false

    private val adCoroutineScope = CoroutineScope((Dispatchers.Main))
    private var adJob: Job? = null

    private lateinit var defaultSharedPreferences: SharedPreferences

    private val sharedPreferencesChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            getString(R.string.settings_ads_key) -> {
                val shouldShowAds = sharedPreferences?.getBoolean(key, true) ?: true
                if (shouldShowAds) {
                    if (adIsInitialized) {
                        // The calls to resume and pause might be causing problems with the ad service suspending my account
                        // I'm going to try and comment them out for the next release
                        // Commenting it out will just hide the ad instead of changing its state I think
                        // adView.resume()
                        adView.visibility = View.VISIBLE
                    } else {
                        adJob = initializeAds()
                    }
                } else {
                    // adView.pause()
                    adView.visibility = View.GONE

                    // Ask the user to consider leaving ads enabled
                    AdDisabledDialogFragment().show(supportFragmentManager, AD_DISABLED_DIALOG_TAG)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the toolbar with navigation support
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // AdMob support
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
                adIsInitialized = true
            }
        }

        val shouldShowAds = defaultSharedPreferences.getBoolean(getString(R.string.settings_ads_key), true)

        if (shouldShowAds) {
            adJob = initializeAds()
        } else {
            // Hide the ad view
            adView.visibility = View.GONE
        }
    }

    override fun onResume() {
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesChangedListener)

        if (adIsInitialized) {
            adView.resume()
        }
        super.onResume()
    }

    override fun onPause() {
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesChangedListener)


        if (adIsInitialized) {
            adView.pause()

        }
        super.onPause()
    }

    override fun onDestroy() {
        adJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }

        if (adIsInitialized) {
            adView.destroy()
        }

        super.onDestroy()
    }

    private fun initializeAds() = adCoroutineScope.launch {
        val adRequest = withContext(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity) {}
            AdRequest.Builder().build()
        }

        adView.loadAd(adRequest)
    }
}
