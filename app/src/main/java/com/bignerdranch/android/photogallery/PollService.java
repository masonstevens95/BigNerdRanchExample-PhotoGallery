package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mason on 2/5/2018.
 */

public class PollService extends IntentService {
    private static final String TAG = "PollService";

    //Set interval to 15 min
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15);

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.bignerdranch.android.photgallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    //starts PollService
    //Takes in context to send intent, request code to distinguish this pendingintent,
        //Intent to send, and and flags to tweak how pendingIntent is created
    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        //sets alarm or cancels it
        //4 params: const describes time basis for alarm, time to start alarm,
            //time interval to repeat alarm, and pendingintent when alarm goes off
        if (isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        //stores alarm setting in shared preferences
        QueryPreferences.setAlarmOn(context, isOn);
    }

    //uses PendingIntent.FLAG_NO_CREATE to tell if alarm is on or not
    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if (!isNetworkAvailableAndConnected()){
            return;
        }
        //pulls out current query and last result ID from Shared Preferences
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        //Fetches latest result with FF
        //If results, grab first
        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos();
        }else {
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size() == 0){
            return;
        }

        //check to see if it is different from last result ID
        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)){
            Log.i(TAG, "Got an old result: " + resultId);
        }else{
            Log.i(TAG, "Got a new result: " + resultId);

            //notifies a new result is ready with notification
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            //actual notification
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            //makes a notification
            showBackgroundNotification(0, notification);
        }

        //Store first result back to Shared Preferences
        QueryPreferences.setLastResultId(this, resultId);
    }

    //sends broadcast intent to show notification
    //PERM_PRIVATE ensures that other programs need permission to see the broadcast
    private void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        /*
        Params: result receiver, handler to run result receiver on, init values for the result code,
        result data, result extras for ordered broadcast
         */
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
