package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by PEF-2 on 29/10/2015.
 */
public class BackupDatabase {

    public static void backup(Context mContext) {
        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            //if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.pef.prathamopenschool//databases//"+"PrathamTabDB.db";
                String backupDBPath = "PrathamTabDB.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                //File currentDB = new File(sd,backupDBPath);
                // File backupDB = new File(data,currentDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }

            //}
        } catch (Exception e) {
            //  ShowAlert showAlert=new ShowAlert();
            //  showAlert.showDialogue(mContext,"Problem occurred to database. Please contact your administrator.");
          /*  SyncActivityLogs syncActivityLogs=new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("backupDatabase-backupDatabase",e,"Error");*/
            e.printStackTrace();
            Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_LONG).show();
            throw new Error("Copying Failed");
        }
    }

}
