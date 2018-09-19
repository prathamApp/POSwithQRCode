package com.example.pef.prathamopenschool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class WebViewService extends Service {
    PlayVideo playVideo;
    ScoreDBHelper scoreDBHelper;
    SessionDBHelper sessionDBHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            if (MultiPhotoSelectActivity.sessionId.equalsIgnoreCase("NA")) {
                // no session created
            } else {
                sessionDBHelper = new SessionDBHelper(getApplicationContext());

                // check session already closed or not
                SessionDBHelper sessionDBHelper = new SessionDBHelper(MyApplication.getInstance());
                sessionDBHelper.UpdateEndTime(MultiPhotoSelectActivity.sessionId);

                MainActivity.sessionFlg = true;
                playVideo = new PlayVideo();
                scoreDBHelper = new ScoreDBHelper(getApplicationContext());
                playVideo.calculateEndTime(scoreDBHelper);
                BackupDatabase.backup(getApplicationContext());
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}