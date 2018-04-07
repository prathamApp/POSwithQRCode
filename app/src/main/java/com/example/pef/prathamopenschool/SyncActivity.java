package com.example.pef.prathamopenschool;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;


public class SyncActivity extends AppCompatActivity {

    public String state;
    public static Boolean transferFlag = false;
    String packageName = null;
    static File file,newProfile;
    String className = null;
    boolean found = false;
    static long time = 30000;
    static BluetoothAdapter btAdapter;
    Intent intent = null;
    int res;
    private static final int DISCOVER_DURATION = 3000;
    private static final int REQUEST_BLU = 1;
    String deviceId;
    public static ProgressDialog progress;
    public static JSONArray _array;
    RadioGroup radioGroup;
    public static boolean scoreNotAvailable = false;
    static String CreatedBy;
    static String currentAdmin;
    static String ReceivePath,TargetPath ;
    StudentDBHelper sdb;
    GroupDBHelper gdb;
    CrlDBHelper cdb;
    AserDBHelper aserdb;
    Context c;
    ArrayList<String> arrayListToTransfer ;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    /*SharedPreferences pref;
    String GroupName, VillageName, SchoolName,GroupID;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        MainActivity.sessionFlg=false;
        sessionContex=this;
        playVideo = new PlayVideo();

        Button b1 = (Button) findViewById(R.id.btn_EditUChildList);

        Button b2 = (Button) findViewById(R.id.btn_AddNewUnit);

        Button b3 = (Button) findViewById(R.id.btn_AddNewGroup);

        Button b4 = (Button) findViewById(R.id.btn_StudentProfile);

        deviceId=Build.SERIAL;

        /*if(MultiPhotoSelectActivity.programID.equals("1")){
            b1.setVisibility(View.INVISIBLE);
            b2.setVisibility(View.INVISIBLE);
        }else {
            b3.setVisibility(View.INVISIBLE);
            b4.setVisibility(View.INVISIBLE);
        }*/

        // Memory Allocation
        c = this;
        sdb = new StudentDBHelper(c);
        cdb = new CrlDBHelper(c);
        gdb = new GroupDBHelper(c);
        aserdb = new AserDBHelper(c);

        /*// Retrieve data from Create New Unit
        // Retrieving Data from Previous Page
        GroupID = pref.getString("Group ID", null);
        GroupName = pref.getString("Group Name", null);
        VillageName = pref.getString("Village Name", null);
        SchoolName = pref.getString("School Name", null);
*/
        Intent i = getIntent();
        CreatedBy = i.getStringExtra("CreatedBy"); // Created by is CRLID

        currentAdmin = i.getStringExtra("UserName");
        Toast.makeText(this, "Welcome " + currentAdmin, Toast.LENGTH_SHORT).show();

        scoreNotAvailable = false;


