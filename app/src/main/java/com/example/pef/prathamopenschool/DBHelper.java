package com.example.pef.prathamopenschool;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PrathamTabDB.db";
    public Utility Util;
    public SQLiteDatabase database;
    public SQLiteDatabase db;
    public ContentValues contentValues;
    public Context c;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 21);
        try {
            c = context;
            contentValues = new ContentValues();
            Util = new Utility();
        } catch (Exception e) {

        }

    }


    public SQLiteDatabase GetWriteableDatabaseInstance() {
        database = this.getWritableDatabase();
        return database;
    }

    public SQLiteDatabase GetReadableDatabaseInstance() {
        database = this.getReadableDatabase();
        return database;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(DatabaseInitialization.CreateAserTable);
            db.execSQL(DatabaseInitialization.CreateVillageTable);
            db.execSQL(DatabaseInitialization.CreateLogTable);
            db.execSQL(DatabaseInitialization.CreateGroupTable);
            db.execSQL(DatabaseInitialization.CreateStudentTable);
            db.execSQL(DatabaseInitialization.CreateScoreTable);
            db.execSQL(DatabaseInitialization.CreateAssessmentScoreTable);
            db.execSQL(DatabaseInitialization.CreateCRLTable);
            db.execSQL(DatabaseInitialization.CreateUserTypeTable);
            db.execSQL(DatabaseInitialization.InsertIndividualUserType);
            db.execSQL(DatabaseInitialization.InsertGroupUserType);
            db.execSQL(DatabaseInitialization.InsertAdminUserType);
            db.execSQL(DatabaseInitialization.InsertSuperAdminUserType);
            db.execSQL(DatabaseInitialization.CreateUserTable);
            db.execSQL(DatabaseInitialization.CreateSessionTable);
            db.execSQL(DatabaseInitialization.CreateStatusTable);
            db.execSQL(DatabaseInitialization.CreateAttendanceTable);
            db.execSQL(DatabaseInitialization.InsertToStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.d("onUpgrade : ", "Old version : " + oldVersion + " New Version : " + newVersion);

            if ((oldVersion < newVersion) && (newVersion == 21)) {
                // Alter Aser Table Query
                db.execSQL(DatabaseInitialization.AlterAserTableSharedBy);
                db.execSQL(DatabaseInitialization.AlterAserTableSharedAtDateTime);
                db.execSQL(DatabaseInitialization.AlterAserTableAppVersion);
                db.execSQL(DatabaseInitialization.AlterAserTableAppName);
                db.execSQL(DatabaseInitialization.AlterAserTableCreatedOn);

                // Alter Student Table Query
                db.execSQL(DatabaseInitialization.AlterStudentTableSharedBy);
                db.execSQL(DatabaseInitialization.AlterStudentTableSharedAtDateTime);
                db.execSQL(DatabaseInitialization.AlterStudentTableAppVersion);
                db.execSQL(DatabaseInitialization.AlterStudentTableAppName);
                db.execSQL(DatabaseInitialization.AlterStudentTableCreatedOn);

                // Alter Groups Table Query
                db.execSQL(DatabaseInitialization.AlterGroupsTableSharedBy);
                db.execSQL(DatabaseInitialization.AlterGroupsTableSharedAtDateTime);
                db.execSQL(DatabaseInitialization.AlterGroupsTableAppVersion);
                db.execSQL(DatabaseInitialization.AlterGroupsTableAppName);
                db.execSQL(DatabaseInitialization.AlterGroupsTableCreatedOn);

                // Alter CRL Table Query
                db.execSQL(DatabaseInitialization.AlterCRLTableSharedBy);
                db.execSQL(DatabaseInitialization.AlterCRLTableSharedAtDateTime);
                db.execSQL(DatabaseInitialization.AlterCRLTableAppVersion);
                db.execSQL(DatabaseInitialization.AlterCRLTableAppName);
                db.execSQL(DatabaseInitialization.AlterCRLTableCreatedOn);

            }

            BackupDatabase.backup(c);

        } catch (Exception e) {
            Log.d("onUpgradeException :", "ERROR");
            e.printStackTrace();
        }
    }

}
