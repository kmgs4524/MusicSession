package com.york.android.musicsession.view.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.york.android.musicsession.R
import com.york.android.musicsession.boradcast.MusicReceiver
import com.york.android.musicsession.model.bitmap.BitmapCompression
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.view.MainActivity

/**
 * Created by York on 2018/4/16.
 */
class PlayerNotificationBuilder(val context: Context, val song: Song) {
    val PLAYER_CONTROL_ID = 1243

    fun show(): Notification {
        val remoteView = RemoteViews(context.packageName, R.layout.playercontrol_notification)
        var largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.no_cover)
//        if(songs.size != 0) {
        largeIconBitmap = BitmapCompression.compressBySize(song.coverImageUrl, 32, 32)
//        }
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

        // PendingIntent that starts Activity which holds PlayerControlFragment
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // create play, pause intents
        val playIntent = Intent()
        val pauseIntent = Intent()
        // set intents'action and handler component
        playIntent.setAction("ACTION_PLAY_MUSIC")
        playIntent.setClass(context, MusicReceiver::class.java)
        pauseIntent.setClass(context, MusicReceiver::class.java)   // set the component to handle the intent
        pauseIntent.setAction("ACTION_PAUSE_MUSIC")
        // puase intent
        val pendingIntent = PendingIntent.getBroadcast(context, 958, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val previousAction = NotificationCompat.Action(R.drawable.back_24dp, "PREVIOUS_ACTION", pendingIntent)
        val nextAction = NotificationCompat.Action(R.drawable.next_24dp, "NEXT_ACTION", pendingIntent)
        val playAction = NotificationCompat.Action(R.drawable.exo_controls_play, "PLAY_ACTION", pendingIntent)

        builder.addAction(previousAction)
                .addAction(playAction)
                .addAction(nextAction)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))

        builder.setContentIntent(resultPendingIntent)   // supply a pending intent when notification is clicked
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = builder.build()
//        notificationManager.notify(PLAYER_CONTROL_ID, builder.build())

        return notification
    }
}