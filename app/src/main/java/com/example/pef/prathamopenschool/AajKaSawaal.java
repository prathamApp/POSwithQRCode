package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Random;

// NOT USING ANYMORE

public class AajKaSawaal extends AppCompatActivity {

//    String newNodeList;
//    TextView tv_Que, tv_result;
//    RadioGroup rg_Opt;
//    RadioButton rb_Opt1, rb_Opt2, rb_Opt3, rb_Opt4, selectedOption;
//    Button btn_Submit, btn_Skip;
//    boolean timer;
//    Context sessionContex;
//    ScoreDBHelper scoreDBHelper;
//    PlayVideo playVideo;
//    String StartTime = "";
//    Utility Util;
//    AttendanceDBHelper attendanceDBHelper;
//    String gid;
//    Context context;
//
//    String QueId, Question, QuestionType, Subject, Option1, Option2, Option3, Option4, Answer, resourceId, resourceName, resourceType, resourcePath, programLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aaj_ka_sawaal);

        // Hide Actionbar
        getSupportActionBar().hide();
//        Util = new Utility();
//        attendanceDBHelper = new AttendanceDBHelper(AajKaSawaal.this);
//
//        StartTime = Util.GetCurrentDateTime();
//        newNodeList = getIntent().getStringExtra("nodeList");
//
//        gid = MultiPhotoSelectActivity.selectedGroupId;
//        if(gid.contains(","))
//            gid = gid.split(",")[0];
//
//
//        // Memory Allocation
//        tv_Que = findViewById(R.id.tv_question);
//        tv_result = findViewById(R.id.tv_result);
//
//        rg_Opt = findViewById(R.id.rg_options);
//
//        rb_Opt1 = findViewById(R.id.rb_O1);
//        rb_Opt2 = findViewById(R.id.rb_O2);
//        rb_Opt3 = findViewById(R.id.rb_O3);
//        rb_Opt4 = findViewById(R.id.rb_O4);
//
//        btn_Submit = findViewById(R.id.btn_submit);
//        btn_Skip = findViewById(R.id.btn_skip);
//
//        MainActivity.sessionFlg = false;
//        sessionContex = this;
//        playVideo = new PlayVideo();
//
//
//        // Set the Question & Options from json
//        try {
//            // Load Json in Array
//            JSONArray queJsonArray = new JSONArray(loadQueJSONFromAsset());
//
//            // Generate Random Que No
//            Random r = new Random();
//            int Low = 0;
//            int High = queJsonArray.length();
//            int randomQuestion = r.nextInt(High - Low) + Low;
//
//            // Get Question Details
//            JSONObject qObj = queJsonArray.getJSONObject(randomQuestion);
//            QueId = qObj.getString("QueId");
//            Question = qObj.getString("Question");
//            QuestionType = qObj.getString("QuestionType");
//            Subject = qObj.getString("Subject");
//            Option1 = qObj.getString("Option1");
//            Option2 = qObj.getString("Option2");
//            Option3 = qObj.getString("Option3");
//            Option4 = qObj.getString("Option4");
//            Answer = qObj.getString("Answer");
//            resourceId = qObj.getString("resourceId");
//            resourceName = qObj.getString("resourceName");
//            resourceType = qObj.getString("resourceType");
//            resourcePath = qObj.getString("resourcePath");
//            programLanguage = qObj.getString("programLanguage");
//
//            // Set Question
//            tv_Que.setText(Question);
//            rb_Opt1.setText(Option1);
//            rb_Opt2.setText(Option2);
//            rb_Opt3.setText(Option3);
//            rb_Opt4.setText(Option4);
//
//
//        } catch (Exception e) {
//            e.getMessage();
//        }
//
//        // Skip Button Action
//        btn_Skip.setOnClickListener(new View.OnClickListener()
//
//        {
//            @Override
//            public void onClick(View v) {
//                Intent main = new Intent(AajKaSawaal.this, MainActivity.class);
//                if (assessmentLogin.assessmentFlg)
//                    main.putExtra("nodeList", newNodeList.toString());
//                startActivity(main);
//                finish();
//            }
//        });
//
//
//        // Submit Button Action
//        btn_Submit.setOnClickListener(new View.OnClickListener()
//
//        {
//            @Override
//            public void onClick(View v) {
//
//                // Flag Played if Submit
//                StatusDBHelper sdbh = new StatusDBHelper(AajKaSawaal.this);
//                sdbh.UpdateAlarm("aajKaSawalPlayed", "1");
//                BackupDatabase.backup(AajKaSawaal.this);
//
//                // Score Table Entry
//                boolean enterScore = false;
//                ScoreDBHelper score = new ScoreDBHelper(AajKaSawaal.this);
//                Score sc = new Score();
//
//                // Disable buttons after selection
//                btn_Submit.setClickable(false);
//                btn_Skip.setClickable(false);
//                rb_Opt1.setClickable(false);
//                rb_Opt2.setClickable(false);
//                rb_Opt3.setClickable(false);
//                rb_Opt4.setClickable(false);
//
//                // get selected radio button from radioGroup
//                int selectedId = rg_Opt.getCheckedRadioButtonId();
//                // find the radio button by returned id
//                selectedOption = (RadioButton) findViewById(selectedId);
//                String option = selectedOption.getText().toString();
//
//                boolean answer = false;
//                sc.SessionID = MultiPhotoSelectActivity.sessionId;
//                sc.ResourceID = resourceId;
//                sc.QuestionId = Integer.parseInt(QueId);
//                sc.TotalMarks = 10;
//                sc.Level = 0;
//                sc.StartTime = StartTime;
//                sc.EndTime = Util.GetCurrentDateTime();
//                sc.GroupID = gid;
//                String deviceId = Settings.Secure.getString(AajKaSawaal.this.getContentResolver(), Settings.Secure.ANDROID_ID);
//                sc.DeviceID = deviceId.equals(null) ? "0000" : deviceId;
//                if (option.equals(Answer)) {
//                    tv_result.setText("Correct Answer !!!");
//                    tv_result.setTextColor(Color.GREEN);
//                    sc.ScoredMarks = 10;
//                } else if (answer == false) {
//                    tv_result.setText("Wrong Answer !!!\nCorrect Answer is " + Answer + " !!!");
//                    tv_result.setTextColor(Color.RED);
//                    sc.ScoredMarks = 0;
//                }
//                enterScore = score.Add(sc);
//                BackupDatabase.backup(context);
//
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Do something after 3000ms
//
//                        // this should run after 3 seconds
//                        Intent main = new Intent(AajKaSawaal.this, MainActivity.class);
//                        if (assessmentLogin.assessmentFlg)
//                            main.putExtra("nodeList", newNodeList.toString());
//                        startActivity(main);
//                        finish();
//                    }
//                }, 3500);
//
//            }
//        });

    } // OnCreate


