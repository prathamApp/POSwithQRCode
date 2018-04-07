package com.example.pef.prathamopenschool;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

// Notification Service

public class AlarmService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    Context context;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // Cancelled Alarm

        /*Context context = this.getApplicationContext();

        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(this, splashScreenVideo.class);
        pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Pratham Open School");
        builder.setContentText("Just play. Have fun. Enjoy the game.");
        builder.setSubText("Aaj Ka Sawaal !");
        builder.setSmallIcon(R.mipmap.launcher_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.launcher_icon));
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        try {

            // reset Played Flag
            StatusDBHelper sdbh = new StatusDBHelper(context);
            sdbh.updateTrailerCountByKey(0, "group1");
            sdbh.updateTrailerCountByKey(0, "group2");
            sdbh.updateTrailerCountByKey(0, "group3");
            sdbh.updateTrailerCountByKey(0, "group4");
            sdbh.updateTrailerCountByKey(0, "group5");
            Log.d("AM Service :::", "AM Service Running");

            BackupDatabase.backup(context);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}