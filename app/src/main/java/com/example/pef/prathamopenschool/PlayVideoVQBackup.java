
package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlayVideoVQBackup extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    CountDownTimer cd;

    VideoView myVideoView;
    boolean timer;
    long duration;
    StatusDBHelper statusDBHelper;
    ScoreDBHelper scoreDBHelper;
    Utility util, Util;
    String deviceID = "";
    String videoStartTime;

    String nodeList = "";
    TextView tv_Que;
    Button tv_opt1, tv_opt2, tv_opt3, tv_opt4;
    ImageButton btn_Submit, btn_Skip, btn_videoHint;
    String qvStartTime;
    String selectedOption = "";
    String QueId, Question, QuestionType, Subject, Option1, Option2, Option3, Option4, Answer, difficultyLevel, resourceName, resourceId, resourcePath, nodeTime, nodeTitle, nodeType, nodeId;
    private List<VideoQuestion> vqList;
    int video_duration = 0;
    int video_current = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        scoreDBHelper = new ScoreDBHelper(getApplicationContext());
        MainActivity.sessionFlg = false;
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        // Generate Unique Device ID
        deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_play_video);

        myVideoView = (VideoView) findViewById(R.id.videoView1);
        String groupId = getIntent().getStringExtra("path");
        myVideoView.setOnPreparedListener(this);
        myVideoView.setOnCompletionListener(this);
        util = new Utility();
        Util = new Utility();

