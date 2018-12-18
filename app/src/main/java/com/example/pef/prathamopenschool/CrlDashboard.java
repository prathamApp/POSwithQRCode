package com.example.pef.prathamopenschool;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.ftpSettings.hotspot_android.Hotspot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CrlDashboard extends AppCompatActivity implements FTPInterface.PushPullInterface {

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    StatusDBHelper sdbh;
    PlayVideo playVideo;
    boolean timer;
    TextView tv_version_code, tv_Serial, tv_DeviceID, tv_prathamCode, tv_loginMode;
    static String CreatedBy, currentAdmin;
    public static Boolean transferFlag = false;
    static String deviceID = "";
    FTPConnect ftpConnect;
    RelativeLayout receiveFtpDialogLayout;
    TextView tv_ssid, tv_ip, tv_port, tv_Details, tv_wifiMAC;
    Button btn_Disconnect;
    Dialog receiverDialog;
    private ProgressBar recievingProgress;
    private String LoginMode = "";
    private int SDCardLocationChooser = 7;
    private Uri treeUri;
    public static ProgressDialog pd;
    private boolean connected = false;
    private String networkSSID = "PrathamHotSpot";
    PowerManager pm;
    PowerManager.WakeLock wl;
    private String treeUriPath;
    private static ArrayList<File_Model> data = new ArrayList<File_Model>();
    DocumentFile temp_documentFile;
    private File tempFile;



    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crl_dashboard);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();

        // Wake lock for disabling sleep
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();

        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        // FTP initialization
        ftpConnect = new FTPConnect(this, this, this);

        // Displaying Version Code of App
        tv_version_code = (TextView) findViewById(R.id.tv_Version);
        tv_Serial = (TextView) findViewById(R.id.tv_Serial);
        tv_DeviceID = (TextView) findViewById(R.id.tv_DeviceID);
        tv_prathamCode = (TextView) findViewById(R.id.tv_prathamCode);
        tv_loginMode = (TextView) findViewById(R.id.tv_loginMode);
        tv_wifiMAC = (TextView) findViewById(R.id.tv_wifiMAC);

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
        String pCode = sdbh.getValue("prathamCode");
        String lMode = sdbh.getValue("loginMode");
        String mac = sdbh.getValue("wifiMAC");

        tv_Serial.setText("" + Serial);
        tv_DeviceID.setText("" + DID);
        tv_prathamCode.setText("" + pCode);
        tv_loginMode.setText("" + lMode);
        tv_wifiMAC.setText("" + mac);

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

        try {
            checkManageDevicePermission();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkManageDevicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Settings.System.canWrite(MyApplication.getInstance())) {
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MyApplication.getInstance().getPackageName()));
                MyApplication.getInstance().startActivity(intent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            if (Settings.System.canWrite(MyApplication.getInstance())) {
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MyApplication.getInstance().getPackageName()));
                MyApplication.getInstance().startActivity(intent);
            }
        }

    }


    // login mode
    public void selectLoginMode(View view) {
        // Dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CrlDashboard.this);
        LayoutInflater inflater = CrlDashboard.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.login_mode_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Please Select Login Mode ");

        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                RadioGroup rgLoginMode = (RadioGroup) dialogView.findViewById(R.id.rg_LoginMode);
                // get selected radio button from radioGroup
                int selectedId = rgLoginMode.getCheckedRadioButtonId();
                RadioButton selectedLoginMode = (RadioButton) dialogView.findViewById(selectedId);
                LoginMode = selectedLoginMode.getText().toString();
                StatusDBHelper sdb = new StatusDBHelper(CrlDashboard.this);
                sdb.Update("loginMode", LoginMode.trim());
                BackupDatabase.backup(CrlDashboard.this);
                tv_loginMode.setText(LoginMode);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Cancel
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
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
    // 08 Dec 2018
    public void receiveData(View view) {
        CrlDBHelper crlObj = new CrlDBHelper(this);
        List<Crl> crlData = crlObj.GetCRLByID(CreatedBy);
        // Set HotSpot Name after crl name
        MyApplication.networkSSID = "PrathamHotSpot_" + crlData.get(0).FirstName + "_" + crlData.get(0).getLastName();

        // Receive Profile/json or Content Dialog
        Dialog receiveDialog = new Dialog(this);
        receiveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        receiveDialog.setContentView(R.layout.receive_datatype_chooser);
        Button regular = (Button) receiveDialog.findViewById(R.id.btn_ReceiveProfileJsonUsage);
        Button newContent = (Button) receiveDialog.findViewById(R.id.btn_ReceiveNewContent);

        regular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Receive Profiles/ Json/ Usage
                File f = new File(Environment.getExternalStorageDirectory() + "/FTPRecieved");
                if (!f.exists())
                    f.mkdir();
                MyApplication.setPath(Environment.getExternalStorageDirectory() + "/FTPRecieved");

                // Create FTP Server
                ftpConnect.createFTPHotspot();

                receiveDialog.dismiss();
            }
        });

        newContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this).getString("URI", null) == null
                        && PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this).getString("PATH", "").equals("")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CrlDashboard.this);
                    LayoutInflater factory = LayoutInflater.from(CrlDashboard.this);
                    final View view = factory.inflate(R.layout.custom_alert_box_sd_card, null);
                    alertDialogBuilder.setView(view);
                    alertDialogBuilder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    startActivityForResult(intent, SDCardLocationChooser);
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setTitle("एसडी कार्ड स्थान का चयन करें");
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setView(view);
                    alertDialog.show();

                } else {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean wifiEnabled = wifiManager.isWifiEnabled();
                    if (!wifiEnabled) {
                        wifiManager.setWifiEnabled(true);
                    }

                    // Proceed if SD Card is selected Receive New Content
                    // todo apply reverse case i.e create ftp server at senders end instead receiver's as unable to copy in sd card
                    Dialog ftpConnectDialog;
                    ftpConnectDialog = new Dialog(CrlDashboard.this);
                    ftpConnectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    ftpConnectDialog.setContentView(R.layout.ftp_connect_dialog);
                    ftpConnectDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                    EditText edt_HostName = ftpConnectDialog.findViewById(R.id.edt_HostName);
                    EditText edt_Port = ftpConnectDialog.findViewById(R.id.edt_Port);
                    EditText edt_Login = ftpConnectDialog.findViewById(R.id.edt_Login);
                    EditText edt_Password = ftpConnectDialog.findViewById(R.id.edt_Password);
                    Button btn_Connect = ftpConnectDialog.findViewById(R.id.btn_Connect);
                    Button btn_Reset = ftpConnectDialog.findViewById(R.id.btn_Reset);
                    btn_Reset.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            edt_HostName.getText().clear();
                            edt_Port.getText().clear();
                            edt_Login.getText().clear();
                            edt_Password.getText().clear();
                        }
                    });
                    btn_Connect.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        public void onClick(View view) {
                            // todo connect to FTP Server
                            FTPClient temp_ftpclient = new FTPClient();

                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    if (pd != null) {
                                        pd.setMessage("Connecting ... Please wait !!!");
                                        pd.setCanceledOnTouchOutside(false);
                                        pd.show();
                                    }
                                }

                                @Override
                                protected String doInBackground(Void... voids) {
                                    // Check if already connected to PrathamHotspot
                                    String SSID = getWifiName(CrlDashboard.this).replace("\"", "");
                                    if (SSID.equalsIgnoreCase(networkSSID)) {
                                        // Connected to PrathamHotspot
                                        connected = true;
                                    } else {
                                        // todo automatically connect to PrathamHotSpot
                                        connectToPrathamHotSpot();
                                        String recheckSSID = getWifiName(CrlDashboard.this).replace("\"", "");
                                        if (recheckSSID.equalsIgnoreCase(networkSSID)) {
                                            connected = true;
                                        } else {
                                            return "notconnected";
                                        }
                                    }
                                    // todo Validate fields & if Connected to FTP Server then Open File Explorer if correct
                                    if (edt_HostName.getText().toString().trim().length() > 0
                                            && edt_Port.getText().toString().trim().length() > 0
                                            && edt_Login.getText().toString().trim().length() > 0
                                            && edt_Password.getText().toString().trim().length() > 0) {
                                        // todo if connected to FTP Server
                                        try {
                                            temp_ftpclient.connect(edt_HostName.getText().toString(), Integer.parseInt(edt_Port.getText().toString()));
                                            temp_ftpclient.login(edt_Login.getText().toString(), edt_Password.getText().toString());
                                            return "connected";
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        return "empty";
                                    }
                                    return "notfound";
                                }

                                @Override
                                protected void onPostExecute(String str) {
                                    super.onPostExecute(str);
                                    if (pd != null)
                                        pd.dismiss();
                                    switch (str) {
                                        case "connected":
                                            new AsyncTask<Void, String, Void>() {
                                                @Override
                                                protected void onPreExecute() {
                                                    super.onPreExecute();
                                                    if (pd != null) {
                                                        pd.setTitle("Downloading ...");
                                                        pd.setMessage("Downloading New Content ... Please wait !!!\nIt might take few minutes ...");
                                                        pd.setCanceledOnTouchOutside(false);
                                                        pd.show();
                                                    }
                                                }

                                                @Override
                                                protected Void doInBackground(Void... voids) {
                                                    if (temp_ftpclient != null && temp_ftpclient.isConnected()) {
                                                        // todo Download data from New Content Folder in .POSExternal
                                                        treeUriPath = PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this).getString("URI", "");
                                                        try {
                                                            // show available files
                                                            FTPFile[] fileName = temp_ftpclient.listFiles();
                                                            temp_documentFile = DocumentFile.fromTreeUri(CrlDashboard.this, Uri.parse(treeUriPath));

                                                            //create .POSExternal folder
                                                            DocumentFile documentFile1 = temp_documentFile.findFile(".POSexternal");
                                                            if (documentFile1 == null)
                                                                temp_documentFile = temp_documentFile.createDirectory(".POSexternal");
                                                            else
                                                                temp_documentFile = documentFile1;

                                                            //create New Content folder
                                                            DocumentFile documentFile2 = temp_documentFile.findFile("New Content");
                                                            if (documentFile2 == null)
                                                                temp_documentFile = temp_documentFile.createDirectory("New Content");
                                                            else
                                                                temp_documentFile = documentFile2;


                                                            for (FTPFile aFile : fileName) {
                                                                Log.d("list : ", aFile.getName());
                                                                if (aFile.isDirectory())
                                                                    downloadDirectoryToSdCard(temp_ftpclient, temp_documentFile, aFile);
                                                                else
                                                                    downloadFile(temp_ftpclient, aFile, temp_documentFile);
                                                            }

                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    return null;
                                                }

                                                // todo put in async task downdir and file
                                                private void downloadDirectoryToSdCard(FTPClient ftpClient, DocumentFile documentFile, FTPFile name) {
                                                    try {
                                                        FTPClient tempClient = ftpClient;
                                                        DocumentFile tempDocument = documentFile;
                                                        if (tempDocument.findFile(name.getName()) == null)
                                                            tempDocument = tempDocument.createDirectory(name.getName());

                                                        tempClient.changeWorkingDirectory(name.getName());
                                                        FTPFile[] subFiles = tempClient.listFiles();
                                                        Log.d("file_size::", subFiles.length + "");
                                                        if (subFiles != null && subFiles.length > 0) {
                                                            for (FTPFile aFile : subFiles) {
                                                                Log.d("name::", aFile.getName() + "");
                                                                String currentFileName = aFile.getName();
                                                                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                                                                    continue;
                                                                }
                                                                if (aFile.isDirectory()) {
                                                                    downloadDirectoryToSdCard(tempClient, tempDocument, aFile);
                                                                } else {
                                                                    downloadFile(tempClient, aFile, tempDocument);
                                                                }
                                                            }
                                                            documentFile = tempDocument.getParentFile();
                                                            tempClient.changeToParentDirectory();
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                // replace file if exists
                                                private void downloadFile(FTPClient ftpClient, FTPFile ftpFile, DocumentFile tempFile) {
                                                    try {
                                                        DocumentFile df = tempFile.findFile(ftpFile.getName());
                                                        if (df == null)
                                                            tempFile = tempFile.createFile("image", ftpFile.getName());
                                                        else
                                                            tempFile = df;

                                                        if (pd != null)
                                                            publishProgress("Downloading... " + ftpFile.getName());

                                                        OutputStream outputStream = CrlDashboard.this.getContentResolver().openOutputStream(tempFile.getUri());
                                                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                                                        ftpClient.retrieveFile(ftpFile.getName(), outputStream);
                                                    } catch (FileNotFoundException e) {
                                                        e.printStackTrace();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                protected void onProgressUpdate(String... values) {
                                                    super.onProgressUpdate(values);
                                                    if (pd != null)
                                                        pd.setMessage(values[0]);
                                                }

                                                @Override
                                                protected void onPostExecute(Void aVoid) {
                                                    super.onPostExecute(aVoid);
                                                    // todo replace config in .POSInternal jsons
                                                    // delete old json
                                                    File oldJson = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Config.json");
                                                    if (!oldJson.isFile())
                                                        oldJson.delete();
                                                    // Copy new Config.json from New Contents folder in .POSExternal
                                                    File sourceLocation = new File(splashScreenVideo.fpath + "New Content/Config.json");
                                                    File targetLocation = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Config.json");
                                                    try {
                                                        if (sourceLocation.exists()) {
                                                            InputStream in = new FileInputStream(sourceLocation);
                                                            OutputStream out = new FileOutputStream(targetLocation);
                                                            // Copy the bits from instream to outstream
                                                            byte[] buf = new byte[1024];
                                                            int len;
                                                            while ((len = in.read(buf)) > 0) {
                                                                out.write(buf, 0, len);
                                                            }
                                                            in.close();
                                                            out.close();
                                                        } else {
                                                            Toast.makeText(CrlDashboard.this, "Config.json NOT present in New Content !", Toast.LENGTH_LONG).show();
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                        Toast.makeText(CrlDashboard.this, "Config.Json file NOT copied !", Toast.LENGTH_SHORT).show();
                                                    }

                                                    // todo dismiss all dialogs
                                                    Toast.makeText(CrlDashboard.this, "Content Transferred Successfully !!!", Toast.LENGTH_LONG).show();
                                                    if (pd != null)
                                                        pd.dismiss();
                                                    onBackPressed();
                                                }
                                            }.execute();

                                            break;

                                        case "notconnected":
                                            Toast.makeText(CrlDashboard.this, "Manually connect to PrathamHotspot !!!", Toast.LENGTH_LONG).show();
                                            break;
                                        case "empty":
                                            edt_HostName.setText("");
                                            edt_Port.setText("");
                                            edt_Login.setText("");
                                            edt_Password.setText("");
                                            Toast.makeText(CrlDashboard.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                                            break;
                                        case "notfound":
                                            edt_HostName.setText("");
                                            edt_Port.setText("");
                                            edt_Login.setText("");
                                            edt_Password.setText("");
                                            Toast.makeText(CrlDashboard.this, "No ftp found on this network", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            }.execute();

                        }
                    });

                    ftpConnectDialog.setCanceledOnTouchOutside(false);
                    ftpConnectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    ftpConnectDialog.show();
                }

            }
        });

        receiveDialog.show();


    }

    public String getWifiName(Context context) {
        String ssid = null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
        }

        ssid = wifiInfo.getSSID();
        Log.d("ssaid::", ssid);
        return ssid;
    }

    private void connectToPrathamHotSpot() {
        try {
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = String.format("\"%s\"", networkSSID);
            wifiConfiguration.priority = 99999;

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            int netId = wifiManager.addNetwork(wifiConfiguration);

            if (wifiManager.isWifiEnabled()) { //---wifi is turned on---
                //---disconnect it first---
                wifiManager.disconnect();
            } else { //---wifi is turned off---
                //---turn on wifi---
                wifiManager.setWifiEnabled(true);
                wifiManager.disconnect();
            }

            wifiManager.enableNetwork(netId, true);
            try {
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wifiManager.reconnect();
            try {
                Thread.sleep(6000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        new Hotspot(MyApplication.getInstance()).stop();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                        new Hotspot(MyApplication.getInstance()).stop();
                    } else {
                        ftpConnect.turnOnOffHotspot(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public void changePrathamCode(View view) {
        final Dialog dialog = new Dialog(CrlDashboard.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setContentView(R.layout.login_code_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        dialog.getWindow().setLayout(600, 350);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });

        EditText edt_code_char = (EditText) dialog.findViewById(R.id.edt_code_char);
        EditText edt_code_no = (EditText) dialog.findViewById(R.id.edt_code_no);
        Button btn_Submit = (Button) dialog.findViewById(R.id.btn_Submit);

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_code_char.getText().toString().trim().length() == 2 && edt_code_no.getText().toString().trim().length() == 3) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    StatusDBHelper statusDBHelper = new StatusDBHelper(CrlDashboard.this);
                    statusDBHelper.Update("prathamCode", "" + edt_code_char.getText().toString().trim() + edt_code_no.getText().toString().trim());
                    BackupDatabase.backup(CrlDashboard.this);
                    String pCode = sdbh.getValue("prathamCode");
                    tv_prathamCode.setText("" + pCode);
                } else {
                    Toast.makeText(CrlDashboard.this, "Please enter valid Pratham Device Code !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

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

    // 08 Dec 2018
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == SDCardLocationChooser) {
                treeUri = data.getData();
                String path = SDCardUtil.getFullPathFromTreeUri(treeUri, this);
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                try {
                    // check path is correct or not
                    extractToSDCard(path, treeUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Toast.makeText(CrlDashboard.this, "You haven't selected anything !!!", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    // 08 Dec 2018
    // method to copy files to sd card
    private void extractToSDCard(String path, final Uri treeUri) {
        String base_path = FileUtil.getExtSdCardFolder(new File(path), CrlDashboard.this);
        if (base_path != null && base_path.equalsIgnoreCase(path)) {
            Log.d("Base path :::", base_path);
            Log.d("targetPath :::", path);
            // Path ( Selected )
            PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this)
                    .edit().putString("URI", treeUri.toString()).apply();
            PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this)
                    .edit().putString("PATH", path).apply();
            PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this).edit().putBoolean("IS_SDCARD",
                    true).apply();
            MyApplication.setPath(PreferenceManager.getDefaultSharedPreferences(CrlDashboard.this).getString("PATH", ""));

        } else {
            // Alert Dialog Call itself if wrong path selected
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CrlDashboard.this);
            //  alertDialogBuilder.setMessage("Keep your Tablet Sufficiently charged & Select External SD Card Path !!!");
            LayoutInflater factory = LayoutInflater.from(CrlDashboard.this);
            final View view = factory.inflate(R.layout.custom_alert_box_sd_card, null);
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivityForResult(intent, SDCardLocationChooser);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setTitle("Select External SD Card Location");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setView(view);
            alertDialog.show();

            Toast.makeText(CrlDashboard.this, "Please Select SD Card Only !!!", Toast.LENGTH_SHORT).show();
        }
    }

}
