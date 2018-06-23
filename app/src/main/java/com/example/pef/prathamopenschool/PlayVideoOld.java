
package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.Dialog;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

// VIDEO QUESTIONS ON PAUSE ISSUE

public class PlayVideoOld extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

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
    private String nList;
    String selectedOption = "";
    int selectedBtn;
    private int position = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        scoreDBHelper = new ScoreDBHelper(getApplicationContext());
        MainActivity.sessionFlg = false;
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;

        nList = getIntent().getStringExtra("nodeList");

        // Generate Unique Device ID
        deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        setContentView(R.layout.activity_play_video);

        myVideoView = (VideoView) findViewById(R.id.videoView1);
        String groupId = getIntent().getStringExtra("path");
        myVideoView.setOnPreparedListener(this);
        myVideoView.setKeepScreenOn(true);
        myVideoView.setOnCompletionListener(this);
        util = new Utility();
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
        // dont show media controller if video questions are available
        if (nList.equalsIgnoreCase("null")) {
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
        } else {
            myVideoView.setVideoURI(path);
        }
    }

    //************************************************************//
    public void calculateEndTime(ScoreDBHelper scoreDBHelper) {

        Utility util = new Utility();
        String res_id = CardAdapter.resId;
        Log.d("destroyed", "-------------- CalculateEndTime -------------- in Destroy");
        int vidDuration = 0;
        if (MainActivity.sessionFlg) {
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
            score.GroupID = gid;
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
        position = myVideoView.getCurrentPosition(); //stopPosition is an int
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
        //if we have a position on savedInstanceState, the video playback should start from here
        myVideoView.seekTo(position);
        if (position == 0) {
            myVideoView.start();
        } else {//if we come from a resumed activity, video playback will be paused
            position = myVideoView.getCurrentPosition(); //stopPosition is an int
            myVideoView.pause();
        }
        MainActivity.sessionFlg = false;
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        System.out.println("REMAINING TIME FOR VIDEO IS :" + duration);
        if (timer) {
            cd.cancel();
            myVideoView.resume();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //we use onSaveInstanceState in order to store the video playback position for orientation change
        savedInstanceState.putInt("Position", myVideoView.getCurrentPosition());
        myVideoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //we use onRestoreInstanceState in order to play the video playback from the stored position
        position = savedInstanceState.getInt("Position");
        myVideoView.seekTo(position);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        if (myVideoView.isPlaying())
//            myVideoView.resume();
//        else
//            myVideoView.start();
        //if we have a position on savedInstanceState, the video playback should start from here
        myVideoView.seekTo(position);
        if (position == 0) {
            myVideoView.start();
        } else {//if we come from a resumed activity, video playback will be paused
            position = myVideoView.getCurrentPosition(); //stopPosition is an int
            myVideoView.pause();
        }
        duration = myVideoView.getDuration();
        if (!nList.equalsIgnoreCase("null")) {
            try {
                parseVideoQuestion();
                initializeQuestion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeQuestion() {
        if (count < videoQuestions.size()) {
            String nodeTime = videoQuestions.get(count).nodeTime;
            //Toast.makeText(this, "" + nodeTime, Toast.LENGTH_SHORT).show();
            //String time = "07:02"; // time format
            long min = Integer.parseInt(nodeTime.substring(0, 2));
            long sec = Integer.parseInt(nodeTime.substring(3));
            long t = (min * 60L) + sec;
            long time = TimeUnit.SECONDS.toMillis(t);//convert to millisecond
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (myVideoView.isPlaying()) {
                        position = myVideoView.getCurrentPosition(); //stopPosition is an int
                        myVideoView.pause();
                    }
                    try {
                        showQuestion();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, time);
        }
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


    private void showQuestion() {
        // MediaPlayer Memory Allocation
        final MediaPlayer correct = MediaPlayer.create(PlayVideoOld.this, R.raw.correct);
        final MediaPlayer wrong = MediaPlayer.create(PlayVideoOld.this, R.raw.wrong);

        Dialog dialog = new Dialog(PlayVideoOld.this);
        final Dialog resultDialog = new Dialog(PlayVideoOld.this);

        resultDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        resultDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        resultDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        resultDialog.setContentView(getLayoutInflater().inflate(R.layout.video_question_dialog, null));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.video_question_dialog);

        dialog.show();

        TextView tv_question = (TextView) dialog.findViewById(R.id.tv_question);
        Button opt1 = (Button) dialog.findViewById(R.id.opt1);
        Button opt2 = (Button) dialog.findViewById(R.id.opt2);
        Button opt3 = (Button) dialog.findViewById(R.id.opt3);
        Button opt4 = (Button) dialog.findViewById(R.id.opt4);
        ImageButton btn_submit = (ImageButton) dialog.findViewById(R.id.btn_submit);
        ImageButton btn_skip = (ImageButton) dialog.findViewById(R.id.btn_skip);

        tv_question.setText(videoQuestions.get(count).question.trim());
        opt1.setText(videoQuestions.get(count).Option1.trim());
        opt2.setText(videoQuestions.get(count).Option2.trim());
        opt3.setText(videoQuestions.get(count).Option3.trim());
        opt4.setText(videoQuestions.get(count).Option4.trim());

        String QueID = videoQuestions.get(count).QueId;
        String Answer = videoQuestions.get(count).Answer.trim();
        String resourceId = videoQuestions.get(count).resourceId;

        btn_submit.setEnabled(false);
        btn_submit.setClickable(false);


        opt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_submit.setEnabled(true);
                btn_submit.setClickable(true);
                opt1.setBackgroundResource(R.drawable.ans_box_left_selected);
                opt2.setBackgroundResource(R.drawable.ans_box_right);
                opt3.setBackgroundResource(R.drawable.ans_box_left);
                opt4.setBackgroundResource(R.drawable.ans_box_right);
                selectedOption = opt1.getText().toString().trim();
                selectedBtn = R.id.opt1;
            }
        });
        opt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_submit.setEnabled(true);
                btn_submit.setClickable(true);
                opt1.setBackgroundResource(R.drawable.ans_box_left);
                opt2.setBackgroundResource(R.drawable.ans_box_right_selected);
                opt3.setBackgroundResource(R.drawable.ans_box_left);
                opt4.setBackgroundResource(R.drawable.ans_box_right);
                selectedOption = opt2.getText().toString().trim();
                selectedBtn = R.id.opt2;
            }
        });
        opt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_submit.setEnabled(true);
                btn_submit.setClickable(true);
                opt1.setBackgroundResource(R.drawable.ans_box_left);
                opt2.setBackgroundResource(R.drawable.ans_box_right);
                opt3.setBackgroundResource(R.drawable.ans_box_left_selected);
                opt4.setBackgroundResource(R.drawable.ans_box_right);
                selectedOption = opt3.getText().toString().trim();
                selectedBtn = R.id.opt3;
            }
        });
        opt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_submit.setEnabled(true);
                btn_submit.setClickable(true);
                opt1.setBackgroundResource(R.drawable.ans_box_left);
                opt2.setBackgroundResource(R.drawable.ans_box_right);
                opt3.setBackgroundResource(R.drawable.ans_box_left);
                opt4.setBackgroundResource(R.drawable.ans_box_right_selected);
                selectedOption = opt4.getText().toString().trim();
                selectedBtn = R.id.opt4;
            }
        });


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // video watched or not then skip
                ScoreDBHelper score = new ScoreDBHelper(PlayVideoOld.this);
                Score sc = new Score();
                // Disable buttons after selection
                btn_submit.setEnabled(false);
                btn_skip.setEnabled(false);
                opt1.setEnabled(false);
                opt2.setEnabled(false);
                opt3.setEnabled(false);
                opt4.setEnabled(false);
                btn_submit.setClickable(false);
                btn_skip.setClickable(false);
                opt1.setClickable(false);
                opt2.setClickable(false);
                opt3.setClickable(false);
                opt4.setClickable(false);

                boolean answer = false;

                // enter score
                sc.SessionID = MultiPhotoSelectActivity.sessionId;
                sc.ResourceID = resourceId;
                sc.QuestionId = Integer.parseInt(QueID);
                sc.TotalMarks = 10;
                sc.Level = 55;
                sc.StartTime = new Utility().GetCurrentDateTime(false);
                sc.EndTime = new Utility().GetCurrentDateTime(false);
                String gid = MultiPhotoSelectActivity.selectedGroupsScore;
                if (gid.contains(","))
                    gid = gid.split(",")[0];
                sc.GroupID = gid;
                String deviceId = Settings.Secure.getString(PlayVideoOld.this.getContentResolver(), Settings.Secure.ANDROID_ID);
                sc.DeviceID = deviceId.equals(null) ? "0000" : deviceId;

                // get selected textview
                if (selectedOption.equals(Answer.trim())) {
                    // Correct Animation
                    Button selBut = dialog.findViewById(selectedBtn);
                    selBut.setBackgroundResource(R.drawable.ans_box_correct);

                    // CORRECT ANSWER
                    LinearLayout mainScreen = resultDialog.findViewById(R.id.video_questions);
                    LinearLayout correctScreen = resultDialog.findViewById(R.id.vq_correct);
                    LinearLayout wrongScreen = resultDialog.findViewById(R.id.vq_wrong);
                    mainScreen.setVisibility(View.GONE);
                    correctScreen.setVisibility(View.VISIBLE);
                    wrongScreen.setVisibility(View.GONE);
                    resultDialog.show();
                    correct.start();
                    sc.ScoredMarks = 10;

                } else if (answer == false) {

                    // setting background red if answer is wrong
                    Button selBut = dialog.findViewById(selectedBtn);
                    selBut.setBackgroundResource(R.drawable.ans_box_wrong);
                    // Setting Correct Answer background
                    if (opt1.getText().toString().equals(Answer.trim()))
                        opt1.setBackgroundResource(R.drawable.ans_box_correct);
                    else if (opt2.getText().toString().equals(Answer.trim()))
                        opt2.setBackgroundResource(R.drawable.ans_box_correct);
                    else if (opt3.getText().toString().equals(Answer.trim()))
                        opt3.setBackgroundResource(R.drawable.ans_box_correct);
                    else if (opt4.getText().toString().equals(Answer.trim()))
                        opt4.setBackgroundResource(R.drawable.ans_box_correct);

                    // if WRONG ANS
                    LinearLayout mainScreen = resultDialog.findViewById(R.id.video_questions);
                    LinearLayout correctScreen = resultDialog.findViewById(R.id.vq_correct);
                    LinearLayout wrongScreen = resultDialog.findViewById(R.id.vq_wrong);
                    TextView tvWrong = resultDialog.findViewById(R.id.tv_wrong_ans);
                    tvWrong.setText("Correct Answer is " + Answer.trim() + " !!!");
                    mainScreen.setVisibility(View.GONE);
                    correctScreen.setVisibility(View.GONE);
                    wrongScreen.setVisibility(View.VISIBLE);
                    resultDialog.show();
                    wrong.start();
                    sc.ScoredMarks = 0;
                }

                // Score Entry
                score.Add(sc);
                BackupDatabase.backup(PlayVideoOld.this);

                try {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 2500ms
                            if (correct.isPlaying()) {
                                correct.stop();
                                correct.reset();
                                correct.release();
                            } else if (wrong.isPlaying()) {
                                wrong.stop();
                                wrong.reset();
                                wrong.release();
                            }

                            if (dialog.isShowing())
                                dialog.dismiss();
                            if (resultDialog.isShowing())
                                resultDialog.dismiss();
                            // resume video
                            count += 1;
                            initializeQuestion();
                            myVideoView.resume();
                        }
                    }, 3000);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dialog.isShowing())
                    dialog.dismiss();
                if (resultDialog.isShowing())
                    resultDialog.dismiss();
                // Disable buttons after selection
                btn_submit.setEnabled(false);
                btn_skip.setEnabled(false);
                opt1.setEnabled(false);
                opt2.setEnabled(false);
                opt3.setEnabled(false);
                opt4.setEnabled(false);
                btn_submit.setClickable(false);
                btn_skip.setClickable(false);
                opt1.setClickable(false);
                opt2.setClickable(false);
                opt3.setClickable(false);
                opt4.setClickable(false);

