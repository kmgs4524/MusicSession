package com.york.android.musicsession.boradcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by York on 2018/4/21.
 */
class MusicReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("MusicReceiver", "intent: ${intent}")
    }
}