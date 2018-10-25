package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CrlAddEditScreen extends AppCompatActivity {

    Button btn_AddNewGroup, btn_AddNewStd, btn_EditStd, btn_AddNewCrl, btn_AddNewUnit, btn_EditUnit;

    Context sessionContex, c;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    ListView listOfCreatedGroups;
    TextView tv_CreatedGroups;
    StudentDBHelper sdb;
    int ChildrenCount, BaselineCount, Endline1Count, Endline2Count, Endline3Count, Endline4Count;
    AserDBHelper adb;
    List<String> analysisReport;
    TableLayout tableLayout;
    String tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crl_add_edit_screen);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        c = this;
        GroupDBHelper gdb = new GroupDBHelper(c);

        // Hide Actionbar
        getSupportActionBar().hide();

        tableLayout = (TableLayout) findViewById(R.id.tablelayout);

        btn_AddNewGroup = (Button) findViewById(R.id.btn_AddNewGroup);
        btn_AddNewStd = (Button) findViewById(R.id.btn_AddNewStudent);
        btn_EditStd = (Button) findViewById(R.id.btn_EditStudent);
        btn_AddNewCrl = (Button) findViewById(R.id.btn_AddNewCrl);
        btn_AddNewUnit = (Button) findViewById(R.id.btn_AddNewUnit);
        btn_EditUnit = (Button) findViewById(R.id.btn_EditUChildList);

        btn_EditUnit.setVisibility(View.GONE);

        listOfCreatedGroups = (ListView) findViewById(R.id.LvCreatedGroups);
        tv_CreatedGroups = (TextView) findViewById(R.id.tv_createdGrp);

        // Display Created Groups by Crl
        List<Group> AllGroups = gdb.GetAll();


        /******************************************************* POPULATING ANALYSIS in Case of RI ****************************************************/

        if (MultiPhotoSelectActivity.programID.equals("2")) {

            tableLayout.setVisibility(View.VISIBLE);

            analysisReport = new ArrayList<>();

            // Add header row
            TableRow rowHeader = new TableRow(c);
            rowHeader.setBackgroundColor(Color.parseColor("#c0c0c0"));
            rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            String[] headerText = {"Group Name", "Children", "BL", "EL 1", "EL 2", "EL 3", "EL 4"};
            for (String c : headerText) {
                TextView tv = new TextView(this);
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18);
                tv.setTextColor(Color.BLACK);
                tv.setPadding(5, 5, 5, 5);
                tv.setText(c);
                rowHeader.addView(tv);
            }
            tableLayout.addView(rowHeader);

            // Fetching Group ID & Collecting Data
            for (int i = 0; i < AllGroups.size(); i++) {

                // Fetching Data
                final String grpid = AllGroups.get(i).GroupID;
                final String grpName = AllGroups.get(i).GroupName; //  Unit ID
                final String SchoolName = AllGroups.get(i).SchoolName;
                final String vilID = String.valueOf(AllGroups.get(i).VillageID);

                // Get Children count from Student Table based on Group ID
                sdb = new StudentDBHelper(c);
                ChildrenCount = sdb.GetStudentCount(grpid);

                // Get Children count from Aser Table based on Group ID
                adb = new AserDBHelper(c);
                BaselineCount = adb.GetBaselineCount(grpid);
                Endline1Count = adb.GetEndline1Count(grpid);
                Endline2Count = adb.GetEndline2Count(grpid);
                Endline3Count = adb.GetEndline3Count(grpid);
                Endline4Count = adb.GetEndline4Count(grpid);

               /* // Creating List of Collected Data
                analysisReport.add("Group ID : " + grpid);
                analysisReport.add("Group Name : " + grpName);
                analysisReport.add("Children Count : " + ChildrenCount);
                analysisReport.add("Baseline Count : " + String.valueOf(BaselineCount));
                analysisReport.add("Endline 1 Count : " + String.valueOf(Endline1Count));
                analysisReport.add("Endline 2 Count : " + String.valueOf(Endline2Count));
                analysisReport.add("Endline 3 Count : " + String.valueOf(Endline3Count));
                analysisReport.add("Endline 4m Count : " + String.valueOf(Endline4Count));*/

                // dara rows
                final TableRow row = new TableRow(c);
                //row.setClickable(true);

                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                final String[] colText = {grpName, String.valueOf(ChildrenCount), String.valueOf(BaselineCount), String.valueOf(Endline1Count), String.valueOf(Endline2Count), String.valueOf(Endline3Count), String.valueOf(Endline4Count)/*, SchoolName, vilID, grpid*/};

                for (String text : colText) {


                    TextView tv = new TextView(this);
                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(16);
                    tv.setPadding(5, 5, 5, 5);
                    tv.setText(text);

                    row.addView(tv);

                }

                /*// Hiding Data
                final TextView School = (TextView) row.getChildAt(7);
                School.setVisibility(View.GONE);
                final TextView vilid = (TextView) row.getChildAt(8);
                vilid.setVisibility(View.GONE);
                final TextView grpd = (TextView) row.getChildAt(9);
                grpd.setVisibility(View.GONE);*/


                // Go to Edit Screen
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent goToSelectClassToEdit = new Intent(CrlAddEditScreen.this, SelectClassForUniversalChild.class);

                        goToSelectClassToEdit.putExtra("School", SchoolName);

                        goToSelectClassToEdit.putExtra("VillageID", vilID);
                        VillageDBHelper vdb = new VillageDBHelper(c);
                        Village vObj = vdb.Get(Integer.parseInt(vilID));
                        goToSelectClassToEdit.putExtra("Village", vObj.VillageName);
                        goToSelectClassToEdit.putExtra("State", vObj.State);
                        goToSelectClassToEdit.putExtra("Block", vObj.Block);

                        goToSelectClassToEdit.putExtra("UnitName", grpName);
                        goToSelectClassToEdit.putExtra("From", "Edit");
                        goToSelectClassToEdit.putExtra("Group ID", grpid);

                        startActivity(goToSelectClassToEdit);
                        finish();

                    }
                });


                tableLayout.addView(row);


            }

        } else if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {

            tableLayout.setVisibility(View.GONE);
            listOfCreatedGroups.setVisibility(View.GONE);
            tv_CreatedGroups.setVisibility(View.GONE);
        } else {
            tableLayout.setVisibility(View.GONE);
            listOfCreatedGroups.setVisibility(View.GONE);
            tv_CreatedGroups.setVisibility(View.GONE);
        }

        // List<String> TotalCollectedData = analysisReport;
        analysisReport = analysisReport;
        /*// Extracting Data
        for (int j = 0; j < analysisReport.size(); j++) {
            String GrpID = analysisReport.get(j);
        }
*/

        /******************************************************* Displaying ANALYSIS ****************************************************/

        /*// Display In List
        if (AllGroups.isEmpty()) {
            listOfCreatedGroups.setVisibility(View.GONE);
            tv_CreatedGroups.setVisibility(View.GONE);
        }
        *//*ArrayAdapter<Group> createdGrpAdapter = new ArrayAdapter<Group>(this, R.layout.created_groups_list_view, AllGroups);
        listOfCreatedGroups.setAdapter(createdGrpAdapter);*//*
        ArrayAdapter<String> createdGrpAdapter = new ArrayAdapter<String>(this, R.layout.created_groups_list_view, TotalCollectedData);
        listOfCreatedGroups.setAdapter(createdGrpAdapter);*/


        if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) {
            btn_AddNewUnit.setVisibility(View.GONE);
            btn_EditUnit.setVisibility(View.GONE);
        } else if (MultiPhotoSelectActivity.programID.equals("2")) {
            btn_AddNewGroup.setVisibility(View.GONE);
            btn_AddNewStd.setVisibility(View.GONE);
            btn_EditStd.setVisibility(View.GONE);
        } else {
            btn_AddNewUnit.setVisibility(View.GONE);
            btn_EditUnit.setVisibility(View.GONE);
        }

    }


    public void goToAddNewCrl(View view) {

        Intent i = new Intent(CrlAddEditScreen.this, AddNewCrl.class);
        startActivity(i);
    }

    public void goToStudentProfiles(View view) {
        Intent i = new Intent(CrlAddEditScreen.this, AddStudentProfiles.class);
        startActivity(i);
    }

    public void goToEditStudent(View view) {

        Intent i = new Intent(CrlAddEditScreen.this, EditStudent.class);
        startActivity(i);
    }

    public void goToAddNewGroup(View view) {

        Intent goToAddNewGrp = new Intent(CrlAddEditScreen.this, AddNewGroup.class);
        startActivity(goToAddNewGrp);
    }

    public void goToAddNewUnit(View view) {
        Intent i = new Intent(CrlAddEditScreen.this, AddNewUnit.class);
        this.finish();
        startActivity(i);
    }

    public void goToSelectUnitForEdit(View view) {
        Intent selectUnit = new Intent(CrlAddEditScreen.this, SelectUnitForEdit.class);
        this.finish();
        startActivity(selectUnit);
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

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlDashboard.class);
        this.finish();
        startActivity(intent);
    }*/

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

    public void goToSwapStudents(View view) {
        Intent goToSwapStudents = new Intent(CrlAddEditScreen.this, SwapStudents.class);
        startActivity(goToSwapStudents);
    }
}
