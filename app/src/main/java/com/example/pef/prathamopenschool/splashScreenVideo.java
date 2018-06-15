package com.example.pef.prathamopenschool;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

//public class splashScreenVideo extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
public class splashScreenVideo extends AppCompatActivity {

    VideoView splashVideo;
    ImageView imgLogo;
    Animation animFadeIn;
    public static String appname = "";
    public static String fpath;
    Context context;
    ArrayList<String> path = new ArrayList<String>();
    Boolean appEnd = false;
    String terminateApp = "";
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    public AlarmManager alarmManagerAM, alarmManagerPM;
    Intent alarmIntentAM, alarmIntentPM;
    PendingIntent pendingIntentAM, pendingIntentPM;

    // DB file to Json
    String deviceId = "";
    String filename = "pushNewDataToServer";
    DateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
    String timeStamp = "";
    ArrayList<String> dbbackuppath = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_video);


        // Hide Actionbar
        getSupportActionBar().hide();
        context = this;

        alarmIntentPM = new Intent(context, AlarmReceiverPM.class);
        Log.d("packageName ::: ", context.getPackageName());

        imgLogo = (ImageView) findViewById(R.id.logo);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
        imgLogo.startAnimation(animFadeIn);
    }

    // Set Notification at 10:00 AM
//    public void setAMAlarm() {
//        alarmManagerAM = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmIntentAM = new Intent(splashScreenVideo.this, AlarmReceiver.class);
//        pendingIntentAM = PendingIntent.getBroadcast(splashScreenVideo.this, 1234, alarmIntentAM, 0);
//
//        //disabling am alarm
//        alarmManagerAM.cancel(pendingIntentAM);
//       /* Calendar alarmStartTime = Calendar.getInstance();
//        alarmStartTime.set(Calendar.HOUR_OF_DAY, 10);
//        alarmStartTime.set(Calendar.MINUTE, 00);
//        alarmStartTime.set(Calendar.SECOND, 0);
//        alarmManagerAM.setRepeating(AlarmManager.RTC, alarmStartTime.getTimeInMillis(), getInterval(), pendingIntentAM);
//        Log.d("AM Service :::", "AM SET Running");*/
//
//    }

    // Set Notification at 04:00 AM
    public void setPMAlarm() {
        alarmManagerPM = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntentPM = PendingIntent.getBroadcast(context, 12234, alarmIntentPM, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar alarmStartTime = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, 16);
        alarmStartTime.set(Calendar.MINUTE, 0);
        alarmStartTime.set(Calendar.SECOND, 0);
//        alarmManagerPM.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(),pendingIntentPM);
        alarmManagerPM.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), getInterval(), pendingIntentPM);
        Log.d("PM Service :::", "PM SET Running");

    }

    private long getInterval() {
        long days = 1;
        long hours = 24;
        long minutes = 60;
        long seconds = 60;
        long milliseconds = 1000;
        long repeatMS = days * hours * minutes * seconds * milliseconds;
        Log.d("Interval :::", String.valueOf(repeatMS));
        return repeatMS;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get External SD Card Path for Data Initialization
        getSdCardPath();

        new LongOperation(splashScreenVideo.this, fpath).execute();

    }

    // Check service is running or not
    private boolean isServiceRunning(PendingIntent pd) {
        boolean alarmUp = (PendingIntent.getBroadcast(context, 12234, alarmIntentPM, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmUp) {
            return true;
        } else
            return false;

    }

    class LongOperation extends AsyncTask<String, Void, String> {

        Context c;
        String path;

        LongOperation(Context c, String fpath) {
            this.c = c;
            this.path = fpath;
        }

        @Override
        protected String doInBackground(String... params) {


            try {

                File existingDBExists = new File(Environment.getExternalStorageDirectory() + "/PrathamTabDB.db");
                File existingPOSinternalExists = new File(Environment.getExternalStorageDirectory() + "/.POSinternal");

                // Both DB & .POSinternal exists
                if (existingDBExists.exists() && existingPOSinternalExists.exists()) {

                    // Copy DB file if already exist
                    RetrieveExistingDatabase.backup(splashScreenVideo.this);
                    // if current db version is not latest
                    StatusDBHelper statusDBHelper = new StatusDBHelper(splashScreenVideo.this);
                    String ver = statusDBHelper.getValue("apkVersion");
                    if (ver == null || ver.equalsIgnoreCase("null")) {
                        createDBJsonforBackup();
                    } else if (!ver.equalsIgnoreCase("2.1.7") && !ver.equalsIgnoreCase("2.1.8") && !ver.equalsIgnoreCase("2.1.9") && !ver.equalsIgnoreCase("2.1.10") && !ver.equalsIgnoreCase("2.1.11")) {
                        createDBJsonforBackup();
                    }
                    //check initial entries
                    checkInitialEntries();
                    return "true";
                }
                // DB exists & .POSinternal not exists
                else if (existingDBExists.exists() && !existingPOSinternalExists.exists()) {
                    // Copy DB file if already exist
                    RetrieveExistingDatabase.backup(splashScreenVideo.this);
                    // if current db version is not latest
                    StatusDBHelper statusDBHelper = new StatusDBHelper(splashScreenVideo.this);
                    String ver = statusDBHelper.getValue("apkVersion");
                    if (ver == null || ver.equalsIgnoreCase("null")) {
                        createDBJsonforBackup();
                    } else if (!ver.equalsIgnoreCase("2.1.7") && !ver.equalsIgnoreCase("2.1.8") && !ver.equalsIgnoreCase("2.1.9") && !ver.equalsIgnoreCase("2.1.10") && !ver.equalsIgnoreCase("2.1.11")) {
                        createDBJsonforBackup();
                    }
                    //check initial entries
                    checkInitialEntries();
                    // Auto Copy Data from External to Internal Storage
                    String SourcePath = path + "toCopy/";
                    File sourceDir = new File(SourcePath);
                    String TargetPath = Environment.getExternalStorageDirectory().toString();
                    File targetDir = new File(TargetPath);
                    File checkDir = new File(TargetPath + "/.POSinternal");
                    if (!sourceDir.exists()) {
                        Toast.makeText(c, "There is no Data for Application in external storage exist", Toast.LENGTH_LONG).show();
                        return "false";
                    } else {
                        if (!checkDir.exists()) {
                            try {
                                // Only executed on first time
                                // Copy Initial Data
                                copyDirectory(sourceDir, targetDir);
                                return "true";
                            } catch (IOException e) {
                                e.printStackTrace();
                                return "false";
                            }
                        } else {
                            // Alarm Notification is Set
                            return "true";
                        }
                    }

                }

                // Both DB & POSinternal not exists
                else {
                    // New Installation

                    //check initial entries
                    checkInitialEntries();
                    setPMAlarm();

                    // Auto Copy Data from External to Internal Storage
                    String SourcePath = path + "toCopy/";
                    File sourceDir = new File(SourcePath);
                    String TargetPath = Environment.getExternalStorageDirectory().toString();
                    File targetDir = new File(TargetPath);
                    File checkDir = new File(TargetPath + "/.POSinternal");
                    if (!sourceDir.exists()) {
                        Toast.makeText(c, "There is no Data for Application in external storage exist", Toast.LENGTH_LONG).show();
                        return "false";
                    } else {
                        if (!checkDir.exists()) {
                            try {
                                // Only executed on first time
                                // Copy Initial Data
                                copyDirectory(sourceDir, targetDir);

                                return "true";

                            } catch (IOException e) {
                                e.printStackTrace();
                                return "false";
                            }
                        } else {
                            // Alarm Notification is Set
                            return "true";
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "false";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            if (result.equalsIgnoreCase("true")) {

                appEnd = false;

                // Memory Allocation

                CrlDBHelper db = new CrlDBHelper(context);
                VillageDBHelper vdb = new VillageDBHelper(context);

                // Check initial DB Entry emptiness for populating data
                Boolean crlResult = db.checkTableEmptyness();
                Boolean villageResult = vdb.checkTableEmptyness();

                if (crlResult == false || villageResult == false) {
                    try {
                        // Add Initial Entries of CRL & Village Json to Database
                        SetInitialValues();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                new Handler().postDelayed(new Runnable() {

                    /*
                     * Showing splash screen with a timer. This will be useful when you
                     * want to show case your app logo / company
                     */

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        Intent splash = new Intent(splashScreenVideo.this, SignInActivity.class);
                        startActivity(splash);

                        // close this activity
                        finish();
                    }
                }, SPLASH_TIME_OUT);

            } else {
                appEnd = true;
                Toast.makeText(c, "Data Not Found in SD Card !!!", Toast.LENGTH_SHORT).show();
                finish();
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        // Pravin Copy Functions
        public void copy(File sourceLocation, File targetLocation) throws IOException {
            if (sourceLocation.isDirectory()) {
                copyDirectory(sourceLocation, targetLocation);
            } else {
                copyFile(sourceLocation, targetLocation);
            }
        }

        private void copyDirectory(File source, File target) throws IOException {
            if (!target.exists()) {
                target.mkdir();
            }

            for (String f : source.list()) {
                copy(new File(source, f), new File(target, f));
            }
        }

        private void copyFile(File source, File target) throws IOException {
            try {
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target);
                byte[] buf = new byte[1024];
                int length;
                while ((length = in.read(buf)) > 0) {
                    out.write(buf, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    private void checkInitialEntries() {
        String deviceIMEI = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String Build = android.os.Build.SERIAL;

        StatusDBHelper s = new StatusDBHelper(this);

        boolean aksAvailable = false;
        boolean langAvailable = false;
        boolean pullFlag = false;
        boolean group1 = false;
        boolean group2 = false;
        boolean group3 = false;
        boolean group4 = false;
        boolean group5 = false;
        boolean state = false;
        boolean district = false;
        boolean block = false;
        boolean village = false;
        boolean jsonForNewVideos = false;
        boolean deviceId = false;
        boolean ActivatedDate = false;
        boolean ActivatedForGroups = false;
        boolean CRL = false;
        boolean AMAlarm = false;
        boolean PMAlarm = false;
        boolean androidIDAvailable = false;
        boolean SerialIDAvailable = false;
        boolean apkVersion = false;
        boolean appName = false;
        boolean gpsFixDuration = false;
        boolean wifiMAC = false;

        wifiMAC = s.initialDataAvailable("wifiMAC");
        gpsFixDuration = s.initialDataAvailable("gpsFixDuration");
        aksAvailable = s.initialDataAvailable("aajKaSawalPlayed");
        langAvailable = s.initialDataAvailable("TabLanguage");
        pullFlag = s.initialDataAvailable("pullFlag");
        group1 = s.initialDataAvailable("group1");
        group2 = s.initialDataAvailable("group2");
        group3 = s.initialDataAvailable("group3");
        group4 = s.initialDataAvailable("group4");
        group5 = s.initialDataAvailable("group5");
        state = s.initialDataAvailable("state");
        district = s.initialDataAvailable("district");
        block = s.initialDataAvailable("block");
        village = s.initialDataAvailable("village");
        jsonForNewVideos = s.initialDataAvailable("jsonForNewVideos");
        deviceId = s.initialDataAvailable("deviceId");
        ActivatedDate = s.initialDataAvailable("ActivatedDate");
        ActivatedForGroups = s.initialDataAvailable("ActivatedForGroups");
        CRL = s.initialDataAvailable("CRL");
        AMAlarm = s.initialDataAvailable("AMAlarm");
        PMAlarm = s.initialDataAvailable("PMAlarm");
        androidIDAvailable = s.initialDataAvailable("AndroidID");
        SerialIDAvailable = s.initialDataAvailable("SerialID");
        apkVersion = s.initialDataAvailable("apkVersion");
        appName = s.initialDataAvailable("appName");

        if (wifiMAC == false) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            String macAddress = wInfo.getMacAddress();
            s = new StatusDBHelper(this);
            s.insertInitialData("wifiMAC", "" + macAddress);
        }
        if (gpsFixDuration == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("gpsFixDuration", "");
        }
        if (androidIDAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("AndroidID", deviceIMEI);
        }
        if (SerialIDAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("SerialID", Build);
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
        if (appName == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("appName", "appName");
        }
        if (district == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("district", "district");
        }
        if (block == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("block", "block");
        }
        if (ActivatedForGroups == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("ActivatedForGroups", "");
        }
        if (village == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("village", "village");
        }
        if (jsonForNewVideos == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("jsonForNewVideos", "");
        }
        if (deviceId == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("deviceId", deviceIMEI);
        }
        if (ActivatedDate == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("ActivatedDate", "0");
        }
        if (CRL == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("CRL", "CRL");
        }
        if (AMAlarm == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("AMAlarm", "0");
        }
        if (PMAlarm == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("PMAlarm", "0");
        }
        if (pullFlag == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("pullFlag", "0");
        }
        if (group1 == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("group1", "");
        }
        if (group2 == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("group2", "");
        }
        if (group3 == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("group3", "");
        }
        if (group4 == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("group4", "");
        }
        if (group5 == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("group5", "");
        }
        if (state == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("state", "state");
        }
        if (aksAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("aajKaSawalPlayed", "0");
        }
        if (langAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("TabLanguage", "English");
        }

        BackupDatabase.backup(splashScreenVideo.this);

        // replace all null values in db if exists
        new checkforNulls().execute();

        // Execute File checking on diff thread
        new fileChecker().execute();


    }

    public class checkforNulls extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // Runs on UI thread
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Runs on the background thread

            try {
                // todo replace null values with dummy values
                CrlDBHelper cdb = new CrlDBHelper(splashScreenVideo.this);
                AserDBHelper adb = new AserDBHelper(splashScreenVideo.this);
                GroupDBHelper gdb = new GroupDBHelper(splashScreenVideo.this);
                StudentDBHelper sdb = new StudentDBHelper(splashScreenVideo.this);
                VillageDBHelper vdb = new VillageDBHelper(splashScreenVideo.this);
                AssessmentScoreDBHelper assdb = new AssessmentScoreDBHelper(splashScreenVideo.this);
                AttendanceDBHelper attdb = new AttendanceDBHelper(splashScreenVideo.this);
                ScoreDBHelper scrdb = new ScoreDBHelper(splashScreenVideo.this);
                StatusDBHelper statdb = new StatusDBHelper(splashScreenVideo.this);

                cdb.replaceNulls();
                adb.replaceNulls();
                gdb.replaceNulls();
                sdb.replaceNulls();
                vdb.replaceNulls();
                assdb.replaceNulls();
                attdb.replaceNulls();
                scrdb.replaceNulls();
                statdb.replaceNulls();

                BackupDatabase.backup(splashScreenVideo.this);

            } catch (Exception e) {
                e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            // Runs on the UI thread
        }

    }

    // Mandatory File Check
    private class fileChecker extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // Runs on UI thread
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // Runs on the background thread
                // Check important Folders existance
                checkPOSInternalStructure("ReceivedContent");
                checkPOSInternalStructure("sharableContent");
                checkPOSInternalStructure("receivedUsage");
                checkPOSInternalStructure("StudentProfiles");
                checkPOSInternalStructure("transferredUsage");
                checkPOSInternalStructure("pushedUsage");
                //checkPOSInternalStructure("databaseBackup");
                checkPOSDBBackups();
            } catch (Exception e) {
                e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            // Runs on the UI thread

        }

    }

    public void fileCutPaste(File toMove, String destFolder) {
        try {
            File destinationFolder = new File(destFolder);
            File destinationFile = new File(destFolder + "/" + toMove.getName());
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }
            FileInputStream fileInputStream = new FileInputStream(toMove);
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);

            int bufferSize;
            byte[] bufffer = new byte[512];
            while ((bufferSize = fileInputStream.read(bufffer)) > 0) {
                fileOutputStream.write(bufffer, 0, bufferSize);
            }
            toMove.delete();
            fileInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Copy Function to Copy file
    public static void copy(File src, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);

            // buffer size 1K
            byte[] buf = new byte[1024];

            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            is.close();
            os.close();
        }
    }

    // Creates folders if dont exists
    private void checkPOSInternalStructure(String folderName) {

        File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/" + folderName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();

            if (folderName.equals("pushedUsage")) {

                try {
                    String path = Environment.getExternalStorageDirectory().toString() + "/.POSinternal/receivedUsage";
                    File receivedUsageDir = new File(path);

                    String destFolder = Environment.getExternalStorageDirectory() + "/.POSinternal/pushedUsage";

                    File[] files = receivedUsageDir.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().contains("pushNewDataToServer")) {
                            fileCutPaste(files[i], destFolder);
                        }
                    }
                } catch (Exception e) {
                    e.getMessage();
                }

            } else if (folderName.equals("databaseBackup")) {

                // Backup Database
                try {

                    File from = new File(Environment.getExternalStorageDirectory() + "/PrathamTabDB.db");
                    File to = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/databaseBackup/PrathamTabDB.db");
                    copy(from, to);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (folderName.equals("StudentProfiles")) {

                // Copy Images
                try {
                    copyB1();
                    copyB2();
                    copyB3();
                    copyG1();
                    copyG2();
                    copyG3();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        } else {
            // Do something else on failure
            // Check Student profiles files
            File boys = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Boys/");
            File b1 = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Boys/1.png");
            File b2 = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Boys/2.png");
            File b3 = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Boys/3.png");

            File girls = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Girls/");
            File g1 = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Girls/1.png");
            File g2 = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Girls/2.png");
            File g3 = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Girls/3.png");

            if (!boys.exists() || !b1.exists() || !b2.exists() || !b3.exists() || !girls.exists() || !g1.exists() || !g2.exists() || !g3.exists()) {

                // Copy Images
                try {
                    copyB1();
                    copyB2();
                    copyB3();
                    copyG1();
                    copyG2();
                    copyG3();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void checkPOSDBBackups() {
        File destFolder = new File(Environment.getExternalStorageDirectory() + "/.POSDBBackups");
        if (!destFolder.exists())
            destFolder.mkdir();
    }

    // Copy Avatars from Drawables
    private void copyB1() throws IOException {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.b1);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.POSinternal/StudentProfiles/Boys/";

        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(path, "1.png");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();


    }

    private void copyB2() throws IOException {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.b2);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.POSinternal/StudentProfiles/Boys/";

        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(path, "2.png");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();


    }

    private void copyB3() throws IOException {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.b3);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.POSinternal/StudentProfiles/Boys/";

        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(path, "3.png");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();


    }

    private void copyG1() throws IOException {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.g1);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.POSinternal/StudentProfiles/Girls/";

        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(path, "1.png");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();


    }

    private void copyG2() throws IOException {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.g2);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.POSinternal/StudentProfiles/Girls/";

        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(path, "2.png");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();


    }

    private void copyG3() throws IOException {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.g3);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.POSinternal/StudentProfiles/Girls/";

        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(path, "3.png");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();


    }





    /* public void Play(Uri path) {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(splashVideo);
        try {
            splashVideo.setVideoURI(path);
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(getApplicationContext());
            syncActivityLogs.addToDB("play-videoPlay", e, "Error");
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        splashVideo.setMediaController(mediaController);
        splashVideo.requestFocus();

    }*/


    /*@Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Intent intent = new Intent(this, MultiPhotoSelectActivity.class);
        startActivity(intent);
        finish();

        this.overridePendingTransition(R.anim.lefttoright, R.anim.lefttoright);
    }*/

    public void getSdCardPath() {
        CharSequence c = "";

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
        try {
            c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
            appname = c.toString();
            Log.w("LABEL", c.toString());
        } catch (Exception e) {//Name Not FOund Exception
        }

//        if (appname.equals("Pratham Digital")) {
//            if ((new File("/storage/extSdCard/.POSexternal/HLearning/").exists()) && (new File("/storage/extSdCard/.POSexternal/KhelBadi/").exists()) && (new File("/storage/extSdCard/.POSexternal/KhelPuri/").exists()) && (new File("/storage/extSdCard/.POSexternal/Media/").exists())) {
//                fpath = "/storage/extSdCard/";
//            } else if ((new File("/storage/sdcard1/.POSexternal/HLearning/").exists()) && (new File("/storage/sdcard1/.POSexternal/KhelBadi/").exists()) && (new File("/storage/sdcard1/.POSexternal/KhelPuri/").exists()) && (new File("/storage/sdcard1/.POSexternal/Media/").exists())) {
//                fpath = "/storage/sdcard1/";
//            } else if ((new File("/storage/usbcard1/.POSexternal/HLearning/").exists()) && (new File("/storage/usbcard1/.POSexternal/KhelBadi/").exists()) && (new File("/storage/usbcard1/.POSexternal/KhelPuri/").exists()) && (new File("/storage/usbcard1/.POSexternal/Media/").exists())) {
//                fpath = "/storage/usbcard1/";
//
//            } else if ((new File("/storage/sdcard0/.POSexternal/HLearning/").exists()) && (new File("/storage/sdcard0/.POSexternal/KhelBadi/").exists()) && (new File("/storage/sdcard0/.POSexternal/KhelPuri/").exists()) && (new File("/storage/sdcard0/.POSexternal/Media/").exists())) {
//                fpath = "/storage/sdcard0/";
//
//            } else if ((new File("/storage/emulated/0/.POSexternal/HLearning/").exists()) && (new File("/storage/emulated/0/.POSexternal/KhelBadi/").exists()) && (new File("/storage/emulated/0/.POSexternal/KhelPuri/").exists()) && (new File("/storage/emulated/0/.POSexternal/Media/").exists())) {
//                fpath = "/storage/emulated/0/";
//            }
//        }

        if (appname.equals("Pratham Digital")) {
            ArrayList<String> base_path = SDCardUtil.getExtSdCardPaths(this);
            if (base_path.size() > 0) {
                String path = base_path.get(0).replace("[", "");
                path = path.replace("]", "");
                fpath = path + "/";
            }
            fpath = fpath + ".POSexternal/";
        }
    }

    /*@Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        splashVideo.start();
    }*/


    void SetInitialValues() throws JSONException {

        // insert your code to run only when application is started first time here
        context = this;

        //CRL Initial DB Process
        CrlDBHelper db = new CrlDBHelper(context);
        // For Loading CRL Json From External Storage (Assets)
        JSONArray crlJsonArray = new JSONArray(loadCrlJSONFromAsset());
        for (int i = 0; i < crlJsonArray.length(); i++) {

            JSONObject clrJsonObject = crlJsonArray.getJSONObject(i);
            Crl crlobj = new Crl();
            crlobj.CRLId = clrJsonObject.getString("CRLId");
            crlobj.FirstName = clrJsonObject.getString("FirstName");
            crlobj.LastName = clrJsonObject.getString("LastName");
            crlobj.UserName = clrJsonObject.getString("UserName");
            crlobj.Password = clrJsonObject.getString("Password");
            crlobj.ProgramId = clrJsonObject.getInt("ProgramId");
            crlobj.Mobile = clrJsonObject.getString("Mobile");
            crlobj.State = clrJsonObject.getString("State");
            crlobj.Email = clrJsonObject.getString("Email");

            // new entries default values
            try {
                crlobj.sharedBy = clrJsonObject.getString("sharedBy");
                crlobj.SharedAtDateTime = clrJsonObject.getString("SharedAtDateTime");
                crlobj.appVersion = clrJsonObject.getString("appVersion");
                crlobj.appName = clrJsonObject.getString("appName");
                crlobj.CreatedOn = clrJsonObject.getString("CreatedOn");
            } catch (Exception e) {
                crlobj.sharedBy = "";
                crlobj.SharedAtDateTime = "";
                crlobj.appVersion = "";
                crlobj.appName = "";
                crlobj.CreatedOn = "";
                e.printStackTrace();
            }

            db.insertData(crlobj);
            BackupDatabase.backup(context);
        }

        //Villages Initial DB Process
        VillageDBHelper database = new VillageDBHelper(context);
        // For Loading Villages Json From External Storage (Assets)
        JSONArray villagesJsonArray = new JSONArray(loadVillageJSONFromAsset());

        for (int j = 0; j < villagesJsonArray.length(); j++) {
            JSONObject villagesJsonObject = villagesJsonArray.getJSONObject(j);

            Village villageobj = new Village();
            villageobj.VillageID = villagesJsonObject.getInt("VillageId");
            villageobj.VillageCode = villagesJsonObject.getString("VillageCode");
            villageobj.VillageName = villagesJsonObject.getString("VillageName");
            villageobj.Block = villagesJsonObject.getString("Block");
            villageobj.District = villagesJsonObject.getString("District");
            villageobj.State = villagesJsonObject.getString("State");
            villageobj.CRLID = villagesJsonObject.getString("CRLId");

            database.insertData(villageobj);
            BackupDatabase.backup(context);
        }

    }

    // Reading CRL Json From Internal Memory
    public String loadCrlJSONFromAsset() {
        String crlJsonStr = null;

        try {
            File crlJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Crl.json");
            FileInputStream stream = new FileInputStream(crlJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                crlJsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return crlJsonStr;
    }

    // Reading Village Json From SDCard
    public String loadVillageJSONFromAsset() {
        String villageJson = null;
        try {
            File villageJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Village.json");
            FileInputStream stream = new FileInputStream(villageJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                villageJson = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return villageJson;

    }


    // Create Json of DB File
    public void createDBJsonforBackup() {

        Calendar cal = Calendar.getInstance();
        timeStamp = timeStampFormat.format(cal.getTime());
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        JSONArray scoreData = new JSONArray(), logsData = new JSONArray(),
                attendanceData = new JSONArray(), studentData = new JSONArray(),
                crlData = new JSONArray(), grpData = new JSONArray(), aserData = new JSONArray();
        try {
            // Score Data
            try {
                ScoreDBHelper scoreDBHelper = new ScoreDBHelper(this);
                List<Score> scores = scoreDBHelper.GetAll();
                if (scores == null) {
                } else {
                    for (int i = 0; i < scores.size(); i++) {
                        JSONObject _obj = new JSONObject();
                        Score _score = scores.get(i);
                        _obj.put("SessionID", _score.SessionID);
                        _obj.put("GroupID", _score.GroupID);
                        _obj.put("DeviceID", _score.DeviceID);
                        _obj.put("ResourceID", _score.ResourceID);
                        _obj.put("QuestionID", _score.QuestionId);
                        _obj.put("ScoredMarks", _score.ScoredMarks);
                        _obj.put("TotalMarks", _score.TotalMarks);
                        _obj.put("StartDateTime", _score.StartTime);
                        _obj.put("EndDateTime", _score.EndTime);
                        _obj.put("Level", _score.Level);
                        scoreData.put(_obj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                scoreData.put(0);
            }

            // Logs Data
            try {
                LogsDBHelper logsDBHelper = new LogsDBHelper(this);
                List<Logs> logs = logsDBHelper.GetAll();

                if (logs == null) {
                    // Great No Errors
                } else {
                    JSONObject _obj = new JSONObject();
                    try {
                        _obj.put("CurrentDateTime", "");
                        _obj.put("ExceptionMsg", "");
                        _obj.put("ExceptionStackTrace", "");
                        _obj.put("MethodName", "");
                        _obj.put("Type", "");
                        _obj.put("GroupId", "");
                        _obj.put("DeviceId", "");
                        _obj.put("LogDetail", "");
                        logsData.put(_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /*for (int x = 0; x < logs.size(); x++) {
                        JSONObject _obj = new JSONObject();
                        Logs _logs = logs.get(x);
                        try {
                            _obj.put("CurrentDateTime", _logs.currentDateTime);
                            _obj.put("ExceptionMsg", _logs.exceptionMessage);
                            _obj.put("ExceptionStackTrace", _logs.exceptionStackTrace);
                            _obj.put("MethodName", _logs.methodName);
                            _obj.put("Type", _logs.errorType);
                            _obj.put("GroupId", _logs.groupId);
                            _obj.put("DeviceId", _logs.deviceId);
                            _obj.put("LogDetail", _logs.LogDetail);
                            logsData.put(_obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/

                }
            } catch (Exception e) {
                e.getMessage();
                logsData.put(0);
            }

            // Attendance Data
            try {
                AttendanceDBHelper attendanceDBHelper1 = new AttendanceDBHelper(this);
                attendanceData = attendanceDBHelper1.GetAll();
                if (attendanceData == null) {
                } else {
                    for (int i = 0; i < attendanceData.length(); i++) {
                        JSONObject jsonObject = attendanceData.getJSONObject(i);
                        String ids[] = jsonObject.getString("PresentStudentIds").split(",");
                        JSONArray presentStudents = new JSONArray();
                        for (int j = 0; j < ids.length; j++) {
                            JSONObject id = new JSONObject();
                            id.put("id", ids[j]);
                            presentStudents.put(id);
                        }
                        jsonObject.remove("PresentStudentIds");
                        jsonObject.put("PresentStudentIds", presentStudents);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                attendanceData.put(0);
            }

            try {
                // Students data
                StudentDBHelper sdb = new StudentDBHelper(this);
                List<Student> studentsList = sdb.GetAllNewStudents();
                JSONObject studentObj;
                if (studentData == null) {

                } else {
                    for (int i = 0; i < studentsList.size(); i++) {
                        studentObj = new JSONObject();
                        studentObj.put("StudentID", studentsList.get(i).StudentID);
                        studentObj.put("FirstName", studentsList.get(i).FirstName);
                        studentObj.put("MiddleName", studentsList.get(i).MiddleName);
                        studentObj.put("LastName", studentsList.get(i).LastName);
                        studentObj.put("Age", studentsList.get(i).Age);
                        studentObj.put("Class", studentsList.get(i).Class);
                        studentObj.put("UpdatedDate", studentsList.get(i).UpdatedDate);
                        studentObj.put("Gender", studentsList.get(i).Gender);
                        studentObj.put("GroupID", studentsList.get(i).GroupID);
                        studentObj.put("CreatedBy", studentsList.get(i).CreatedBy);
                        studentObj.put("newStudent", studentsList.get(i).newStudent); // DO THE CHANGES for HANDLING NULLS
                        studentObj.put("StudentUID", studentsList.get(i).StudentUID == null ? "" : studentsList.get(i).StudentUID);
                        studentObj.put("IsSelected", studentsList.get(i).IsSelected == null ? false : !studentsList.get(i).IsSelected);
                        // new entries
                        studentObj.put("sharedBy", studentsList.get(i).sharedBy == null ? "" : studentsList.get(i).sharedBy);
                        studentObj.put("SharedAtDateTime", studentsList.get(i).SharedAtDateTime == null ? "" : studentsList.get(i).SharedAtDateTime);
                        studentObj.put("appName", studentsList.get(i).appName == null ? "" : studentsList.get(i).appName);
                        studentObj.put("appVersion", studentsList.get(i).appVersion == null ? "" : studentsList.get(i).appVersion);
                        studentObj.put("CreatedOn", studentsList.get(i).CreatedOn == null ? "" : studentsList.get(i).CreatedOn);

                        studentData.put(studentObj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                studentData.put(0);
            }

            try {
                // Crl Data
                CrlDBHelper cdb = new CrlDBHelper(this);
                List<Crl> crlsList = cdb.GetAllNewCrl();
                JSONObject crlObj;
                if (crlData == null) {

                } else {
                    for (int i = 0; i < crlsList.size(); i++) {
                        crlObj = new JSONObject();
                        crlObj.put("CRLId", crlsList.get(i).CRLId);
                        crlObj.put("FirstName", crlsList.get(i).FirstName);
                        crlObj.put("LastName", crlsList.get(i).LastName);
                        crlObj.put("UserName", crlsList.get(i).UserName);
                        crlObj.put("Password", crlsList.get(i).Password);
                        crlObj.put("ProgramId", crlsList.get(i).ProgramId);
                        crlObj.put("Mobile", crlsList.get(i).Mobile);
                        crlObj.put("State", crlsList.get(i).State);
                        crlObj.put("Email", crlsList.get(i).Email);
                        crlObj.put("CreatedBy", crlsList.get(i).CreatedBy);
                        crlObj.put("newCrl", !crlsList.get(i).newCrl);
                        // new entries
                        crlObj.put("sharedBy", crlsList.get(i).sharedBy == null ? "" : crlsList.get(i).sharedBy);
                        crlObj.put("SharedAtDateTime", crlsList.get(i).SharedAtDateTime == null ? "" : crlsList.get(i).SharedAtDateTime);
                        crlObj.put("appName", crlsList.get(i).appName == null ? "" : crlsList.get(i).appName);
                        crlObj.put("appVersion", crlsList.get(i).appVersion == null ? "" : crlsList.get(i).appVersion);
                        crlObj.put("CreatedOn", crlsList.get(i).CreatedOn == null ? "" : crlsList.get(i).CreatedOn);

                        crlData.put(crlObj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                crlData.put(0);
            }

            try {
                // Groups Data
                GroupDBHelper gdb = new GroupDBHelper(this);
                List<Group> groupsList = gdb.GetAllNewGroups();
                JSONObject grpObj;
                if (grpData == null) {

                } else {
                    for (int i = 0; i < groupsList.size(); i++) {
                        grpObj = new JSONObject();
                        grpObj.put("GroupID", groupsList.get(i).GroupID);
                        grpObj.put("GroupCode", groupsList.get(i).GroupCode);
                        grpObj.put("GroupName", groupsList.get(i).GroupName);
                        grpObj.put("UnitNumber", groupsList.get(i).UnitNumber);
                        grpObj.put("DeviceID", groupsList.get(i).DeviceID);
                        grpObj.put("Responsible", groupsList.get(i).Responsible);
                        grpObj.put("ResponsibleMobile", groupsList.get(i).ResponsibleMobile);
                        grpObj.put("VillageID", groupsList.get(i).VillageID);
                        grpObj.put("ProgramID", groupsList.get(i).ProgramID);
                        grpObj.put("CreatedBy", groupsList.get(i).CreatedBy);
                        grpObj.put("newGroup", !groupsList.get(i).newGroup);
                        grpObj.put("VillageName", groupsList.get(i).VillageName == null ? "" : groupsList.get(i).VillageName);
                        grpObj.put("SchoolName", groupsList.get(i).SchoolName == null ? "" : groupsList.get(i).SchoolName);
                        // new entries
                        grpObj.put("sharedBy", groupsList.get(i).sharedBy == null ? "" : groupsList.get(i).sharedBy);
                        grpObj.put("SharedAtDateTime", groupsList.get(i).SharedAtDateTime == null ? "" : groupsList.get(i).SharedAtDateTime);
                        grpObj.put("appName", groupsList.get(i).appName == null ? "" : groupsList.get(i).appName);
                        grpObj.put("appVersion", groupsList.get(i).appVersion == null ? "" : groupsList.get(i).appVersion);
                        grpObj.put("CreatedOn", groupsList.get(i).CreatedOn == null ? "" : groupsList.get(i).CreatedOn);

                        grpData.put(grpObj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                grpData.put(0);
            }

            try {
                // Aser Data
                AserDBHelper aserdb = new AserDBHelper(this);
                List<Aser> aserList = aserdb.GetAll();
                JSONObject aserObj;
                if (aserData == null) {

                } else {
                    for (int i = 0; i < aserList.size(); i++) {
                        aserObj = new JSONObject();
                        aserObj.put("StudentId", aserList.get(i).StudentId);
                        aserObj.put("ChildID", aserList.get(i).ChildID);
                        aserObj.put("GroupID", aserList.get(i).GroupID);
                        aserObj.put("TestType", aserList.get(i).TestType);
                        aserObj.put("TestDate", aserList.get(i).TestDate);
                        aserObj.put("Lang", aserList.get(i).Lang);
                        aserObj.put("Num", aserList.get(i).Num);
                        aserObj.put("OAdd", aserList.get(i).OAdd);
                        aserObj.put("OSub", aserList.get(i).OSub);
                        aserObj.put("OMul", aserList.get(i).OMul);
                        aserObj.put("ODiv", aserList.get(i).ODiv);
                        aserObj.put("WAdd", aserList.get(i).WAdd);
                        aserObj.put("WSub", aserList.get(i).WSub);
                        aserObj.put("CreatedBy", aserList.get(i).CreatedBy);
                        aserObj.put("CreatedDate", aserList.get(i).CreatedDate);
                        aserObj.put("DeviceId", aserList.get(i).DeviceId);
                        aserObj.put("FLAG", aserList.get(i).FLAG);
                        // new entries
                        aserObj.put("sharedBy", aserList.get(i).sharedBy == null ? "" : aserList.get(i).sharedBy);
                        aserObj.put("SharedAtDateTime", aserList.get(i).SharedAtDateTime == null ? "" : aserList.get(i).SharedAtDateTime);
                        aserObj.put("appName", aserList.get(i).appName == null ? "" : aserList.get(i).appName);
                        aserObj.put("appVersion", aserList.get(i).appVersion == null ? "" : aserList.get(i).appVersion);
                        aserObj.put("CreatedOn", aserList.get(i).CreatedOn == null ? "" : aserList.get(i).CreatedOn);

                        aserData.put(aserObj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                aserData.put(0);
            }

            try {
                // Status Data
                StatusDBHelper statusDBHelper = new StatusDBHelper(this);
                JSONObject obj = new JSONObject();
                obj.put("ScoreCount", "" + scoreData.length());
                obj.put("AttendanceCount", "" + attendanceData.length());
                obj.put("CRLID", "CRLID = DBBackup");
                obj.put("NewStudentsCount", "" + studentData.length());
                obj.put("NewCrlsCount", "" + crlData.length());
                obj.put("NewGroupsCount", "" + grpData.length());
                obj.put("AserDataCount", "" + aserData.length());
                obj.put("TransId", new Utility().GetUniqueID());
                obj.put("DeviceId", "" + statusDBHelper.getValue("deviceId"));
                obj.put("MobileNumber", "0");
                obj.put("ActivatedDate", statusDBHelper.getValue("ActivatedDate"));
                obj.put("ActivatedForGroups", statusDBHelper.getValue("ActivatedForGroups"));
                // new status table fields
                obj.put("Latitude", statusDBHelper.getValue("Latitude"));
                obj.put("Longitude", statusDBHelper.getValue("Longitude"));
                obj.put("GPSDateTime", statusDBHelper.getValue("GPSDateTime"));
                obj.put("AndroidID", statusDBHelper.getValue("AndroidID"));
                obj.put("SerialID", statusDBHelper.getValue("SerialID"));
                obj.put("apkVersion", statusDBHelper.getValue("apkVersion"));
                obj.put("appName", statusDBHelper.getValue("appName"));
                obj.put("gpsFixDuration", statusDBHelper.getValue("gpsFixDuration"));

                String requestString = "{ " +
                        "\"metadata\": " + obj + "," +
                        " \"scoreData\": " + scoreData + ", " +
                        "\"LogsData\": " + logsData + ", " +
                        "\"attendanceData\": " + attendanceData + ", " +
                        "\"newStudentsData\": " + studentData + ", " +
                        "\"newCrlsData\": " + crlData + ", " +
                        "\"newGroupsData\": " + grpData + ", " +
                        "\"AserTableData\": " + aserData +
                        "}";
                WriteSettings(this, requestString, "pushNewDataToServer-" + timeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }//createDBJsonforBackup()


    // Creating file in POSDBBackups & clearing db
    public void WriteSettings(Context context, String data, String fName) {

        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        int Operation = 0; // 0:Failed, 1:Successfull

        try {
            File destFolder = new File(Environment.getExternalStorageDirectory() + "/.POSDBBackups");
            if (!destFolder.exists())
                destFolder.mkdir();

            String MainPath = Environment.getExternalStorageDirectory() + "/.POSDBBackups/" + fName + ".json";
            File file = new File(MainPath);
            try {
                dbbackuppath.add(MainPath);
                fOut = new FileOutputStream(file);
                osw = new OutputStreamWriter(fOut);
                osw.write(data);
                osw.flush();
                osw.close();
                fOut.close();

                Operation = 1;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // If Operation successfull then delete data & continue data
                if (Operation == 1) {
                    AserDBHelper aserDBHelper = new AserDBHelper(this);
                    aserDBHelper.DeleteAll();
                    AttendanceDBHelper attendanceDBHelper = new AttendanceDBHelper(this);
                    attendanceDBHelper.DeleteAll();
                    CrlDBHelper crlDBHelper = new CrlDBHelper(this);
                    crlDBHelper.DeleteAll();
                    GroupDBHelper groupDBHelper = new GroupDBHelper(this);
                    groupDBHelper.DeleteAll();
                    LogsDBHelper logsDBHelper = new LogsDBHelper(this);
                    logsDBHelper.DeleteAll();
                    StatusDBHelper statusDBHelper = new StatusDBHelper(this);
                    statusDBHelper.DeleteAll();
                    StudentDBHelper studentDBHelper = new StudentDBHelper(this);
                    studentDBHelper.DeleteAll();
                    VillageDBHelper villageDBHelper = new VillageDBHelper(this);
                    villageDBHelper.DeleteAll();
                    AssessmentScoreDBHelper assessmentScoreDBHelper = new AssessmentScoreDBHelper(this);
                    assessmentScoreDBHelper.DeleteAll();
                    UserDBHelper userDBHelper = new UserDBHelper(this);
                    userDBHelper.DeleteAll();
                    ScoreDBHelper delScore = new ScoreDBHelper(this);
                    delScore.DeleteAll();

                    BackupDatabase.backup(this);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Settings not saved", Toast.LENGTH_SHORT).show();
        }
    }

}
