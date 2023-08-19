package com.zachvg.streetsweepingreminder.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.zachvg.streetsweepingreminder.*
import com.zachvg.streetsweepingreminder.activity.MainActivity

/*
This is a foreground service that will shown an activity if the screen is off,
and a heads up notification if the user is doing something.
 */
class AlarmService : Service(), MediaPlayer.OnPreparedListener {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var wakelock: PowerManager.WakeLock

    private val dismissAlarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DISMISS_ALARM) {
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private lateinit var timer: CountDownTimer

    override fun onCreate() {

        val timerLengthMinutes = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.alarm_timeout_key), "5")
                ?.toInt() ?: 5

        val timerLengthMillis = timerLengthMinutes * 60_000L

        // Hold the wakelock 1 second longer than the alarm silence setting just in case there needs to be cleanup.
        // Not sure if the extra second is needed, but seems like a good idea to me.
        wakelock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ALARM_SERVICE_WAKELOCK_TAG).apply {
                acquire(timerLengthMillis + 1_000L)
            }
        }

        // Register the receiver without having to declare it in the manifest.
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_DISMISS_ALARM)
        }
        registerReceiver(dismissAlarmReceiver, intentFilter)

        mediaPlayer = MediaPlayer().apply {
            // Sets the volume to be based on the alarm volume setting instead of media volume setting.
            setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build())

            val alarmURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            setDataSource(this@AlarmService, alarmURI)
            isLooping = true
            setOnPreparedListener(this@AlarmService)
            prepareAsync()
        }

        // End the alarm after the amount of minutes set in the settings
        // A notification is shown if the alarm is dismissed once the timer runs out
        // in case the user didn't see it
        timer = object : CountDownTimer(timerLengthMillis, timerLengthMillis) {
            override fun onFinish() {
                val title = getString(R.string.street_sweeping_reminder)
                val content = getString(R.string.missed_alarm_content)
                val tapIntent = Intent(this@AlarmService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                val tapPendingIntent = PendingIntent.getActivity(this@AlarmService, MISSED_ALARM_NOTIFICATION_REQUEST_CODE, tapIntent, flags)
                val missedAlarmNotification = NotificationCompat.Builder(this@AlarmService, ALARM_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setContentIntent(tapPendingIntent)
                        .setAutoCancel(true)
                        .build()
                NotificationManagerCompat.from(this@AlarmService).notify(MISSED_ALARM_NOTIFICATION_ID, missedAlarmNotification)

                val dismissIntent = Intent(ACTION_DISMISS_ALARM)
                sendBroadcast(dismissIntent)
            }

            override fun onTick(millisUntilFinished: Long) {
                // Don't need to do anything here
            }

        }

        timer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fullScreenIntent = Intent(this, AlarmActivity::class.java)
        var pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_UPDATE_CURRENT
        val fullScreenPendingIntent =
                PendingIntent.getActivity(this, FULL_SCREEN_ALARM_PENDING_INTENT_REQUEST_CODE, fullScreenIntent, pendingIntentFlags)

        val dismissIntent = Intent(ACTION_DISMISS_ALARM)
        pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val dismissPendingIntent =
                PendingIntent.getBroadcast(this, DISMISS_ALARM_REQUEST_CODE, dismissIntent, pendingIntentFlags)

        val titleString = getString(R.string.street_sweeping_reminder)
        val dismissString = getString(R.string.dismiss)
        val contentString = intent?.getStringExtra(EXTRA_ALARM_CONTENT_TEXT_KEY)
                ?: getString(R.string.notification_content_text_default)

        val notificationBuilder = NotificationCompat.Builder(this, ALARM_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(titleString)
                .setContentText(contentString)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(R.drawable.ic_clear, dismissString, dismissPendingIntent)

        val alarmNotification = notificationBuilder.build()

        startForeground(ALARM_NOTIFICATION_ID, alarmNotification)

        val scheduleNextAlarmIntent = Intent(ACTION_SCHEDULE_NEXT_ALARM)
        ScheduleNextAlarmService.enqueueWork(this, scheduleNextAlarmIntent)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(dismissAlarmReceiver)
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()

        timer.cancel()

        wakelock.release()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }
}
