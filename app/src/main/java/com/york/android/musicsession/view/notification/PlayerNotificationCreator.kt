package com.york.android.musicsession.view.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.util.Log
import android.view.KeyEvent
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BitmapCompression
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.view.MainActivity

/**
 * Created by York on 2018/4/16.
 */
class PlayerNotificationCreator(val context: Context, val sessionToken: MediaSessionCompat.Token,
                                val controller: MediaControllerCompat, val metadataCompat: MediaMetadataCompat) {
    val PLAYER_CONTROL_ID = 1243

    fun create(): Notification {
//        val remoteView = RemoteViews(context.packageName, R.layout.playercontrol_notification)
        var largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.no_cover)
        if (metadataCompat.getString("ALBUM_ARTWORK") != "") {
            largeIconBitmap = BitmapCompression.compressBySize(metadataCompat.getString("ALBUM_ARTWORK"), 100, 100)
        }
        val builder = NotificationCompat.Builder(context)
                .setContentTitle(metadataCompat.getString("SONG_NAME"))
                .setContentText(metadataCompat.getString("ARTIST_NAME"))
                .setSmallIcon(R.drawable.album_cover)
                .setLargeIcon(largeIconBitmap)
                .setContentIntent(controller.sessionActivity)
                .setColor(context.resources.getColor(R.color.transparent))  // set small icon's background color
                .setPriority(NotificationCompat.PRIORITY_HIGH)

//        val notificationIntent = Intent(context, MainActivity::class.java)
//        val stackBuilder = TaskStackBuilder.create(context)
//        stackBuilder.addParentStack(MainActivity::class.java)
//        stackBuilder.addNextIntent(notificationIntent)

        // PendingIntent that starts Activity which holds PlayerControlFragment
//        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // create play, pause intents
        val playIntent = Intent()
        val pauseIntent = Intent()
        // set intents'action and currentPositionUpdateHandler component
//        playIntent.setAction("ACTION_PLAY_MUSIC")
//        playIntent.setClass(context,  MainActivity::class.java)
//        pauseIntent.setAction("ACTION_PAUSE_MUSIC")
//        pauseIntent.setClass(context, MainActivity::class.java)   // set the component to handle the intent
//        // puase intent
//        val playPendingIntent = PendingIntent.getActivity(context, (8513), playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        val pausePendingIntent = PendingIntent.getActivity(context, 958, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

//        val previousAction = NotificationCompat.Action(R.drawable.back_24dp, "PREVIOUS_ACTION",
//                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY))
//        val nextAction = NotificationCompat.Action(R.drawable.next_24dp, "NEXT_ACTION", pausePendingIntent)
        val keyCode = PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PLAY_PAUSE)
        Log.d("mediaButtonEvent", "keyCode: ${keyCode} KeyEvent.KEYCODE_MEDIA_PLAY: ${KeyEvent.KEYCODE_MEDIA_PLAY}")
        var mediaButtonEvent = Intent()
        mediaButtonEvent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))

        var playAction = NotificationCompat.Action(R.drawable.exo_controls_play, "PLAY_ACTION",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        ComponentName(context, MediaButtonReceiver::class.java),
                        PlaybackStateCompat.ACTION_PLAY))    // ComponentName(context, MediaButtonReceiver::class.java)
        var pauseAction = NotificationCompat.Action(R.drawable.exo_controls_pause, "PAUSE_ACTION",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        ComponentName(context, MediaButtonReceiver::class.java),
                        PlaybackStateCompat.ACTION_PAUSE))
        var prevAction = NotificationCompat.Action(R.drawable.exo_controls_previous, "PLAY_PREVIOUS_ACTION",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        ComponentName(context, MediaButtonReceiver::class.java),
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
        var nextAction = NotificationCompat.Action(R.drawable.exo_controls_next, "PLAY_NEXT_ACTION",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        ComponentName(context, MediaButtonReceiver::class.java),
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
//        if(state.state == PlaybackStateCompat.STATE_PLAYING) {
//            playAction = NotificationCompat.Action(R.drawable.ic_pause, "PLAY_PAUSE", pausePendingIntent)
//        }

        builder.addAction(prevAction)
                .addAction(playAction)
                .addAction(nextAction)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(0))

//        builder.setContentIntent(resultPendingIntent)   // supply a pending intent when notification is clicked
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = builder.build()
//        notificationManager.notify(PLAYER_CONTROL_ID, builder.create())

        return notification
    }

    fun changeState() {
        val builder = NotificationCompat.Builder(context)
                .setContentTitle(metadataCompat.getString("SONG_NAME"))
                .setContentText(metadataCompat.getString("ARTIST_NAME"))
                .setSmallIcon(R.drawable.album_cover)
                .setLargeIcon(largeIconBitmap)
                .setContentIntent(controller.sessionActivity)
                .setColor(context.resources.getColor(R.color.transparent))  // set small icon's background color
                .setPriority(NotificationCompat.PRIORITY_HIGH)
    }
}