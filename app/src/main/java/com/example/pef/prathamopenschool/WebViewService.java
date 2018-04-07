package com.example.pef.prathamopenschool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class WebViewService extends Service {
    PlayVideo playVideo;
    ScoreDBHelper scoreDBHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            MainActivity.sessionFlg = true;
            playVideo = new PlayVideo();
            scoreDBHelper = new ScoreDBHelper(getApplicationContext());
            playVideo.calculateEndTime(scoreDBHelper);
            BackupDatabase.backup(getApplicationContext());
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}