package com.example.pef.prathamopenschool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


// Auto Start AlarmPMService on Boot

public class BootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("PMSBoot :::","Service Started");

        Intent myIntent = new Intent(context, AlarmServicePM.class);
        context.startService(myIntent);

    }
}
