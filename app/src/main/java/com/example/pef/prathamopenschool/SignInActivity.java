package com.example.pef.prathamopenschool;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.gpsmodule.Interfaces.GpsTestListener;
import com.example.pef.prathamopenschool.gpsmodule.util.GpsTestUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.example.pef.prathamopenschool.gpsmodule.util.GpsTestUtil.writeGnssMeasurementToLog;
import static com.example.pef.prathamopenschool.gpsmodule.util.GpsTestUtil.writeNavMessageToLog;
import static com.example.pef.prathamopenschool.gpsmodule.util.GpsTestUtil.writeNmeaToLog;

public class SignInActivity extends AppCompatActivity implements LocationListener, GpsTestListener {

    List<JSONArray> students;
    List<String> groupNames, assignedIds;
    StatusDBHelper statusDBHelper;
    StudentDBHelper studentDBHelper;
    GroupDBHelper groupDBHelper;
    TextView tv_title;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button next;
    Utility utility;
    ArrayList<JSONObject> attendenceData;
    Context sessionContex;
    boolean timer;
    String newNodeList;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    int aajKaSawalPlayed = 3;
    boolean doubleBackToExitPressedOnce = false;
    String checkQJson;

    // GPS
    private static final String TAG = "GpsTest";
    boolean mStarted;
    boolean mFaceTrueNorth;
    boolean mWriteGnssMeasurementToLog;
    boolean mLogNmea;
    boolean mWriteNmeaTimestampToLog;
    private LocationManager mLocationManager;
    private LocationProvider mProvider;
    private GpsStatus mLegacyStatus;
    private GpsStatus.Listener mLegacyStatusListener;
    private GpsStatus.NmeaListener mLegacyNmeaListener;
    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener; // For SNRs
    private OnNmeaMessageListener mOnNmeaMessageListener;
    private GnssNavigationMessage.Callback mGnssNavMessageListener;
    // Listeners for Fragments
    private ArrayList<GpsTestListener> mGpsTestListeners = new ArrayList<GpsTestListener>();
    private Location mLastLocation;
    private long minTime; // Min Time between location updates, in milliseconds
    private float minDistance; // Min Distance between location updates, in meters
    private SignInActivity sInstance;
    private long mFixTime;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm:ss.SS a");
    private String mTtff;
    Dialog gpsTimeDialog;
    TextView tv_msg, tv_msgBottom;
    boolean appName = false;
    StatusDBHelper s;
    public static String sessionStartTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Multiphotoselect initialization
        MultiPhotoSelectActivity.dilog = new DilogBoxForProcess();
        MultiPhotoSelectActivity.programID = new Utility().getProgramId();
        MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();
        MultiPhotoSelectActivity.timeout = (long) 20000 * 60;
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        MultiPhotoSelectActivity.deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check if location & gpstime is available
        s = new StatusDBHelper(this);
        boolean androidIDAvailable = false;
        boolean SerialIDAvailable = false;
        boolean apkVersion = false;

        androidIDAvailable = s.initialDataAvailable("AndroidID");
        SerialIDAvailable = s.initialDataAvailable("SerialID");
        apkVersion = s.initialDataAvailable("apkVersion");
        appName = s.initialDataAvailable("appName");

