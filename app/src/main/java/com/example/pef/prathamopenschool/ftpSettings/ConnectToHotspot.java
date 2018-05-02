package com.example.pef.prathamopenschool.ftpSettings;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.pef.prathamopenschool.FTPConnect;
import com.example.pef.prathamopenschool.FTPInterface;
import com.example.pef.prathamopenschool.MyApplication;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;

import static android.content.Context.WIFI_SERVICE;
import static com.example.pef.prathamopenschool.MyApplication.networkSSID;

public class ConnectToHotspot extends AsyncTask<Void, Void, Void> {
    private ProgressDialog pd;
    private Context context;
    private boolean connected = false;
    FTPClient client1;
    FTPInterface.FTPConnectInterface ftpConnectInterface;
    String ipaddress;
    String port;

    public ConnectToHotspot(Context context, FTPInterface.FTPConnectInterface ftpConnectInterface
            , String ipaddress, String port) {
        this.context = context;
        this.ftpConnectInterface = ftpConnectInterface;
        this.ipaddress = ipaddress.replace("ftp://", "");
        this.port = port;
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
//        String SSID = getWifiName(context).replace("\"", "");
        client1 = new FTPClient();
//        if (SSID.equalsIgnoreCase(networkSSID)) {
//            // Connected to PrathamHotspot
//            connected = true;
//        } else {
//            // todo automatically connect to PrathamHotSpot
////            connectToPrathamHotSpot();
//            String recheckSSID = getWifiName(context).replace("\"", "");
//            if (recheckSSID.equalsIgnoreCase(networkSSID)) {
//                connected = true;
//            } else {
////                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
////                intent.addCategory(Intent.CATEGORY_LAUNCHER);
////                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
////                intent.setComponent(cn);
////                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
////                startActivity(intent);
//
//                // Changing message text color
//            }
//        }

//        if (true) {
        // todo if connected to FTP Server
//            final FTPClient[] client = new FTPClient[1];
        try {
            ipaddress=ipaddress.replace("ftp://","");
            ipaddress=ipaddress.replace(":8080","");
            client1.connect(ipaddress, 8080);
            client1.login("ftp", "ftp");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (pd != null)
            pd.dismiss();
        MyApplication.ftpClient = client1;
        ftpConnectInterface.onConnectionEshtablished(client1.isConnected());
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
}
