package com.example.pef.prathamopenschool.ftpSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.pef.prathamopenschool.CrlPullPushTransferUsageScreen;
import com.example.pef.prathamopenschool.FTPInterface;
import com.example.pef.prathamopenschool.MyApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.example.pef.prathamopenschool.MyApplication.networkSSID;

public class CreateWifiAccessPoint extends AsyncTask<Void, Void, Boolean> {

    private ProgressDialog pd;
    public Context context;
    public Activity activity;
    FTPInterface.FTPConnectInterface ftpConnectInterface;

    public CreateWifiAccessPoint(Context context, Activity activity, FTPInterface.FTPConnectInterface ftpConnectInterface) {
        this.context = context;
        this.activity = activity;
        this.ftpConnectInterface=ftpConnectInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage("Starting Server ... Please wait !!!");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) activity.getBaseContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
        boolean methodFound = false;
        for (Method method : wmMethods) {
            if (method.getName().equals("setWifiApEnabled")) {
                methodFound = true;
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.SSID = networkSSID;
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                try {
                    final boolean apStatus = (Boolean) method.invoke(wifiManager, netConfig, true);
                    for (Method isWifiApEnabledMethod : wmMethods)
                        if (isWifiApEnabledMethod.getName().equals("isWifiApEnabled")) {
                            while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
                            }
                            for (Method method1 : wmMethods) {
                                if (method1.getName().equals("getWifiApState")) {
                                }
                            }
                        }

                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return methodFound;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        // delay for creating hotspot
        try {
            // Snackbar instead of Toast
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ftpConnectInterface.showDialog();
        //start Server
        context.sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));

        Toast.makeText(context, "Hotspot created", Toast.LENGTH_SHORT).show();

        if (pd != null)
            pd.dismiss();

    }
}
