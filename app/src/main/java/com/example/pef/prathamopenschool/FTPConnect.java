package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.example.pef.prathamopenschool.ftpSettings.ConnectToHotspot;
import com.example.pef.prathamopenschool.ftpSettings.CreateWifiAccessPoint;
import com.example.pef.prathamopenschool.ftpSettings.CreateWifiAccessPointOnHigherAPI;
import com.example.pef.prathamopenschool.ftpSettings.FsService;
import com.example.pef.prathamopenschool.ftpSettings.WifiAPController;
import com.example.pef.prathamopenschool.ftpSettings.WifiApControl;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class FTPConnect implements FTPInterface.FTPConnectInterface {
    private Context context;
    public Activity activity;
    String treeUri;
    ArrayList<Integer> level = new ArrayList<>();
    FTPClient tempFtpClient = null;
    private DocumentFile tempUri;
    private File tempFile;
    FTPInterface.PushPullInterface pushPullInterface;
    String typeOfFile = "";

    public FTPConnect(Context context) {
        this.context = context;
    }

    public FTPConnect(Context context, Activity activity, FTPInterface.PushPullInterface pushPullInterface) {
        this.context = context;
        this.activity = activity;
        this.pushPullInterface = pushPullInterface;
    }

    public void createFTPHotspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CreateWifiAccessPointOnHigherAPI createOneHAPI = new CreateWifiAccessPointOnHigherAPI(context);
            createOneHAPI.execute((Void) null);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            // Start HotSpot
            WifiAPController wifiAPController = new WifiAPController();
            wifiAPController.wifiToggle(MyApplication.networkSSID, ""/*, wifiManager*/, context);
        } else {
            // Start HotSpot (Tablet)
            CreateWifiAccessPoint createOne = new CreateWifiAccessPoint(context, activity,FTPConnect.this);
            createOne.execute((Void) null);
        }
    }

    public void connectFTPHotspot(String typeOfFile, String ipaddress, String port) {
        this.typeOfFile = typeOfFile;
        new ConnectToHotspot(context, FTPConnect.this, ipaddress, port).execute();
    }

    @Override
    public void onConnectionEshtablished(boolean connected) {
        if (MyApplication.ftpClient != null && connected) {
//            new ListFilesOnFTP(false, null, false).execute();
            new UploadTHroughFTP(typeOfFile).execute();
        } else {
            pushPullInterface.showDialog();
        }
    }

    @Override
    public void showDialog() {
        pushPullInterface.showDialog();
    }

    @Override
    public void onFolderClicked(int position, String name) {

    }

    public void onDownload(int position, FTPFile name) {
        File final_file = null;
        DocumentFile finalDocumentFile = null;
        boolean isSdCard = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("IS_SDCARD", false);
        if (!isSdCard) {
            String path = PreferenceManager.getDefaultSharedPreferences(context).getString("PATH", "");
            if (name.getName().equalsIgnoreCase("app_PrathamImages")) {
                File file = new File(path, "/app_PrathamImages");
                if (!file.exists())
                    file.mkdir();
                final_file = file;
            } else {
                File file = new File(path, "/app_PrathamGame");
                if (!file.exists())
                    file.mkdir();
                File child_file = new File(file, name.getName());
                if (!child_file.exists())
                    child_file.mkdir();
                final_file = child_file;
            }
        } else {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, Uri.parse(treeUri));
            if (name.getName().equalsIgnoreCase("app_PrathamImages")) {
                //check whether root folder "PraDigi" exists or not
                DocumentFile documentFile1 = documentFile.findFile("PraDigi");
                if (documentFile1 == null)
                    documentFile = documentFile.createDirectory("PraDigi");
                else
                    documentFile = documentFile1;

                //check whether sub folder folder "app_PrathamGame" exists or not
                DocumentFile documentFile2 = documentFile.findFile("app_PrathamImages");
                if (documentFile2 == null)
                    documentFile = documentFile.createDirectory("app_PrathamImages");
                else
                    documentFile = documentFile2;
            } else {
                DocumentFile documentFile1 = documentFile.findFile("PraDigi");
                if (documentFile1 == null)
                    documentFile = documentFile.createDirectory("PraDigi");
                else
                    documentFile = documentFile1;
                //check whether sub folder folder "app_PrathamGame" exists or not
                DocumentFile documentFile2 = documentFile.findFile("app_PrathamGame");
                if (documentFile2 == null)
                    documentFile = documentFile.createDirectory("app_PrathamGame");
                else
                    documentFile = documentFile2;
                //check whether downloading file exists or not
                DocumentFile documentFile3 = documentFile.findFile(name.getName());
                if (documentFile3 == null)
                    documentFile = documentFile.createDirectory(name.getName());
                else
                    documentFile = documentFile3;
            }
            finalDocumentFile = documentFile;
        }
        DocumentFile finalDocumentFile1 = finalDocumentFile;
        File final_file1 = final_file;
        new DownloadTHroughFTP(finalDocumentFile1, final_file1, isSdCard, name);
    }


    private void downloadDirectoryToInternal(FTPClient client1, File final_file1, FTPFile name) {
        try {
            tempFile = final_file1;
            FTPClient tempClient = client1;
            if (name != null)
                tempClient.changeWorkingDirectory(name.getName());
            FTPFile[] subFiles = tempClient.listFiles();
            Log.d("file_size::", subFiles.length + "");
            if (subFiles != null && subFiles.length > 0) {
                for (FTPFile aFile : subFiles) {
                    Log.d("name::", aFile.getName() + "");
                    String currentFileName = aFile.getName();
                    if (currentFileName.equals(".") || currentFileName.equals("..")) {
                        // skip parent directory and the directory itself
                        continue;
                    }
                    if (aFile.isDirectory()) {
                        // create the directory in saveDir
                        File file = new File(tempFile.getAbsolutePath(), currentFileName);
                        if (!file.exists())
                            file.mkdir();
                        tempClient.changeWorkingDirectory(currentFileName);
                        downloadDirectoryToInternal(client1, file, null);
                    } else {
                        downloadFileToInternal(client1, aFile, tempFile);
                    }
                }
                tempFile = tempFile.getParentFile();
                tempClient.changeToParentDirectory();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFileToInternal(FTPClient client1, FTPFile aFile, File tempFile) {
        try {
            tempFile = new File(tempFile, aFile.getName());
            Log.d("tempFile::", tempFile.getAbsolutePath());
            OutputStream outputStream = context.getContentResolver().openOutputStream(Uri.fromFile(tempFile));
            client1.setFileType(FTP.BINARY_FILE_TYPE);
            client1.retrieveFile(aFile.getName(), outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Todo copy json to database
            if (aFile.getName().endsWith(".json") && typeOfFile.equalsIgnoreCase("ContentTransfer")) {
                Log.d("json_path:::", tempFile.getAbsolutePath() + "");
                Log.d("json_path:::", aFile.getName() + "");
                //Todo read json from file
                String response = loadJSONFromAsset(tempFile.getAbsolutePath());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Gson gson = new Gson();
//                Modal_DownloadContent download_content = gson.fromJson(jsonObject.toString(), Modal_DownloadContent.class);
//                addContentToDatabase(download_content);
            } else if (typeOfFile.equalsIgnoreCase("TransferUsage")) {
                //todo parse and show count of files, score and deviceId
//                pushPullInterface.onFilesRecievedComplete(typeOfFile,"");
            } else if (typeOfFile.equalsIgnoreCase("ReceiveProfiles") && !typeOfFile.equalsIgnoreCase("ReceiveJson")) {
                //todo parse and show count of files
//                pushPullInterface.onFilesRecievedComplete(typeOfFile,"");
            }
        }
    }

    private void downloadDirectoryToSdCard(FTPClient ftpClient, DocumentFile documentFile, FTPFile name) {
        try {
            tempUri = documentFile;
            FTPClient tempClient = ftpClient;
            if (name != null)
                tempClient.changeWorkingDirectory(name.getName());
            FTPFile[] subFiles = tempClient.listFiles();
            Log.d("file_size::", subFiles.length + "");
            if (subFiles != null && subFiles.length > 0) {
                for (FTPFile aFile : subFiles) {
                    Log.d("name::", aFile.getName() + "");
                    String currentFileName = aFile.getName();
                    if (currentFileName.equals(".") || currentFileName.equals("..")) {
                        // skip parent directory and the directory itself
                        continue;
                    }
                    if (aFile.isDirectory()) {
                        // create the directory in saveDir
                        if (tempUri.findFile(currentFileName) == null) {
                            tempUri = tempUri.createDirectory(currentFileName);
                        }
                        tempClient.changeWorkingDirectory(currentFileName);
                        downloadDirectoryToSdCard(ftpClient, tempUri, null);
                    } else {
                        downloadFile(ftpClient, aFile, tempUri);
                    }
                }
                tempUri = tempUri.getParentFile();
                tempClient.changeToParentDirectory();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(FTPClient ftpClient, FTPFile ftpFile, DocumentFile tempFile) {
        try {
            tempFile = tempFile.createFile("image", ftpFile.getName());
            OutputStream outputStream = context.getContentResolver().openOutputStream(tempFile.getUri());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(ftpFile.getName(), outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Todo copy json to database
            if (ftpFile.getName().endsWith(".json")) {
                String path = SDCardUtil.getRealPathFromURI_API19(context, tempFile.getUri());
                Log.d("json_path:::", path + "");
                Log.d("json_path:::", ftpFile.getName() + "");
                //Todo read json from file
                String response = loadJSONFromAsset(path);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Gson gson = new Gson();
//                Modal_DownloadContent download_content = gson.fromJson(jsonObject.toString(), Modal_DownloadContent.class);
//                addContentToDatabase(download_content);
            }
        }
    }

    // Reading Json From Internal Storage
    public String loadJSONFromAsset(String path) {
        String JsonStr = null;
        try {
            File queJsonSDCard = new File(path);
            FileInputStream stream = new FileInputStream(queJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                JsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonStr;
    }

    public class ListFilesOnFTP extends AsyncTask<Void, Void, FTPFile[]> {
        boolean isExpand;
        String name;
        boolean changeToParent;

        public ListFilesOnFTP(boolean isExpand, String name, boolean changeToParent) {
            this.isExpand = isExpand;
            this.name = name;
            this.changeToParent = changeToParent;
            if (tempFtpClient == null)
                tempFtpClient = MyApplication.ftpClient;
        }

        @Override
        protected FTPFile[] doInBackground(Void... voids) {
            try {
                if (isExpand) {
                    level.add(1);
                    tempFtpClient.changeWorkingDirectory(name);
                }
                if (changeToParent) {
                    level.remove(level.size() - 1);
                    tempFtpClient.changeToParentDirectory();
                }
                FTPFile[] files = tempFtpClient.listFiles();
                // todo show count of files
                return files;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FTPFile[] ftpFiles) {
            super.onPostExecute(ftpFiles);
            if (ftpFiles.length > 0) {
                for (FTPFile temp_file : ftpFiles) {
                    if (typeOfFile.equalsIgnoreCase("ReceiveProfiles")) {
                        tempFtpClient = MyApplication.ftpClient;
                        File f = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/ReceivedContent");
                        new DownloadTHroughFTP(null, f, false, temp_file).execute();
                    } else if (typeOfFile.equalsIgnoreCase("TransferUsage")) {
                        tempFtpClient = MyApplication.ftpClient;
                        File f = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/transferredUsage");
                        new DownloadTHroughFTP(null, f, false, temp_file).execute();
                    } else if (typeOfFile.equalsIgnoreCase("ReceiveJson")) {
                        tempFtpClient = MyApplication.ftpClient;
                        File f = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json");
                        new DownloadTHroughFTP(null, f, false, temp_file).execute();
                    }
                }
            }
        }
    }

    public class DownloadTHroughFTP extends AsyncTask<Void, Void, Void> {
        DocumentFile finalDocumentFile1;
        File final_file1;
        boolean isSdCard;
        FTPFile name;

        public DownloadTHroughFTP(DocumentFile finalDocumentFile1, File final_file1, boolean isSdCard, FTPFile name) {
            this.finalDocumentFile1 = finalDocumentFile1;
            this.final_file1 = final_file1;
            this.isSdCard = isSdCard;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (name.isDirectory()) {
                if (isSdCard)
                    downloadDirectoryToSdCard(tempFtpClient, finalDocumentFile1, name);
                else
                    downloadDirectoryToInternal(tempFtpClient, final_file1, name);
            } else {
                if (isSdCard)
                    downloadFile(tempFtpClient, name, finalDocumentFile1);
                else
                    downloadFileToInternal(tempFtpClient, name, final_file1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class UploadTHroughFTP extends AsyncTask<Void, Void, Boolean> {
        //        DocumentFile finalDocumentFile1;
//        File final_file1;
//        boolean isSdCard;
        FTPClient temp = null;
        String sendingClient;

        public UploadTHroughFTP(String sendingClient) {
//            this.finalDocumentFile1 = finalDocumentFile1;
//            this.final_file1 = final_file1;
//            this.isSdCard = isSdCard;
//            this.name = name;
            this.sendingClient = sendingClient;
            if (temp == null) {
                temp = MyApplication.ftpClient;
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result = false;
            try {
                // for Transfer Usage
                temp.enterLocalPassiveMode();
                temp.setFileType(FTP.BINARY_FILE_TYPE);
                if (sendingClient.equalsIgnoreCase("TransferUsage")) {
                    boolean ifDirExists = checkDirectoryExists(temp, "RecievedUsage");
                    if (!ifDirExists)
                        temp.makeDirectory("RecievedUsage");
//                    temp.changeWorkingDirectory("RecievedUsage");
                    String path = Environment.getExternalStorageDirectory().toString() + "/.POSDBBackups";
                    File directory = new File(path);
                    File[] files = directory.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().startsWith("pushNewDataToServer")) {
                            Log.d("Files", "FileName:" + files[i].getName());
                            String data = path + "/" + files[i].getName();
                            FileInputStream in = new FileInputStream(new File(data));
                            result = temp.storeFile("/RecievedUsage/" + files[i].getName(), in);
                            Log.v("upload_result:::", files[i].getName() + "...." + result);
                        }
                    }
                    temp.logout();
                    temp.disconnect();
                } else if (sendingClient.equalsIgnoreCase("TransferProfiles")) {
                    boolean ifDirExists = checkDirectoryExists(temp, "RecievedProfiles");
                    if (!ifDirExists)
                        temp.makeDirectory("RecievedProfiles");
//                    temp.changeWorkingDirectory("RecievedProfiles");
                    String path = Environment.getExternalStorageDirectory() + "/.POSinternal/sharableContent";
                    File directory = new File(path);
                    File[] files = directory.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().startsWith("NewProfiles")) {
                            Log.d("Files", "FileName:" + files[i].getName());
                            String data = path + "/" + files[i].getName();
                            FileInputStream in = new FileInputStream(new File(data));
                            result = temp.storeFile("/RecievedProfiles/" + files[i].getName(), in);
                            Log.v("upload_result:::", files[i].getName() + "...." + result);
                        }
                    }
                    temp.logout();
                    temp.disconnect();
                } else if (sendingClient.equalsIgnoreCase("TransferJson")) {
                    boolean ifDirExists = checkDirectoryExists(temp, "RecievedJson");
                    if (!ifDirExists)
                        temp.makeDirectory("RecievedJson");
//                    temp.changeWorkingDirectory("RecievedJson");
                    String path = Environment.getExternalStorageDirectory().toString() + "/.POSinternal/Json";
                    File directory = new File(path);
                    File[] files = directory.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        Log.d("Files", "FileName:" + files[i].getName());
                        String data = path + "/" + files[i].getName();
                        FileInputStream in = new FileInputStream(new File(data));
                        result = temp.storeFile("/RecievedJson/" + files[i].getName(), in);
                        Log.v("upload_result:::", files[i].getName() + "...." + result);
                    }
                    temp.logout();
                    temp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result=false;
            }
            return result;
        }

        boolean checkDirectoryExists(FTPClient ftpClient, String dirPath) throws IOException {
            ftpClient.changeWorkingDirectory(dirPath);
            int returnCode = ftpClient.getReplyCode();
            if (returnCode == 550) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                if (typeOfFile.equalsIgnoreCase("TransferUsage")) {
                    pushPullInterface.onFilesRecievedComplete("","");
                }else if (typeOfFile.equalsIgnoreCase("TransferProfiles")) {
                    pushPullInterface.onFilesRecievedComplete("TransferProfiles","");
                }else {
                    pushPullInterface.onFilesRecievedComplete("TransferJson","");
                }
            }
        }
    }

    public void startServer() {
        context.sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
    }

    public void stopServer() {
        context.sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }

    // Checking FTP Service is on or not
    public boolean checkServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().contains("ftpSettings.FsService")) {
                return true;
            }
/*
            if ("ftpSettings.FsService".contains(service.service.getClassName())) {
                return true;
            }
*/
        }
        return false;
    }

    // Turns off WiFi HotSpot
    public void turnOnOffHotspot(boolean isTurnToOn) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiApControl apControl = WifiApControl.getApControl(wifiManager);
        if (apControl != null) {

            // TURN OFF YOUR WIFI BEFORE ENABLE HOTSPOT
            //if (isWifiOn(context) && isTurnToOn) {
            //  turnOnOffWifi(context, false);
            //}

            apControl.setWifiApEnabled(apControl.getWifiApConfiguration(),
                    isTurnToOn);
        }
    }

    public ArrayList<String> scanNearbyWifi() {
        WifiManager mainWifiObj = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
        String wifis[] = new String[wifiScanList.size()];
        for (int i = 0; i < wifiScanList.size(); i++) {
            wifis[i] = ((wifiScanList.get(i)).toString());
        }
//        String filtered[] = new String[wifiScanList.size()];
        ArrayList<String> filtered = new ArrayList<>();
        int counter = 0;
        for (String eachWifi : wifis) {
            String[] temp = eachWifi.split(",");
            filtered.add(temp[0].substring(5).trim());//+"\n" + temp[2].substring(12).trim()+"\n" +temp[3].substring(6).trim();//0->SSID, 2->Key Management 3-> Strength
//            Log.d("scanNearbyWifi: ", "" + filtered.get(counter));
            counter++;
        }
        return filtered;
    }

    public void connectToPrathamHotSpot(String SSID) {
        try {
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = /*String.format("\"%s\"",*/ SSID;
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
            /*try {
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            wifiManager.reconnect();
/*            try {
                Thread.sleep(6000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
