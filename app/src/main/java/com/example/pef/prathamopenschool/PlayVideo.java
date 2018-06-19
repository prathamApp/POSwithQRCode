
package com.example.pef.prathamopenschool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.pef.prathamopenschool.gps.EventBusMSG;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    List<Modal_VideoQuestion> videoQuestions;
    int count = 0;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        playVideo(Uri.parse(groupId));
    }

    public void parseVideoQuestion() {
        try {
            String questions = getIntent().getStringExtra("nodeList");
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Modal_VideoQuestion>>() {
            }.getType();
            videoQuestions = gson.fromJson(questions, listType);
        } catch (Exception e) {
            e.printStackTrace();
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
        long time = TimeUnit.SECONDS.toMillis(Long.parseLong(videoQuestions.get(count).getNodeTime())); //convert to millisecond
        new Video_Question(time).execute();
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

    public class Video_Question extends AsyncTask<Void, Void, Boolean> {
        long time;

        public Video_Question(long time) {
            this.time = time;
            Log.d("time::", time + "");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (!myVideoView.isPlaying()) {
                myVideoView.start();
            }
            do {
                Log.d("onEvent::", myVideoView.getCurrentPosition() + "");
                if (count < videoQuestions.size()) {
                    if (myVideoView.getCurrentPosition() == time) {
                        if (myVideoView.isPlaying()) {
                            myVideoView.pause();
                            return true;
                        }
                    }
                }
            } while (myVideoView.getCurrentPosition() <= myVideoView.getDuration());
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                count += 1;
                showQuestion();
            }
        }
    }

    private void showQuestion() {
        Dialog dialog = new Dialog(PlayVideo.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.video_question_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        TextView tv_question = (TextView) dialog.findViewById(R.id.tv_question);
        Button opt1 = (Button) dialog.findViewById(R.id.opt1);
        Button opt2 = (Button) dialog.findViewById(R.id.opt2);
        Button opt3 = (Button) dialog.findViewById(R.id.opt3);
        Button opt4 = (Button) dialog.findViewById(R.id.opt4);
        ImageButton btn_submit = (ImageButton) dialog.findViewById(R.id.btn_submit);
        ImageButton btn_skip = (ImageButton) dialog.findViewById(R.id.btn_skip);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time = TimeUnit.SECONDS.toMillis(Long.parseLong(videoQuestions.get(count).getNodeTime())); //convert to millisecond
                new Video_Question(time).execute();
            }
        });
    }

}

