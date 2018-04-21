package com.york.android.musicsession.boradcast;

import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by York on 2018/4/21.
 */

public class SetBroadcastReceiver {
    public void setReceiver(Context context, MusicReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        context.registerReceiver(receiver, filter);
    }
}
