package com.york.android.exomusicplayer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.os.Binder
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log


class PlayService : Service() {
    val ONGOING_NOTIFICATION_ID = 1

    override fun onBind(intent: Intent): IBinder? {
        Log.d("PlayService", "onBind: ")
        return LocalBinder()
    }

    override fun onCreate() {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
        val notification = Notification.Builder(applicationContext)
                .setContentTitle("音樂播放")
                .setContentText("正在播放歌曲")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.exo_controls_play)
                .build()

        Log.d("PlayService", "onCreate notification: ${notification}")
//        val notification = Notification(R.drawable.exo_controls_play, "音樂播放",
//                System.currentTimeMillis())

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PlayService", "onStartCommand intent: ${intent}")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("PlayService", "onUnBind: intent: ${intent}")
        return true
    }

    class LocalBinder: Binder() {

    }

}
