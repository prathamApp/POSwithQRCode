package com.example.pef.prathamopenschool;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CrlDashboard extends AppCompatActivity implements FTPInterface.PushPullInterface {

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    StatusDBHelper sdbh;
    PlayVideo playVideo;
    boolean timer;
    TextView tv_version_code, tv_Serial, tv_DeviceID;
    static String CreatedBy, currentAdmin;
    public static Boolean transferFlag = false;
    static String deviceID = "";
    FTPConnect ftpConnect;
    RelativeLayout receiveFtpDialogLayout;
    TextView tv_ssid, tv_ip, tv_port, tv_Details;
    Button btn_Disconnect;
    Dialog receiverDialog;
    private ProgressBar recievingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crl_dashboard);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Hide Actionbar
        getSupportActionBar().hide();

        // FTP initialization
        ftpConnect = new FTPConnect(this, this, this);

        // Displaying Version Code of App
        tv_version_code = (TextView) findViewById(R.id.tv_Version);
        tv_Serial = (TextView) findViewById(R.id.tv_Serial);
        tv_DeviceID = (TextView) findViewById(R.id.tv_DeviceID);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String verCode = pInfo.versionName;
            tv_version_code.setText(String.valueOf(verCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        sdbh = new StatusDBHelper(this);

        String Serial = sdbh.getValue("SerialID");
        String DID = sdbh.getValue("AndroidID");

        tv_Serial.setText("" + Serial);
        tv_DeviceID.setText("" + DID);

        // replace all null values in db if exists
        new checkforNulls().execute();

        // Execute File checking on diff thread
        new fileChecker().execute();

        // delete zips
        deleteZips();

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        Intent i = getIntent();
        CreatedBy = i.getStringExtra("CreatedBy"); // Created by is CRLID

        currentAdmin = i.getStringExtra("UserName");
        // Toast.makeText(this, "Welcome " + currentAdmin, Toast.LENGTH_SHORT).show();

        //enable wifi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

    }

    // Delete NewJson.zip from Bluetooth folder
    private void deleteZips() {
        try {
            String posJsonDirectory = Environment.getExternalStorageDirectory() + "/.POSinternal/Json";
            File delNewJsonZip = new File(posJsonDirectory);
            for (File zipfile : delNewJsonZip.listFiles()) {
                if (!zipfile.isDirectory()) {
                    if (zipfile.getName().contains(".zip"))
                        zipfile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gotoTabReportActivity(View view) {
        Intent intent = new Intent(CrlDashboard.this, AssessmentCrlDashBoardView.class);
        intent.putExtra("fromActivity", "crl");
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public static String filename = "";

    // This method will be called when a MessageEvent is posted (in the UI thread)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equalsIgnoreCase("Recieved")) {
            filename = "";
            tv_Details.setText("");
            recievingProgress.setVisibility(View.VISIBLE);
            //add profiles to db
            File src = new File(Environment.getExternalStorageDirectory() + "/FTPRecieved/RecievedProfiles");
            if (src.exists() && src.listFiles().length > 0) {
                File[] files = src.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().startsWith("NewProfiles")) {
                        Utility.targetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage";
                        Utility.recievedFilePath = files[i].getAbsolutePath();
                        new RecieveFiles(CrlDashboard.this, Utility.targetPath, Utility.recievedFilePath, "profiles").execute();
                    }
                }
            }
            //add json to db
            File jsonSrc = new File(Environment.getExternalStorageDirectory() + "/FTPRecieved/RecievedJson");
            if (jsonSrc.exists() && jsonSrc.listFiles().length > 0) {
                wipeJsonFolder();
                File[] files = jsonSrc.listFiles();
                for (int i = 0; i < files.length; i++) {
                    Utility.targetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/Json";
                    Utility.recievedFilePath = files[i].getAbsolutePath();
                    try {
                        filename += "\n" + files[i].getName() + "   " + Integer.parseInt(String.valueOf(files[i].length() / 1024)) + " kb";
                        FileUtils.copyFile(new File(Utility.recievedFilePath),
                                new File(Utility.targetPath + "/" + files[i].getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        new File(Utility.recievedFilePath).delete();
                        tv_Details.setText("Files Recieved...." + filename);
                    }
                }
                filename = "";
                new RecieveFiles(CrlDashboard.this, Utility.targetPath, Utility.recievedFilePath, "json").execute();
            }
            File transferSrc = new File(Environment.getExternalStorageDirectory() + "/FTPRecieved/RecievedUsage");
            if (transferSrc.exists() && transferSrc.listFiles().length > 0) {
                File[] files = transferSrc.listFiles();
                for (int i = 0; i < files.length; i++) {
                    Utility.targetPath = Environment.getExternalStorageDirectory() + "/.POSDBBackups";
                    Utility.recievedFilePath = files[i].getAbsolutePath();
                    try {
                        filename += "\n" + files[i].getName() + "   " + Integer.parseInt(String.valueOf(files[i].length() / 1024)) + " kb";
                        FileUtils.moveFileToDirectory(new File(Utility.recievedFilePath),
                                new File(Utility.targetPath /*+ "/" + files[i].getName()*/), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        tv_Details.setText("Files Recieved...." + filename);
                    }
                }
                filename = "";
            }
            recievingProgress.setVisibility(View.GONE);
        } else if (event.message.equalsIgnoreCase("showCount")) {
            filename = "";
            recievingProgress.setVisibility(View.VISIBLE);
            tv_Details.setText("");
        } else if (event.message.equalsIgnoreCase("showDetails")) {
            tv_Details.setText("Files Received...." + filename);
            recievingProgress.setVisibility(View.GONE);
        } else if (event.message.equalsIgnoreCase("stopDialog")) {
            filename = "";
//            if (recievingDialog.isShowing()) {
//                recievingDialog.dismiss();
//            }
        }
    }


    private void wipeJsonFolder() {
        // Delete Receive Folder Contents after Transferring
        String directoryToDelete = Environment.getExternalStorageDirectory() + "/.POSinternal/Json";
        File dir = new File(directoryToDelete);
        for (File file : dir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }

    /********************************************** RECEIVE DATA ******************************************************/
    // Start FTP Server for receiving Usage/ Profiles/ Jsons
    public void receiveData(View view) {
        // get CRL Name by ID
//        ArrayList<String> f = ftpConnect.scanNearbyWifi();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        if (!ftpConnect.checkServiceRunning()) {
            CrlDBHelper crlObj = new CrlDBHelper(this);
            List<Crl> crlData = crlObj.GetCRLByID(CreatedBy);

            // Set HotSpot Name after crl name
            MyApplication.networkSSID = "PrathamHotSpot_" + crlData.get(0).FirstName + "_" + crlData.get(0).getLastName();
            File f = new File(Environment.getExternalStorageDirectory() + "/FTPRecieved");
            if (!f.exists())
                f.mkdir();
            MyApplication.setPath(Environment.getExternalStorageDirectory() + "/FTPRecieved");
            // Create FTP Server
            ftpConnect.createFTPHotspot();
        } else {
            Toast.makeText(CrlDashboard.this, "Server already running", Toast.LENGTH_SHORT).show();
        }
//        for (int i = 0; i < f.size(); i++) {
//            if ((f.get(i)).equalsIgnoreCase("PrathamHotspot"))
//        }
//        ftpConnect.connectToPrathamHotSpot(f.get(i));
    }

    ProgressDialog recievingDialog;

    @Override
    public void showDialog() {

        receiverDialog = new Dialog(CrlDashboard.this);
        receiverDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        receiverDialog.setContentView(R.layout.receive_ftpserver_dialog);
        receiverDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        receiveFtpDialogLayout = receiverDialog.findViewById(R.id.receiveFtpDialog);
        tv_ssid = receiverDialog.findViewById(R.id.tv_SSID);
        recievingProgress = receiverDialog.findViewById(R.id.recievingProgress);
        tv_ip = receiverDialog.findViewById(R.id.tv_ipaddr);
        tv_port = receiverDialog.findViewById(R.id.tv_port);
        btn_Disconnect = receiverDialog.findViewById(R.id.btn_Disconnect);
        tv_Details = receiverDialog.findViewById(R.id.tv_details);

        tv_ssid.setText("SSID : " + MyApplication.networkSSID);
        tv_ip.setText("IP : 192.168.43.1");
        tv_port.setText("Port : 8080");

        receiverDialog.setCanceledOnTouchOutside(false);
        receiverDialog.setCancelable(false);
        receiverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        receiverDialog.show();

        btn_Disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop Server
                if (ftpConnect.checkServiceRunning()) {
                    ftpConnect.stopServer();
                }
                ftpConnect.turnOnOffHotspot(false);
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(false);
                try {
                    FileUtils.deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/FTPRecieved"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                receiverDialog.dismiss();
            }
        });


    }

    @Override
    public void onFilesRecievedComplete(String typeOfFile, String filename) {

    }


    /********************************************** RECEIVE DATA ******************************************************/

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
                CrlDBHelper cdb = new CrlDBHelper(CrlDashboard.this);
                AserDBHelper adb = new AserDBHelper(CrlDashboard.this);
                GroupDBHelper gdb = new GroupDBHelper(CrlDashboard.this);
                StudentDBHelper sdb = new StudentDBHelper(CrlDashboard.this);
                VillageDBHelper vdb = new VillageDBHelper(CrlDashboard.this);
                AssessmentScoreDBHelper assdb = new AssessmentScoreDBHelper(CrlDashboard.this);
                AttendanceDBHelper attdb = new AttendanceDBHelper(CrlDashboard.this);
                ScoreDBHelper scrdb = new ScoreDBHelper(CrlDashboard.this);
                StatusDBHelper statdb = new StatusDBHelper(CrlDashboard.this);

                cdb.replaceNulls();
                adb.replaceNulls();
                gdb.replaceNulls();
                sdb.replaceNulls();
                vdb.replaceNulls();
                assdb.replaceNulls();
                attdb.replaceNulls();
                scrdb.replaceNulls();
                statdb.replaceNulls();

                BackupDatabase.backup(CrlDashboard.this);

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


    public void goToCrlAddEditScreen(View view) {

        Intent goToAddEdit = new Intent(CrlDashboard.this, CrlAddEditScreen.class);
        startActivity(goToAddEdit);

    }


    public void AssignGroups(View view) {

        Intent intent = new Intent(CrlDashboard.this, AssignGroups.class);
        startActivity(intent);

    }


    public void goToCrlPullPushTransferUsageScreen(View view) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        Intent intent = new Intent(CrlDashboard.this, CrlPullPushTransferUsageScreen.class);
        startActivity(intent);

    }


    public void goToCrlShareReceiveProfiles(View view) {

        Intent intent = new Intent(CrlDashboard.this, CrlShareReceiveProfiles.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

        super.onResume();
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
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
                    System.exit(0);
                    finishAffinity();

                }
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        // disable wifi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (wifiEnabled) {
            wifiManager.setWifiEnabled(false);
        }

        // turn off FTP Server & Hotspot
        if (ftpConnect.checkServiceRunning()) {
            ftpConnect.stopServer();
        }
        ftpConnect.turnOnOffHotspot(false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // turn off FTP Server & Hotspot
        if (ftpConnect.checkServiceRunning()) {
            ftpConnect.stopServer();
        }
        try {
            FileUtils.deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/FTPRecieved"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ftpConnect.turnOnOffHotspot(false);
        // disable wifi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (wifiEnabled) {
            wifiManager.setWifiEnabled(false);
        }

        super.onBackPressed();
    }
}
