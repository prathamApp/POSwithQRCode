package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class POSDatabaseBackups {

    public static void moveFile() {

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

        InputStream inStream = null;
        OutputStream outStream = null;
        Calendar cal = Calendar.getInstance();
        String timestamp = dateFormat.format(cal.getTime());
        String Serial = Build.SERIAL;

        try {
            File currentFile = new File(Environment.getExternalStorageDirectory() + "/PrathamTabDB.db");
            File destFolder = new File(Environment.getExternalStorageDirectory() + "/.POSDBBackups");
            if (!destFolder.exists())
                destFolder.mkdir();
            File dest = new File(Environment.getExternalStorageDirectory() + "/.POSDBBackups/" + Serial + "_" + timestamp + ".db");
            inStream = new FileInputStream(currentFile);
            outStream = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            inStream.close();
            outStream.close();
            //delete the original file
            currentFile.delete();
            Log.d("Path Operation :", "Done");
        } catch (Exception e) {
            Log.d("Path Operation :", "Exception");
            e.printStackTrace();
        }

    }

}
