package com.zachvg.streetsweepingreminder.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.zachvg.streetsweepingreminder.ACTION_DISMISS_ALARM
import com.zachvg.streetsweepingreminder.R
import kotlinx.android.synthetic.main.activity_alarm.*

/*
Shown only when the screen is off. The alarm service handles playing the sound.
 */
class AlarmActivity : AppCompatActivity() {

    // Dismisses the alarm activity. The broadcast can originate from either this activity,
    // or the alarm service.
    private val dismissAlarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DISMISS_ALARM) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        // Register a broadcast receiver without having to declare it in the manifest.
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_DISMISS_ALARM)
        }
        registerReceiver(dismissAlarmReceiver, intentFilter)

        // Option to wake the screen up and keep it on while.
        // The depreciated values have to be used pre API 27.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Dismisses the keyguard if needed so the activity can be shown.
        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@AlarmActivity, null)
            }
        }

        // Close dialogs and window shade, so the activity is fully visible.
//        sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

        floatingActionButton.setOnClickListener {
            sendBroadcast(Intent(ACTION_DISMISS_ALARM))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(dismissAlarmReceiver)
    }
}