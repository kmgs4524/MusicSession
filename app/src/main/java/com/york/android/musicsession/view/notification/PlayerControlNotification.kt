package com.york.android.musicsession.view.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BitmapCompression
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.MainActivity

/**
 * Created by York on 2018/4/16.
 */
class PlayerControlNotification(val context: Context, val songs: List<Song>, var currentWindowIndex: Int) {
    val PLAYER_CONTROL_ID = 1243

    fun show() {
        val remoteView = RemoteViews(context.packageName, R.layout.playercontrol_notification)
        var largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.no_cover)
        if(songs.size != 0) {
            largeIconBitmap = BitmapCompression.compressBySize(songs[currentWindowIndex].coverImageUrl, 32, 32)
        }
        val builder = NotificationCompat.Builder(context)
                .setContentTitle("音樂播放")
                .setContentText("正在播放歌曲")
                .setSmallIcon(R.drawable.album_cover)
                .setLargeIcon(largeIconBitmap)
                .setColor(context.resources.getColor(R.color.transparent))  // set small icon's background color
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        PendingIntent.getActivity(context, 0, notificationIntent, 0)

        // set action and style of builder
        val playAction = NotificationCompat.Action(R.drawable.ic_play, "PLAY_ACTION", resultPendingIntent)
        val previousAction = NotificationCompat.Action(R.drawable.back_24dp, "PREVIOUS_ACTION", resultPendingIntent)
        val nextAction = NotificationCompat.Action(R.drawable.next_24dp, "NEXT_ACTION", resultPendingIntent)
        builder.addAction(previousAction)
                .addAction(playAction)
                .addAction(nextAction)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))


        builder.setContentIntent(resultPendingIntent)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(PLAYER_CONTROL_ID, builder.build())
    }
}