//                String nodeTime = videoQuestions.get(count).nodeTime;
//                //String time = "07:02"; // time format
//                long min = Integer.parseInt(nodeTime.substring(0, 2));
//                long sec = Integer.parseInt(nodeTime.substring(3));
//                long t = (min * 60L) + sec;
//                long time = TimeUnit.SECONDS.toMillis(t);//convert to millisecond
//                new Video_Question(time).execute();

                // resume video
                count += 1;
                initializeQuestion();
                myVideoView.resume();

                // enter score
                ScoreDBHelper score = new ScoreDBHelper(PlayVideoOld.this);
                Score sc = new Score();
                boolean answer = false;
                sc.SessionID = MultiPhotoSelectActivity.sessionId;
                sc.ResourceID = resourceId;
                sc.QuestionId = Integer.parseInt(QueID);
                sc.TotalMarks = 10;
                sc.Level = 55;
                sc.StartTime = new Utility().GetCurrentDateTime(false);
                sc.EndTime = new Utility().GetCurrentDateTime(false);
                String gid = MultiPhotoSelectActivity.selectedGroupsScore;
                if (gid.contains(","))
                    gid = gid.split(",")[0];
                sc.GroupID = gid;
                String deviceId = Settings.Secure.getString(PlayVideoOld.this.getContentResolver(), Settings.Secure.ANDROID_ID);
                sc.DeviceID = deviceId.equals(null) ? "0000" : deviceId;
                sc.ScoredMarks = 0;

                score.Add(sc);
                BackupDatabase.backup(PlayVideoOld.this);
            }
        });
    }

}