        TextView tv = (TextView) findViewById(R.id.message);
        tv.setText("");
    }

    public void pushToServer(View v) throws IOException {

        //Moving to Receive usage
        String path = Environment.getExternalStorageDirectory().toString() + "/Bluetooth";
        File directory = new File(path);
        String destFolder = Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage";
        if (!directory.exists()) {
            Toast.makeText(c, "Bluetooth folder does not exist", Toast.LENGTH_SHORT).show();
        } else {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().contains("pushNewDataToServer")) {
                    fileCutPaste(files[i], destFolder);
                }
            }
            if(files.length>0)
            Toast.makeText(c, "Files Moved in ReceivedUsage", Toast.LENGTH_SHORT).show();
        }

        //Pushing to server
        File receivedUsage = new File(destFolder);
        if(!receivedUsage.exists()){
            Toast.makeText(c, "receivedUsage folder does not exist", Toast.LENGTH_SHORT).show();
        }
        else {
             File[] filesToPush = receivedUsage.listFiles();
             for (int i=0; i<filesToPush.length; i++){
                 if (filesToPush[i].getName().contains("pushNewDataToServer")) {
                     startPushing(convertToString(filesToPush[i]));
                 }
             }
        }
    }

    public String convertToString(File file) throws IOException {
        int length = (int) file.length();
        FileInputStream in = null;
        byte[] bytes = new byte[length];
        try {
            in = new FileInputStream(file);
            in.read(bytes);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            in.close();
        }
        String contents = new String(bytes);
        return contents;
    }

    public void startPushing(String jasonDataToPush){
        ArrayList<String> arrayListToTransfer = new ArrayList<String>();
        arrayListToTransfer.add(jasonDataToPush);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new PushSyncClient(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayListToTransfer);
        else
            new PushSyncClient(this).execute(arrayListToTransfer);
    }

    public void fileCutPaste(File toMove, String destFolder) {
        try {
            File destinationFolder = new File(destFolder);
            File destinationFile = new File( destFolder+"/"+ toMove.getName());
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

    public void AssignGroups(View v) {

        Intent intent = new Intent(SyncActivity.this, AssignGroups.class);
        startActivity(intent);

        // Old Code
        /*
        StatusDBHelper statusDBHelper = new StatusDBHelper(getApplicationContext());
        String pullFlag = statusDBHelper.getValue("pullFlag");
        //LogInPage.sharedPreferences=getApplicationContext().getSharedPreferences("SelectedValues",getApplicationContext().MODE_PRIVATE);
        if (pullFlag.equals("0")) {
            showDialogue("Please pull the data first, by selecting your state.");
        } else {
            Intent intent = new Intent(SyncActivity.this, AssignGroups.class);
            startActivity(intent);
        }*/
    }


    /*public void pullData(View v) {

        TextView tv = (TextView) findViewById(R.id.message);
        //tv.setText("");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Select the state and pull data...");

        final LinearLayout layout = new LinearLayout(this, null);
        layout.setOrientation(LinearLayout.VERTICAL);


        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5, 0, 0, 0);
        int i = 0;

        radioGroup = new RadioGroup(this);
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(i);
        i++;
        radioButton.setTextColor(Color.BLACK);
        radioButton.setText("Maharashtra");
        radioButton.setTextSize(25);
        radioGroup.addView(radioButton, lp);

        RadioButton radioButton1 = new RadioButton(this);
        radioButton1.setId(i);
        i++;
        radioButton1.setTextColor(Color.BLACK);
        radioButton1.setText("Rajasthan");
        radioButton1.setTextSize(25);
        radioGroup.addView(radioButton1, lp);

        RadioButton radioButton2 = new RadioButton(this);
        radioButton2.setId(i);
        radioButton2.setTextColor(Color.BLACK);
        radioButton2.setText("Uttar Pradesh");
        radioButton2.setTextSize(25);
        radioGroup.addView(radioButton2, lp);

        layout.addView(radioGroup);

        alert.setView(layout);

        alert.setCancelable(false);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = alert.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean doNotClose = false;
                String state = null;
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {

                    RadioButton radioButton = (RadioButton) layout.findViewById(selectedId);
                    state = radioButton.getText().toString();
                    switch (state) {
                        case "Maharashtra":
                            state = "MH";
                            break;
                        case "Rajasthan":
                            state = "RJ";
                            break;
                        case "Uttar Pradesh":
                            state = "UP";
                            break;
                    }
                } else {
                    doNotClose = true;
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select state.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (!doNotClose) {
                    dialog.dismiss();
                    TextView msg = (TextView) findViewById(R.id.message);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        SyncClient syncClient = new SyncClient(SyncActivity.this, state, msg);
                        syncClient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        SyncClient syncClient = new SyncClient(SyncActivity.this, state, msg);
                        syncClient.execute("");
                    }
                }
            }
        });

    }*/


    public void WriteSettings(Context context, String data, String fName) {
        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;
     //   deviceId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        try {
            File dir = Environment.getExternalStorageDirectory();
            File file = new File(dir, "." + fName + "_" + deviceId + ".json");
            try {
                fOut = new FileOutputStream(file);

                osw = new OutputStreamWriter(fOut);
                osw.write(data);
                osw.flush();
                osw.close();
                fOut.close();

            } catch (Exception e) {
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(getApplicationContext());
                syncActivityLogs.addToDB("WriteSettings-SyncActivity", e, "Error");
            } finally {

            }
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(getApplicationContext());
            syncActivityLogs.addToDB("WriteSettings-SyncActivity", e, "Error");
            e.printStackTrace();
            Toast.makeText(context, "Settings not saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void test() {
        try {
            // Open your local db as the input stream
            InputStream myInput = getApplicationContext().getAssets().open("PrathamTabDB.db");

            // Path to the just created empty db
            String outFileName = "/data/data/com.example.pef.prathamopenschool/databases/PrathamTabDB.db";

            OutputStream myOutput = new FileOutputStream(outFileName);

            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
            BackupDatabase.backup(getApplicationContext());
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(getApplicationContext());
            syncActivityLogs.addToDB("test-SyncActivity", e, "Error");
            // BackupDatabase.backup(getApplicationContext());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Delete Received Files
        wipeReceivedData();

        // Going on Admin Login Page
        Intent intent = new Intent(this, StartingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }

    public JSONArray readFromDatabase(int table) {

        JSONArray _array = new JSONArray();
        JSONArray _arrayLog = new JSONArray();
        String requestString = "";
        if (table == 1)//score transfer
        {
            try {
                List<Score> _scores = new ScoreDBHelper(this).GetAll();


                if (_scores == null) {
                } else {
                    for (int x = 0; x < _scores.size(); x++) {
                        JSONObject _obj = new JSONObject();
                        Score score = _scores.get(x);
                        _obj.put("PlayerId", score.PlayerID);
                        _obj.put("SessionId", score.SessionID);
                        _obj.put("GroupId", score.GroupID);
                        _obj.put("DeviceId", score.DeviceID);
                        _obj.put("ResourceId", score.ResourceID);
                        _obj.put("QuestionId", score.QuestionId);
                        _obj.put("ScoredMarks", score.ScoredMarks);
                        _obj.put("TotalMarks", score.TotalMarks);
                        _obj.put("StartDateTime", score.StartTime);
                        _obj.put("EndDateTime", score.EndTime);
                        _obj.put("Level", score.Level);

                        _array.put(_obj);
                    }
                }

            } catch (Exception ex) {
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(getApplicationContext());
                syncActivityLogs.addToDB("readFromDatabase-SyncActivity", ex, "Error");
                BackupDatabase.backup(getApplicationContext());
            }
            return _array;
        } else {//get logs from database
            try {

                Toast.makeText(SyncActivity.this, "Logs !!!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
            return _arrayLog;
        }

    }


    public void transferData(View v) {
        createJsonforTransfer();
//************************** integrate push data code here********************/

        String fileName="";

        ArrayList<String> arrayList = new ArrayList<String>();
        TextView msg = (TextView) findViewById(R.id.message);
        _array = new JSONArray();
        TextView tv = (TextView) findViewById(R.id.message);
        tv.setText("");
        //  test();
        //test function is used only for reading database file from assets
        //Used when we want to push data from our side.

        //enableBlu();
        progress = new ProgressDialog(SyncActivity.this);
        progress.setMessage("Please Wait...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        TreansferFile("pushNewDataToServer-");

    }


    public void TreansferFile(String filename) {

        progress.dismiss();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't give bluetooth support.", Toast.LENGTH_LONG).show();
        } else {
            intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            file = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent/" + filename+ deviceId + ".json");
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
                        startActivityForResult(intent, 0);

                        //sendBroadcast(intent);
                    }
                }
            } else
                Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_sync, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        res = resultCode;
        if (res == 0) {
            if (btAdapter.isEnabled()) {
                progress = new ProgressDialog(SyncActivity.this);
                progress.setTitle("Please Wait...");
                progress.setMessage("We are processing your request! Please check in notification bar.");
                progress.setCanceledOnTouchOutside(false);
                progress.setCancelable(false);
                progress.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.cancel();
                    }
                }, DISCOVER_DURATION+7000);
            }
        } else if (!(resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU)) {
            Toast.makeText(this, "BT cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void enableBlu() {
        // enable device discovery - this will automatically enable Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {

        }
    }


    public void transferLogs(View v) {

        ArrayList<String> arrayList = new ArrayList<String>();
        TextView msg = (TextView) findViewById(R.id.message);
        _array = new JSONArray();
        progress = new ProgressDialog(SyncActivity.this);
        progress.setMessage("Please Wait...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                _array = readFromDatabase(0);
                if (_array.length() == 0) {
                    progress.dismiss();
                } else {
                    WriteSettings(getApplicationContext(), _array.toString(), "Logs");
                    TreansferFile("Logs");
                }

            }
        }, 3000);
    }


    public void createJsonforTransfer() {
        //we will push logs and scores directly to the server

        TextView msg = (TextView) findViewById(R.id.message);
        msg.setText("");

        ScoreDBHelper scoreDBHelper = new ScoreDBHelper(this);
        List<Score> scores = scoreDBHelper.GetAll();

        if (scores == null) {
        } else if (scores.size() == 0) {
        } else {
            try {
                JSONArray scoreData = new JSONArray(), logsData = new JSONArray(), attendanceData=new JSONArray(),studentData=new JSONArray(),crlData=new JSONArray(),grpData=new JSONArray(), aserData=new JSONArray();

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
                        List<Student> studentsList=sdb.GetAllNewStudents();
                        JSONObject studentObj=new JSONObject();
                        if (studentData != null) {
                            for (int i = 0; i < studentsList.size(); i++) {
                                studentObj.put("StudentID",studentsList.get(i).StudentID);
                                studentObj.put("FirstName",studentsList.get(i).FirstName);
                                studentObj.put("MiddleName",studentsList.get(i).MiddleName);
                                studentObj.put("LastName",studentsList.get(i).LastName);
                                studentObj.put("Age",studentsList.get(i).Age);
                                studentObj.put("Class",studentsList.get(i).Class);
                                studentObj.put("UpdatedDate",studentsList.get(i).UpdatedDate);
                                studentObj.put("Gender",studentsList.get(i).Gender);
                                studentObj.put("GroupID",studentsList.get(i).GroupID);
                                studentObj.put("CreatedBy",studentsList.get(i).CreatedBy);
                                studentObj.put("newStudent",studentsList.get(i).newStudent);
                                studentObj.put("StudentUID",studentsList.get(i).StudentUID == null ? "": studentsList.get(i).StudentUID);
                                studentObj.put("IsSelected",studentsList.get(i).IsSelected == null ? false: !studentsList.get(i).IsSelected);


                                studentData.put(studentObj);
                            }
                        }

                        //pravin
                        //For New Crls data
                        List<Crl> crlsList=cdb.GetAllNewCrl();
                        JSONObject crlObj=new JSONObject();
                        if (crlData != null) {
                            for (int i = 0; i < crlsList.size(); i++) {
                                crlObj.put("CRLId",crlsList.get(i).CRLId);
                                crlObj.put("FirstName",crlsList.get(i).FirstName);
                                crlObj.put("LastName",crlsList.get(i).LastName);
                                crlObj.put("UserName",crlsList.get(i).UserName);
                                crlObj.put("Password",crlsList.get(i).Password);
                                crlObj.put("ProgramId",crlsList.get(i).ProgramId);
                                crlObj.put("Mobile",crlsList.get(i).Mobile);
                                crlObj.put("State",crlsList.get(i).State);
                                crlObj.put("Email",crlsList.get(i).Email);
                                crlObj.put("CreatedBy",crlsList.get(i).CreatedBy);
                                crlObj.put("newCrl",!crlsList.get(i).newCrl);
                                crlData.put(crlObj);
                            }
                        }

                        //pravin
                        //For New Groups data
                        List<Group> groupsList=gdb.GetAllNewGroups();
                        JSONObject grpObj=new JSONObject();
                        if (grpData != null) {
                            for (int i = 0; i < groupsList.size(); i++) {
                                grpObj.put("GroupID",groupsList.get(i).GroupID);
                                grpObj.put("GroupCode",groupsList.get(i).GroupCode);
                                grpObj.put("GroupName",groupsList.get(i).GroupName);
                                grpObj.put("UnitNumber",groupsList.get(i).UnitNumber);
                                grpObj.put("DeviceID",groupsList.get(i).DeviceID);
                                grpObj.put("Responsible",groupsList.get(i).Responsible);
                                grpObj.put("ResponsibleMobile",groupsList.get(i).ResponsibleMobile);
                                grpObj.put("VillageID",groupsList.get(i).VillageID);
                                grpObj.put("ProgramID",groupsList.get(i).ProgramID);
                                grpObj.put("CreatedBy",groupsList.get(i).CreatedBy);
                                grpObj.put("newGroup", !groupsList.get(i).newGroup);
                                grpObj.put("VillageName",groupsList.get(i).VillageName == null ? "" : groupsList.get(i).VillageName);
                                grpObj.put("SchoolName",groupsList.get(i).SchoolName == null ? "" : groupsList.get(i).SchoolName);
                                grpData.put(grpObj);
                            }
                        }

                        //Ketan
                        //For New Aser data
                        List<Aser> aserList=aserdb.GetAll();
                        JSONObject aserObj=new JSONObject();
                        if (aserData != null) {
                            for (int i = 0; i < aserList.size(); i++) {
                                aserObj.put("StudentId",aserList.get(i).StudentId);
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

                                aserData.put(aserObj);
                            }
                        }

                        StatusDBHelper statusDBHelper = new StatusDBHelper(this);
                        JSONObject obj = new JSONObject();
                        obj.put("ScoreCount", scores.size());
                        obj.put("AttendanceCount", attendanceData.length());
                        //obj.put("LogsCount", logs.size());
                        obj.put("NewStudentsCount", studentData.length());
                        obj.put("NewCrlsCount", crlData.length());
                        obj.put("NewGroupsCount", grpData.length());
                        obj.put("AserDataCount", aserData.length());
                        obj.put("TransId", new Utility().GetUniqueID());
                        obj.put("DeviceId", deviceId);
                        obj.put("MobileNumber", "0");
                        obj.put("ActivatedDate", statusDBHelper.getValue("ActivatedDate"));
                        obj.put("ActivatedForGroups", statusDBHelper.getValue("ActivatedForGroups"));
                        String requestString = "{ \"metadata\": " + obj + ", \"scoreData\": " + scoreData + ", \"attendanceData\": " + attendanceData + ", \"newStudentsData\": " + studentData+ ", \"newCrlsData\": " + crlData + ", \"newGroupsData\": " + grpData +", \"AserTableData\": " + aserData + "}";//Ketan
                        ShareProfiles ssp=new ShareProfiles();
                        ssp.WriteSettings(c,requestString,"pushNewDataToServer-"+deviceId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    static Boolean ClearDatabaseEntriesDuringFetchData(String className, Context mContext) {

        if (className.equals("Scores")) {
            ScoreDBHelper scoreDBHelper = new ScoreDBHelper(mContext);
            return scoreDBHelper.DeleteAll();
        } else if (className.equals("Logs")) {
            return false;
        } else if (className.equals("Attendance")) {
            AttendanceDBHelper attendanceDBHelper = new AttendanceDBHelper(mContext);
            return attendanceDBHelper.DeleteAll();
        }
        return false;
    }


    public void goToStudentProfiles(View view) {
        Intent i = new Intent(SyncActivity.this, AddStudentProfiles.class);
        startActivity(i);
    }


    public void goToShareStudentProfiles(View view) {
        Intent goToShareD = new Intent(SyncActivity.this, ShareProfiles.class);
        startActivity(goToShareD);
    }


    public void goToAddNewGroup(View view) {
        Intent goToAddNewGrp = new Intent(SyncActivity.this, AddNewGroup.class);
        startActivity(goToAddNewGrp);
    }


    public void goToAddNewCrl(View view) {

        Intent i = new Intent(SyncActivity.this, AddNewCrl.class);
        startActivity(i);

    }


    public void pullDataOffline(View view) {
        Intent goToPullDataOffline = new Intent(SyncActivity.this, PullData.class);
        startActivity(goToPullDataOffline);
    }

    public void ReceiveProfiles(View view) {

        // Path Declaration
        ReceivePath = Environment.getExternalStorageDirectory() + "/bluetooth/NewProfiles.zip";
        TargetPath = Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/";

        //Checking if src file exist or not (pravin)
        newProfile = new File(ReceivePath);
        if (!newProfile.exists()) {
            Toast.makeText(this, "NewProfile.zip not exist", Toast.LENGTH_SHORT).show();
        }
        else {
            MultiPhotoSelectActivity.dilog.showDilog(c, "Receiving Profiles");

            Thread mThread = new Thread() {
                @Override
                public void run() {

                    // Extraction of contents
                    Compress extract = new Compress();
                    List<String> unzippedFileNames = extract.unzip(ReceivePath, TargetPath);

                    // Inserting All Jsons in Database
                    try {
                        UpdateAllJson();
                        newProfile.delete();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(c, "Files Received & Updated in Database Successfully !!!", Toast.LENGTH_SHORT).show();

                    // Transfer Student's Profiles from Receive folder to Student Profiles
                    File src = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent");
                    File dest = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles");
                    try {
                        if (!src.exists()) {
                            Toast.makeText(c, "No folder exist in Internal Storage to copy", Toast.LENGTH_LONG).show();
                        } else if (dest.exists()) {
                            copyDirectory(src, dest);
                            Toast.makeText(c, "Files copied successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(c, "Folder does not Exist!", Toast.LENGTH_LONG).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    MultiPhotoSelectActivity.dilog.dismissDilog();
                    Toast.makeText(c, "Profiles received", Toast.LENGTH_LONG).show();
                }
            };
            mThread.start();
        }
    }

    private void wipeReceivedData() {
        // Delete Receive Folder Contents after Transferring
        String directoryToDelete = Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent";
        File dir = new File(directoryToDelete);
        for (File file : dir.listFiles())
            if (!file.isDirectory())
                file.delete();

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


    // Update Json in Device's Database when Received new files from another Device
    public void UpdateAllJson() throws JSONException {

        // For Loading CRL Json From External Storage (Assets)
        JSONArray crlJsonArray = new JSONArray(loadCrlJSONFromAsset());
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

            cdb.insertData(crlobj);
        }


        // For Loading Student Json From External Storage (Assets)
        JSONArray studentsJsonArray = new JSONArray(loadStudentJSONFromAsset());

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
            stdObj.newStudent = true;

            sdb.insertData(stdObj);
        }

        // For Loading Group Json From External Storage (Assets)
        JSONArray grpJsonArray = new JSONArray(loadGroupJSONFromAsset());

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
            grpObj.newGroup = true;

            gdb.insertData(grpObj);
        }
        BackupDatabase.backup(c);
    }

    // Reading CRL Json From internal memory
    public String loadCrlJSONFromAsset() {
        String crlJsonStr = null;

        try {
            File crlJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Crl.json");
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

    // Reading Student Json From internal memory
    public String loadStudentJSONFromAsset() {
        String studentJson = null;
        try {
            File studentJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Student.json");
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

        } catch (Exception e) {
        }

        return studentJson;

    }

    // Reading Student Json From internal memory
    public String loadGroupJSONFromAsset() {
        String groupJson = null;
        try {
            File groupJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent/", "Group.json");
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

        } catch (Exception e) {
        }

        return groupJson;

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


    public void goToAddNewUnit(View view) {

        Intent i = new Intent(SyncActivity.this, AddNewUnit.class);
        startActivity(i);

    }

    public void goToSelectUnitForEdit(View view) {
        Intent selectUnit = new Intent(SyncActivity.this, SelectUnitForEdit.class);
        startActivity(selectUnit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MultiPhotoSelectActivity.pauseFlg){
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg=false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        MultiPhotoSelectActivity.pauseFlg=true;

        MultiPhotoSelectActivity.cd = new CountDownTimer(MultiPhotoSelectActivity.duration, 1000) {
            //cd = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished)
            {
                MultiPhotoSelectActivity.duration = millisUntilFinished;
                timer = true;
            }
            @Override
            public void onFinish() {
                timer = false;
                MainActivity.sessionFlg=true;
                if(!CardAdapter.vidFlg) {
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    finishAffinity();
                }
            }
        }.start();

    }


}
