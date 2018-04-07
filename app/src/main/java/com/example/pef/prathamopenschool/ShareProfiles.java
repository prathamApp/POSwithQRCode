package com.example.pef.prathamopenschool;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class ShareProfiles extends AppCompatActivity {

    List<Student> Students;
    StudentDBHelper sdb;
    List<Crl> Crls;
    CrlDBHelper cdb;
    List<Group> Groups;
    GroupDBHelper gdb;
    List<Aser> Asers;
    AserDBHelper adb;
    ArrayList<String> path = new ArrayList<String>();

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    Context c;
    public static ProgressDialog progress;
    static BluetoothAdapter btAdapter;
    Intent intent = null;
    String packageName = null;
    static File file;
    boolean found = false;
    String className = null;
    int res;
    private static final int DISCOVER_DURATION = 3000;
    private static final int REQUEST_BLU = 1;
    JSONArray newStudentArray, newCrlArray, newGrpArray, newAserArray;
    JSONObject stdObj, crlObj, grpObj, asrObj;
    String deviceId = "";
    TextView tv_Students, tv_Crls, tv_Groups;


    Button btnShareUsage, btnShareProfiles;

    StatusDBHelper stat;
    Utility util;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_profiles);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        // Generate Device ID
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        util = new Utility();

        // Memory Allocation
        c = this;
        sdb = new StudentDBHelper(c);
        cdb = new CrlDBHelper(c);
        gdb = new GroupDBHelper(c);
        adb = new AserDBHelper(c);

        btnShareProfiles = (Button) findViewById(R.id.btn_ShareStudentProfiles);

        tv_Students = (TextView) findViewById(R.id.tv_studentsShared);
        tv_Crls = (TextView) findViewById(R.id.tv_crlsShared);
        tv_Groups = (TextView) findViewById(R.id.tv_groupssShared);

        tv_Students.setVisibility(View.GONE);
        tv_Crls.setVisibility(View.GONE);
        tv_Groups.setVisibility(View.GONE);

        btnShareProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Transfer Newly created entries
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        pd = new ProgressDialog(ShareProfiles.this);
                        pd.setMessage("Please wait ... ");
                        pd.setCanceledOnTouchOutside(false);
                        pd.show();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        File zipFolder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/NewProfiles.zip");
                        if (zipFolder.exists()) {
                            wipeSentFiles();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        transferData();

//                        if (pd != null)
//                            pd.dismiss();
                    }
                }.execute();

                // Sending the List of Newly Added Students in Database
                //Students = sdb.GetAllNewStudents();
            }
        });
    }


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

//        if (folder.exists()) {
//            Cursor imagecursor = getContentResolver().query(EXTERNAL,
//                    null,
//                    MediaStore.Images.Media.DATA + " like ? ",
//                    new String[]{"%StudentProfiles%"},
//                    null);
//
//
//            String fileNameWithExtension = "";//default fileName
//            Uri filePathUri;
//            for (int i = 1; i < imagecursor.getCount(); i++) {
//                String imgName = imagecursor.getColumnName(i);
//                int imgCount = imagecursor.getCount();
//                imagecursor.moveToPosition(i);
//                int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
//                filePathUri = Uri.parse(imagecursor.getString(dataColumnIndex));
//                fileNameWithExtension = filePathUri.getLastPathSegment().toString();
//                String[] fileName = fileNameWithExtension.split("\\.");
//
//                for (int j = 0; j < Students.size(); j++) {
//                    stdID = String.valueOf(Students.get(j).StudentID);
//                    if (fileName[0].equals(stdID)) {
//                        imageUrls.add(imagecursor.getString(dataColumnIndex));
//                    }
//                }
//            }
//            return imageUrls;
//        } else {
//        return null;
//        }
    }


    // On Button Click Start this function to share std json via bluetooth
    public void transferData() {
//************************** integrate push data code here********************/

        //enableBlu();


//        MultiPhotoSelectActivity.dilog.showDilog(ShareProfiles.this, "Collecting data to transfer");

        Thread mThread = new Thread() {
            @Override
            public void run() {
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
//                    MultiPhotoSelectActivity.dilog.dismissDilog();
                    if (pd != null)
                        pd.dismiss();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ShareProfiles.this, "No New Data Found !!!", Toast.LENGTH_LONG).show();
                        }
                    });
