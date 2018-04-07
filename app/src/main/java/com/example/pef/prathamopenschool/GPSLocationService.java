package com.example.pef.prathamopenschool;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GPSLocationService extends Service {
    private static final String TAG = "PrathamGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 20000;
    private static final float LOCATION_DISTANCE = 0;
    private long mFixTime;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
//            Toast.makeText(GPSLocationService.this, "Location : " + location, Toast.LENGTH_LONG).show();
            mLastLocation = location;
            mFixTime = location.getTime();

            DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
            Date gdate = new Date(location.getTime());
            String gpsDateTime = format.format(gdate);

            StatusDBHelper s = new StatusDBHelper(GPSLocationService.this);
            boolean latitudeAvailable = false;
            boolean longitudeAvailable = false;
            boolean GPSDateTimeAvailable = false;

            latitudeAvailable = s.initialDataAvailable("Latitude");
            longitudeAvailable = s.initialDataAvailable("Longitude");
            GPSDateTimeAvailable = s.initialDataAvailable("GPSDateTime");

            if (latitudeAvailable == false) {
                s = new StatusDBHelper(GPSLocationService.this);
                s.insertInitialData("Latitude", String.valueOf(location.getLatitude()));
            } else {
                s = new StatusDBHelper(GPSLocationService.this);
                s.Update("Latitude", String.valueOf(location.getLatitude()));
            }
            if (longitudeAvailable == false) {
                s = new StatusDBHelper(GPSLocationService.this);
                s.insertInitialData("Longitude", String.valueOf(location.getLongitude()));
            } else {
                s = new StatusDBHelper(GPSLocationService.this);
                s.Update("Longitude", String.valueOf(location.getLongitude()));
            }
            if (GPSDateTimeAvailable == false) {
                s = new StatusDBHelper(GPSLocationService.this);
                s.insertInitialData("GPSDateTime", gpsDateTime);
            } else {
                s = new StatusDBHelper(GPSLocationService.this);
                s.Update("GPSDateTime", gpsDateTime);
            }

//            Toast.makeText(MyApplication.getInstance(), "GpsDateTime = " + gpsDateTime, Toast.LENGTH_SHORT).show();
//            Toast.makeText(MyApplication.getInstance(), "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();

            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}