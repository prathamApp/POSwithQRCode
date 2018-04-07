package com.example.pef.prathamopenschool;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


// Notification Broadcast

public class AlarmReceiverPM extends BroadcastReceiver {

    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("rececive::", "called");
        Intent servicePM = new Intent(context, AlarmServicePM.class);
        context.startService(servicePM);
    }
}