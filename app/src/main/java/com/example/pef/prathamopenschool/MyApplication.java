package com.example.pef.prathamopenschool;

/**
 * Created by PEF on 13/06/2017.
 */


// MHM
// Class for Checking internet connection

import android.app.Application;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MyApplication extends Application {

    private static MyApplication mInstance;
    static int count, gpsFixCount;
    static Timer gpsTimer, gpsFixTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }


    public static void startTimer() {
        gpsTimer = new Timer();
        gpsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                count++;
            }
        }, 1000, 1000);
    }

    public static void resetTimer() {
        if (gpsTimer != null) {
            gpsTimer.cancel();
            count = 0;
        } else {
            count = 00;
        }
    }

    public static int getTimerCount() {
        return count;
    }

    public static String getAccurateTimeStamp() {
        // String to Date
        StatusDBHelper statusDBHelper = new StatusDBHelper(mInstance);
        String gpsTime = statusDBHelper.getValue("GPSDateTime");
        Date gpsDateTime = null;
        try {
            gpsDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(gpsTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Add Seconds to Gps Date Time
        Calendar addSec = Calendar.getInstance();
        addSec.setTime(gpsDateTime);
        addSec.add(addSec.SECOND, getTimerCount());
        String updatedTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(addSec.getTime());

        return updatedTime;
    }

    public static String getAccurateDate() {
        // String to Date
        StatusDBHelper statusDBHelper = new StatusDBHelper(mInstance);
        String gpsTime = statusDBHelper.getValue("GPSDateTime");
        Date gpsDateTime = null;
        try {
            gpsDateTime = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(gpsTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Add Seconds to Gps Date Time
        Calendar addSec = Calendar.getInstance();
        addSec.setTime(gpsDateTime);
        addSec.add(addSec.SECOND, getTimerCount());
        String updatedTime = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(addSec.getTime());

        return updatedTime;
    }

    public static void startGPSFixTimer() {
        gpsFixTimer = new Timer();
        gpsFixTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gpsFixCount++;
            }
        }, 1000, 1000);
    }
    public static void resetGPSFixTimer() {
        if (gpsFixTimer != null) {
            gpsFixTimer.cancel();
            gpsFixCount = 0;
        } else {
            gpsFixCount = 00;
        }
    }
    public static int getGPSFixTimerCount() {
        return gpsFixCount;
    }


}