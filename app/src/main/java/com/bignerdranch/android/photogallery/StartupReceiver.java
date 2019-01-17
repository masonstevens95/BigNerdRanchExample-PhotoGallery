package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Mason on 2/8/2018.
 * registers with the system
 * recevies intents
 * listens for BOOT_COMPLETED action in manifest
 */

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent){
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        //turns alarm on when device is booted up
        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
