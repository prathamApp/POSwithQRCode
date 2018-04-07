package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class assessmentLogin extends AppCompatActivity {

    String newNodeList;
    static String crlID;
    EditText et_UserName, et_Pass;
    Button btn_Login, btn_CancleLogin;
    Context context;
    static Context c;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    static boolean assessmentFlg = false, assessmentFlgTable = false;
    Context sessionContex;
    boolean timer, demoPressed = false;

    private static int SPLASH_TIME_OUT = 1500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display Assessment Splash Screen
        setContentView(R.layout.assessment_splash);

        context = this;
        c = this;
        sessionContex = this;
        assessmentFlg = false;


        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        CardAdapter.vidFlg = false;

        newNodeList = getIntent().getStringExtra("nodeList");

        //Opening SQLite Pipeline
        final CrlDBHelper db = new CrlDBHelper(context);


        // Switch to Assessment Login Screen
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                setContentView(R.layout.activity_assessment_login);


                et_UserName = (EditText) findViewById(R.id.et_UName);
                et_Pass = (EditText) findViewById(R.id.et_Password);
                btn_Login = (Button) findViewById(R.id.btn_Login);
                btn_CancleLogin = (Button) findViewById(R.id.btn_CancleLogin);

                playVideo = new PlayVideo();


                btn_Login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String enteredUserName = et_UserName.getText().toString();
                        String enteredPassWord = et_Pass.getText().toString();

                        boolean result;
                        result = db.CrlLogin(enteredUserName, enteredPassWord);


                        if (result && !assessmentFlg) {

                            crlID = "CRLID-" + db.getCrlID(enteredUserName, enteredPassWord);
                            playVideo = new PlayVideo();

                            assessmentFlgTable = true;
                            assessmentFlg = true;
                            scoreDBHelper = new ScoreDBHelper(context);
                            playVideo.calculateEndTime(scoreDBHelper);
                            BackupDatabase.backup(context);

                            assessmentFlg = true;
                            Intent mainNew = new Intent(context, MultiPhotoSelectActivity.class);
                            mainNew.putExtra("nodeList", newNodeList.toString());
                            ((Activity) context).startActivityForResult(mainNew, 1);

                            et_UserName.setText("");
                            et_Pass.setText("");
                        }
                        else if (result && assessmentFlg) {

                            crlID = "CRLID-" + db.getCrlID(enteredUserName, enteredPassWord);
                            playVideo = new PlayVideo();

                            assessmentFlgTable = true;
                            assessmentFlg = true;
                            scoreDBHelper = new ScoreDBHelper(context);
                            playVideo.calculateEndTime(scoreDBHelper);
                            BackupDatabase.backup(context);

                            assessmentFlg = false;
                            onBackPressed();

                        } else {

                            Toast.makeText(context, "Invalid Credentials !!!", Toast.LENGTH_SHORT).show();
                            et_UserName.setText("");
                            et_Pass.setText("");

                        }

                    }
                });


                btn_CancleLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!assessmentFlg) {
                            crlID = "Practice";
                            playVideo = new PlayVideo();

                            assessmentFlgTable = true;
                            assessmentFlg = true;
                            scoreDBHelper = new ScoreDBHelper(context);
                            playVideo.calculateEndTime(scoreDBHelper);
                            BackupDatabase.backup(context);
                            assessmentFlg = true;

                            Intent mainNew = new Intent(context, MainActivity.class);
                            mainNew.putExtra("nodeList", newNodeList.toString());
                            ((Activity) context).startActivityForResult(mainNew, 1);

                            et_UserName.setText("");
                            et_Pass.setText("");

                        } else {

                            demoPressed = true;
                            crlID = "Practice";
                            playVideo = new PlayVideo();

                            assessmentFlgTable = true;
                            assessmentFlg = true;
                            scoreDBHelper = new ScoreDBHelper(context);
                            playVideo.calculateEndTime(scoreDBHelper);
                            BackupDatabase.backup(context);
                            assessmentFlg = false;

                            onBackPressed();
                            et_UserName.setText("");
                            et_Pass.setText("");

                        }


                    }
                });

                //Assessment Report Code of button click

            }
        }, SPLASH_TIME_OUT);


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
    public void onBackPressed() {
        super.onBackPressed();

        if (!demoPressed && assessmentFlg) {
            crlID = "Practice";
            playVideo = new PlayVideo();
            assessmentFlgTable = true;
            assessmentFlg = true;
            scoreDBHelper = new ScoreDBHelper(context);
            playVideo.calculateEndTime(scoreDBHelper);
            BackupDatabase.backup(context);
            assessmentFlg = false;
            CardAdapter.newAssessmentFlg = false;
            finish();
        }
        else {
            assessmentFlg = false;
            CardAdapter.newAssessmentFlg = false;
            finish();
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