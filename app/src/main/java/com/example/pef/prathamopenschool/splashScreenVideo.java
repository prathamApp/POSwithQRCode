package com.example.pef.prathamopenschool;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
                    return "true";
                }

                // DB exists & .POSinternal not exists
                else if (existingDBExists.exists() && !existingPOSinternalExists.exists()) {
                    // Copy DB file if already exist
                    RetrieveExistingDatabase.backup(splashScreenVideo.this);

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

                    // Check 'TabLanguage','English'),('AMAlarm','0'),('PMAlarm','0') Entry in DB
                    StatusDBHelper s = new StatusDBHelper(context);
                    boolean valuesAvailable = false;
                    boolean langAvailable = false;

                    valuesAvailable = s.initialDataAvailable("aajKaSawalPlayed");
                    langAvailable = s.initialDataAvailable("TabLanguage");


                    if (valuesAvailable == false) {
                        s = new StatusDBHelper(context);
                        s.insertInitialData("TabLanguage", "English");
                    }

                    if (langAvailable == false) {
                        s = new StatusDBHelper(context);
                        s.insertInitialData("TabLanguage", "English");
                    }

                    BackupDatabase.backup(splashScreenVideo.this);

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

        if (appname.equals("Pratham Digital")) {
            if ((new File("/storage/extSdCard/.POSexternal/HLearning/").exists()) && (new File("/storage/extSdCard/.POSexternal/KhelBadi/").exists()) && (new File("/storage/extSdCard/.POSexternal/KhelPuri/").exists()) && (new File("/storage/extSdCard/.POSexternal/Media/").exists())) {
                fpath = "/storage/extSdCard/";
            } else if ((new File("/storage/sdcard1/.POSexternal/HLearning/").exists()) && (new File("/storage/sdcard1/.POSexternal/KhelBadi/").exists()) && (new File("/storage/sdcard1/.POSexternal/KhelPuri/").exists()) && (new File("/storage/sdcard1/.POSexternal/Media/").exists())) {
                fpath = "/storage/sdcard1/";
            } else if ((new File("/storage/usbcard1/.POSexternal/HLearning/").exists()) && (new File("/storage/usbcard1/.POSexternal/KhelBadi/").exists()) && (new File("/storage/usbcard1/.POSexternal/KhelPuri/").exists()) && (new File("/storage/usbcard1/.POSexternal/Media/").exists())) {
                fpath = "/storage/usbcard1/";

            } else if ((new File("/storage/sdcard0/.POSexternal/HLearning/").exists()) && (new File("/storage/sdcard0/.POSexternal/KhelBadi/").exists()) && (new File("/storage/sdcard0/.POSexternal/KhelPuri/").exists()) && (new File("/storage/sdcard0/.POSexternal/Media/").exists())) {
                fpath = "/storage/sdcard0/";

            } else if ((new File("/storage/emulated/0/.POSexternal/HLearning/").exists()) && (new File("/storage/emulated/0/.POSexternal/KhelBadi/").exists()) && (new File("/storage/emulated/0/.POSexternal/KhelPuri/").exists()) && (new File("/storage/emulated/0/.POSexternal/Media/").exists())) {
                fpath = "/storage/emulated/0/";
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


}
