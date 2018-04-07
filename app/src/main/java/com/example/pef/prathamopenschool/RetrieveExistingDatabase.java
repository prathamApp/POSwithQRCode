package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

// Existing DB File found
public class RetrieveExistingDatabase extends SQLiteOpenHelper{

    public RetrieveExistingDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public RetrieveExistingDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    public static void backup(Context mContext) {
        try {

            File data = Environment.getExternalStorageDirectory();
            File sd = Environment.getDataDirectory();

            String currentDBPath = "PrathamTabDB.db";
            String backupDBPath = "//data//com.example.pef.prathamopenschool//databases//" + "PrathamTabDB.db";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();

                if (!backupDB.exists()) {

                    File databasesFolder = new File(Environment.getDataDirectory() + "//data//com.example.pef.prathamopenschool//databases");

                    databasesFolder.mkdir();

                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }

            }


        } catch (Exception e) {

            e.printStackTrace();
            throw new Error("Copying Failed");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
