package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Mason on 2/8/2018.
 * hides foreground notifications
 */

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    //passes intent filter
    //PERM_PRIVATE ensures noone can create a receiver to hear our broadcast
    @Override
    public void onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,
                PollService.PERM_PRIVATE, null);
    }

    //cleans up dynamically registered broadcast receivers
    @Override
    public void onStop(){
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if we receive this, were visible, so cancel the notification
            Log.i(TAG, "cancelling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
