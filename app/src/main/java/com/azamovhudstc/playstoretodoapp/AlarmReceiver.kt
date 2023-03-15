package com.azamovhudstc.playstoretodoapp

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import java.sql.Time
import java.util.*


class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = "notification-id"
        const val NOTIFICATION = "notification"
        const val NOTIFICATION_CHANNEL_ID = "10001"
        const val default_notification_channel_id = "default"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, Time(calendar.timeInMillis).hours)
        calendar.set(Calendar.MINUTE, Time(calendar.timeInMillis).minutes)

        var isRuning = false
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val l: MutableList<RunningAppProcessInfo> =
            activityManager.runningAppProcesses

        for (info in l) {
            if (info.uid == context.applicationInfo.uid && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                isRuning = true
            }
        }
        if (!isRuning) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification: Notification? = intent!!.getParcelableExtra(NOTIFICATION) as Notification?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "NOTIFICATION_CHANNEL_NAME",
                    importance
                )
                assert(notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val id = intent.getIntExtra(NOTIFICATION_ID, 0)
            assert(notificationManager != null)
            notificationManager.notify(id, notification)
            var media =MediaPlayer.create(context,R.raw.vibrate)
            media.start()
        }
    }

}



