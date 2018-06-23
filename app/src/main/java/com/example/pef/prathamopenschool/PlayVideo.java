
package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideo extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    CountDownTimer cd;

    VideoView myVideoView;
    boolean timer;
    long duration;
    StatusDBHelper statusDBHelper;
    ScoreDBHelper scoreDBHelper;
    Utility util;
    String deviceID = "";
    String videoStartTime;

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
//        videoStartTime = getIntent().getStringExtra("startTime");
        videoStartTime = util.GetCurrentDateTime(false);
        ;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        playVideo(Uri.parse(groupId));

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