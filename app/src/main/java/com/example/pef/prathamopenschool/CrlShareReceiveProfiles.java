package com.example.pef.prathamopenschool;

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
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.interfaces.ExtractInterface;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
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
    File newProfile, newJson;
    String ReceivePath, TargetPath, shareItPath;

    int SDCardLocationChooser = 7, ZipFilePicker = 9;
    String zipPath;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    Button btn_updateMedia;

    TextView tv_Students, tv_Crls, tv_Groups;

    JSONArray crlJsonArray, studentsJsonArray, grpJsonArray;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crl_share_receive_profiles);
        ftpConnect = new FTPConnect(CrlShareReceiveProfiles.this, CrlShareReceiveProfiles.this,
                CrlShareReceiveProfiles.this);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(CrlShareReceiveProfiles.this);


        c = this;
        sdb = new StudentDBHelper(c);
        cdb = new CrlDBHelper(c);
        gdb = new GroupDBHelper(c);
        adb = new AserDBHelper(c);

        wipeSentFiles();

        tv_Students = (TextView) findViewById(R.id.tv_studentsShared);
        tv_Crls = (TextView) findViewById(R.id.tv_crlsShared);
        tv_Groups = (TextView) findViewById(R.id.tv_groupssShared);

        tv_Students.setVisibility(View.GONE);
        tv_Crls.setVisibility(View.GONE);
        tv_Groups.setVisibility(View.GONE);

        btn_updateMedia = (Button) findViewById(R.id.btn_updateMedia);
        // btn_updateMedia.setVisibility(View.GONE);
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
                        Integer cls = std.Class;
                        stdObj.put("Class", cls == null ? 0 : std.Class);
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

        File zipFolder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent");
        if (zipFolder.exists()) {
            wipeSentFiles();
        }

        ArrayList<String> fetchedStudents = fetchStudentProfiles();
        try {
            copyStdProfilesTosharableContent(fetchedStudents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Files are created
        // todo check null for all values
        path.clear();
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
                Compress mergeFiles = new Compress(paths, Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/NewProfiles.zip");
                mergeFiles.zip();
            } catch (Exception e) {
                e.printStackTrace();
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
        ListView lst_networks = dialog.findViewById(R.id.lst_network);

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
                            if (edt_HostName.getText().toString().trim().length() > 0) {
                                ftpConnect.connectFTPHotspot("TransferProfiles", edt_HostName.getText().toString(), "8080");
                                // Display Count
                                int std = Students.size();
                                int crl = Crls.size();
                                int grp = Groups.size();
                                tv_Details.setText("\nStudents Shared : " + std + "\nCRLs Shared : " + crl + "\nGroups Shared : " + grp);
                            } else
                                Toast.makeText(CrlShareReceiveProfiles.this, "Please enter the IP Address of FTP Server !!!", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        super.onBackPressed();
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

    // Reading CRL Json From internal memory
    public String loadCrlJSONFromAsset() {
        String crlJsonStr = "";

        try {
            File crlJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Crl.json");
            if (crlJsonSDCard.exists()) {
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
            }
        } catch (Exception e) {
            e.getMessage();
        }

        return crlJsonStr;
    }


    // Reading Aser Json From internal memory
    public String loadAserJSONFromAsset() {
        String aserJson = "";
        try {
            File AserJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Aser.json");
            if (AserJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(AserJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    aserJson = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }

        } catch (Exception e) {
        }

        return aserJson;

    }

    // Reading Student Json From internal memory
    public String loadStudentJSONFromAsset() {
        String studentJson = "";
        try {
            File studentJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Student.json");
            if (studentJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(studentJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    studentJson = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }

        } catch (Exception e) {
        }

        return studentJson;

    }

    // Reading Student Json From internal memory
    public String loadGroupJSONFromAsset() {
        String groupJson = "";
        try {
            File groupJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Group.json");
            if (groupJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(groupJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    groupJson = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }
        } catch (Exception e) {
        }

        return groupJson;

    }

    // Update Json in Device's Database when Received new files from another Device
    public void UpdateDB() throws JSONException {

        // For Loading CRL Json From External Storage (Assets)
        String crljsonData = loadCrlJSONFromAsset();
        Log.d("crljsonData::", crljsonData);
        if (!crljsonData.equals("")) {
            crlJsonArray = new JSONArray(crljsonData);
            for (int i = 0; i < crlJsonArray.length(); i++) {
                JSONObject clrJsonObject = crlJsonArray.getJSONObject(i);
                Crl crlobj = new Crl();
                crlobj.CRLId = clrJsonObject.getString("CRLID");
                crlobj.FirstName = clrJsonObject.getString("FirstName");
                crlobj.LastName = clrJsonObject.getString("LastName");
                crlobj.UserName = clrJsonObject.getString("UserName");
                crlobj.Password = clrJsonObject.getString("PassWord");
                crlobj.ProgramId = clrJsonObject.getInt("ProgramId");
                crlobj.Mobile = clrJsonObject.getString("Mobile");
                crlobj.State = clrJsonObject.getString("State");
                crlobj.Email = clrJsonObject.getString("Email");
                crlobj.CreatedBy = clrJsonObject.getString("CreatedBy");
                crlobj.newCrl = true;

                // new entries

                try {
                    crlobj.sharedBy = clrJsonObject.getString("sharedBy");
                    crlobj.SharedAtDateTime = clrJsonObject.getString("SharedAtDateTime");
                    crlobj.appVersion = clrJsonObject.getString("appVersion");
                    crlobj.appName = clrJsonObject.getString("appName");
                    crlobj.CreatedOn = clrJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    crlobj.sharedBy = "";
                    crlobj.SharedAtDateTime = "";
                    crlobj.appVersion = "";
                    crlobj.appName = "";
                    crlobj.CreatedOn = "";
                    e.printStackTrace();
                }

                cdb.replaceData(crlobj);
                BackupDatabase.backup(c);
            }
        }


        // For Loading Aser Json From External Storage (Assets)
        String aserjsonData = loadAserJSONFromAsset();
        if (!aserjsonData.equals("")) {
            JSONArray aserJsonArray = new JSONArray(aserjsonData);
            for (int i = 0; i < aserJsonArray.length(); i++) {

                JSONObject aserJsonObject = aserJsonArray.getJSONObject(i);
                Aser asrobj = new Aser();

                asrobj.StudentId = aserJsonObject.getString("StudentId");
                asrobj.ChildID = aserJsonObject.getString("ChildID");
                asrobj.GroupID = aserJsonObject.getString("GroupID");
                asrobj.TestType = aserJsonObject.getInt("TestType");
                asrobj.TestDate = aserJsonObject.getString("TestDate");
                asrobj.Lang = aserJsonObject.getInt("Lang");
                asrobj.Num = aserJsonObject.getInt("Num");
                asrobj.OAdd = aserJsonObject.getInt("OAdd");
                asrobj.OSub = aserJsonObject.getInt("OSub");
                asrobj.OMul = aserJsonObject.getInt("OMul");
                asrobj.ODiv = aserJsonObject.getInt("ODiv");
                asrobj.WAdd = aserJsonObject.getInt("WAdd");
                asrobj.WSub = aserJsonObject.getInt("WSub");
                asrobj.CreatedBy = aserJsonObject.getString("CreatedBy");
                asrobj.CreatedDate = aserJsonObject.getString("CreatedDate");
                asrobj.DeviceId = aserJsonObject.getString("DeviceId");
                asrobj.FLAG = aserJsonObject.getInt("FLAG");

                // new entries
                try {
                    asrobj.sharedBy = aserJsonObject.getString("sharedBy");
                    asrobj.SharedAtDateTime = aserJsonObject.getString("SharedAtDateTime");
                    asrobj.appVersion = aserJsonObject.getString("appVersion");
                    asrobj.appName = aserJsonObject.getString("appName");
                    asrobj.CreatedOn = aserJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    asrobj.sharedBy = "";
                    asrobj.SharedAtDateTime = "";
                    asrobj.appVersion = "";
                    asrobj.appName = "";
                    asrobj.CreatedOn = "";
                    e.printStackTrace();
                }

                if (asrobj.TestType == 0) {
                    boolean result;
                    result = adb.CheckDataExists(asrobj.StudentId, 0);
                    if (result == false) {
                        adb.insertData(asrobj);
                        BackupDatabase.backup(c);
                    } else {
                        adb.UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(c);
                    }
                } else if (asrobj.TestType == 1) {
                    boolean result;
                    result = adb.CheckDataExists(asrobj.StudentId, 1);
                    if (result == false) {
                        adb.insertData(asrobj);
                        BackupDatabase.backup(c);
                    } else {
                        adb.UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(c);
                    }
                } else if (asrobj.TestType == 2) {
                    boolean result;
                    result = adb.CheckDataExists(asrobj.StudentId, 2);
                    if (result == false) {
                        adb.insertData(asrobj);
                        BackupDatabase.backup(c);
                    } else {
                        adb.UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(c);
                    }
                } else if (asrobj.TestType == 3) {
                    boolean result;
                    result = adb.CheckDataExists(asrobj.StudentId, 3);
                    if (result == false) {
                        adb.insertData(asrobj);
                        BackupDatabase.backup(c);
                    } else {
                        adb.UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(c);
                    }
                } else if (asrobj.TestType == 4) {
                    boolean result;
                    result = adb.CheckDataExists(asrobj.StudentId, 4);
                    if (result == false) {
                        adb.insertData(asrobj);
                        BackupDatabase.backup(c);
                    } else {
                        adb.UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(c);
                    }

                    //adb.insertData(asrobj);
                    //BackupDatabase.backup(c);

                }


            }// For Loop

        }


        // For Loading Student Json From External Storage (Assets)
        String studentjsonData = loadStudentJSONFromAsset();
        if (!studentjsonData.equals("")) {
            studentsJsonArray = new JSONArray(studentjsonData);

            for (int j = 0; j < studentsJsonArray.length(); j++) {

                JSONObject stdJsonObject = studentsJsonArray.getJSONObject(j);

                Student stdObj = new Student();

                stdObj.StudentID = stdJsonObject.getString("StudentID");
                stdObj.FirstName = stdJsonObject.getString("FirstName");
                stdObj.MiddleName = stdJsonObject.getString("MiddleName");
                stdObj.LastName = stdJsonObject.getString("LastName");
                stdObj.Age = stdJsonObject.getInt("Age");
                stdObj.Class = stdJsonObject.getInt("Class");
                stdObj.UpdatedDate = stdJsonObject.getString("UpdatedDate");
                stdObj.Gender = stdJsonObject.getString("Gender");
                stdObj.GroupID = stdJsonObject.getString("GroupID");
                stdObj.CreatedBy = stdJsonObject.getString("CreatedBy");
                stdObj.StudentUID = stdJsonObject.getString("StudentUID");
                stdObj.newStudent = true;
                //todo new
                stdObj.IsSelected = stdJsonObject.getBoolean("IsSelected");

                // new entries
                try {
                    stdObj.sharedBy = stdJsonObject.getString("sharedBy");
                    stdObj.SharedAtDateTime = stdJsonObject.getString("SharedAtDateTime");
                    stdObj.appVersion = stdJsonObject.getString("appVersion");
                    stdObj.appName = stdJsonObject.getString("appName");
                    stdObj.CreatedOn = stdJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    stdObj.sharedBy = "";
                    stdObj.SharedAtDateTime = "";
                    stdObj.appVersion = "";
                    stdObj.appName = "";
                    stdObj.CreatedOn = "";
                    e.printStackTrace();
                }

                sdb.replaceData(stdObj);
                BackupDatabase.backup(c);
            }
        }

        // For Loading Group Json From External Storage (Assets)
        String groupjsonData = loadGroupJSONFromAsset();
        if (!groupjsonData.equals("")) {
            grpJsonArray = new JSONArray(groupjsonData);

            for (int j = 0; j < grpJsonArray.length(); j++) {

                JSONObject grpJsonObject = grpJsonArray.getJSONObject(j);

                Group grpObj = new Group();

                grpObj.GroupID = grpJsonObject.getString("GroupID");
                grpObj.GroupCode = grpJsonObject.getString("GroupCode");
                grpObj.GroupName = grpJsonObject.getString("GroupName");
                grpObj.UnitNumber = grpJsonObject.getString("UnitNumber");
                grpObj.DeviceID = grpJsonObject.getString("DeviceID");
                grpObj.Responsible = grpJsonObject.getString("Responsible");
                grpObj.ResponsibleMobile = grpJsonObject.getString("ResponsibleMobile");
                grpObj.VillageID = grpJsonObject.getInt("VillageID");
                grpObj.ProgramID = grpJsonObject.getInt("ProgramId");
                grpObj.CreatedBy = grpJsonObject.getString("CreatedBy");
                grpObj.SchoolName = grpJsonObject.getString("SchoolName");
                grpObj.VillageName = grpJsonObject.getString("VillageName");
                grpObj.newGroup = true;

                // new entries
                try {
                    grpObj.sharedBy = grpJsonObject.getString("sharedBy");
                    grpObj.SharedAtDateTime = grpJsonObject.getString("SharedAtDateTime");
                    grpObj.appVersion = grpJsonObject.getString("appVersion");
                    grpObj.appName = grpJsonObject.getString("appName");
                    grpObj.CreatedOn = grpJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    grpObj.sharedBy = "";
                    grpObj.SharedAtDateTime = "";
                    grpObj.appVersion = "";
                    grpObj.appName = "";
                    grpObj.CreatedOn = "";
                    e.printStackTrace();
                }

                gdb.replaceData(grpObj);

            }
        }
        BackupDatabase.backup(c);
    }

    // Transfer image files from Received to StudentProfiles
    public void copy(File sourceLocation, File targetLocation) throws IOException {
        try {
            if (sourceLocation.isDirectory()) {
                copyDirectory(sourceLocation, targetLocation);
            } else {
                copyFile(sourceLocation, targetLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        try {
            if (!target.exists()) {
                target.mkdir();
            }

            for (String f : source.list()) {
                copy(new File(source, f), new File(target, f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFile(File source, File target) throws IOException {
        if (source.getName().contains(".jpg")) {
            try (
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target)
            ) {
                byte[] buf = new byte[1024];
                int length;
                while ((length = in.read(buf)) > 0) {
                    out.write(buf, 0, length);
                }
            }
        }
    }

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
        ListView lst_networks = dialog.findViewById(R.id.lst_network);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Onlistener
        ArrayList<String> networkList = ftpConnect.scanNearbyWifi();

        lst_networks.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.lst_wifi_item, R.id.label, networkList));

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
                            if (edt_HostName.getText().toString().trim().length() > 0) {
                                String path = Environment.getExternalStorageDirectory().toString() + "/.POSinternal/Json";
                                File directory = new File(path);
                                File[] files = directory.listFiles();
                                ftpConnect.connectFTPHotspot("TransferJson", edt_HostName.getText().toString(), "8080");
                                tv_Details.setText("\nNo of Files Shared : " + files.length);
                            } else
                                Toast.makeText(CrlShareReceiveProfiles.this, "Please enter the IP Address of FTP Server !!!", Toast.LENGTH_SHORT).show();
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
            ftpConnect.createFTPHotspot();

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


    void SetInitialValuesReceiveOff() throws JSONException {
        // insert your code to run only when application is started first time here
        context = this;
        //CRL Initial DB Process
        CrlDBHelper db = new CrlDBHelper(context);
        // For Loading CRL Json From External Storage (Assets)
        JSONArray crlJsonArray = new JSONArray(loadCrlJSONFromAssetReceiveOff());
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
            crlobj.newCrl = true;
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
            db.updateJsonData(crlobj);
            BackupDatabase.backup(context);
        }
        //Villages Initial DB Process
        VillageDBHelper database = new VillageDBHelper(context);
        // For Loading Villages Json From External Storage (Assets)
        JSONArray villagesJsonArray = new JSONArray(loadVillageJSONFromAssetReceiveOff());
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
            database.updateJsonData(villageobj);
            BackupDatabase.backup(context);
        }
    }

    // Reading CRL Json From Internal Memory
    public String loadCrlJSONFromAssetReceiveOff() {
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
    public String loadVillageJSONFromAssetReceiveOff() {
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


    private void wipeJsonFolder() {

        // Delete Receive Folder Contents after Transferring
        String directoryToDelete = Environment.getExternalStorageDirectory() + "/.POSinternal/Json";
        File dir = new File(directoryToDelete);
        for (File file : dir.listFiles())
            if (!file.isDirectory())
                file.delete();
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
                    extractToSDCard(path, treeUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CrlShareReceiveProfiles.this, "You haven't selected anything !!!", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    // method to copy files to sd card
    private void extractToSDCard(String path, final Uri treeUri) {
        String base_path = FileUtil.getExtSdCardFolder(new File(path), CrlShareReceiveProfiles.this);
        if (base_path != null && base_path.equalsIgnoreCase(path)) {
            Log.d("Base path :::", base_path);
            Log.d("targetPath :::", path);
            // Path ( Selected )
            PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this)
                    .edit().putString("URI", treeUri.toString()).apply();
            PreferenceManager.getDefaultSharedPreferences(CrlShareReceiveProfiles.this)
                    .edit().putString("PATH", path).apply();

//            new UnZipTask(CrlShareReceiveProfiles.this, shareItPath).execute();
            new CopyFiles(shareItPath, CrlShareReceiveProfiles.this,
                    CrlShareReceiveProfiles.this).execute();
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
                            startActivityForResult(intent, SDCardLocationChooser);
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
    public void onFilesRecievedComplete(String typeOfFile) {
        if (typeOfFile.equalsIgnoreCase("ReceiveProfiles")) {
            TargetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/";
            File NewProfilesExists = new File(TargetPath + "NewProfiles.zip");
            if (NewProfilesExists.exists())
                new RecieveFiles(TargetPath, NewProfilesExists.getAbsolutePath()).execute();

        } else if (typeOfFile.equalsIgnoreCase("ReceiveJson")) {
            // Update DB
            try {
                // Add Initial Entries of CRL & Village Json to Database
                SetInitialValuesReceiveOff();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class RecieveFiles extends AsyncTask<Void, Integer, String> {

        String targetPath;
        String recieveProfilePath;
        ProgressDialog dialog;

        public RecieveFiles(String targetPath, String recieveProfilePath) {
            this.targetPath = targetPath;
            this.recieveProfilePath = recieveProfilePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(CrlShareReceiveProfiles.this);
            dialog.setMessage("Receiving Profiles");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Extraction of contents
            newProfile = new File(recieveProfilePath);
            Compress extract = new Compress();
//            ReceivePath = recieveProfilePath.replace("content://com.estrongs.files", "");

            Log.d("ReceivePath :::", recieveProfilePath);
            Log.d("TargetPath :::", targetPath);

            // Exctracting Data
            List<String> unzippedFileNames = extract.unzip(recieveProfilePath, targetPath);

            // Inserting All Jsons in Database
            try {
                // todo
                UpdateDB();
                // Error causing here
                newProfile.delete();
                // Transfer Student's Profiles from Receive folder to Student Profiles
                File src = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage");
                File dest = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles");
                try {
                    if (!src.exists()) {
                        //Toast.makeText(c, "No folder exist in Internal Storage to copy", Toast.LENGTH_LONG).show();
                    } else if (dest.exists()) {
                        copyDirectory(src, dest);
//                        CrlShareReceiveProfiles.this.runOnUiThread(new Runnable() {
//                            public void run() {
//                                Toast.makeText(c, "Files copied successfully!", Toast.LENGTH_LONG).show();
//                            }
//                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "true";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (dialog != null) {
                dialog.dismiss();
            }

            Toast.makeText(c, "Files Received & Updated in Database Successfully !!!", Toast.LENGTH_SHORT).show();
            tv_Students.setVisibility(View.VISIBLE);
            tv_Crls.setVisibility(View.VISIBLE);
            tv_Groups.setVisibility(View.VISIBLE);
            int crl = crlJsonArray == null ? 0 : crlJsonArray.length();
            int std = studentsJsonArray == null ? 0 : studentsJsonArray.length();
            int grp = grpJsonArray == null ? 0 : grpJsonArray.length();
            tv_Students.setText("Students Received : " + std);
            tv_Crls.setText("CRLs Received : " + crl);
            tv_Groups.setText("Groups Received : " + grp);

            Toast.makeText(c, "Profiles received", Toast.LENGTH_LONG).show();

        }
    }
}
