package com.example.pef.prathamopenschool.ftpSettings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.pef.prathamopenschool.MessageEvent;
import com.example.pef.prathamopenschool.MyApplication;
import com.example.pef.prathamopenschool.R;

import net.vrallev.android.cat.Cat;

import org.greenrobot.eventbus.EventBus;

import java.net.InetAddress;

public class FsNotification extends BroadcastReceiver {

    private final int NOTIFICATION_ID = 7891;

    @Override
    public void onReceive(Context context, Intent intent) {
        Cat.d("onReceive broadcast: " + intent.getAction());
        switch (intent.getAction()) {
            case FsService.ACTION_STARTED:
                setupNotification(context);
                break;
            case FsService.ACTION_STOPPED:
                clearNotification(context);
                break;
        }
    }

    private void setupNotification(Context context) {
        Cat.d("Setting up the notification");
        // Get NotificationManager reference
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);

        // get ip address
        InetAddress address = FsService.getLocalInetAddress();
        if (address == null) {
            Cat.w("Unable to retrieve the local ip address");
            return;
        }
        String iptext = "ftp://" + address.getHostAddress() + ":"
                + FsSettings.getPortNumber() + "/";

        // Instantiate a Notification
        int icon = R.mipmap.launcher_icon;

//        int icon = R.mipmap.launcher_icon;
        CharSequence tickerText = String.format(context.getString(R.string.notification_server_starting), iptext);
        long when = System.currentTimeMillis();

        // Define Notification's message and Intent
//        CharSequence contentTitle = context.getString(R.string.notification_title);
        CharSequence contentTitle = "" + MyApplication.networkSSID;
        CharSequence contentText = String.format(context.getString(R.string.ftpIsRunningAt), iptext);

//        Intent notificationIntent = new Intent(context, CrlPullPushTransferUsageScreen.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        int stopIcon = android.R.drawable.ic_menu_close_clear_cancel;
        CharSequence stopText = context.getString(R.string.notification_stop_text);
        Intent stopIntent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0,
                stopIntent, PendingIntent.FLAG_ONE_SHOT);

//        int preferenceIcon = android.R.drawable.ic_menu_preferences;
//        CharSequence preferenceText = context.getString(R.string.notif_settings_text);
//        Intent preferenceIntent = new Intent(context, DashboardActivity.class);
//        preferenceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent preferencePendingIntent = PendingIntent.getActivity(context, 0, preferenceIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
//                .setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(stopIcon, stopText, stopPendingIntent)
                //   .addAction(preferenceIcon, preferenceText, preferencePendingIntent)
                .setShowWhen(false)
                .build();

        // Pass Notification to NotificationManager
        nm.notify(NOTIFICATION_ID, notification);

        Cat.d("Notification setup done");
    }


    private void clearNotification(Context context) {
        Cat.d("Clearing the notifications");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);
        nm.cancelAll();
        Cat.d("Cleared notification");
        EventBus.getDefault().post(new MessageEvent("stopDialog"));
    }
}