//                    Toast.makeText(ShareProfiles.this, "No new data found !!!", Toast.LENGTH_SHORT).show();
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
//                    MultiPhotoSelectActivity.dilog.dismissDilog();
                    if (pd != null)
                        pd.dismiss();

                    ShareProfiles.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ShareProfiles.this, " Data collected Successfully !!!", Toast.LENGTH_SHORT).show();
                            // Transferring Created Zip
                            TreansferFile("NewProfiles");
                        }
                    });

                }
            }
        };
        mThread.start();
    }


    // Delete Sent Files
    private void wipeSentFiles() {
        try {
            // Delete Files after Sending
            String directoryToDelete = Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent";
            File dir = new File(directoryToDelete);
            for (File file : dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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


    /*public void enableBlu() {
        // enable device discovery - this will automatically enable Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {

        }
    }*/

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
                        stat = new StatusDBHelper(ShareProfiles.this);
                        stdObj.put("sharedBy", stat.getValue("AndroidID"));
                        stdObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        stdObj.put("appVersion", stat.getValue("apkVersion"));
                        stdObj.put("appName", stat.getValue("appName"));
                        stdObj.put("CreatedOn", std.CreatedOn == null ? "" : std.CreatedOn);

                        newStudentArray.put(stdObj);
                    }

                    String requestString = String.valueOf(newStudentArray);

                    WriteSettings(getApplicationContext(), requestString, "Student");
                    //  TreansferFile("Student");
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
            //  Toast.makeText(ShareProfiles.this, "There are No new Crls !!!", Toast.LENGTH_SHORT).show();
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
                    stat = new StatusDBHelper(ShareProfiles.this);
                    crlObj.put("sharedBy", stat.getValue("AndroidID"));
                    crlObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                    crlObj.put("appVersion", stat.getValue("apkVersion"));
                    crlObj.put("appName", stat.getValue("appName"));
                    crlObj.put("CreatedOn", crl.CreatedOn == null ? "" : crl.CreatedOn);

                    newCrlArray.put(crlObj);
                }

                String requestString = String.valueOf(newCrlArray);

                WriteSettings(getApplicationContext(), requestString, "Crl");
                // TreansferFile("Crl");

            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    }


    public void sendNewGroup() {
        newGrpArray = new JSONArray();
        Groups = gdb.GetAllNewGroups();

        if (Groups == null || Groups.isEmpty()) {
            // Toast.makeText(ShareProfiles.this, "There are No new Groups !!!", Toast.LENGTH_SHORT).show();
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
                        stat = new StatusDBHelper(ShareProfiles.this);
                        grpObj.put("sharedBy", stat.getValue("AndroidID"));
                        grpObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        grpObj.put("appVersion", stat.getValue("apkVersion"));
                        grpObj.put("appName", stat.getValue("appName"));
                        grpObj.put("CreatedOn", grp.CreatedOn == null ? "" : grp.CreatedOn);

                        newGrpArray.put(grpObj);
                    }

                    String requestString = String.valueOf(newGrpArray);

                    WriteSettings(getApplicationContext(), requestString, "Group");
                    //TreansferFile("Group");

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
            //     Toast.makeText(ShareProfiles.this, "There are No new Asers !!!", Toast.LENGTH_SHORT).show();
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
                        stat = new StatusDBHelper(ShareProfiles.this);
                        asrObj.put("sharedBy", stat.getValue("AndroidID"));
                        asrObj.put("SharedAtDateTime", util.GetCurrentDateTime(false).toString());
                        asrObj.put("appVersion", stat.getValue("apkVersion"));
                        asrObj.put("appName", stat.getValue("appName"));
                        asrObj.put("CreatedOn", asr.CreatedOn == null ? "" : asr.CreatedOn);

                        newAserArray.put(asrObj);

                    }

                    String requestString = String.valueOf(newAserArray);

                    WriteSettings(getApplicationContext(), requestString, "Aser");
                    //  TreansferFile("Student");
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    }


    public void TreansferFile(String filename) {

        int resultCode = 1;
        res = resultCode;
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

        intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        // intent.setClassName("com.lenovo.anyshare.gps", "com.lenovo.anyshare.share.ShareActivity");
        intent.setType("text/plain");
        file = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/" + filename + ".zip");

        int x = 0;
        if (file.exists()) {

//                PackageManager pm = getPackageManager();
//                List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);
//                if (appsList.size() > 0) {
//
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
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                        intent.setClassName(packageName, className);
            startActivity(Intent.createChooser(intent, "Select SHAREit App from the list"));

            // Display Count
            tv_Students.setVisibility(View.VISIBLE);
            tv_Crls.setVisibility(View.VISIBLE);
            tv_Groups.setVisibility(View.VISIBLE);
            int std = Students.size();
            int crl = Crls.size();
            int grp = Groups.size();
            tv_Students.setText("Students Shared : " + std);
            tv_Crls.setText("CRLs Shared : " + crl);
            tv_Groups.setText("Groups Shared : " + grp);
//                    }
//                }
        } else
            Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG).show();

//        }
    }


    /*public JSONArray readNewStudentFromDatabase() {

        JSONArray newStudentArray = new JSONArray();
        String requestString = "";
        {
            try {

                Students = sdb.GetAllNewStudents();

                if (Students == null) {
                } else {
                    for (int x = 0; x < Students.size(); x++) {
                        JSONObject stdObj = new JSONObject();
                        Student std = Students.get(x);
                        stdObj.put("StudentID", std.StudentID);
                        stdObj.put("FirstName", std.FirstName);
                        stdObj.put("MiddleName", std.MiddleName);
                        stdObj.put("LastName", std.LastName);
                        stdObj.put("Age", std.Age);
                        stdObj.put("Class", std.Class);
                        stdObj.put("UpdatedDate", std.UpdatedDate);
                        stdObj.put("Gender", std.Gender);
                        stdObj.put("GroupID", std.GroupID);
                        stdObj.put("CreatedBy", std.CreatedBy);
                        stdObj.put("NewFlag", std.newStudent);

                        newStudentArray.put(stdObj);
                    }
                }

            } catch (Exception ex) {
                ex.getMessage();
            }
            return newStudentArray;
        }
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            /*finish();
            Intent crlSharercv = new Intent(this,CrlShareReceiveProfiles.class);
            startActivity(crlSharercv);*/
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        wipeSentFiles();
    }

    /*@Override
    public void onBackPressed() {
        wipeSentFiles();

        Intent i = new Intent(ShareProfiles.this,CrlShareReceiveProfiles.class);
        finish();
        startActivity(i);
    }*/

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


}