//    // Reading CRL Json From Internal Memory
//    public String loadQueJSONFromAsset() {
//        String queJsonStr = null;
//
//        try {
//            File queJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Questions.json");
//            FileInputStream stream = new FileInputStream(queJsonSDCard);
//            try {
//                FileChannel fc = stream.getChannel();
//                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//                queJsonStr = Charset.defaultCharset().decode(bb).toString();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                stream.close();
//            }
//
//        } catch (Exception e) {
//        }
//
//        return queJsonStr;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        MultiPhotoSelectActivity.pauseFlg = true;
//
//        MultiPhotoSelectActivity.cd = new CountDownTimer(MultiPhotoSelectActivity.duration, 1000) {
//            //cd = new CountDownTimer(duration, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                MultiPhotoSelectActivity.duration = millisUntilFinished;
//                timer = true;
//            }
//
//            @Override
//            public void onFinish() {
//                timer = false;
//                MainActivity.sessionFlg = true;
//
//                if (!CardAdapter.vidFlg) {
//                    scoreDBHelper = new ScoreDBHelper(sessionContex);
//                    playVideo.calculateEndTime(scoreDBHelper);
//                    BackupDatabase.backup(sessionContex);
//                    try {
//                        finishAffinity();
//                    } catch (Exception e) {
//                        e.getMessage();
//                    }
//                }
//            }
//        }.start();
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (MultiPhotoSelectActivity.pauseFlg) {
//            MultiPhotoSelectActivity.cd.cancel();
//            MultiPhotoSelectActivity.pauseFlg = false;
//            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
//        }
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        MainActivity.sessionFlg = true;
//        super.onDestroy();
//    }

}
