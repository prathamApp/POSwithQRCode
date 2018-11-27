package com.example.pef.prathamopenschool;

/**
 * Created by PEF on 13/06/2017.
 */


// MHM
// Class for Checking internet connection

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.pef.prathamopenschool.gps.EventBusMSG;

import net.vrallev.android.cat.Cat;

import org.apache.commons.net.ftp.FTPClient;
import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MyApplication extends Application implements GpsStatus.Listener, LocationListener {

    private static MyApplication mInstance;
    static int count, gpsFixCount;
    static Timer gpsTimer, gpsFixTimer;
    static Boolean gpsFixAquired = false;
    public static String networkSSID = "PrathamHotSpot-" + Build.SERIAL;
    public static FTPClient ftpClient;
    private static final int NOT_AVAILABLE = -100000;
    private static final int GPS_DISABLED = 0;
    private static final int GPS_OUTOFSERVICE = 1;
    private static final int GPS_TEMPORARYUNAVAILABLE = 2;
    private static final int GPS_SEARCHING = 3;
    private static final int GPS_STABILIZING = 4;
    private static final int GPS_OK = 5;
    private int GPSStatus = GPS_SEARCHING;
    private LocationManager mlocManager = null;             // GPS LocationManager
    private int _NumberOfSatellites = 0;
    private int _NumberOfSatellitesUsedInFix = 0;

    // Preferences Variables
    private long prefGPSupdatefrequency = 10000L;
    // Singleton instance
    public static Location location;
    public static String ageGrp = "0";


    final Handler gpsunavailablehandler = new Handler();
    Runnable unavailr = new Runnable() {
        @Override
        public void run() {
            if ((GPSStatus == GPS_OK) || (GPSStatus == GPS_STABILIZING)) {
                GPSStatus = GPS_TEMPORARYUNAVAILABLE;
                EventBus.getDefault().post(EventBusMSG.UPDATE_FIX);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        getLocation(this);
    }

    public void getLocation(Context mcontext) {
        mlocManager = (LocationManager) mcontext.getSystemService(Context.LOCATION_SERVICE);     // Location Manager
        if (ContextCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mlocManager.addGpsStatusListener(MyApplication.this);
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefGPSupdatefrequency, 0, MyApplication.this); // Requires Location update
//            mlocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, prefGPSupdatefrequency, 0, MyApplication.this); // Requires Location update
        }
    }


    public void updateSats() {
        try {
            if ((mlocManager != null) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                GpsStatus gs = mlocManager.getGpsStatus(null);
                int sats_inview = 0;    // Satellites in view;
                int sats_used = 0;      // Satellites used in fix;

                if (gs != null) {
                    Iterable<GpsSatellite> sats = gs.getSatellites();
                    for (GpsSatellite sat : sats) {
                        sats_inview++;
                        if (sat.usedInFix()) sats_used++;
                        //Log.w("VisionApplication", "[#] GPSApplication.java - updateSats: i=" + i);
                    }
                    _NumberOfSatellites = sats_inview;
                    _NumberOfSatellitesUsedInFix = sats_used;
                } else {
                    _NumberOfSatellites = NOT_AVAILABLE;
                    _NumberOfSatellitesUsedInFix = NOT_AVAILABLE;
                }
            } else {
                _NumberOfSatellites = NOT_AVAILABLE;
                _NumberOfSatellitesUsedInFix = NOT_AVAILABLE;
            }
        } catch (NullPointerException e) {
            _NumberOfSatellites = NOT_AVAILABLE;
            _NumberOfSatellitesUsedInFix = NOT_AVAILABLE;
            Log.w("VisionApplication", "[#] GPSApplication.java - updateSats: Caught NullPointerException: " + e);
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.d("gps:::", "satellite");
                // TODO: get here the status of the GPS, and save into a GpsStatus to be used for satellites visualization;
                // Use GpsStatus getGpsStatus (GpsStatus status)
                // https://developer.android.com/reference/android/location/LocationManager.html#getGpsStatus(android.location.GpsStatus)
                updateSats();
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                getLocation(getApplicationContext());
                Log.d("onStatusChanged::", "GPS started");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.d("onStatusChanged::", "GPS stopped");
                break;
        }

    }

    @Override
    public void onLocationChanged(Location loc) {
        //if ((loc != null) && (loc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
        if (loc != null) {      // Location data is valid
            //Log.w("VisionApplication", "[#] GPSApplication.java - onLocationChanged: provider=" + loc.getProvider());
            location = loc;
            Log.d("onLocationChanged:", "" + loc.getTime());
            Log.d("onLocationChanged:", "" + loc.getAltitude());
            Log.d("onLocationChanged:", "" + loc.getLatitude());
            Log.d("onLocationChanged:", "" + loc.getLongitude());
            EventBus.getDefault().post(EventBusMSG.UPDATE_TRACK);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                //Log.w("VisionApplication", "[#] GPSApplication.java - GPS Out of Service");
                gpsunavailablehandler.removeCallbacks(unavailr);            // Cancel the previous unavail countdown handler
                GPSStatus = GPS_OUTOFSERVICE;
                Log.d("onStatusChanged::", "GPS Out of Service");
                EventBus.getDefault().post(EventBusMSG.UPDATE_FIX);
                //Toast.makeText( getApplicationContext(), "GPS Out of Service", Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                //Log.w("VisionApplication", "[#] GPSApplication.java - GPS Temporarily Unavailable");
                gpsunavailablehandler.removeCallbacks(unavailr);            // Cancel the previous unavail countdown handler
                GPSStatus = GPS_TEMPORARYUNAVAILABLE;
                Log.d("onStatusChanged::", "GPS Temporarily Unavailable");
                EventBus.getDefault().post(EventBusMSG.UPDATE_FIX);
                //Toast.makeText( getApplicationContext(), "GPS Temporarily Unavailable", Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:
                gpsunavailablehandler.removeCallbacks(unavailr);            // Cancel the previous unavail countdown handler
                Log.d("onStatusChanged::", "GPS Available: " + _NumberOfSatellites + " satellites");
//Log.w("VisionApplication", "[#] GPSApplication.java - GPS Available: " + _NumberOfSatellites + " satellites");
                break;
        }

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("onProviderEnabled::", provider);
        GPSStatus = GPS_SEARCHING;
        EventBus.getDefault().post(EventBusMSG.UPDATE_FIX);
    }

    @Override
    public void onProviderDisabled(String provider) {
        GPSStatus = GPS_DISABLED;
        EventBus.getDefault().post(EventBusMSG.UPDATE_FIX);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void stopLocationUpdate() {
        mlocManager.removeUpdates(MyApplication.this);
    }

    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }

    public static String path = "";

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        MyApplication.path = path;
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


    public static String getVersion() {
        Context context = getAppContext();
        String packageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Cat.e("Unable to find the name " + packageName + " in the package");
            return null;
        }
    }
}