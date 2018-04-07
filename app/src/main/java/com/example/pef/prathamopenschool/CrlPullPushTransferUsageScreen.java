package com.example.pef.prathamopenschool;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CrlPullPushTransferUsageScreen extends AppCompatActivity {

    public static ProgressDialog progress;
    public static JSONArray _array;
    StudentDBHelper sdb;
    GroupDBHelper gdb;
    CrlDBHelper cdb;
    AserDBHelper aserdb;
    Context c;
    static BluetoothAdapter btAdapter;
    Intent intent = null;
    int res;
    private static final int DISCOVER_DURATION = 3000;
    private static final int REQUEST_BLU = 1;
    String deviceId = "";
    static File file;
    public static Boolean transferFlag = false;
    String packageName = null;
    String className = null;
    boolean found = false;
    public static boolean sentFlag = false;
    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    boolean isConnected;
    PlayVideo playVideo;
    boolean timer;
    TextView tv;
    ArrayList<String> path = new ArrayList<String>();

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlDashboard.class);
        this.finish();
        startActivity(intent);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crl_pull_push_transfer_usage_screen);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        // Memory Allocation
        c = this;
        sdb = new StudentDBHelper(c);
        cdb = new CrlDBHelper(c);
        gdb = new GroupDBHelper(c);
        aserdb = new AserDBHelper(c);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        tv = (TextView) findViewById(R.id.message);
        tv.setVisibility(View.GONE);


    }


    // Pull Data
    public void pullData(View view) {

        Intent goToPullData = new Intent(CrlPullPushTransferUsageScreen.this, PullData.class);
        startActivity(goToPullData);
        finish();

    }

    /* ***************************  PUSH DATA ********************************************************************/

    public void goToPushData(View view) {

        Intent goToPushData = new Intent(CrlPullPushTransferUsageScreen.this, PushData.class);
        startActivity(goToPushData);

    }


    /*

    // Receiver for checking connection
    private void checkConnection() {
        isConnected = ConnectivityReceiver.isConnected();
    }

    // Push Data
    public void pushToServer(View v) throws IOException, ExecutionException, InterruptedException {

        // Checking Internet Connection
        checkConnection();

        if (isConnected) {

            Toast.makeText(CrlPullPushTransferUsageScreen.this, "Connected to the Internet !!!", Toast.LENGTH_SHORT).show();

            int cnt = 0;
            //Moving to Receive usage
            String path = Environment.getExternalStorageDirectory().toString() + "/Bluetooth";
            File blueToothDir = new File(path);
            String destFolder = Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage";
            if (!blueToothDir.exists()) {
                Toast.makeText(this, "Bluetooth folder does not exist", Toast.LENGTH_SHORT).show();
            } else {

                progress = new ProgressDialog(CrlPullPushTransferUsageScreen.this);
                progress.setMessage("Please Wait...");
                progress.setCanceledOnTouchOutside(false);
                progress.show();

                File[] files = blueToothDir.listFiles();
                Toast.makeText(this, "Pushing data to server Please wait...", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().contains("pushNewDataToServer")) {
                        cnt++;
                        startPushing(convertToString(files[i]));
                        if (sentFlag) {

                            fileCutPaste(files[i], destFolder);
                        }
                    }
                }
                if (cnt == 0) {
                    progress.dismiss();
                    Toast.makeText(this, "No files available !!!\nPlease collect data and then transfer !!!", Toast.LENGTH_LONG).show();
                } else if (sentFlag) {
                    progress.dismiss();
                    Toast.makeText(this, "Data succesfully pushed to the server !!!", Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Files moved in received usage !!!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (isConnected == false) {
            Toast.makeText(CrlPullPushTransferUsageScreen.this, "Please Connect to the Internet !!!", Toast.LENGTH_SHORT).show();
        }


    }

    public String convertToString(File file) throws IOException {
        int length = (int) file.length();
        FileInputStream in = null;
        byte[] bytes = new byte[length];
        try {
            in = new FileInputStream(file);
            in.read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        String contents = new String(bytes);
        return contents;
    }

    public void startPushing(String jasonDataToPush) throws ExecutionException, InterruptedException {
        ArrayList<String> arrayListToTransfer = new ArrayList<String>();
        arrayListToTransfer.add(jasonDataToPush);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new PushSyncClient(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayListToTransfer).get();
        else
            new PushSyncClient(this).execute(arrayListToTransfer).get();
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
    }*/


    // Transfer Usage
    /* ***************************  TRANSFER DATA ********************************************************************/


    // Transfer Data Over Bluetooth
    public void transferData(View v) {

        // Generate Json file

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new ProgressDialog(CrlPullPushTransferUsageScreen.this);
                progress.setMessage("Please Wait...");
                progress.setCanceledOnTouchOutside(false);
                progress.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                createJsonforTransfer();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (progress.isShowing())
                    progress.dismiss();
//************************** integrate push data code here********************/

                String fileName = "";

                ArrayList<String> arrayList = new ArrayList<String>();
                TextView msg = (TextView) findViewById(R.id.message);
                _array = new JSONArray();
                TextView tv = (TextView) findViewById(R.id.message);
                tv.setText("");
                //  test();
                //test function is used only for reading database file from assets
                //Used when we want to push data from our side.

                //enableBlu();
                progress = new ProgressDialog(CrlPullPushTransferUsageScreen.this);
                progress.setMessage("Please Wait...");
                progress.setCanceledOnTouchOutside(false);
                progress.show();

                TreansferFile("pushNewDataToServer-");
            }
        }.execute();

        //Wont Work (Gets Executed Immediately)
        //Toast.makeText(c, "After Transfer File !!!", Toast.LENGTH_SHORT).show();

    }

    public void createJsonforTransfer() {
        //we will push logs and scores directly to the server

//        TextView msg = (TextView) findViewById(R.id.message);
//        msg.setText("");

        ScoreDBHelper scoreDBHelper = new ScoreDBHelper(this);
        List<Score> scores = scoreDBHelper.GetAll();

        if (scores == null) {
        } else if (scores.size() == 0) {
            // No Score No Transfer
        } else {
            try {

                JSONArray scoreData = new JSONArray(), logsData = new JSONArray(), attendanceData = new JSONArray(), studentData = new JSONArray(), crlData = new JSONArray(), grpData = new JSONArray(), aserData = new JSONArray();

                for (int i = 0; i < scores.size(); i++) {
                    JSONObject _obj = new JSONObject();
                    Score _score = scores.get(i);

                    try {
                        _obj.put("SessionID", _score.SessionID);
                        // _obj.put("PlayerID",_score.PlayerID);
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                {
                    try {
                        LogsDBHelper logsDBHelper = new LogsDBHelper(getApplicationContext());
                        List<Logs> logs = logsDBHelper.GetAll();

                        if (logs == null) {
                            // Great No Errors
                        } else {
                            for (int x = 0; x < logs.size(); x++) {
                                JSONObject _obj = new JSONObject();
                                Logs _logs = logs.get(x);//Changed by Ameya on 24/11
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
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.getMessage();
                        logsData.put(0);
                    }

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

                        //pravin
                        //For New Students data
                        List<Student> studentsList = sdb.GetAllNewStudents();
                        Log.d("student_list_size::", String.valueOf(sdb.GetAllNewStudents().size()));
                        JSONObject studentObj;
                        if (studentData != null) {
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

                        //pravin
                        //For New Crls data
                        List<Crl> crlsList = cdb.GetAllNewCrl();
                        JSONObject crlObj;
                        if (crlData != null) {
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

                        //pravin
                        //For New Groups data
                        List<Group> groupsList = gdb.GetAllNewGroups();
                        JSONObject grpObj;
                        if (grpData != null) {
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

                        //Ketan
                        //For New Aser data
                        List<Aser> aserList = aserdb.GetAll();
                        JSONObject aserObj;
                        if (aserData != null) {
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

                        StatusDBHelper statusDBHelper = new StatusDBHelper(this);
                        JSONObject obj = new JSONObject();
                        obj.put("ScoreCount", scores.size());
                        obj.put("AttendanceCount", attendanceData.length());
                        obj.put("CRLID", CrlDashboard.CreatedBy.equals(null) ? "CreatedBy" : CrlDashboard.CreatedBy);
                        //obj.put("LogsCount", logs.size());
                        obj.put("NewStudentsCount", studentData.length());
                        obj.put("NewCrlsCount", crlData.length());
                        obj.put("NewGroupsCount", grpData.length());
                        obj.put("AserDataCount", aserData.length());
                        obj.put("TransId", new Utility().GetUniqueID());
                        obj.put("DeviceId", deviceId.equals(null) ? "0000" : deviceId);
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

                        String requestString = "{ " +
                                "\"metadata\": " + obj + "," +
                                " \"scoreData\": " + scoreData + ", " +
                                "\"LogsData\": " + logsData + ", " +
                                "\"attendanceData\": " + attendanceData + ", " +
                                "\"newStudentsData\": " + studentData + ", " +
                                "\"newCrlsData\": " + crlData + ", " +
                                "\"newGroupsData\": " + grpData + ", " +
                                "\"AserTableData\": " + aserData +
                                "}";//Ketan
                        WriteSettings(c, requestString, "pushNewDataToServer-" + (deviceId.equals(null) ? "0000" : deviceId));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    // Creating file in Transferred Usage
    public void WriteSettings(Context context, String data, String fName) {

        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        try {
            String MainPath = Environment.getExternalStorageDirectory() + "/.POSinternal/transferredUsage/" + fName + ".json";
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

    public void TreansferFile(String filename) {

        // progress.dismiss();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't give bluetooth support.", Toast.LENGTH_LONG).show();
        } else {
            intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            file = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/transferredUsage/" + filename + (deviceId.equals(null) ? "0000" : deviceId) + ".json");
            int x = 0;
            if (file.exists()) {
                PackageManager pm = getPackageManager();
                List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);
                if (appsList.size() > 0) {

                    for (ResolveInfo info : appsList) {
                        packageName = info.activityInfo.packageName;
                        if (packageName.equals("com.android.bluetooth")) {
                            className = info.activityInfo.name;
                            found = true;
                            break;// found
                        }
                    }
                    if (!found) {
                        Toast.makeText(this, "Bluetooth not in list", Toast.LENGTH_SHORT).show();
                    } else {
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        intent.setClassName(packageName, className);

                        // Treat Like Capture
                        startActivityForResult(intent, 3);
                        //setResult(1);

                        // Gets Executed First
                        //Toast.makeText(CrlPullPushTransferUsageScreen.this, "Data Transferred Successfully !!!", Toast.LENGTH_SHORT).show();

                        //sendBroadcast(intent);
                    }
                }
            } else {
                if (progress.isShowing())
                    progress.dismiss();
                Toast.makeText(getApplicationContext(), "File not found in transferredUsage content", Toast.LENGTH_LONG).show();
            }
        }
    }


    // Not Working
    // MHM Action for Success/ failure on Transfer Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progress.dismiss();
        clearRecordsOrNot();

    }

    private void clearRecordsOrNot() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(c, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_Material_Dialog_Alert,Theme_DeviceDefault_Dialog_Alert,Theme_DeviceDefault_Light_DarkActionBar
        } else {
            builder = new AlertDialog.Builder(c);
        }
        builder.setTitle(Html.fromHtml("<font color='#2E96BB'>SHARE SUCCESSFUL ?</font>"))

                .setMessage("If you see 'File received successfully' message on master tab,\nClick SHARE SUCCESSFUL.\n\nWARNING : If you click SHARE SUCCESSFUL without receiving\n data on master tab, Data will be LOST !!!")

                .setNegativeButton("SHARE FAILED", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        tv.setVisibility(View.VISIBLE);
                        tv.setText("File Not Transferred !!!");
                    }
                })


                .setPositiveButton("SHARE SUCCESSFUL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        clearDBRecords();
                        tv.setVisibility(View.VISIBLE);
                        tv.setText("File Transferred Successfully !!!");
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }

    private void clearDBRecords() {

        // Resetting Score Table
        ScoreDBHelper scoreToDelete = new ScoreDBHelper(c);
        if (scoreToDelete.DeleteAll()) {
            Toast.makeText(c, "Score database cleared", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, "Problem in clearing score database", Toast.LENGTH_SHORT).show();
        }

        LogsDBHelper LogsToDelete = new LogsDBHelper(c);
        if (LogsToDelete.DeleteAll()) {
            Toast.makeText(c, "Logs database cleared", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, "Problem in clearing Logs database", Toast.LENGTH_SHORT).show();
        }
        BackupDatabase.backup(c);
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


    public void clearLogs(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Clear Logs")
                .setMessage("Do you really want to Clear Logs ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        LogsDBHelper LogsToDelete = new LogsDBHelper(c);
                        if (LogsToDelete.DeleteAll()) {
                            Toast.makeText(c, "Logs database cleared", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(c, "Problem in clearing Logs database", Toast.LENGTH_SHORT).show();
                        }
                        BackupDatabase.backup(c);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
}
