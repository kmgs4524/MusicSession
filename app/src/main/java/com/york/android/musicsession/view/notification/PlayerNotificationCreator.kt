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
                                val controller: MediaControllerCompat) {
    val PLAYER_CONTROL_ID = 1243

    val builder = NotificationCompat.Builder(context)

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

    init {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        // PendingIntent that starts Activity which holds PlayerControlFragment
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // intent that start specified activity normally
        val intent = Intent()
        intent.setClass(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(context, 513, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // test code
//        val keyCode = PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PLAY_PAUSE)
//        Log.d("mediaButtonEvent", "keyCode: ${keyCode} KeyEvent.KEYCODE_MEDIA_PLAY: ${KeyEvent.KEYCODE_MEDIA_PLAY}")
//        var mediaButtonEvent = Intent()
//        mediaButtonEvent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))

        builder.addAction(prevAction)
                .addAction(pauseAction)
                .addAction(nextAction)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(0, 1, 2))
                .setContentIntent(activityPendingIntent) // supply a pending intent when notification is clicked
        val notification = builder.build()
    }

    fun setMetadata(metadata: MediaMetadataCompat) {
        var largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.no_cover)
        if (metadata.getString("ALBUM_ARTWORK") != "") {
            largeIconBitmap = BitmapCompression.compressBySize(metadata.getString("ALBUM_ARTWORK"), 100, 100)
        }
        builder.setContentTitle(metadata.getString("SONG_NAME"))
                .setContentText(metadata.getString("ARTIST_NAME"))
                .setSmallIcon(R.drawable.album_cover)
                .setLargeIcon(largeIconBitmap)
                .setColor(context.resources.getColor(R.color.transparent))  // set small icon's background color
                .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun setState(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            builder.mActions[1] = pauseAction
        } else {
            builder.mActions[1] = playAction
        }
    }

    fun create(): Notification {
        return builder.build()
    }
}