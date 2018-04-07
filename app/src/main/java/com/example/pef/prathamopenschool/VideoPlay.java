package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by PEF on 22/01/2016.
 */
public class VideoPlay extends Activity implements OnCompletionListener, OnPreparedListener {

    VideoView myVideoView;
    videoTracking vt;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        MainActivity.sessionFlg=false;
        sessionContex=this;
        playVideo = new PlayVideo();

        try{
            setContentView(R.layout.videoplay);
            myVideoView = (VideoView) findViewById(R.id.videoView1);
            JSInterface.MediaFlag = true;
            String groupId=getIntent().getStringExtra("path");
            Play(Uri.parse(groupId));
            myVideoView.setOnPreparedListener(this);
            myVideoView.setOnCompletionListener(this);

            if(JSInterface.VideoFlag == 1)
                myVideoView.setMediaController(null);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        catch (Exception e){
        }
    }

    public void Play(Uri path)
    {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(myVideoView);
        try {
            myVideoView.setVideoURI(path);
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs=new SyncActivityLogs(getApplicationContext());
            syncActivityLogs.addToDB("play-videoPlay",e, "Error");
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        myVideoView.setMediaController(mediaController);
        myVideoView.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("REMAINING TIME FOR VIDEO IS :"+MultiPhotoSelectActivity.duration);
        if(timer == true)
        {
            MultiPhotoSelectActivity.cd.cancel();
            myVideoView.start();
        }
        if(MultiPhotoSelectActivity.pauseFlg){
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg=false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Runtime rs = Runtime.getRuntime();
        rs.freeMemory();
        rs.gc();
        rs.freeMemory();
        this.finish();
        //JSInterface.MediaFlag=false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        myVideoView.start();
        MultiPhotoSelectActivity.duration =(long) myVideoView.getDuration();
    }

    @Override
    public void onBackPressed() {
        if(JSInterface.VideoFlag == 1){
            JSInterface.VideoFlag = 0;
            Runtime rs = Runtime.getRuntime();
            rs.freeMemory();
            rs.gc();
            rs.freeMemory();
            this.finish();
           // MainActivity.webView.goBack();
        }
        else{
            Runtime rs = Runtime.getRuntime();
            rs.freeMemory();
            rs.gc();
            rs.freeMemory();
            this.finish();
        }
    }

    @Override
    public void onPause() {
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