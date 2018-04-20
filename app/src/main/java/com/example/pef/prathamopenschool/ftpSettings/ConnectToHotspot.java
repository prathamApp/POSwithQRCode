package com.example.pef.prathamopenschool.ftpSettings;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.FolderClick;
import com.example.pef.prathamopenschool.MyApplication;

import org.apache.commons.net.ftp.FTPClient;

import static android.content.Context.WIFI_SERVICE;
import static com.example.pef.prathamopenschool.MyApplication.ftpClient;
import static com.example.pef.prathamopenschool.MyApplication.networkSSID;

public class ConnectToHotspot extends AsyncTask<Void, Void, Void> {
    private ProgressDialog pd;
    private Context context;
    private boolean connected = false;
    FTPClient client1;
    FolderClick folderClick;

    public ConnectToHotspot(Context context, FolderClick folderClick) {
        this.context = context;
        this.folderClick = folderClick;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage("Connecting ... Please wait !!!");
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Check if already connected to PrathamHotspot
        String SSID = getWifiName(context).replace("\"", "");
        if (SSID.equalsIgnoreCase(networkSSID)) {
            // Connected to PrathamHotspot
            connected = true;
        } else {
            // todo automatically connect to PrathamHotSpot
            connectToPrathamHotSpot();
            String recheckSSID = getWifiName(context).replace("\"", "");
            if (recheckSSID.equalsIgnoreCase(networkSSID)) {
                connected = true;
            } else {
//                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
//                intent.setComponent(cn);
//                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);

                // Changing message text color
            }
        }

        if (connected) {
            // todo if connected to FTP Server
//            final FTPClient[] client = new FTPClient[1];
            client1 = new FTPClient();
            try {
                client1.connect("ftp://192.168.43.1", 2121);
                client1.login("ftp", "ftp");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (pd != null)
            pd.dismiss();
        MyApplication.ftpClient = client1;
        folderClick.onConnectionEshtablished();
        Toast.makeText(context, "hotspot connected", Toast.LENGTH_SHORT).show();
    }

    public String getWifiName(Context context) {
        String ssid = null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
        }

        ssid = wifiInfo.getSSID();
        Log.d("ssaid::", ssid);
        return ssid;
    }

    private void connectToPrathamHotSpot() {
        try {
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = String.format("\"%s\"", networkSSID);
            wifiConfiguration.priority = 99999;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            int netId = wifiManager.addNetwork(wifiConfiguration);

            if (wifiManager.isWifiEnabled()) { //---wifi is turned on---
                //---disconnect it first---
                wifiManager.disconnect();
            } else { //---wifi is turned off---
                //---turn on wifi---
                wifiManager.setWifiEnabled(true);
                wifiManager.disconnect();
            }

            wifiManager.enableNetwork(netId, true);
            try {
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wifiManager.reconnect();
            try {
                Thread.sleep(6000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