//        videoStartTime = getIntent().getStringExtra("startTime");
        videoStartTime = util.GetCurrentDateTime(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get NodeList
        Intent i = getIntent();
        nodeList = i.getStringExtra("nodeList");
        vqList = new ArrayList<>();


        // Play Video
        playVideo(Uri.parse(groupId));

        if (!nodeList.equalsIgnoreCase("null")) {
            try {

                try {
                    //question string
                    JSONArray queJsonArray = new JSONArray(nodeList);
                    for (int z = 0; z < queJsonArray.length(); z++) {
                        JSONObject q = queJsonArray.getJSONObject(z);

                        VideoQuestion vq = new VideoQuestion();
                        vq.nodeId = q.optString("nodeId");
                        vq.nodeType = q.optString("nodeType");
                        vq.nodeTitle = q.optString("nodeTitle");
                        vq.nodeTime = q.optString("nodeTime");
                        vq.QueId = q.optString("QueId");
                        vq.Question = q.optString("Question");
                        vq.QuestionType = q.optString("QuestionType");
                        vq.Option1 = q.optString("Option1");
                        vq.Option2 = q.optString("Option2");
                        vq.Option3 = q.optString("Option3");
                        vq.Option4 = q.optString("Option4");
                        vq.Answer = q.optString("Answer");
                        vq.difficultyLevel = q.optString("difficultyLevel");
                        vq.resourceName = q.optString("resourceName");
                        vq.resourceType = q.optString("resourceType");
                        vq.resourceId = q.optString("resourceId");
                        vq.resourcePath = q.optString("resourcePath");
                        vq.programLanguage = q.optString("programLanguage");
                        vq.nodelist = q.optString("nodelist");

                        vqList.add(vq);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
/*

                // RUN BELOW FOR EVERY QUESTION
                // have questions data
                // video dialog
                final MediaPlayer correct = MediaPlayer.create(PlayVideo.this, R.raw.correct);
                final MediaPlayer wrong = MediaPlayer.create(PlayVideo.this, R.raw.wrong);
                // Dialog Memory Allocation
                final Dialog resultDialog = new Dialog(PlayVideo.this);
                final Dialog dialog = new Dialog(PlayVideo.this);
                resultDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                resultDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                resultDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.video_question_dialog);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                // Layout Memory Allocation
                LinearLayout mainScreen = dialog.findViewById(R.id.video_questions);
                LinearLayout correctScreen = dialog.findViewById(R.id.vq_correct);
                LinearLayout wrongScreen = dialog.findViewById(R.id.vq_wrong);
                // Layout Appearance
                mainScreen.setVisibility(View.VISIBLE);
                correctScreen.setVisibility(View.GONE);
                wrongScreen.setVisibility(View.GONE);
                // Memory Allocation
                tv_Que = dialog.findViewById(R.id.tv_question);
                btn_Submit = dialog.findViewById(R.id.btn_submit);
                btn_Skip = dialog.findViewById(R.id.btn_skip);
                tv_opt1 = dialog.findViewById(R.id.opt1);
                tv_opt2 = dialog.findViewById(R.id.opt2);
                tv_opt3 = dialog.findViewById(R.id.opt3);
                tv_opt4 = dialog.findViewById(R.id.opt4);
                btn_Submit.setEnabled(false);
                btn_Submit.setClickable(false);
                // Setting Dialog
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //show dialog according to question appearance
                qvStartTime = Util.GetCurrentDateTime(false);
//                dialog.show();

*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void playVideo(Uri path) {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(myVideoView);
        try {
            myVideoView.setVideoURI(path);
        } catch (Exception e) {
            Log.e("Cant Play Video", e.getMessage());
            e.printStackTrace();
        }
        myVideoView.setMediaController(mediaController);
        myVideoView.requestFocus();
        new Handler().postDelayed(updateUI,1);
    }

    //************************************************************//
    public void calculateEndTime(ScoreDBHelper scoreDBHelper) {

        Utility util = new Utility();
        String res_id = CardAdapter.resId;
        Log.d("destroyed", "-------------- CalculateEndTime -------------- in Destroy");
        int vidDuration = 0;
        if (MainActivity.sessionFlg) {
//            videoStartTime = SignInActivity.sessionStartTime;
            videoStartTime = util.GetCurrentDateTime(false);
            ;
            vidDuration = 0;
            res_id = "SessionTracking";
        }
        if (CardAdapter.vidFlg) {
            vidDuration = myVideoView.getDuration();
            res_id = CardAdapter.resId;
        } else if (assessmentLogin.assessmentFlg) {
            videoStartTime = util.GetCurrentDateTime(false);
            res_id = "Assessment-" + assessmentLogin.crlID;
            assessmentLogin.assessmentFlg = false;
        }

        try {
            Boolean _wasSuccessful = null;
            String endTime = util.GetCurrentDateTime(false);

//            statusDBHelper = new StatusDBHelper(getApplicationContext());

            Score score = new Score();
            score.SessionID = MultiPhotoSelectActivity.sessionId;
            score.ResourceID = res_id;
            score.QuestionId = 0;
            score.ScoredMarks = vidDuration;
            score.TotalMarks = vidDuration;
            score.StartTime = videoStartTime;
            String gid = MultiPhotoSelectActivity.selectedGroupsScore;
            if (gid.contains(","))
                gid = gid.split(",")[0];
            score.GroupID = gid;//ketan 17/6/17
            String deviceId = MultiPhotoSelectActivity.deviceID;
            score.DeviceID = deviceId.equals(null) ? "0000" : deviceId;
            score.EndTime = endTime;
            score.Level = 0;
            _wasSuccessful = scoreDBHelper.Add(score);
            if (!_wasSuccessful) {

            }
            if (CardAdapter.vidFlg)
                BackupDatabase.backup(this);

            CardAdapter.vidFlg = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //************************************************************//

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause ::: ", "onPause Called !!!");
        myVideoView.pause();
        MainActivity.sessionFlg = true;

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
                if (CardAdapter.vidFlg) {
                    MainActivity.sessionFlg = false;
                    calculateEndTime(scoreDBHelper);
                    MainActivity.sessionFlg = true;
                }
                if (MainActivity.sessionFlg) {
                    calculateEndTime(scoreDBHelper);
                    MainActivity.sessionFlg = false;
                    CardAdapter.vidFlg = false;
                }
                BackupDatabase.backup(getApplicationContext());
                try {
//                    System.exit(0);
                    finishAffinity();
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.sessionFlg = false;
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        System.out.println("REMAINING TIME FOR VIDEO IS :" + duration);
        if (timer == true) {
            cd.cancel();
            myVideoView.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        calculateEndTime(scoreDBHelper);
//        Runtime rs = Runtime.getRuntime();
//        rs.freeMemory();
//        rs.gc();
//        rs.freeMemory();
//        this.finish();
        onBackPressed();
        //JSInterface.MediaFlag=false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        myVideoView.start();
        duration = myVideoView.getDuration();
    }

    private final Runnable updateUI = new Runnable() {
        public void run() {
            try {
                //update ur ui here
                do {
                    Log.d("start::", (myVideoView.getCurrentPosition() / myVideoView.getDuration()) * 100 + "");
                    Log.d("end::", myVideoView.getDuration() - myVideoView.getCurrentPosition() + "");
                } while (myVideoView.getCurrentPosition() < myVideoView.getDuration());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        calculateEndTime(scoreDBHelper);
        Runtime rs = Runtime.getRuntime();
        rs.freeMemory();
        rs.gc();
        rs.freeMemory();
        this.finish();
    }
}

