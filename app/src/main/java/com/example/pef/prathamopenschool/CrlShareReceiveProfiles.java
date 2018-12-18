package com.example.pef.prathamopenschool;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.ftpSettings.FsService;
import com.example.pef.prathamopenschool.ftpSettings.WifiApControl;
import com.example.pef.prathamopenschool.interfaces.ExtractInterface;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CrlShareReceiveProfiles extends AppCompatActivity implements ExtractInterface, FTPInterface.PushPullInterface {

    StudentDBHelper sdb;
    Context context;
    GroupDBHelper gdb;
    AserDBHelper adb;
    CrlDBHelper cdb;
    Context c;
    FTPConnect ftpConnect;
    ArrayList<String> path = new ArrayList<String>();
    int res;
    private static final int DISCOVER_DURATION = 3000;
    private static final int REQUEST_BLU = 1;
    //    static BluetoothAdapter btAdapter;
    Intent intent = null;
    String packageName = null;
    public static ProgressDialog progress;
    static File file;
    boolean found = false;
    String className = null;
    Boolean FlagShareOff = false, FlagReceiveOff = false;
    File newJson;
    String ReceivePath, TargetPath, shareItPath;
    int PraDigiPath = 99;
    int SDCardLocationChooser = 7, ZipFilePicker = 9;
    String zipPath;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    Button btn_updateMedia;

    TextView tv_Students, tv_Crls, tv_Groups;

    public ProgressDialog progressDialog;

    RelativeLayout ftpDialogLayout;
    EditText edt_HostName;
    EditText edt_Port;
    Button btn_Connect;

    // Share Profiles
    List<Student> Students;
    List<Crl> Crls;
    List<Group> Groups;
    List<Aser> Asers;
    JSONArray newStudentArray, newCrlArray, newGrpArray, newAserArray;
    JSONObject stdObj, crlObj, grpObj, asrObj;
    String deviceId = "";
    StatusDBHelper stat;
    Utility util;
    TextView tv_Details;
    private ProgressDialog dialog;
    ListView lst_networks;

    private Uri treeUri;
    private String networkSSID = "PrathamHotSpot";
    public static ProgressDialog pd;
    Dialog ftpdialog;
    Switch sw_FtpServer;
    PowerManager.WakeLock wl;
    PowerManager pm;
    public static boolean shareContent = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crl_share_receive_profiles);
        getSupportActionBar().hide();

        initializeVariables();

        btn_updateMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                // Call File Choser for selectiong POS Ext Zip
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CrlShareReceiveProfiles.this);
                alertDialogBuilder.setMessage("अपना डिवाइस चार्ज रखें और Internal Storage से POSexternal.zip चुनें !!!");
                alertDialogBuilder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                chooseFile();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setTitle("POSexternal.zip फ़ाइल चुनें");
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });

    }

    @SuppressLint("InvalidWakeLockTag")
    private void initializeVariables() {
        ftpConnect = new FTPConnect(CrlShareReceiveProfiles.this, CrlShareReceiveProfiles.this, CrlShareReceiveProfiles.this);
        MainActivity.sessionFlg = false;
        playVideo = new PlayVideo();
        progressDialog = new ProgressDialog(CrlShareReceiveProfiles.this);
        sdb = new StudentDBHelper(this);
        cdb = new CrlDBHelper(this);
        gdb = new GroupDBHelper(this);
        adb = new AserDBHelper(this);
        tv_Students = (TextView) findViewById(R.id.tv_studentsShared);
        tv_Crls = (TextView) findViewById(R.id.tv_crlsShared);
        tv_Groups = (TextView) findViewById(R.id.tv_groupssShared);
        btn_updateMedia = (Button) findViewById(R.id.btn_updateMedia);
        // Memory Allocation
        sdb = new StudentDBHelper(CrlShareReceiveProfiles.this);
        cdb = new CrlDBHelper(CrlShareReceiveProfiles.this);
        gdb = new GroupDBHelper(CrlShareReceiveProfiles.this);
        adb = new AserDBHelper(CrlShareReceiveProfiles.this);
        tv_Students = (TextView) findViewById(R.id.tv_studentsShared);
        tv_Crls = (TextView) findViewById(R.id.tv_crlsShared);
        tv_Groups = (TextView) findViewById(R.id.tv_groupssShared);

        wipeSentFiles();
        tv_Students.setVisibility(View.GONE);
        tv_Crls.setVisibility(View.GONE);
        tv_Groups.setVisibility(View.GONE);

        // Wake lock for disabling sleep
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();
        pd = new ProgressDialog(this);
    }

    // Update Media
    private void chooseFile() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(CrlShareReceiveProfiles.this, properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                Log.d("path:::", files[0]);
                shareItPath = files[0];
                selectSDCard();
            }
        });
        dialog.show();
    }

    private void selectSDCard() {
        final String ReceivedFileName = shareItPath.replace("content://com.estrongs.files/storage/emulated/0/SHAREit/files/", "");
        Log.d("shareItPath ::::", shareItPath);
        Log.d("ReceivedFileName ::::", ReceivedFileName);
        // If Zip Found
        if (ReceivedFileName.endsWith("POSexternal.zip")) {
//                    String TargetPath = shareItPath.replace("POSexternal.zip","");
            String target = new File(shareItPath).getParent();
            Log.d("target::", target);
            if (PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("URI", null) == null
                    && PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("PATH", "").equals("")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CrlShareReceiveProfiles.this);
                //  alertDialogBuilder.setMessage("Keep your Tablet Sufficiently charged & Select External SD Card Path !!!");
                LayoutInflater factory = LayoutInflater.from(CrlShareReceiveProfiles.this);
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
                try {
                    new CopyFiles(shareItPath, CrlShareReceiveProfiles.this,
                            CrlShareReceiveProfiles.this).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(CrlShareReceiveProfiles.this, "Please Select POSexternal.zip file only !!!", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch student profiles (SHARE PROFILESS)
    // Function to fetch Photos filtered by Student ID
    public ArrayList<String> fetchStudentProfiles() {
        ArrayList<String> imageUrls = new ArrayList<String>();

        try {
            Students = sdb.GetAllNewStudents();
            String stdID = "";


            Uri EXTERNAL = MediaStore.Files.getContentUri("external");

            File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles");
            File[] listFile;
            if (folder.isDirectory()) {
                listFile = folder.listFiles();
                for (int i = 0; i < listFile.length; i++) {
                    Uri uri = Uri.fromFile(listFile[i]);
                    String fileNameWithExtension = uri.getLastPathSegment();
                    String[] fileName = fileNameWithExtension.split("\\.");
                    Log.d("img_file_name::", listFile[i].getAbsolutePath());

                    for (int j = 0; j < Students.size(); j++) {
                        stdID = String.valueOf(Students.get(j).StudentID);
                        if (fileName[0].equals(stdID)) {
                            imageUrls.add(String.valueOf(uri));
                            break;
                        }
                    }
                }
            }
            Log.d("img_file_size::", "" + imageUrls.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUrls;
    }

    // Write Settings for Share Profiles
    public void WriteSettings(Context context, String data, String fName) {

        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        try {
            String MainPath = Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/" + fName + ".json";
            File file = new File(MainPath);
            try {
                path.add(MainPath);
                fOut = new FileOutputStream(file);
                osw = new OutputStreamWriter(fOut);
                osw.write(data);
                osw.flush();
                osw.close();
                fOut.close();

            } catch (Exception e) {
            } finally {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Settings not saved", Toast.LENGTH_SHORT).show();
        }
    }


    // Share Profiles Send Functions
    public void sendNewStudent() {

        newStudentArray = new JSONArray();
        Students = sdb.GetAllNewStudents();

        if (Students == null || Students.isEmpty()) {
            //   Toast.makeText(ShareProfiles.this, "There are No new Students !!!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                if (Students == null) {
                } else {
                    for (int x = 0; x < Students.size(); x++) {
                        stdObj = new JSONObject();
                        Student std = Students.get(x);
                        stdObj.put("StudentID", std.StudentID);
                        stdObj.put("FirstName", std.FirstName);
                        stdObj.put("MiddleName", std.MiddleName);
                        stdObj.put("LastName", std.LastName);
                        Integer age = std.Age;
                        stdObj.put("Age", age == null ? 0 : std.Age);
                        Integer cls = std.stdClass;
                        stdObj.put("Class", cls == null ? 0 : std.stdClass);
                        stdObj.put("UpdatedDate", std.UpdatedDate);
                        stdObj.put("Gender", std.Gender.equals(null) ? "Male" : std.Gender);
                        stdObj.put("GroupID", std.GroupID.equals(null) ? "GroupID" : std.GroupID);
                        stdObj.put("CreatedBy", std.CreatedBy.equals(null) ? "CreatedBy" : std.CreatedBy);
                        stdObj.put("NewFlag", "true");
                        stdObj.put("StudentUID", std.StudentUID.equals(null) ? "" : std.StudentUID);
                        stdObj.put("IsSelected", std.IsSelected == null ? false : std.IsSelected);

                        // new entries
                        stat = new StatusDBHelper(CrlShareReceiveProfiles.this);
                        stdObj.put("sharedBy", stat.getValue("AndroidID"));
                        stdObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        stdObj.put("appVersion", stat.getValue("apkVersion"));
                        stdObj.put("appName", stat.getValue("appName"));
                        stdObj.put("CreatedOn", std.CreatedOn == null ? "" : std.CreatedOn);

                        newStudentArray.put(stdObj);
                    }

                    String requestString = String.valueOf(newStudentArray);

                    WriteSettings(getApplicationContext(), requestString, "Student");
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    }

    public void sendNewCrl() {

        newCrlArray = new JSONArray();
        Crls = cdb.GetAllNewCrl();

        if (Crls == null || Crls.isEmpty()) {
        } else {
            if (Crls == null) {
            } else {
                try {
                    for (int x = 0; x < Crls.size(); x++) {
                        crlObj = new JSONObject();
                        Crl crl = Crls.get(x);
                        crlObj.put("CRLID", crl.CRLId);
                        crlObj.put("FirstName", crl.FirstName);
                        crlObj.put("LastName", crl.LastName);
                        crlObj.put("UserName", crl.UserName);
                        crlObj.put("PassWord", crl.Password);
                        Integer pid = crl.ProgramId;
                        crlObj.put("ProgramId", pid == null ? 0 : crl.ProgramId);
                        crlObj.put("Mobile", crl.Mobile);
                        crlObj.put("State", crl.State);
                        crlObj.put("Email", crl.Email);
                        crlObj.put("CreatedBy", crl.CreatedBy.equals(null) ? "Created By" : crl.CreatedBy);
                        crlObj.put("NewFlag", crl.newCrl == null ? false : !crl.newCrl);

                        // new entries
                        stat = new StatusDBHelper(CrlShareReceiveProfiles.this);
                        crlObj.put("sharedBy", stat.getValue("AndroidID"));
                        crlObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        crlObj.put("appVersion", stat.getValue("apkVersion"));
                        crlObj.put("appName", stat.getValue("appName"));
                        crlObj.put("CreatedOn", crl.CreatedOn == null ? "" : crl.CreatedOn);

                        newCrlArray.put(crlObj);
                    }

                    String requestString = String.valueOf(newCrlArray);

                    WriteSettings(getApplicationContext(), requestString, "Crl");
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        }
    }

    public void sendNewGroup() {
        newGrpArray = new JSONArray();
        Groups = gdb.GetAllNewGroups();

        if (Groups == null || Groups.isEmpty()) {
        } else {
            try {
                if (Groups == null) {
                } else {
                    for (int x = 0; x < Groups.size(); x++) {
                        grpObj = new JSONObject();
                        Group grp = Groups.get(x);
                        grpObj.put("GroupID", grp.GroupID);
                        grpObj.put("GroupCode", grp.GroupCode);
                        grpObj.put("GroupName", grp.GroupName);
                        grpObj.put("UnitNumber", grp.UnitNumber);
                        grpObj.put("DeviceID", grp.DeviceID.equals(null) ? "DeviceID" : grp.DeviceID);
                        grpObj.put("Responsible", grp.Responsible);
                        grpObj.put("ResponsibleMobile", grp.ResponsibleMobile);
                        Integer vid = grp.VillageID;
                        grpObj.put("VillageID", vid == null ? 0 : grp.VillageID);
                        Integer pid = grp.ProgramID;
                        grpObj.put("ProgramId", pid == null ? 0 : grp.ProgramID);
                        grpObj.put("CreatedBy", grp.CreatedBy);
                        grpObj.put("NewFlag", !grp.newGroup);
                        grpObj.put("VillageName", grp.VillageName.equals(null) ? "" : grp.VillageName);
                        grpObj.put("SchoolName", grp.SchoolName.equals(null) ? "" : grp.SchoolName);

                        // new entries
                        stat = new StatusDBHelper(CrlShareReceiveProfiles.this);
                        grpObj.put("sharedBy", stat.getValue("AndroidID"));
                        grpObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        grpObj.put("appVersion", stat.getValue("apkVersion"));
                        grpObj.put("appName", stat.getValue("appName"));
                        grpObj.put("CreatedOn", grp.CreatedOn == null ? "" : grp.CreatedOn);

                        newGrpArray.put(grpObj);
                    }

                    String requestString = String.valueOf(newGrpArray);

                    WriteSettings(getApplicationContext(), requestString, "Group");
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    }

    public void sendNewAser() {
        newAserArray = new JSONArray();
        Asers = adb.GetAllNewAserGroups();

        if (Asers == null || Asers.isEmpty()) {
        } else {
            try {
                if (Asers == null) {
                } else {

                    for (int x = 0; x < Asers.size(); x++) {

                        asrObj = new JSONObject();

                        Aser asr = Asers.get(x);

                        asrObj.put("StudentId", asr.StudentId);
                        asrObj.put("ChildID", asr.ChildID);
                        asrObj.put("GroupID", asr.GroupID);
                        asrObj.put("TestType", asr.TestType);
                        asrObj.put("TestDate", asr.TestDate);
                        asrObj.put("Lang", asr.Lang);
                        asrObj.put("Num", asr.Num);
                        asrObj.put("OAdd", asr.OAdd);
                        asrObj.put("OSub", asr.OSub);
                        asrObj.put("OMul", asr.OMul);
                        asrObj.put("ODiv", asr.ODiv);
                        asrObj.put("WAdd", asr.WAdd);
                        asrObj.put("WSub", asr.WSub);
                        asrObj.put("CreatedBy", asr.CreatedBy.equals(null) ? "" : asr.CreatedBy);
                        asrObj.put("CreatedDate", asr.CreatedDate);
                        asrObj.put("DeviceId", asr.DeviceId.equals(null) ? "" : asr.DeviceId);
                        asrObj.put("FLAG", asr.FLAG);

                        // new entries
                        stat = new StatusDBHelper(CrlShareReceiveProfiles.this);
                        asrObj.put("sharedBy", stat.getValue("AndroidID"));
                        asrObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        asrObj.put("appVersion", stat.getValue("apkVersion"));
                        asrObj.put("appName", stat.getValue("appName"));
                        asrObj.put("CreatedOn", asr.CreatedOn == null ? "" : asr.CreatedOn);

                        newAserArray.put(asrObj);

                    }

                    String requestString = String.valueOf(newAserArray);

                    WriteSettings(getApplicationContext(), requestString, "Aser");
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    }

    // Share profiles function copyStdProfiles
    public void copyStdProfilesTosharableContent(ArrayList<String> fetchedStudents) throws IOException {
        String fileName = "";
        //String targetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/";
        for (int k = 0; k < fetchedStudents.size(); k++) {
            fileName = fetchedStudents.get(k);
            fileCopyFunction(fileName);
        }
    }

    public void fileCopyFunction(String fileName) throws IOException {
        try {
            File sourceFile = new File(new URI(fileName));
            File destinationFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/" + sourceFile.getName());
            path.add(destinationFile.getPath());

            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);

            int bufferSize;
            byte[] bufffer = new byte[512];
            while ((bufferSize = fileInputStream.read(bufffer)) > 0) {
                fileOutputStream.write(bufffer, 0, bufferSize);
            }
            fileInputStream.close();
            fileOutputStream.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }


    // Share Profiles
    public void transferData() {
//        Thread mThread = new Thread() {
//            @Override
//            public void run() {
//        Utility.showDialog(CrlShareReceiveProfiles.this);
        dialog = new ProgressDialog(CrlShareReceiveProfiles.this);
        dialog.setMessage("Please wait ... or Press back button again to Cancel !!! ");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        File zipFolder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent");
        if (zipFolder.exists()) {
            wipeSentFiles();
        }

        path.clear();
        ArrayList<String> fetchedStudents = fetchStudentProfiles();
        try {
            copyStdProfilesTosharableContent(fetchedStudents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Files are created
        // todo check null for all values
        sendNewStudent();
        sendNewGroup();
        sendNewCrl();
        sendNewAser();


        // todo dont allow next process if everything is empty
        if (Students.isEmpty() && Asers.isEmpty() && Groups.isEmpty() && Crls.isEmpty()) {
        } else {
            // Creating Json Zip
            try {
                String paths[] = new String[path.size()];
                int size = path.size();
                for (int i = 0; i < size; i++) {
                    paths[i] = path.get(i);
                }
                // Compressing Files
                Compress mergeFiles = new Compress(paths, Environment.getExternalStorageDirectory()
                        + "/.POSinternal/sharableContent/NewProfiles_" + Build.SERIAL + ".zip");
                mergeFiles.zip();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (dialog != null) {
                    dialog.dismiss();
                }

//                Utility.dismissDialog();
            }
//                    TreansferFile("NewProfiles");

        }
    }
//};
//        mThread.start();
//    }


    // Share Profiles Function
    public void goToShareProfiles(View view) {
//        Intent goToShareD = new Intent(CrlShareReceiveProfiles.this, ShareProfiles.class);
//        startActivity(goToShareD);

        // SHARE PROFILES
        // Transfer Newly created entries

        // Generate Device ID
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        util = new Utility();

        // Memory Allocation
        sdb = new StudentDBHelper(CrlShareReceiveProfiles.this);
        cdb = new CrlDBHelper(CrlShareReceiveProfiles.this);
        gdb = new GroupDBHelper(CrlShareReceiveProfiles.this);
        adb = new AserDBHelper(CrlShareReceiveProfiles.this);

        tv_Students = (TextView) findViewById(R.id.tv_studentsShared);
        tv_Crls = (TextView) findViewById(R.id.tv_crlsShared);
        tv_Groups = (TextView) findViewById(R.id.tv_groupssShared);

        transferData();


        // FTP
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }
        // Display ftp dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_visible_wifi_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        ftpDialogLayout = dialog.findViewById(R.id.ftpDialog);
        lst_networks = dialog.findViewById(R.id.lst_network);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Onlistener
        ArrayList<String> networkList = ftpConnect.scanNearbyWifi();
        lst_networks.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.lst_wifi_item, R.id.label, networkList));

        ImageButton refresh = dialog.findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Onlistener
                ArrayList<String> networkList = ftpConnect.scanNearbyWifi();
                Log.d("Network List :::", String.valueOf(networkList));
                lst_networks.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.lst_wifi_item, R.id.label, networkList));
            }
        });

        // listening to single list item on click
        lst_networks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String ssid = ((TextView) view).getText().toString();
//                connectToWifi(ssid);
                // check if pratham hotspot selected or not
                if (ssid.contains("PrathamHotSpot_")) {
                    // connect to wifi
                    ftpConnect.connectToPrathamHotSpot(ssid);
                    dialog.dismiss();
                    Toast.makeText(CrlShareReceiveProfiles.this, "Wifi SSID : " + ssid, Toast.LENGTH_SHORT).show();

                    // Display ftp dialog
                    Dialog dialog = new Dialog(CrlShareReceiveProfiles.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.connect_to_ftpserver_dialog);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                    ftpDialogLayout = dialog.findViewById(R.id.ftpDialog);
                    edt_HostName = dialog.findViewById(R.id.edt_HostName);
                    edt_Port = dialog.findViewById(R.id.edt_Port);
                    btn_Connect = dialog.findViewById(R.id.btn_Connect);
                    tv_Details = dialog.findViewById(R.id.tv_details);

                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(true);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();

                    btn_Connect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            WifiInfo info = wifiManager.getConnectionInfo();
                            String ssid = info.getSSID();
                            if (ssid.contains("PrathamHotSpot_"))
                                ftpConnect.connectFTPHotspot("TransferProfiles", "192.168.43.1", "8080");
                            else
                                Toast.makeText(CrlShareReceiveProfiles.this, "Connected to Wrong Network !!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CrlShareReceiveProfiles.this, "Invalid Network !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // Receive Profiles Function
    public void ReceiveProfiles(View view) {

        // file picker
//        DialogProperties properties = new DialogProperties();
//        properties.selection_mode = DialogConfigs.SINGLE_MODE;
//        properties.selection_type = DialogConfigs.FILE_SELECT;
//        properties.root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
//        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
//        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
//        properties.extensions = null;
//        FilePickerDialog dialog = new FilePickerDialog(CrlShareReceiveProfiles.this, properties);
//        dialog.setTitle("Select a File");
//        dialog.setDialogSelectionListener(new DialogSelectionListener() {
//            @Override
//            public void onSelectedFilePaths(String[] files) {
//                //files is the array of the paths of files selected by the Application User.
//                Log.d("path:::", files[0]);
//                shareItPath = files[0];
//                recieveProfiles(shareItPath);
//            }
//        });
//        dialog.show();
//        ftpConnect.connectFTPHotspot("ReceiveProfiles");
        //todo recieve zips and extract
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");
//        startActivityForResult(intent, 5);
// Display ftp dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.connect_to_ftpserver_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        ftpDialogLayout = dialog.findViewById(R.id.ftpDialog);
        edt_HostName = dialog.findViewById(R.id.edt_HostName);
        edt_Port = dialog.findViewById(R.id.edt_Port);
        btn_Connect = dialog.findViewById(R.id.btn_Connect);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ftpConnect.connectFTPHotspot("TransferProfiles", edt_HostName.getText().toString(), edt_Port.getText().toString());
            }
        });
    }

    // receive profiles after picker code
    private void recieveProfiles(String recieveProfilePath) {
//        try {
//            recieveProfilePath = SDCardUtil.getRealPathFromURI(CrlShareReceiveProfiles.this, data.getData());
//        } catch (Exception e) {
//            e.getMessage();
//        }
        TargetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/";
// Checking that file is appropriate or not
//content://com.estrongs.files/storage/emulated/0/SHAREit/files/NewProfiles.zip
        final String ReceivedFileName = recieveProfilePath.replace("content://com.estrongs.files/storage/emulated/0/SHAREit/files/", "");

        if (recieveProfilePath.endsWith("NewProfiles.zip")) {

//            new RecieveFiles(TargetPath, recieveProfilePath).execute();
/*  //Checking if src file exist or not (pravin)
                    newProfile = new File(shareItPath);
                    if (!newProfile.exists()) {
                        Toast.makeText(this, "NewProfile.zip not exist", Toast.LENGTH_SHORT).show();
                    } else */
            {


//                progressDialog.setCancelable(false);
//                progressDialog.setMessage("Receiving Profiles");
//                progressDialog.show();

//                        Thread mThread = new Thread() {
//                            @Override
//                            public void run() {


//                CrlShareReceiveProfiles.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(c, "Files Received & Updated in Database Successfully !!!", Toast.LENGTH_SHORT).show();
//                    }
//                });


//                progressDialog.dismiss();

//                CrlShareReceiveProfiles.this.runOnUiThread(new Runnable() {
//                    public void run() {
//
//                         Display Count
//                        tv_Students.setVisibility(View.VISIBLE);
//                        tv_Crls.setVisibility(View.VISIBLE);
//                        tv_Groups.setVisibility(View.VISIBLE);
//                        int crl = crlJsonArray == null ? 0 : crlJsonArray.length();
//                        int std = studentsJsonArray == null ? 0 : studentsJsonArray.length();
//                        int grp = grpJsonArray == null ? 0 : grpJsonArray.length();
//                        tv_Students.setText("Students Received : " + std);
//                        tv_Crls.setText("CRLs Received : " + crl);
//                        tv_Groups.setText("Groups Received : " + grp);
//
//                        Toast.makeText(c, "Profiles received", Toast.LENGTH_LONG).show();
//                    }
//                });
//                            }
//                        };
//                        mThread.start();
            }
        } else {
            Toast.makeText(CrlShareReceiveProfiles.this, "You Have Selected Wrong File !!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void wipeReceivedData() {

        // Delete Receive Folder Contents after Transferring
        try {
            String directoryToDelete = Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage";

            File dir = new File(directoryToDelete);
            for (File file : dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
        } catch (Exception e) {
            e.getMessage();
        }

        // Delete NewProfiles.zip from ShareIt folder
        // Receive Path = ShareIt Path
        try {
            String ShareItPath = ReceivePath.replace("NewProfiles.zip", "");
            File delNewProfilesFromShareIt = new File(ShareItPath);

            for (File zipfile : delNewProfilesFromShareIt.listFiles()) {
                if (!zipfile.isDirectory()) {
                    if (zipfile.getName().contains(".zip"))
                        zipfile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Delete NewProfiles.zip from Bluetooth folder

        try {

            String bluetoothDirectory = Environment.getExternalStorageDirectory() + "/bluetooth";
            File delNewProfiles = new File(bluetoothDirectory);

            for (File zipfile : delNewProfiles.listFiles()) {
                if (!zipfile.isDirectory()) {
                    if (zipfile.getName().contains(".zip"))
                        zipfile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void wipeReceivedJsonData() {
        // Delete NewProfiles.zip from Bluetooth folder
        String bluetoothDirectory = Environment.getExternalStorageDirectory() + "/bluetooth";
        File delNewProfiles = new File(bluetoothDirectory);
        for (File zipfile : delNewProfiles.listFiles()) {
            if (!zipfile.isDirectory()) {
                if (zipfile.getName().contains(".zip"))
                    zipfile.delete();
            }
        }

    }

    @Override
    protected void onDestroy() {
        wl.release();
        shareContent = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        shareContent = false;

        if (ftpConnect.checkServiceRunning()) {
            ftpConnect.stopServer();
        }
        // Delete Received Files
        try {
//            wipeReceivedData();
        } catch (Exception e) {
            e.getMessage();
        }

        if (FlagShareOff == true) {
            // Delete Sent Json Zip File
            try {
                wipeSharedJson();
            } catch (Exception e) {

            }
        }

        if (FlagReceiveOff == true) {
            try {
                wipeReceivedJsonData();
            } catch (Exception e) {

            }
        }
        finish();

        /*// Going on Admin Login Page
        Intent intent = new Intent(this, CrlDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();*/
    }


    private void wipeSharedJson() {


        // Delete NewJson.zip from Json folder
        String bluetoothDirectory = Environment.getExternalStorageDirectory() + "/.POSinternal/Json/";
        File delNewProfiles = new File(bluetoothDirectory);
        for (File zipfile : delNewProfiles.listFiles()) {
            if (!zipfile.isDirectory()) {
                if (zipfile.getName().contains(".zip"))
                    zipfile.delete();
            }
        }

    }

    // Update Json in Device's Database when Received new files from another Device

    /************************************************* SHARE OFFLINE ********************************************************************/


    public void goToShareOff(View view) {

        // Creating Share off json

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }
        // Display ftp dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_visible_wifi_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        ftpDialogLayout = dialog.findViewById(R.id.ftpDialog);
        lst_networks = dialog.findViewById(R.id.lst_network);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Onlistener
        ArrayList<String> networkList = ftpConnect.scanNearbyWifi();

        lst_networks.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.lst_wifi_item, R.id.label, networkList));

        ImageButton refresh = dialog.findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Onlistener
                ArrayList<String> networkList = ftpConnect.scanNearbyWifi();
                Log.d("Network List :::", String.valueOf(networkList));
                lst_networks.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.lst_wifi_item, R.id.label, networkList));
            }
        });
        // listening to single list item on click
        lst_networks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String ssid = ((TextView) view).getText().toString();
//                connectToWifi(ssid);
                // check if pratham hotspot selected or not
                if (ssid.contains("PrathamHotSpot_")) {
                    // connect to wifi
                    ftpConnect.connectToPrathamHotSpot(ssid);
                    dialog.dismiss();
                    Toast.makeText(CrlShareReceiveProfiles.this, "Wifi SSID : " + ssid, Toast.LENGTH_SHORT).show();
                    // Delay of 2 secs for connecting
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                        }
                    }, 2000);

                    // Display ftp dialog
                    Dialog dialog = new Dialog(CrlShareReceiveProfiles.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.connect_to_ftpserver_dialog);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                    ftpDialogLayout = dialog.findViewById(R.id.ftpDialog);
                    edt_HostName = dialog.findViewById(R.id.edt_HostName);
                    edt_Port = dialog.findViewById(R.id.edt_Port);
                    btn_Connect = dialog.findViewById(R.id.btn_Connect);
                    tv_Details = dialog.findViewById(R.id.tv_details);

                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(true);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();

                    btn_Connect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            WifiInfo info = wifiManager.getConnectionInfo();
                            String ssid = info.getSSID();
                            if (ssid.contains("PrathamHotSpot_"))
                                ftpConnect.connectFTPHotspot("TransferJson", "192.168.43.1", "8080");
                            else
                                Toast.makeText(CrlShareReceiveProfiles.this, "Connected to Wrong Network !!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CrlShareReceiveProfiles.this, "Invalid Network !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        FlagShareOff = true;
//        File newJason = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/NewJson.zip");
//        if (newJason.exists()) {
//            newJason.delete();
//        }
//
//        MultiPhotoSelectActivity.dilog.showDilog(c, "Collecting data for transfer");

//        Thread mThread = new Thread() {
//            @Override
//            public void run() {
//                // Creating Json Zip
//                try {
//
//                    path.add(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Crl.json");
//                    path.add(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Group.json");
//                    path.add(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Student.json");
//                    path.add(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Village.json");
//                    path.add(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Config.json");
//
//                    String paths[] = new String[path.size()];
//                    for (int i = 0; i < path.size(); i++) {
//                        paths[i] = path.get(i);
//                    }
//                    // Compressing Files
//                    Compress mergeFiles = new Compress(paths, Environment.getExternalStorageDirectory() + "/.POSinternal/Json/NewJson.zip");
//                    mergeFiles.zip();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                MultiPhotoSelectActivity.dilog.dismissDilog();
//                CrlShareReceiveProfiles.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(CrlShareReceiveProfiles.this, " Data collected Successfully !!!", Toast.LENGTH_SHORT).show();
//                        // Transferring Created Zip
//        TreansferFile("NewJson");

//                    }
//                });
//            }
//        };
//        mThread.start();
    }

    public void TreansferFile(String filename) {

        int resultCode = 1;
//        res = resultCode;
//        if (res == 0) {
//            if (btAdapter.isEnabled()) {
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                }, 30000);
//            }
//        } else if (!(resultCode == DISCOVER_DURATION && REQUEST_BLU == 1)) {
//            // Toast.makeText(this, "BT cancelled", Toast.LENGTH_SHORT).show();
//        }

//        btAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (btAdapter == null) {
//            Toast.makeText(getApplicationContext(), "This device doesn't give bluetooth support.", Toast.LENGTH_LONG).show();
//        } else {
//            intent = new Intent();
//            intent.setAction(Intent.ACTION_SEND);
//            intent.setType("text/plain");
        file = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json"/* + filename + ".zip"*/);

        int x = 0;
        if (file.exists()) {

//                PackageManager pm = getPackageManager();
//                List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);
//                if (appsList.size() > 0) {

//                    for (ResolveInfo info : appsList) {
//                        packageName = info.activityInfo.packageName;
//                        if (packageName.equals("com.android.bluetooth")) {
//                            className = info.activityInfo.name;
//                            found = true;
//                            break;// found
//                        }
//                    }
//                    if (!found) {
//                        Toast.makeText(this, "Bluetooth not in list", Toast.LENGTH_SHORT).show();
//                    } else {
            MyApplication.setPath(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/");
//            ftpConnect.createFTPHotspot();

            //todo ftp connect dialog same for every recieve
            //todo show count on that dialog
//                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                        intent.setClassName(packageName, className);
//                        startActivityForResult(intent, 0);
            //sendBroadcast(intent);
//                    }
//                }
        } else
            Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG).show();
//        }
    }


    /*-------------------------------------------------------------------------------------------*/


// Receive Json Offline Function

    public void goToReceiveOff(View view) {

        // Display ftp dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.connect_to_ftpserver_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        ftpDialogLayout = dialog.findViewById(R.id.ftpDialog);
        edt_HostName = dialog.findViewById(R.id.edt_HostName);
        edt_Port = dialog.findViewById(R.id.edt_Port);
        btn_Connect = dialog.findViewById(R.id.btn_Connect);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ftpConnect.connectFTPHotspot("TransferJson", edt_HostName.getText().toString(), edt_Port.getText().toString());
            }
        });


        //todo connect ftp hotspot and download files
//        Toast.makeText(CrlShareReceiveProfiles.this, "Receive Offline Clicked !!!", Toast.LENGTH_SHORT).show();

//        FlagReceiveOff = true;

        // Path Declaration
//        ReceivePath = Environment.getExternalStorageDirectory() + "/bluetooth/NewJson.zip";
//        TargetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/Json/";

        //Checking if src file exist or not (pravin)
//        newJson = new File(ReceivePath);
//        if (!newJson.exists()) {
//            Toast.makeText(this, "NewJson.zip not exist", Toast.LENGTH_SHORT).show();
//        } else {
//            MultiPhotoSelectActivity.dilog.showDilog(c, "Collecting transfered data");
//
//            Thread mThread = new Thread() {
//                @Override
//                public void run() {
//                    wipeJsonFolder();
//
//                    // Extraction of contents
//                    Compress extract = new Compress();
//                    List<String> unzippedFileNames = extract.unzip(ReceivePath, TargetPath);
//
//                    MultiPhotoSelectActivity.dilog.dismissDilog();
//
//                    Runtime rs = Runtime.getRuntime();
//                    rs.freeMemory();
//                    rs.gc();
//                    rs.freeMemory();
//
//                    CrlShareReceiveProfiles.this.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(CrlShareReceiveProfiles.this, "Files Received & Updated Successfully !!!", Toast.LENGTH_SHORT).show();
//                            newJson.delete();
//
//                            // Update DB
//
//                            try {
//                                // Add Initial Entries of CRL & Village Json to Database
//                                SetInitialValuesReceiveOff();
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//
//                        }
//                    });
//                }
//            };
//            mThread.start();
//        }

    }


    // Delete Sent Files
    private void wipeSentFiles() {
        try {
            // Delete Files after Sending
            String directoryToDelete = Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent";
            File dir = new File(directoryToDelete);
            for (File file : dir.listFiles())
//                if (!file.isDirectory())
//                    file.delete();
//                else
                file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == 0) {
                Intent crlsharercv = new Intent(this, CrlShareReceiveProfiles.class);
                finish();
                startActivity(crlsharercv);
            } else if (requestCode == 5) {
                // Receive Profiles
                // Path Declaration
            } else if (requestCode == SDCardLocationChooser) {
                Uri treeUri = data.getData();
                String path = SDCardUtil.getFullPathFromTreeUri(treeUri, this);
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                try {
                    // check path is correct or not
                    extractToSDCard(path, treeUri, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PraDigiPath) {
                if (data != null && data.getData() != null) {
                    treeUri = data.getData();
                    String path = SDCardUtil.getFullPathFromTreeUri(treeUri, this);
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                    try {
                        // check path is correct or not
                        extractToSDCard(path, treeUri, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(this, "SD Card Selected !!!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Please select SD Card!!!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CrlShareReceiveProfiles.this, "You haven't selected anything !!!", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    // method to copy files to sd card
    private void extractToSDCard(String path, final Uri treeUri, int option) {
        String base_path = FileUtil.getExtSdCardFolder(new File(path), CrlShareReceiveProfiles.this);
        if (base_path != null && base_path.equalsIgnoreCase(path)) {
            Log.d("Base path :::", base_path);
            Log.d("targetPath :::", path);
            // Path ( Selected )
            PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this)
                    .edit().putString("URI", treeUri.toString()).apply();
            PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this)
                    .edit().putString("PATH", path).apply();
            PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).edit().putBoolean("IS_SDCARD",
                    true).apply();
            MyApplication.setPath(PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("PATH", ""));

            if (option == 1)
                new CopyFiles(shareItPath, CrlShareReceiveProfiles.this, CrlShareReceiveProfiles.this).execute();
        } else {
            // Alert Dialog Call itself if wrong path selected
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CrlShareReceiveProfiles.this);
            //  alertDialogBuilder.setMessage("Keep your Tablet Sufficiently charged & Select External SD Card Path !!!");
            LayoutInflater factory = LayoutInflater.from(CrlShareReceiveProfiles.this);
            final View view = factory.inflate(R.layout.custom_alert_box_sd_card, null);
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            if (option == 1)
                                startActivityForResult(intent, SDCardLocationChooser);
                            else if (option == 0)
                                startActivityForResult(intent, PraDigiPath);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setTitle("Select External SD Card Location");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setView(view);
            alertDialog.show();

            Toast.makeText(CrlShareReceiveProfiles.this, "Please Select SD Card Only !!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FTP
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
        Boolean serviceRunning = checkServiceRunning();
        // check service is running or not
        if (ftpdialog != null)
            if (ftpdialog.isShowing())
                if (!serviceRunning)
                    sw_FtpServer.setChecked(false);

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


    String folder_path = "";

    @Override
    public void onExtractDone(String zipPath) {
        //folder_path = filepath;
        dismissProgress();
        this.zipPath = zipPath;
        try {
            new UnZipTask(CrlShareReceiveProfiles.this, shareItPath).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showDialog() {
        // Manually connect to PrathamHotSpot if not connected to PrathamHotSpot
        Snackbar snackbar = Snackbar
                .make(ftpDialogLayout, "Manually connect to PrathamHotspot !!!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                        intent.setComponent(cn);
                        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    @Override
    public void onFilesRecievedComplete(String typeOfFile, String filename) {
//        if (typeOfFile.equalsIgnoreCase("ReceiveProfiles")) {
//            TargetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/";
//            File NewProfilesExists = new File(TargetPath + "NewProfiles.zip");
//            if (NewProfilesExists.exists())
//                new RecieveFiles(TargetPath, NewProfilesExists.getAbsolutePath()).execute();
//
//        } else if (typeOfFile.equalsIgnoreCase("ReceiveJson")) {
//            // Update DB
//            try {
//                // Add Initial Entries of CRL & Village Json to Database
//                SetInitialValuesReceiveOff();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        if (typeOfFile.equalsIgnoreCase("TransferProfiles")) {
            // Display Count
            int std = Students.size();
            int crl = Crls.size();
            int grp = Groups.size();
            tv_Details.setText("\nStudents Shared : " + std + "\nCRLs Shared : " + crl + "\nGroups Shared : " + grp);
        } else {
            String path = Environment.getExternalStorageDirectory().toString() + "/.POSinternal/Json";
            File directory = new File(path);
            File[] files = directory.listFiles();
            int cnt = 0;
            String fileName = "";
            for (int i = 0; i < files.length; i++) {
                fileName += "\n" + files[i].getName() + "   " + Integer.parseInt(String.valueOf(files[i].length() / 1024)) + " kb";
                cnt++;
            }
            tv_Details.setText("\nFiles Transferred : " + cnt + fileName);
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equalsIgnoreCase("RecieveFiles")) {
//            int crl = crlJsonArray == null ? 0 : crlJsonArray.length();
//            int std = studentsJsonArray == null ? 0 : studentsJsonArray.length();
//            int grp = grpJsonArray == null ? 0 : grpJsonArray.length();
//            if (tv_Students != null) {
//                tv_Students.setVisibility(View.VISIBLE);
//                tv_Students.setText("Students Received : " + std);
//            }
//            if (tv_Crls != null) {
//                tv_Crls.setVisibility(View.VISIBLE);
//                tv_Crls.setText("CRLs Received : " + crl);
//            }
//            if (tv_Groups != null) {
//                tv_Groups.setVisibility(View.VISIBLE);
//                tv_Groups.setText("Groups Received : " + grp);
//            }
        }
    }

    // Share New Content
    public void shareContent(View view) {
        // start Wifi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }
        // choose sd card
        if (PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("URI", null) == null
                && PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("PATH", "").equals("")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CrlShareReceiveProfiles.this);
            LayoutInflater factory = LayoutInflater.from(CrlShareReceiveProfiles.this);
            view = factory.inflate(R.layout.custom_alert_box_sd_card, null);
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivityForResult(intent, PraDigiPath);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setTitle("एसडी कार्ड स्थान का चयन करें");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setView(view);
            alertDialog.show();

        } else {
            // SD Card Selected
            // check new content available or not
            File newContentFolder = new File(PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("PATH", "") + "/.POSExternal/New Content");
            if (newContentFolder.exists()) {
                // todo Share New Content
                MyApplication.setPath(PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this).getString("PATH", "") + "/.POSExternal/New Content/");

                String pat = MyApplication.getPath();
                ftpdialog = new Dialog(CrlShareReceiveProfiles.this);
                ftpdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                ftpdialog.setContentView(R.layout.start_ftp_switch);
                ftpdialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                TextView tv_note = ftpdialog.findViewById(R.id.tv_note);
                sw_FtpServer = ftpdialog.findViewById(R.id.btn_ftpSettings);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    tv_note.setVisibility(View.VISIBLE);
                else
                    tv_note.setVisibility(View.GONE);

                // Switch Press Action
                sw_FtpServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked == true) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if (!Settings.System.canWrite(getApplicationContext())) {
                                    sw_FtpServer.setChecked(false);
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, 200);
                                } else {
                                    CreateWifiAccessPointOnHigherAPI createOneHAPI = new CreateWifiAccessPointOnHigherAPI();
                                    createOneHAPI.execute((Void) null);
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                                if (!Settings.System.canWrite(getApplicationContext())) {
                                    sw_FtpServer.setChecked(false);
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, 200);
                                } else {
                                    CreateWifiAccessPointOnHigherAPI createOneHAPI = new CreateWifiAccessPointOnHigherAPI();
                                    createOneHAPI.execute((Void) null);
                                }
                            } else {
                                CrlShareReceiveProfiles.CreateWifiAccessPoint createOne = new CrlShareReceiveProfiles.CreateWifiAccessPoint();
                                createOne.execute((Void) null);
                            }
                        } else if (isChecked == false) {
                            // Stop Hotspot
                            turnOnOffHotspot(CrlShareReceiveProfiles.this, false);
                            // Stop Server
                            stopServer();
                            Toast.makeText(CrlShareReceiveProfiles.this, "Server Stopped!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ftpdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (sw_FtpServer.isChecked()) {
                            // Stop Hotspot
                            turnOnOffHotspot(CrlShareReceiveProfiles.this, false);
                            // Stop Server
                            stopServer();
                            Toast.makeText(CrlShareReceiveProfiles.this, "Server Stopped!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ftpdialog.setCanceledOnTouchOutside(false);
                ftpdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                ftpdialog.show();
            } else {
                Toast.makeText(this, "You Don't have New Content Folder !!!", Toast.LENGTH_LONG).show();
            }

        }

    }


    private class CreateWifiAccessPointOnHigherAPI extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(CrlShareReceiveProfiles.this);
            pd.setMessage("Starting Server ... Please wait !!!");
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            setWifiTetheringEnabled(true);
            return null;
        }

        public void setWifiTetheringEnabled(boolean enable) {
            //Log.d(TAG,"setWifiTetheringEnabled: "+enable);
            String SSID = networkSSID; // my function is to get a predefined SSID
            @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (enable) {
                wifiManager.setWifiEnabled(!enable);    // Disable all existing WiFi Network
            } else {
                if (!wifiManager.isWifiEnabled())
                    wifiManager.setWifiEnabled(!enable);
            }
            Method[] methods = wifiManager.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    WifiConfiguration netConfig = new WifiConfiguration();
                    if (!SSID.isEmpty()) {
                        netConfig.SSID = SSID;
                        netConfig.status = WifiConfiguration.Status.ENABLED;
                        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                        netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//                        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    }
                    try {
                        method.invoke(wifiManager, netConfig, enable);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            // delay for creating hotspot
            try {
                Toast.makeText(CrlShareReceiveProfiles.this, "HotSpot Created !!!", Toast.LENGTH_SHORT).show();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Start Server
            startServer();
            if (pd != null)
                pd.dismiss();
            Toast.makeText(CrlShareReceiveProfiles.this, "Server Started !!!", Toast.LENGTH_SHORT).show();
        }
    }

    private class CreateWifiAccessPoint extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(CrlShareReceiveProfiles.this);
            pd.setMessage("Starting Server ... Please wait !!!");
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
            boolean methodFound = false;
            for (Method method : wmMethods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    methodFound = true;
                    WifiConfiguration netConfig = new WifiConfiguration();
                    netConfig.SSID = networkSSID;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    try {
                        final boolean apStatus = (Boolean) method.invoke(wifiManager, netConfig, true);
                        for (Method isWifiApEnabledMethod : wmMethods)
                            if (isWifiApEnabledMethod.getName().equals("isWifiApEnabled")) {
                                while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
                                }
                                for (Method method1 : wmMethods) {
                                    if (method1.getName().equals("getWifiApState")) {
                                    }
                                }
                            }

                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return methodFound;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            // delay for creating hotspot
            try {
                // Snackbar instead of Toast
                Toast.makeText(CrlShareReceiveProfiles.this, "HotSpot Created !!!", Toast.LENGTH_SHORT).show();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Start Server
            startServer();
            if (pd != null)
                pd.dismiss();
            Toast.makeText(CrlShareReceiveProfiles.this, "Server Started !!!", Toast.LENGTH_SHORT).show();
        }
    }

    // Turns off WiFi HotSpot
    public static void turnOnOffHotspot(Context context, boolean isTurnToOn) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiApControl apControl = WifiApControl.getApControl(wifiManager);
        if (apControl != null) {

            // TURN OFF YOUR WIFI BEFORE ENABLE HOTSPOT
            //if (isWifiOn(context) && isTurnToOn) {
            //  turnOnOffWifi(context, false);
            //}

            apControl.setWifiApEnabled(apControl.getWifiApConfiguration(),
                    isTurnToOn);
        }
    }

    // Checking FTP Service is on or not
    public boolean checkServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().contains("ftpSettings.FsService")) {
                return true;
            }

/*
            if ("ftpSettings.FsService".contains(service.service.getClassName())) {
                return true;
            }
*/
        }
        return false;
    }

    private void startServer() {
        sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
    }

    private void stopServer() {
        sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }


}