        if (appName == false) {
            s = new StatusDBHelper(this);
            // app name
            if (MultiPhotoSelectActivity.programID.equals("1"))
                s.insertInitialData("appName", "Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                s.insertInitialData("appName", "Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                s.insertInitialData("appName", "Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("4"))
                s.insertInitialData("appName", "Pratham Digital - Pratham Institute");

        } else {
            s = new StatusDBHelper(this);
            // app name
            if (MultiPhotoSelectActivity.programID.equals("1"))
                s.Update("appName", "Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                s.Update("appName", "Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                s.Update("appName", "Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("4"))
                s.Update("appName", "Pratham Digital - Pratham Institute");

        }

        String deviceID = "";
        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (androidIDAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("AndroidID", deviceID);
        } else {
            s = new StatusDBHelper(this);
            s.Update("AndroidID", deviceID);
        }

        if (SerialIDAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("SerialID", Build.SERIAL);
        } else {
            s = new StatusDBHelper(this);
            s.Update("SerialID", Build.SERIAL);
        }

        if (apkVersion == false) {
            s = new StatusDBHelper(this);
            PackageInfo pInfo = null;
            String verCode = "";
            try {
                pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                verCode = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            s.insertInitialData("apkVersion", verCode);
        } else {
            s = new StatusDBHelper(this);
            PackageInfo pInfo = null;
            String verCode = "";
            try {
                pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                verCode = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            s.Update("apkVersion", verCode);
        }
        // GET GPS TIME
        sInstance = this;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);

        // Timer Start
        // Todo get GPS DateTime & Location
        if (MyApplication.gpsTimer == null) {
            // Execute GPS Location & Time Dialog
            // GPS Signal Dialog
            gpsTimeDialog = new Dialog(this);
            gpsTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            gpsTimeDialog.setContentView(R.layout.customgpsdialog);
            gpsTimeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

            GifView gifView = gpsTimeDialog.findViewById(R.id.gif_satellite);
            gifView.setGifResource(R.drawable.satellite);

            // Setting Dialog
            gpsTimeDialog.setCanceledOnTouchOutside(false);
            gpsTimeDialog.setCancelable(false);
            gpsTimeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            gpsTimeDialog.show();

            // execution of the app
            if (mProvider == null) {
                Log.e(TAG, "Unable to get GPS_PROVIDER");
                Toast.makeText(this, "gps_not_supported", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            gpsStart();
            // if time more than minute then show " Go outside dialog " i.e set message
            tv_msgBottom = gpsTimeDialog.findViewById(R.id.tv_msgBottom);
            tv_msgBottom.setVisibility(View.GONE);
            try {
                doAfterSomeTime();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

//            // GET GPS TIME
//            sInstance = this;
//            Toast.makeText(this, "Timer : "+new Utility().GetCurrentDateTime(false), Toast.LENGTH_SHORT).show();
//            // execution of the app
//            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
//            if (mProvider == null) {
//                Log.e(TAG, "Unable to get GPS_PROVIDER");
//                Toast.makeText(this, "gps_not_supported", Toast.LENGTH_SHORT).show();
//                finish();
//                return;
//            }
//            gpsStart();
        }

    }//onCreate

    /****************************************************************************************************************/

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void doAfterSomeTime() {
        Runnable delayedTask = new Runnable() {
            @Override
            public void run() {
                if (gpsTimeDialog.isShowing()) {
                    tv_msgBottom.setVisibility(View.VISIBLE);
                }
            }
        };
        mainThreadHandler.postDelayed(delayedTask, 60000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reset Timer
        MyApplication.resetGPSFixTimer();
        MyApplication.startGPSFixTimer();

        // session
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
        }

        // Check if location & gpstime is available
        if (true) {
            addStatusListener();
            addNmeaListener();
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                promptEnableGps();
            } else {
                gpsStart();
            }
            checkTimeAndDistance(null);
            if (GpsTestUtil.isGnssStatusListenerSupported()) {
                addGnssMeasurementsListener();
            }
            if (GpsTestUtil.isGnssStatusListenerSupported()) {
                addNavMessageListener();
            }
        }

        if (!isMyServiceRunning(GPSLocationService.class))

        {
            // Start Location Service
            startService(new Intent(this, GPSLocationService.class));
        } else {
            //Service Already runnung
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent goToAdminLogin = new Intent(SignInActivity.this, AdminActivity.class);
//            finish();
            startActivity(goToAdminLogin);
        }
        if (id == R.id.action_leaderboard) {
            Intent goToAdminLogin = new Intent(SignInActivity.this, TabUsage.class);
//            finish();
            startActivity(goToAdminLogin);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();

        MultiPhotoSelectActivity.pauseFlg = true;

        MultiPhotoSelectActivity.cd = new CountDownTimer(MultiPhotoSelectActivity.duration, 1000) {
            //cd = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                MultiPhotoSelectActivity.duration = millisUntilFinished;
                timer = true;
            }

            @Override
            public void onFinish() {
                timer = false;
                MainActivity.sessionFlg = true;
                if (!CardAdapter.vidFlg) {
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


        // Remove status listeners
        removeStatusListener();
        removeNmeaListener();
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            removeNavMessageListener();
        }
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            removeGnssMeasurementsListener();
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // GPS TIME
    void addListener(GpsTestListener listener) {
        mGpsTestListeners.add(listener);
    }

    @SuppressLint("MissingPermission")
    public synchronized void gpsStart() {
        if (!mStarted) {
            mLocationManager
                    .requestLocationUpdates(mProvider.getName(), 5000, 0, this);
            mStarted = true;
        }
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.gpsStart();
        }
    }

    public synchronized void gpsStop() {
        if (mStarted) {
            mLocationManager.removeUpdates(this);
            mStarted = false;
        }
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.gpsStop();
        }
    }

    private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
        updateGnssStatus(status);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateGnssStatus(GnssStatus status) {
        mDateFormat.format(mFixTime);
//        time.setText(mDateFormat.format(mFixTime) + "");
        if (mPrns == null) {
            /**
             * We need to allocate arrays big enough so we don't overflow them.  Per
             * https://developer.android.com/reference/android/location/GnssStatus.html#getSvid(int)
             * 255 should be enough to contain all known satellites world-wide.
             */
            final int MAX_LENGTH = 255;
            mPrns = new int[MAX_LENGTH];
            mSnrCn0s = new float[MAX_LENGTH];
            mSvElevations = new float[MAX_LENGTH];
            mSvAzimuths = new float[MAX_LENGTH];
            mConstellationType = new int[MAX_LENGTH];
            mHasEphemeris = new boolean[MAX_LENGTH];
            mHasAlmanac = new boolean[MAX_LENGTH];
            mUsedInFix = new boolean[MAX_LENGTH];
        }

        final int length = status.getSatelliteCount();
        mSvCount = 0;
        mUsedInFixCount = 0;
        while (mSvCount < length) {
            int prn = status.getSvid(mSvCount);
            mPrns[mSvCount] = prn;
            mConstellationType[mSvCount] = status.getConstellationType(mSvCount);
            mSnrCn0s[mSvCount] = status.getCn0DbHz(mSvCount);
            mSvElevations[mSvCount] = status.getElevationDegrees(mSvCount);
            mSvAzimuths[mSvCount] = status.getAzimuthDegrees(mSvCount);
            mHasEphemeris[mSvCount] = status.hasEphemerisData(mSvCount);
            mHasAlmanac[mSvCount] = status.hasAlmanacData(mSvCount);
            mUsedInFix[mSvCount] = status.usedInFix(mSvCount);
            if (status.usedInFix(mSvCount)) {
                mUsedInFixCount++;
            }

            mSvCount++;
        }
    }

    @Deprecated
    private void updateLegacyStatus(GpsStatus status) {
        mDateFormat.format(mFixTime);
//        time.setText(mDateFormat.format(mFixTime) + "");
        Iterator<GpsSatellite> satellites = status.getSatellites().iterator();
        if (mPrns == null) {
            int length = status.getMaxSatellites();
            mPrns = new int[length];
            mSnrCn0s = new float[length];
            mSvElevations = new float[length];
            mSvAzimuths = new float[length];
            // Constellation type isn't used, but instantiate it to avoid NPE in legacy devices
            mConstellationType = new int[length];
            mHasEphemeris = new boolean[length];
            mHasAlmanac = new boolean[length];
            mUsedInFix = new boolean[length];
        }

        mSvCount = 0;
        mUsedInFixCount = 0;
        while (satellites.hasNext()) {
            GpsSatellite satellite = satellites.next();
            int prn = satellite.getPrn();
            mPrns[mSvCount] = prn;
            mSnrCn0s[mSvCount] = satellite.getSnr();
            mSvElevations[mSvCount] = satellite.getElevation();
            mSvAzimuths[mSvCount] = satellite.getAzimuth();
            mHasEphemeris[mSvCount] = satellite.hasEphemeris();
            mHasAlmanac[mSvCount] = satellite.hasAlmanac();
            mUsedInFix[mSvCount] = satellite.usedInFix();
            if (satellite.usedInFix()) {
                mUsedInFixCount++;
            }
            mSvCount++;
        }
    }

    @Override
    public void onGpsStatusChanged(int event, GpsStatus status) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                mTtff = GpsTestUtil.getTtffString(status.getTimeToFirstFix());
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                updateLegacyStatus(status);
                break;
        }
    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {
        mTtff = GpsTestUtil.getTtffString(ttffMillis);
    }

    @Override
    public void onGnssStarted() {

    }

    @Override
    public void onGnssStopped() {

    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

    }

    private boolean sendExtraCommand(String command) {
        return mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, command, null);
    }

    private void addStatusListener() {
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            addGnssStatusListener();
        } else {
            addLegacyStatusListener();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    private void addGnssStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                for (GpsTestListener listener : mGpsTestListeners) {
                    listener.onGnssStarted();
                }
            }

            @Override
            public void onStopped() {
                for (GpsTestListener listener : mGpsTestListeners) {
                    listener.onGnssStopped();
                }
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                for (GpsTestListener listener : mGpsTestListeners) {
                    listener.onGnssFirstFix(ttffMillis);
                }
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;

                // Stop progress bar after the first status information is obtained
                setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);

                for (GpsTestListener listener : mGpsTestListeners) {
                    listener.onSatelliteStatusChanged(mGnssStatus);
                }
            }
        };
        mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addGnssMeasurementsListener() {
        mGnssMeasurementsListener = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                for (GpsTestListener listener : mGpsTestListeners) {
                    listener.onGnssMeasurementsReceived(event);
                }
                if (mWriteGnssMeasurementToLog) {
                    for (GnssMeasurement m : event.getMeasurements()) {
                        writeGnssMeasurementToLog(m);
                    }
                }
            }

            @Override
            public void onStatusChanged(int status) {
                final String statusMessage;
                switch (status) {
                    case STATUS_LOCATION_DISABLED:
                        statusMessage = "gnss_measurement_status_loc_disabled";
                        break;
                    case STATUS_NOT_SUPPORTED:
                        statusMessage = "gnss_measurement_status_not_supported";
                        break;
                    case STATUS_READY:
                        statusMessage = "gnss_measurement_status_ready";
                        break;
                    default:
                        statusMessage = "gnss_status_unknown";
                }
                Log.d(TAG, "GnssMeasurementsEvent.Callback.onStatusChanged() - " + statusMessage);
            }
        };
        mLocationManager.registerGnssMeasurementsCallback(mGnssMeasurementsListener);
    }

    @SuppressLint("MissingPermission")
    private void addLegacyStatusListener() {
        mLegacyStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                mLegacyStatus = mLocationManager.getGpsStatus(mLegacyStatus);

                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        // Stop progress bar after the first status information is obtained
                        setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
                        break;
                }
//                time.setText(mDateFormat.format(mFixTime) + "");
                for (GpsTestListener listener : mGpsTestListeners) {
                    listener.onGpsStatusChanged(event, mLegacyStatus);
                }
            }
        };
        mLocationManager.addGpsStatusListener(mLegacyStatusListener);
    }

    private void removeStatusListener() {
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            removeGnssStatusListener();
        } else {
            removeLegacyStatusListener();
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void removeGnssStatusListener() {
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeGnssMeasurementsListener() {
        if (mLocationManager != null && mGnssMeasurementsListener != null) {
            mLocationManager.unregisterGnssMeasurementsCallback(mGnssMeasurementsListener);
        }
    }

    private void removeLegacyStatusListener() {
        if (mLocationManager != null && mLegacyStatusListener != null) {
            mLocationManager.removeGpsStatusListener(mLegacyStatusListener);
        }
    }

    private void addNmeaListener() {
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            addNmeaListenerAndroidN();
        } else {
            addLegacyNmeaListener();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addNmeaListenerAndroidN() {
        if (mOnNmeaMessageListener == null) {
            mOnNmeaMessageListener = new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String message, long timestamp) {
                    for (GpsTestListener listener : mGpsTestListeners) {
                        listener.onNmeaMessage(message, timestamp);
                    }
                    if (mLogNmea) {
                        writeNmeaToLog(message,
                                mWriteNmeaTimestampToLog ? timestamp : Long.MIN_VALUE);
                    }
                }
            };
        }
        mLocationManager.addNmeaListener(mOnNmeaMessageListener);
    }

    @SuppressLint("MissingPermission")
    private void addLegacyNmeaListener() {
        if (mLegacyNmeaListener == null) {
            mLegacyNmeaListener = new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    for (GpsTestListener listener : mGpsTestListeners) {
                        listener.onNmeaMessage(nmea, timestamp);
                    }
                    if (mLogNmea) {
                        writeNmeaToLog(nmea, mWriteNmeaTimestampToLog ? timestamp : Long.MIN_VALUE);
                    }
                }
            };
        }
        mLocationManager.addNmeaListener(mLegacyNmeaListener);
    }

    private void removeNmeaListener() {
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            if (mLocationManager != null && mOnNmeaMessageListener != null) {
                mLocationManager.removeNmeaListener(mOnNmeaMessageListener);
            }
        } else {
            if (mLocationManager != null && mLegacyNmeaListener != null) {
                mLocationManager.removeNmeaListener(mLegacyNmeaListener);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addNavMessageListener() {
        if (mGnssNavMessageListener == null) {
            mGnssNavMessageListener = new GnssNavigationMessage.Callback() {
                @Override
                public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                    writeNavMessageToLog(event);
                }

                @Override
                public void onStatusChanged(int status) {
                    final String statusMessage;
                    switch (status) {
                        case STATUS_LOCATION_DISABLED:
                            statusMessage = "gnss_nav_msg_status_loc_disabled";
                            break;
                        case STATUS_NOT_SUPPORTED:
                            statusMessage = "gnss_nav_msg_status_not_supported";
                            break;
                        case STATUS_READY:
                            statusMessage = "gnss_nav_msg_status_ready";
                            break;
                        default:
                            statusMessage = "gnss_status_unknown";
                    }
                    Log.d(TAG, "GnssNavigationMessage.Callback.onStatusChanged() - " + statusMessage);
                }
            };
        }
        mLocationManager.registerGnssNavigationMessageCallback(mGnssNavMessageListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeNavMessageListener() {
        if (mLocationManager != null && mGnssNavMessageListener != null) {
            mLocationManager.unregisterGnssNavigationMessageCallback(mGnssNavMessageListener);
        }
    }

    /**
     * Ask the user if they want to enable GPS
     */
    private void promptEnableGps() {
        new AlertDialog.Builder(this)
                .setMessage("enable_gps")
                .setPositiveButton("enable",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton("later",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();
    }

    @SuppressLint("MissingPermission")
    private void checkTimeAndDistance(SharedPreferences settings) {
        if (mStarted) {
            mLocationManager
                    .requestLocationUpdates(mProvider.getName(), 5000, 0, this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkNavMessageOutput(SharedPreferences settings) {
    }


    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mFixTime = location.getTime();

        DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        Date gdate = new Date(location.getTime());
        String gpsDateTime = format.format(gdate);
//        Toast.makeText(this, "CurrentDateTime = " + CurrentDateTime + "\nGpsDateTime = " + gpsDateTime, Toast.LENGTH_SHORT).show();


//        Toast.makeText(this, "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.d("onLocationChanged:::", mFixTime + "");
        Log.d("onLocationChanged:::", location.hasAltitude() + "");
        Log.d("onLocationChanged:::", location.hasAccuracy() + "");
        Log.d("onLocationChanged:::", location.hasBearing() + "");
        Log.d("onLocationChanged:::", location.hasSpeed() + "");
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onLocationChanged(location);
        }
//        Toast.makeText(this, mDateFormat.format(mFixTime) + "", Toast.LENGTH_SHORT).show();

        // Check if location & gpstime is available
        StatusDBHelper s = new StatusDBHelper(this);
        boolean latitudeAvailable = false;
        boolean longitudeAvailable = false;
        boolean GPSDateTimeAvailable = false;
        boolean gpsFixDuration = false;


        latitudeAvailable = s.initialDataAvailable("Latitude");
        longitudeAvailable = s.initialDataAvailable("Longitude");
        GPSDateTimeAvailable = s.initialDataAvailable("GPSDateTime");
        gpsFixDuration = s.initialDataAvailable("gpsFixDuration");


        if (latitudeAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("Latitude", String.valueOf(location.getLatitude()));
        }
        if (longitudeAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("Longitude", String.valueOf(location.getLongitude()));

        }

        if (GPSDateTimeAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("GPSDateTime", gpsDateTime);
            // Reset Timer
            MyApplication.resetTimer();
            MyApplication.startTimer();
        } else {
            s = new StatusDBHelper(this);
            s.Update("GPSDateTime", gpsDateTime);
            // Reset Timer
            MyApplication.resetTimer();
            MyApplication.startTimer();
        }

        // GPS Fix Time
        if (gpsFixDuration == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("gpsFixDuration", "");
        } else {
            s = new StatusDBHelper(this);
            s.Update("gpsFixDuration", "" + MyApplication.getGPSFixTimerCount());
//            Toast.makeText(this, "GPSFixDuration = " + MyApplication.getGPSFixTimerCount(), Toast.LENGTH_SHORT).show();
        }

        BackupDatabase.backup(this);

        // if dialog is open then close
        if (gpsTimeDialog != null) {
            if (gpsTimeDialog.isShowing())
                gpsTimeDialog.dismiss();
        }

        // todo problem
        Log.d("before : ", "GetCurrentDateTime ");
        sessionStartTime = new Utility().GetCurrentDateTime(false);
        Log.d("beforeafter : ", "GetCurrentDateTime ");

        // stop getting location
        gpsStop();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onStatusChanged(provider, status, extras);
        }
    }

    public void onProviderEnabled(String provider) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onProviderEnabled(provider);
        }
    }

    public void onProviderDisabled(String provider) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onProviderDisabled(provider);
        }
    }


    /****************************************************************************************************************/
    public void goToQRLogin(View view) {
        Intent i = new Intent(this, QRLogin.class);
        startActivity(i);
    }

    public void goToMultiphotoSelect(View view) {
        Intent i = new Intent(this, MultiPhotoSelectActivity.class);
        startActivity(i);
    }
}
