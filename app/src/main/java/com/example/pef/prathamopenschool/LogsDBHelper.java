package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class LogsDBHelper extends DBHelper {
    final String TABLENAME = "Logs";
    final String ERRORTABLENAME = "Logs";
    Context mContext;

    LogsDBHelper(Context c){
        super(c);
        mContext=c;
        database=getWritableDatabase();
    }

    public List<Logs> GetAll() {
        try {
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + "", null);
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex,"GetAll-logs");
            return null;
        }
    }

    public boolean DeleteAll() {
        try {
            // database = getWritableDatabase();
            long resultCount = database.delete(TABLENAME, null, null);
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex,"DeleteAll-Logs");
            return false;
        }
    }

    private List<Logs> _PopulateListFromCursor(Cursor cursor) {
        try {
            List<Logs> logsList = new ArrayList<Logs>();
            Logs logs= new Logs();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                logs.logId=cursor.getInt(cursor.getColumnIndex("LogID"));
                logs.currentDateTime=cursor.getString(cursor.getColumnIndex("CurrentDateTime"));
                logs.exceptionMessage=cursor.getString(cursor.getColumnIndex("ExceptionMsg"));
                logs.exceptionStackTrace=cursor.getString(cursor.getColumnIndex("ExceptionStackTrace"));
                logs.methodName=cursor.getString(cursor.getColumnIndex("MethodName"));
                logs.errorType=cursor.getString(cursor.getColumnIndex("Type"));
                logs.groupId=cursor.getString(cursor.getColumnIndex("GroupId"));
                logs.deviceId=cursor.getString(cursor.getColumnIndex("DeviceId"));
                logs.LogDetail = cursor.getString(cursor.getColumnIndex("LogDetail"));

                logsList.add(logs);
                cursor.moveToNext();
            }
            cursor.close();database.close();
            return logsList;
        } catch (Exception ex) {
            _PopulateLogValues(ex,"PopulateListFromCursor-Logs");
            return null;
        }
    }


    private void _PopulateLogValues(Exception ex,String method) {

        Logs logs=new Logs();

        logs.currentDateTime=Util.GetCurrentDateTime(false);
        logs.errorType="Error";
        logs.exceptionMessage=ex.getMessage().toString();
        logs.exceptionStackTrace=ex.getStackTrace().toString();
        logs.methodName=method;
        logs.groupId= MultiPhotoSelectActivity.selectedGroupId;
        logs.deviceId= MultiPhotoSelectActivity.deviceID;

        contentValues.put("CurrentDateTime",logs.currentDateTime);
        contentValues.put("ExceptionMsg",logs.exceptionMessage);
        contentValues.put("ExceptionStackTrace",logs.exceptionStackTrace);
        contentValues.put("MethodName",logs.methodName);
        contentValues.put("Type",logs.errorType);
        contentValues.put("GroupId",logs.groupId);
        contentValues.put("DeviceId",logs.deviceId);

        contentValues.put("LogDetail","ErrorLogs");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }
}
