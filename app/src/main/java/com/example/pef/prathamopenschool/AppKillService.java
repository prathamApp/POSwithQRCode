package com.example.pef.prathamopenschool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AppKillService extends Service {
    StatusDBHelper s;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            // App closed before gpsFixAquired
            if (!MyApplication.gpsFixAquired) {
                s = new StatusDBHelper(getApplicationContext());
                String previousFix = s.getValue("appClosedBeforeGPSFix");
                if (previousFix.length() > 2000) {
                    // reset & reinitialize appClosedBeforeGPSFix if length of record is greater than 2000
                    s.Update("appClosedBeforeGPSFix", "" + MyApplication.getGPSFixTimerCount());
                } else {
                    // gōo on creating records
                    s.Update("appClosedBeforeGPSFix", "" + previousFix + "," + MyApplication.getGPSFixTimerCount());
                }
                BackupDatabase.backup(getApplicationContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}