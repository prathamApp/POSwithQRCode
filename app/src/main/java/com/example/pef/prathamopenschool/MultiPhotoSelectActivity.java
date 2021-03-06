package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

//import android.support.annotation.NonNull;

public class MultiPhotoSelectActivity extends AppCompatActivity {

    //\public static TextToSp tts;
    public ImageAdapter imageAdapter;
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
    public static RelativeLayout myView;
    public static String deviceID = "DeviceID";
    int aajKaSawalPlayed = 3;
    boolean doubleBackToExitPressedOnce = false;
    String checkQJson;
    TextView tv_msg, tv_msgBottom;
    boolean appName = false;
    StatusDBHelper s;

    public static DilogBoxForProcess dilog;
    private static final int REQUEST_FOR_STORAGE_PERMISSION = 123;

    static String programID, language;
    static String sessionId;
    public static Boolean grpSelectFlag;

    static String presentStudents[];
    static String selectedGroupName;
    static String selectedGroupId;
    public static String selectedGroupsScore = "";
    static CountDownTimer cd;
    static Long timeout = (long) 20000 * 60;
    static Long duration = timeout;
    static Boolean pauseFlg = false;
    String ageGrp = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_select);

        startService(new Intent(this, WebViewService.class));

        Intent i = getIntent();
        ageGrp = i.getStringExtra("ageGroup");

        dilog = new DilogBoxForProcess();
        grpSelectFlag = true;
        attendenceData = new ArrayList<>();
        ActionBar actBar = getSupportActionBar();
        statusDBHelper = new StatusDBHelper(this);
        studentDBHelper = new StudentDBHelper(this);
        groupDBHelper = new GroupDBHelper(this);
        utility = new Utility();

        if (sessionId == null)
            sessionId = utility.GetUniqueID().toString();

        sessionContex = this;
        playVideo = new PlayVideo();
        MainActivity.sessionFlg = false;
        if (assessmentLogin.assessmentFlg)
            newNodeList = getIntent().getStringExtra("nodeList");

        // Generate Unique Device ID
        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getLanguage(Context c) {
        StatusDBHelper statdb;
        statdb = new StatusDBHelper(c);
        return statdb.getValue("TabLanguage");
    }

    public void setSelectedStudents(String groupName) throws IOException {
        grpSelectFlag = false;
        //selectedGroupId = groupDBHelper.getGroupIdByName(selectedGroupName);
        students = new ArrayList<JSONArray>();
        students.add(studentDBHelper.getStudentsList(selectedGroupId));
        setContentView(R.layout.layout_multi_photo_select);
        populateImagesFromGallery();
    }


    public void setLanguage() {
        String langString = null;
        try {
            File myJsonFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Config.json");
            FileInputStream stream = new FileInputStream(myJsonFile);
            String jsonStr = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
            JSONObject jsonObj = new JSONObject(jsonStr);
            langString = jsonObj.getString("programLanguage");

            StatusDBHelper sdb;
            sdb = new StatusDBHelper(MultiPhotoSelectActivity.this);
            sdb.Update("TabLanguage", langString);
            BackupDatabase.backup(MultiPhotoSelectActivity.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void btnChoosePhotosClick(View v) throws JSONException {

        if (sessionId == null)
            sessionId = utility.GetUniqueID().toString();

        selectedGroupsScore = "";
        Attendance attendance = null;
        AttendanceDBHelper attendanceDBHelper = new AttendanceDBHelper(this);

        SessionDBHelper sessionDBHelper = new SessionDBHelper(this);
        Session s = new Session();
        s.SessionID = MultiPhotoSelectActivity.sessionId;
        s.StartTime = new Utility().GetCurrentDateTime(false);
        s.EndTime = "NA";
        sessionDBHelper.Add(s);

        // If no student present in RI
        if (programID.equals("2") && students.get(0).length() == 0) {
            presentStudents = new String[1];
            presentStudents[0] = "RI";
            try {
                attendance = new Attendance();
                attendance.SessionID = sessionId;
                attendance.PresentStudentIds = "RI";
                attendance.GroupID = selectedGroupId;
                if (!selectedGroupsScore.contains(attendance.GroupID)) {
                    selectedGroupsScore += attendance.GroupID + ",";
                }
                attendanceDBHelper.Add(attendance);
                BackupDatabase.backup(this);


                /* -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- AAJ KA SAWAAL PLAYED ? -*-*-*-*-*-*-*-**-*-*-*-*-*-*-*-*-*-*- */

                // Decide the next screen depending on aajKaSawalPlayed status

                // Get Current Date
                String currentDate = utility.GetCurrentDate();

                // Get Currently Selected Group
                String currentlySelectedGroup = selectedGroupId;

                // Fetch records
                StatusDBHelper sdbh = new StatusDBHelper(sessionContex);
                List<String> AKSRecords = sdbh.getAKSRecordsOfSelectedGroup(currentlySelectedGroup);

                // Loop to get Aaj Ka Sawaal Played Status
                if (!AKSRecords.equals(null)) {

                    if (AKSRecords.size() == 0) {
                        // No Record Found
                        aajKaSawalPlayed = 0;
                    }

                    for (int i = 0; i < AKSRecords.size(); i++) {

                        if (AKSRecords.get(i).contains(currentDate)) {
                            // If Record Found i.e Aaj Ka Sawaal Played
                            aajKaSawalPlayed = 1;
                        } else {
                            // If Record NOT Found i.e Aaj Ka Sawaal Played
                            aajKaSawalPlayed = 0;
                        }
                    }

                }

                /*// if Questions.json not present
                JSONArray queJsonArray = null;
                try {
                    checkQJson = loadQueJSONFromAsset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (checkQJson == null) {
                    aajKaSawalPlayed = 3;
                } else {
                    // if old que json then aksplayer = 3
                    if (!checkQJson.contains("nodelist"))
                        aajKaSawalPlayed = 3;
                }*/


                // Aaj Ka Sawaal Played
                if (aajKaSawalPlayed == 1) {
                    Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                    if (assessmentLogin.assessmentFlg) {
                        main.putExtra("nodeList", newNodeList.toString());
                    }
                    MainActivity.sessionFlg = true;
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.putExtra("ageGrp", ageGrp);
                    main.putExtra("aajKaSawalPlayed", "1");
                    main.putExtra("selectedGroupId", selectedGroupId);
                    startActivity(main);
                    finish();
                }
                // Aaj Ka Sawaal NOT Played
                else if (aajKaSawalPlayed == 0) {

                    // Update updateTrailerCountbyGroupID to 1 if played
//                    StatusDBHelper updateTrailerCount = new StatusDBHelper(MultiPhotoSelectActivity.this);
//                    updateTrailerCount.updateTrailerCountbyGroupID(1, selectedGroupId);
//                    BackupDatabase.backup(MultiPhotoSelectActivity.this);

                    Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                    if (assessmentLogin.assessmentFlg) {
                        main.putExtra("nodeList", newNodeList.toString());
                    }
                    MainActivity.sessionFlg = true;
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.putExtra("ageGrp", ageGrp);
                    main.putExtra("aajKaSawalPlayed", "0");
                    main.putExtra("selectedGroupId", selectedGroupId);

                    startActivity(main);
                    finish();
                }

                // if Questions.json not present
                else if (aajKaSawalPlayed == 3) {

                    Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                    if (assessmentLogin.assessmentFlg) {
                        main.putExtra("nodeList", newNodeList.toString());
                    }
                    MainActivity.sessionFlg = true;
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.putExtra("ageGrp", ageGrp);
                    main.putExtra("aajKaSawalPlayed", "3");
                    main.putExtra("selectedGroupId", selectedGroupId);


                    startActivity(main);
                    finish();
                }

                /*Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                if (assessmentLogin.assessmentFlg)
                    main.putExtra("nodeList", newNodeList.toString());
                startActivity(main);
                finish();*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Atleast one student is present in group/ unit
        else {
            ArrayList<JSONObject> selectedItems = imageAdapter.getCheckedItems();
            if (selectedItems.size() > 0) {

                presentStudents = new String[selectedItems.size()];
                try {
                    if (selectedItems != null && selectedItems.size() > 0) {
                        for (int i = 0; i < selectedItems.size(); i++) {
                            attendance = new Attendance();
                            attendance.SessionID = sessionId;
                            attendance.PresentStudentIds = selectedItems.get(i).getString("stdId");
                            presentStudents[i] = selectedItems.get(i).getString("stdId");
                            attendance.GroupID = selectedItems.get(i).getString("grpId");
                            if (!selectedGroupsScore.contains(attendance.GroupID)) {
                                selectedGroupsScore += attendance.GroupID + ",";
                            }
                            attendanceDBHelper.Add(attendance);
                        }
                        BackupDatabase.backup(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                /* -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- AAJ KA SAWAAL PLAYED ? -*-*-*-*-*-*-*-**-*-*-*-*-*-*-*-*-*-*- */


                // Decide the next screen depending on aajKaSawalPlayed status

                // Get Current Date
                String currentDate = utility.GetCurrentDate();

                // Get Currently Selected Group
                String currentlySelectedGroup = selectedGroupId;

                // Fetch records
                StatusDBHelper sdbh = new StatusDBHelper(sessionContex);
                List<String> AKSRecords = sdbh.getAKSRecordsOfSelectedGroup(currentlySelectedGroup);

                // Loop to get Aaj Ka Sawaal Played Status
                if (!AKSRecords.equals(null)) {

                    if (AKSRecords.size() == 0) {
                        // No Record Found
                        aajKaSawalPlayed = 0;
                    }

                    for (int i = 0; i < AKSRecords.size(); i++) {

                        if (AKSRecords.get(i).contains(currentDate)) {
                            // If Record Found i.e Aaj Ka Sawaal Played
                            aajKaSawalPlayed = 1;
                        } else {
                            // If Record NOT Found i.e Aaj Ka Sawaal Played
                            aajKaSawalPlayed = 0;
                        }
                    }

                }

                /*// if Questions.json not present
                JSONArray queJsonArray = null;
                try {
                    checkQJson = loadQueJSONFromAsset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (checkQJson == null) {
                    aajKaSawalPlayed = 3;
                } else {
                    // if old que json then aksplayer = 3
                    if (!checkQJson.contains("nodelist"))
                        aajKaSawalPlayed = 3;
                }*/

                if (aajKaSawalPlayed == 1) {
                    Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                    if (assessmentLogin.assessmentFlg) {
                        main.putExtra("nodeList", newNodeList.toString());
                    }
                    MainActivity.sessionFlg = true;
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.putExtra("ageGrp", ageGrp);
                    main.putExtra("aajKaSawalPlayed", "1");
                    main.putExtra("selectedGroupId", selectedGroupId);
                    startActivity(main);
                    finish();
                } else if (aajKaSawalPlayed == 0) {

                    // Update updateTrailerCountbyGroupID to 1 if played
//                    StatusDBHelper updateTrailerCount = new StatusDBHelper(MultiPhotoSelectActivity.this);
//                    updateTrailerCount.updateTrailerCountbyGroupID(1, selectedGroupId);
//                    BackupDatabase.backup(MultiPhotoSelectActivity.this);

                    Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                    if (assessmentLogin.assessmentFlg) {
                        main.putExtra("nodeList", newNodeList.toString());
                    }
                    MainActivity.sessionFlg = true;
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.putExtra("ageGrp", ageGrp);
                    main.putExtra("aajKaSawalPlayed", "0");
                    main.putExtra("selectedGroupId", selectedGroupId);
                    startActivity(main);
                    finish();
                }
                // if Questions.json not present
                else if (aajKaSawalPlayed == 3) {

                    Intent main = new Intent(MultiPhotoSelectActivity.this, MainActivity.class);
                    if (assessmentLogin.assessmentFlg) {
                        main.putExtra("nodeList", newNodeList.toString());
                    }
                    MainActivity.sessionFlg = true;
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.putExtra("ageGrp", ageGrp);
                    main.putExtra("aajKaSawalPlayed", "3");
                    main.putExtra("selectedGroupId", selectedGroupId);
                    startActivity(main);
                    finish();
                }


            } else {
                Toast.makeText(this, "Please Select your Profile !!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Reading CRL Json From Internal Memory
    public String loadQueJSONFromAsset() {
        String queJsonStr = null;

        try {
            File queJsonSDCard = new File(splashScreenVideo.fpath + "AajKaSawaal/", "Questions.json");
            FileInputStream stream = new FileInputStream(queJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                queJsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return queJsonStr;
    }

    public void populateImagesFromGallery() throws IOException {
        if (!mayRequestGalleryImages()) {
            return;
        }

        ArrayList<JSONObject> imageUrls = loadPhotosFromNativeGallery();
        initializeRecyclerView(imageUrls);
    }

    private boolean mayRequestGalleryImages() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            //promptStoragePermission();
            showPermissionRationaleSnackBar();
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_FOR_STORAGE_PERMISSION);
        }

        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        switch (requestCode) {

            case REQUEST_FOR_STORAGE_PERMISSION: {

                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        try {
                            populateImagesFromGallery();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                            showPermissionRationaleSnackBar();
                        } else {
                            Toast.makeText(this, "Go to settings and enable permission", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                break;
            }
        }
    }

    private ArrayList<JSONObject> loadPhotosFromNativeGallery() throws IOException {
        ArrayList<JSONObject> imageUrls = new ArrayList<JSONObject>();
        JSONObject studentWithImgs;

        File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles");
        if (folder.exists()) {

            File photos[] = folder.listFiles();

            String fileNameWithExtension = "";//default fileName

            try {
                for (int j = 0; j < students.size(); j++) {//noOfGroups
                    for (int k = 0; k < students.get(j).length(); k++) {//noOfStudents
                        int flag = 0;
                        String stdid = students.get(j).getJSONObject(k).getString("studentId");
                        String stdName = students.get(j).getJSONObject(k).getString("studentName");
                        String grpId = students.get(j).getJSONObject(k).getString("groupId");
                        String gender = students.get(j).getJSONObject(k).getString("gender");
                        for (int i = 0; i < photos.length; i++) {//noOfImages
                            fileNameWithExtension = photos[i].getName();
                            String filePath = photos[i].getAbsolutePath();
                            String[] fileName = fileNameWithExtension.split("\\.");
                            if (fileName[0].equals(stdid)) {
                                flag = 1;
                                studentWithImgs = new JSONObject();
                                studentWithImgs.put("id", stdid);
                                studentWithImgs.put("name", stdName);
                                studentWithImgs.put("grpId", grpId);
                                studentWithImgs.put("imgPath", filePath);
                                imageUrls.add(studentWithImgs);
                                break;
                            }
                        }
                        if (flag == 0) {
                            studentWithImgs = new JSONObject();
                            studentWithImgs.put("id", stdid);
                            studentWithImgs.put("name", stdName);
                            studentWithImgs.put("grpId", grpId);
                            if ((gender.equals("M")) || (gender.equals("Male")))
                                studentWithImgs.put("imgPath", Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/Boys/" + generateRandomNumber() + ".png");
                            else
                                studentWithImgs.put("imgPath", Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/Girls/" + generateRandomNumber() + ".png");
                            imageUrls.add(studentWithImgs);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return imageUrls;
        } else {
            return null;
        }
    }

    private String generateRandomNumber() {
        Random rand = new Random();

        int n = rand.nextInt(3) + 1;
        return Integer.toString(n);
    }


    private void initializeRecyclerView(ArrayList<JSONObject> imageUrls) {
        imageAdapter = new ImageAdapter(this, imageUrls);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 8);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.item_offset2));
        recyclerView.setAdapter(imageAdapter);
    }

    private void showPermissionRationaleSnackBar() {
        Snackbar.make(findViewById(R.id.btn_continue), getString(R.string.permission_rationale),
                Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Request the permission
                ActivityCompat.requestPermissions(MultiPhotoSelectActivity.this,
                        new String[]{READ_EXTERNAL_STORAGE},
                        REQUEST_FOR_STORAGE_PERMISSION);
            }
        }).show();

    }


    @Override
    public void onBackPressed() {
        if (grpSelectFlag) {
            super.onBackPressed();
        } else {
            setContentView(R.layout.group_select);
            onResume();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.group_select);
        myView = (RelativeLayout) findViewById(R.id.my_layoutId);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        tv_title = (TextView) findViewById(R.id.tv_select);
        next = (Button) findViewById(R.id.goNext);
        grpSelectFlag = true;

        if (radioGroup.getChildCount() > 0) {
            radioGroup.removeAllViews();
        }

        displayStudentsByAgeGroup(ageGrp);

        setLanguage();

        // set Title according to program
        if (MultiPhotoSelectActivity.programID.equals("1"))
            setTitle("Pratham Digital - H Learning");
        else if (MultiPhotoSelectActivity.programID.equals("2"))
            setTitle("Pratham Digital - Read India");
        else if (MultiPhotoSelectActivity.programID.equals("3"))
            setTitle("Pratham Digital - Second Chance");
        else if (MultiPhotoSelectActivity.programID.equals("10"))
            setTitle("Pratham Digital - Pratham Institute");
        else if (MultiPhotoSelectActivity.programID.equals("8"))
            setTitle("Pratham Digital - ECE");
        else
            setTitle("Pratham Digital");

        s = new StatusDBHelper(this);
        appName = s.initialDataAvailable("appName");
        if (appName == false) {
            s = new StatusDBHelper(MultiPhotoSelectActivity.this);
            // app name
            if (MultiPhotoSelectActivity.programID.equals("1"))
                s.insertInitialData("appName", "Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                s.insertInitialData("appName", "Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                s.insertInitialData("appName", "Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("10"))
                s.insertInitialData("appName", "Pratham Digital - Pratham Institute");
            else if (MultiPhotoSelectActivity.programID.equals("8"))
                s.insertInitialData("appName", "Pratham Digital - ECE");
            else
                s.insertInitialData("appName", "Pratham Digital");

        } else {
            s = new StatusDBHelper(MultiPhotoSelectActivity.this);
            // app name
            if (MultiPhotoSelectActivity.programID.equals("1"))
                s.Update("appName", "Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                s.Update("appName", "Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                s.Update("appName", "Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("10"))
                s.Update("appName", "Pratham Digital - Pratham Institute");
            else if (MultiPhotoSelectActivity.programID.equals("8"))
                s.Update("appName", "Pratham Digital - ECE");
            else
                s.Update("appName", "Pratham Digital");
        }

        BackupDatabase.backup(MultiPhotoSelectActivity.this);

        // session
        duration = timeout;
        if (pauseFlg) {
            cd.cancel();
            pauseFlg = false;
        }

    }

    private void displayStudentsByAgeGroup(String ageGrp) {
        if (ageGrp.equalsIgnoreCase("5")) {
            display3to6Students();
        } else if (ageGrp.equalsIgnoreCase("8")) {
            display7to14Students();
        } else if (ageGrp.equalsIgnoreCase("15")) {
            // display all Stds for PI
            display14to18Students();
        } else if (ageGrp.equalsIgnoreCase("25")) {
            // display vocational stds for PI
            displayVocationalStudents();
        } else {
            displayStudents();
        }
    }

    public void display14to18Students() {
        String assignedGroupIDs[];
        students = new ArrayList<JSONArray>();
        groupNames = new ArrayList<String>();
        assignedIds = new ArrayList<String>();
        try {
            programID = new Utility().getProgramId();

            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                tv_title.setText("Select Groups");
            } else if (programID.equals("2")) {
                tv_title.setText("Select Units");
            } else
                tv_title.setText("Select Groups");

//            if (programID.equals("1") || programID.equals("2") || programID.equals("3") || programID.equals("10")) {
            next = (Button) findViewById(R.id.goNext);
            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                next.setText("Select Groups");
            } else if (programID.equals("2")) {
                next.setText("Select Units");
            } else
                next.setText("Select Groups");

            assignedGroupIDs = statusDBHelper.getGroupIDs();
            if (!assignedGroupIDs[0].equals("")) {
                for (int i = 0; i < assignedGroupIDs.length; i++) {
                    if (!assignedGroupIDs[i].equals("0")) {
                        // check students age & add accordingly
                        // Get Std count by group id
                        StudentDBHelper stdDBHelper = new StudentDBHelper(this);
                        List<Student> lstStudent = stdDBHelper.getStudentsByGroup(assignedGroupIDs[i]);
                        int stdCount = 0;
                        int wrongStdCount = 0;
                        // get age of each std & then add grp if student age is less than 8
                        for (int j = 0; j < lstStudent.size(); j++) {
                            int age = lstStudent.get(j).Age;
                            if (age > 13 && age < 19) {
                                stdCount++;
                            } else {
                                wrongStdCount++;
                            }
                        }
                        // if all student age criteria satisfied
                        if (stdCount == lstStudent.size()) {
                            assignedIds.add(assignedGroupIDs[i]);
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                        }
                        // if all student age criteria not satisfied
                        else if (wrongStdCount == lstStudent.size()) {

                        }
                        // few std fullfills criteria then add whole grp
                        else if (stdCount > 0 && wrongStdCount > 0 && lstStudent.size() > 0) {
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            assignedIds.add(assignedGroupIDs[i]);
                        }
                    }
                }
            }
            next.setClickable(false);
            int groupCount = groupNames.size();
            if (groupNames.isEmpty()) {
                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                } else if (programID.equals("2")) {
                    Toast.makeText(this, "Assign Units First", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();

            } else {
                next.setClickable(false);
                radioGroup.setPadding(0, 50, 0, 0);
                RadioButton rb;
                for (int i = 0; i < groupCount; i++) {
                    rb = new RadioButton(this);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 0, 0, 0);
                    rb.setLayoutParams(params);

                    rb.setHeight(160);
                    rb.setWidth(135);
                    rb.setTextSize(16);

                    // For HL
                    if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {
                        rb.setBackgroundResource(R.drawable.groups);
                    } else if (MultiPhotoSelectActivity.programID.equals("2")) {
                        rb.setBackgroundResource(R.drawable.units);
                    } else
                        rb.setBackgroundResource(R.drawable.groups);

                    rb.setPadding(0, 0, 2, 0);
                    rb.setId(i);
                    rb.setText(groupNames.get(i));
                    radioGroup.addView(rb);
                }
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // get selected radio button from radioGroup
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            if (selectedId == -1) {
                                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                } else if (programID.equals("2")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one unit", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();

                            } else {
                                // find the radiobutton by returned id
                                radioButton = (RadioButton) findViewById(selectedId);
                                radioButton.setBackgroundColor(getResources().getColor(R.color.selected));
                                selectedGroupId = assignedIds.get(selectedId);
                                selectedGroupName = (String) radioButton.getText();
                                if (selectedGroupName.equals(null)) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Assign Groups First", Toast.LENGTH_SHORT).show();
                                } else {
                                    setSelectedStudents(selectedGroupName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            /*} else {
                Toast.makeText(this, "Invalid Program Id", Toast.LENGTH_SHORT).show();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void displayStudents() {
        String assignedGroupIDs[];
        students = new ArrayList<JSONArray>();
        groupNames = new ArrayList<String>();
        assignedIds = new ArrayList<String>();
        try {
            programID = new Utility().getProgramId();

            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                tv_title.setText("Select Groups");
            } else if (programID.equals("2")) {
                tv_title.setText("Select Units");
            } else {
                tv_title.setText("Select Groups");
            }

//            if (programID.equals("1") || programID.equals("2") || programID.equals("3") || programID.equals("10")) {
            next = (Button) findViewById(R.id.goNext);
            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                next.setText("Select Groups");
            } else if (programID.equals("2")) {
                next.setText("Select Units");
            } else {
                next.setText("Select Groups");
            }

            assignedGroupIDs = statusDBHelper.getGroupIDs();
            if (!assignedGroupIDs[0].equals("")) {
                for (int i = 0; i < assignedGroupIDs.length; i++) {
                    if (!assignedGroupIDs[i].equals("0")) {
                        students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                        groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                        assignedIds.add(assignedGroupIDs[i]);
                    }
                }
            }
            next.setClickable(false);
            int groupCount = groupNames.size();
            if (groupNames.isEmpty()) {
                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                } else if (programID.equals("2")) {
                    Toast.makeText(this, "Assign Units First", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                }
            } else {
                next.setClickable(false);
                radioGroup.setPadding(0, 50, 0, 0);
                RadioButton rb;
                for (int i = 0; i < groupCount; i++) {
                    rb = new RadioButton(this);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 0, 0, 0);
                    rb.setLayoutParams(params);

                    rb.setHeight(160);
                    rb.setWidth(135);
                    rb.setTextSize(16);

                    // For HL
                    if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {
                        rb.setBackgroundResource(R.drawable.groups);
                    } else if (MultiPhotoSelectActivity.programID.equals("2")) {
                        rb.setBackgroundResource(R.drawable.units);
                    } else {
                        rb.setBackgroundResource(R.drawable.groups);
                    }

                    rb.setPadding(0, 0, 2, 0);
                    rb.setId(i);
                    rb.setText(groupNames.get(i));
                    radioGroup.addView(rb);
                }
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // get selected radio button from radioGroup
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            if (selectedId == -1) {
                                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                } else if (programID.equals("2")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one unit", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // find the radiobutton by returned id
                                radioButton = (RadioButton) findViewById(selectedId);
                                radioButton.setBackgroundColor(getResources().getColor(R.color.selected));
                                selectedGroupId = assignedIds.get(selectedId);
                                selectedGroupName = (String) radioButton.getText();
                                if (selectedGroupName.equals(null)) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Assign Groups First", Toast.LENGTH_SHORT).show();
                                } else {
                                    setSelectedStudents(selectedGroupName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            /*} else {
                Toast.makeText(this, "Invalid Program Id", Toast.LENGTH_SHORT).show();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display3to6Students() {
        String assignedGroupIDs[];
        students = new ArrayList<JSONArray>();
        groupNames = new ArrayList<String>();
        assignedIds = new ArrayList<String>();
        try {
            programID = new Utility().getProgramId();

            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                tv_title.setText("Select Groups");
            } else if (programID.equals("2")) {
                tv_title.setText("Select Units");
            } else {
                tv_title.setText("Select Groups");
            }

//            if (programID.equals("1") || programID.equals("2") || programID.equals("3") || programID.equals("10")) {
            next = (Button) findViewById(R.id.goNext);
            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                next.setText("Select Groups");
            } else if (programID.equals("2")) {
                next.setText("Select Units");
            } else {
                next.setText("Select Groups");
            }

            assignedGroupIDs = statusDBHelper.getGroupIDs();
            if (!assignedGroupIDs[0].equals("")) {
                for (int i = 0; i < assignedGroupIDs.length; i++) {
                    if (!assignedGroupIDs[i].equals("0")) {
                        // check students age & add accordingly
                        // Get Std count by group id
                        StudentDBHelper stdDBHelper = new StudentDBHelper(this);
                        List<Student> lstStudent = stdDBHelper.getStudentsByGroup(assignedGroupIDs[i]);
                        int stdCount = 0;
                        int wrongStdCount = 0;
                        // get age of each std & then add grp if student age is less than 8
                        for (int j = 0; j < lstStudent.size(); j++) {
                            int age = lstStudent.get(j).Age;
                            if (age < 7) {
                                stdCount++;
                            } else {
                                wrongStdCount++;
                            }
                        }
                        // if all student age criteria satisfied
                        if (stdCount == lstStudent.size()) {
                            assignedIds.add(assignedGroupIDs[i]);
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                        }
                        // if all student age criteria not satisfied
                        else if (wrongStdCount == lstStudent.size()) {

                        }
                        // few std fullfills criteria then add whole grp
                        else if (stdCount > 0 && wrongStdCount > 0 && lstStudent.size() > 0) {
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            assignedIds.add(assignedGroupIDs[i]);
                        }
                    }
                }
            }
            next.setClickable(false);
            int groupCount = groupNames.size();
            if (groupNames.isEmpty()) {
                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                } else if (programID.equals("2")) {
                    Toast.makeText(this, "Assign Units First", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                }
            } else {
                next.setClickable(false);
                radioGroup.setPadding(0, 50, 0, 0);
                RadioButton rb;
                for (int i = 0; i < groupCount; i++) {
                    rb = new RadioButton(this);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 0, 0, 0);
                    rb.setLayoutParams(params);

                    rb.setHeight(160);
                    rb.setWidth(135);
                    rb.setTextSize(16);

                    // For HL
                    if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {
                        rb.setBackgroundResource(R.drawable.groups);
                    } else if (MultiPhotoSelectActivity.programID.equals("2")) {
                        rb.setBackgroundResource(R.drawable.units);
                    } else {
                        rb.setBackgroundResource(R.drawable.groups);
                    }

                    rb.setPadding(0, 0, 2, 0);
                    rb.setId(i);
                    rb.setText(groupNames.get(i));
                    radioGroup.addView(rb);
                }
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // get selected radio button from radioGroup
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            if (selectedId == -1) {
                                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                } else if (programID.equals("2")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one unit", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // find the radiobutton by returned id
                                radioButton = (RadioButton) findViewById(selectedId);
                                radioButton.setBackgroundColor(getResources().getColor(R.color.selected));
                                selectedGroupId = assignedIds.get(selectedId);
                                selectedGroupName = (String) radioButton.getText();
                                if (selectedGroupName.equals(null)) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Assign Groups First", Toast.LENGTH_SHORT).show();
                                } else {
                                    setSelectedStudents(selectedGroupName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
       /*     } else {
                Toast.makeText(this, "Invalid Program Id", Toast.LENGTH_SHORT).show();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display7to14Students() {
        String assignedGroupIDs[];
        students = new ArrayList<JSONArray>();
        groupNames = new ArrayList<String>();
        assignedIds = new ArrayList<String>();
        try {
            programID = new Utility().getProgramId();

            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                tv_title.setText("Select Groups");
            } else if (programID.equals("2")) {
                tv_title.setText("Select Units");
            } else {
                tv_title.setText("Select Groups");
            }

//            if (programID.equals("1") || programID.equals("2") || programID.equals("3") || programID.equals("10")) {
            next = (Button) findViewById(R.id.goNext);
            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                next.setText("Select Groups");
            } else if (programID.equals("2")) {
                next.setText("Select Units");
            } else {
                next.setText("Select Groups");
            }

            assignedGroupIDs = statusDBHelper.getGroupIDs();
            if (!assignedGroupIDs[0].equals("")) {
                for (int i = 0; i < assignedGroupIDs.length; i++) {
                    if (!assignedGroupIDs[i].equals("0")) {
                        // check students age & add accordingly
                        // Get Std count by group id
                        StudentDBHelper stdDBHelper = new StudentDBHelper(this);
                        List<Student> lstStudent = stdDBHelper.getStudentsByGroup(assignedGroupIDs[i]);
                        int stdCount = 0;
                        int wrongStdCount = 0;
                        // get age of each std & then add grp if student age is less than 8
                        for (int j = 0; j < lstStudent.size(); j++) {
                            int age = lstStudent.get(j).Age;
                            if (age > 6 && age < 15) {
                                stdCount++;
                            } else {
                                wrongStdCount++;
                            }
                        }
                        // if all student age criteria satisfied
                        if (stdCount == lstStudent.size()) {
                            assignedIds.add(assignedGroupIDs[i]);
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                        }
                        // if all student age criteria not satisfied
                        else if (wrongStdCount == lstStudent.size()) {

                        }
                        // few std fullfills criteria then add whole grp
                        else if (stdCount > 0 && wrongStdCount > 0 && lstStudent.size() > 0) {
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            assignedIds.add(assignedGroupIDs[i]);
                        }
                    }
                }
            }
            next.setClickable(false);
            int groupCount = groupNames.size();
            if (groupNames.isEmpty()) {
                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                } else if (programID.equals("2")) {
                    Toast.makeText(this, "Assign Units First", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                }
            } else {
                next.setClickable(false);
                radioGroup.setPadding(0, 50, 0, 0);
                RadioButton rb;
                for (int i = 0; i < groupCount; i++) {
                    rb = new RadioButton(this);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 0, 0, 0);
                    rb.setLayoutParams(params);

                    rb.setHeight(160);
                    rb.setWidth(135);
                    rb.setTextSize(16);

                    // For HL
                    if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {
                        rb.setBackgroundResource(R.drawable.groups);
                    } else if (MultiPhotoSelectActivity.programID.equals("2")) {
                        rb.setBackgroundResource(R.drawable.units);
                    } else {
                        rb.setBackgroundResource(R.drawable.groups);
                    }

                    rb.setPadding(0, 0, 2, 0);
                    rb.setId(i);
                    rb.setText(groupNames.get(i));
                    radioGroup.addView(rb);
                }
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // get selected radio button from radioGroup
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            if (selectedId == -1) {
                                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                } else if (programID.equals("2")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one unit", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // find the radiobutton by returned id
                                radioButton = (RadioButton) findViewById(selectedId);
                                radioButton.setBackgroundColor(getResources().getColor(R.color.selected));
                                selectedGroupId = assignedIds.get(selectedId);
                                selectedGroupName = (String) radioButton.getText();
                                if (selectedGroupName.equals(null)) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Assign Groups First", Toast.LENGTH_SHORT).show();
                                } else {
                                    setSelectedStudents(selectedGroupName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            /*} else {
                Toast.makeText(this, "Invalid Program Id", Toast.LENGTH_SHORT).show();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayVocationalStudents() {
        String assignedGroupIDs[];
        students = new ArrayList<JSONArray>();
        groupNames = new ArrayList<String>();
        assignedIds = new ArrayList<String>();
        try {
            programID = new Utility().getProgramId();

            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                tv_title.setText("Select Groups");
            } else if (programID.equals("2")) {
                tv_title.setText("Select Units");
            } else
                tv_title.setText("Select Groups");

//            if (programID.equals("1") || programID.equals("2") || programID.equals("3") || programID.equals("10")) {
            next = (Button) findViewById(R.id.goNext);
            if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                next.setText("Select Groups");
            } else if (programID.equals("2")) {
                next.setText("Select Units");
            } else
                next.setText("Select Groups");

            assignedGroupIDs = statusDBHelper.getGroupIDs();
            if (!assignedGroupIDs[0].equals("")) {
                for (int i = 0; i < assignedGroupIDs.length; i++) {
                    if (!assignedGroupIDs[i].equals("0")) {
                        // check students age & add accordingly
                        // Get Std count by group id
                        StudentDBHelper stdDBHelper = new StudentDBHelper(this);
                        List<Student> lstStudent = stdDBHelper.getStudentsByGroup(assignedGroupIDs[i]);
                        int stdCount = 0;
                        int wrongStdCount = 0;
                        // get age of each std & then add grp if student age is less than 8
                        for (int j = 0; j < lstStudent.size(); j++) {
                            int age = lstStudent.get(j).Age;
                            if (age > 17 && age < 30) {
                                stdCount++;
                            } else {
                                wrongStdCount++;
                            }
                        }
                        // if all student age criteria satisfied
                        if (stdCount == lstStudent.size()) {
                            assignedIds.add(assignedGroupIDs[i]);
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                        }
                        // if all student age criteria not satisfied
                        else if (wrongStdCount == lstStudent.size()) {

                        }
                        // few std fullfills criteria then add whole grp
                        else if (stdCount > 0 && wrongStdCount > 0 && lstStudent.size() > 0) {
                            students.add(studentDBHelper.getStudentsList(assignedGroupIDs[i]));
                            groupNames.add(groupDBHelper.getGroupById(assignedGroupIDs[i]));
                            assignedIds.add(assignedGroupIDs[i]);
                        }
                    }
                }
            }
            next.setClickable(false);
            int groupCount = groupNames.size();
            if (groupNames.isEmpty()) {
                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();
                } else if (programID.equals("2")) {
                    Toast.makeText(this, "Assign Units First", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(this, "Assign Groups First", Toast.LENGTH_LONG).show();

            } else {
                next.setClickable(false);
                radioGroup.setPadding(0, 50, 0, 0);
                RadioButton rb;
                for (int i = 0; i < groupCount; i++) {
                    rb = new RadioButton(this);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 0, 0, 0);
                    rb.setLayoutParams(params);

                    rb.setHeight(160);
                    rb.setWidth(135);
                    rb.setTextSize(16);

                    // For HL
                    if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {
                        rb.setBackgroundResource(R.drawable.groups);
                    } else if (MultiPhotoSelectActivity.programID.equals("2")) {
                        rb.setBackgroundResource(R.drawable.units);
                    } else
                        rb.setBackgroundResource(R.drawable.groups);

                    rb.setPadding(0, 0, 2, 0);
                    rb.setId(i);
                    rb.setText(groupNames.get(i));
                    radioGroup.addView(rb);
                }
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // get selected radio button from radioGroup
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            if (selectedId == -1) {
                                if (programID.equals("1") || programID.equals("3") || programID.equals("10")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();
                                } else if (programID.equals("2")) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one unit", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Select atleast one group", Toast.LENGTH_SHORT).show();

                            } else {
                                // find the radiobutton by returned id
                                radioButton = (RadioButton) findViewById(selectedId);
                                radioButton.setBackgroundColor(getResources().getColor(R.color.selected));
                                selectedGroupId = assignedIds.get(selectedId);
                                selectedGroupName = (String) radioButton.getText();
                                if (selectedGroupName.equals(null)) {
                                    Toast.makeText(MultiPhotoSelectActivity.this, "Assign Groups First", Toast.LENGTH_SHORT).show();
                                } else {
                                    setSelectedStudents(selectedGroupName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        Log.d("destroyed", "------------- Multi Photo --------------- in Destroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();

        pauseFlg = true;

        cd = new CountDownTimer(duration, 1000) {
            //cd = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                duration = millisUntilFinished;
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
    }

}