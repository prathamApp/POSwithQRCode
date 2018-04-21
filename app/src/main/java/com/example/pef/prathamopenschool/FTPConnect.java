package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.example.pef.prathamopenschool.ftpSettings.ConnectToHotspot;
import com.example.pef.prathamopenschool.ftpSettings.CreateWifiAccessPoint;
import com.example.pef.prathamopenschool.ftpSettings.CreateWifiAccessPointOnHigherAPI;
import com.example.pef.prathamopenschool.ftpSettings.WifiAPController;
import com.google.gson.Gson;

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

public class FTPConnect implements FolderClick {
    private Context context;
    public Activity activity;
    String treeUri;
    ArrayList<Integer> level = new ArrayList<>();
    FTPClient tempFtpClient = null;
    private DocumentFile tempUri;
    private File tempFile;

    public FTPConnect(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
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
            CreateWifiAccessPoint createOne = new CreateWifiAccessPoint(context, activity);
            createOne.execute((Void) null);
        }
    }

    public void connectFTPHotspot() {
        new ConnectToHotspot(context, FTPConnect.this).execute();
    }

    @Override
    public void onConnectionEshtablished() {
        new ListFilesOnFTP(false, null, false).execute();
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
            if (aFile.getName().endsWith(".json")) {
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
                return files;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
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
}
