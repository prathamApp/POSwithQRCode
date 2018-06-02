package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SelectClassForUniversalChild extends AppCompatActivity {

    Button btn_Class1, btn_Class2, btn_Class3, btn_Class4, btn_Class5;
    TextView edt_UnitName, edt_VillageName, edt_SchoolName;
    String GroupID, GroupName, VillageName, SchoolName, villageID, State, Block;
    List<String> childIDData;
    static List<String> selectChildIDData;
    String From;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    static String ClickedClass = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_class_for_universal_child);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();


        // Hide Actionbar
        getSupportActionBar().hide();


        // Initial Info
        Intent getValues = getIntent();

        villageID = getValues.getStringExtra("VillageID");
        State = getValues.getStringExtra("State");
        Block = getValues.getStringExtra("Block");
        GroupID = getValues.getStringExtra("Group ID");
        GroupName = getValues.getStringExtra("UnitName");
        VillageName = getValues.getStringExtra("Village");
        SchoolName = getValues.getStringExtra("School");
        From = getValues.getStringExtra("From");


        // Memory Allocation for Fields
        edt_UnitName = (TextView) findViewById(R.id.edt_UnitName);
        edt_UnitName.setText(GroupName);
        edt_VillageName = (TextView) findViewById(R.id.edt_VillageName);
        edt_VillageName.setText(VillageName);
        edt_SchoolName = (TextView) findViewById(R.id.edt_SchoolName);
        edt_SchoolName.setText(SchoolName);

        btn_Class1 = (Button) findViewById(R.id.btn_Class1);
        btn_Class2 = (Button) findViewById(R.id.btn_Class2);
        btn_Class3 = (Button) findViewById(R.id.btn_Class3);
        btn_Class4 = (Button) findViewById(R.id.btn_Class4);
        btn_Class5 = (Button) findViewById(R.id.btn_Class5);


        btn_Class1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                childIDData = new ArrayList<String>();
                for (int i = 101; i <= 199; i++) {
                    childIDData.add(String.valueOf(i));
                }

                sendChildDataToUCMF(childIDData);
                ClickedClass = "1";
                goToUniversalChildList();


            }
        });


        btn_Class2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                childIDData = new ArrayList<String>();
                for (int i = 2001; i <= 2100; i++) {
                    childIDData.add(String.valueOf(i));
                }

                sendChildDataToUCMF(childIDData);
                ClickedClass = "2";
                goToUniversalChildList();


            }
        });

        btn_Class3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                childIDData = new ArrayList<String>();
                for (int i = 1; i <= 100; i++) {
                    childIDData.add(String.valueOf(i));
                }

                sendChildDataToUCMF(childIDData);
                ClickedClass = "3";
                goToUniversalChildList();


            }
        });

        btn_Class4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                childIDData = new ArrayList<String>();
                for (int i = 4001; i <= 4100; i++) {
                    childIDData.add(String.valueOf(i));
                }

                sendChildDataToUCMF(childIDData);
                ClickedClass = "4";
                goToUniversalChildList();


            }
        });


        btn_Class5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                childIDData = new ArrayList<String>();
                for (int i = 5001; i <= 5100; i++) {
                    childIDData.add(String.valueOf(i));
                }

                sendChildDataToUCMF(childIDData);
                ClickedClass = "5";
                goToUniversalChildList();


            }
        });


        // Hiding Buttons as they are not useful & We are going to merge them
        btn_Class2.setVisibility(View.GONE);
        btn_Class4.setVisibility(View.GONE);
        btn_Class5.setVisibility(View.GONE);


    }


    public void sendChildDataToUCMF(List<String> childIDData) {

        selectChildIDData = childIDData;

        /*Intent passData = new Intent(SelectClassForUniversalChild.this,UniversalChildMenuFragment.class);
        passData.putStringArrayListExtra("childData", (ArrayList<String>) childIDData);
        startActivity(passData);*/
    }

    private void goToUniversalChildList() {


        Intent goToUniversalChildList = new Intent(SelectClassForUniversalChild.this, UniversalChildList.class);

        goToUniversalChildList.putExtra("VillageID", villageID);
        goToUniversalChildList.putExtra("State", State);
        goToUniversalChildList.putExtra("Block", Block);
        goToUniversalChildList.putExtra("Group ID", GroupID);
        goToUniversalChildList.putExtra("UnitName", GroupName);
        goToUniversalChildList.putExtra("Village", VillageName);
        goToUniversalChildList.putExtra("School", SchoolName);
        goToUniversalChildList.putExtra("From", From);

        startActivity(goToUniversalChildList);

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
                    System.exit(0);
                    finishAffinity();

                }
            }
        }.start();

    }

/*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CrlAddEditScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
*/


}
