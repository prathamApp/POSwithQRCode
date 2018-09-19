package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class SessionDBHelper extends DBHelper {
    final String TABLENAME = "Session";
    final String ERRORTABLENAME = "Logs";
    private Cursor cursor;

    public SessionDBHelper(Context context) {
        super(context);
        database = getWritableDatabase();
    }

    private void _PopulateLogValues(Exception ex, String method) {

        Logs logs = new Logs();

        logs.currentDateTime = Util.GetCurrentDateTime(false);
        logs.errorType = "Error";
        logs.exceptionMessage = ex.getMessage().toString();
        logs.exceptionStackTrace = ex.getStackTrace().toString();
        logs.methodName = method;
        logs.groupId = MultiPhotoSelectActivity.selectedGroupId;
        logs.deviceId = MultiPhotoSelectActivity.deviceID;

        contentValues.put("CurrentDateTime", logs.currentDateTime);
        contentValues.put("ExceptionMsg", logs.exceptionMessage);
        contentValues.put("ExceptionStackTrace", logs.exceptionStackTrace);
        contentValues.put("MethodName", logs.methodName);
        contentValues.put("Type", logs.errorType);
        contentValues.put("GroupId", logs.groupId == null ? "" : logs.groupId);
        contentValues.put("DeviceId", logs.deviceId);
        contentValues.put("LogDetail", "SessionLogs");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }


    // calculate total time usage of student from sessionID
    public List<ScoreList> getUsageDetails(String sessionID) {
        try {
            database = getWritableDatabase();
            List<ScoreList> list = new ArrayList<ScoreList>();
            {
                Cursor cursor = database.rawQuery("SELECT StartTime,EndTime FROM Session WHERE SessionID = ?", new String[]{sessionID});
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    list.add(new ScoreList(cursor.getString(cursor.getColumnIndex("StartTime")), cursor.getString(cursor.getColumnIndex("EndTime"))));
                    cursor.moveToNext();
                }
                database.close();
            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "getUsageDetails");
            return null;
        }

    }


    public boolean Add(Session sessionObj) {
        try {
            _PopulateContentValues(sessionObj);
            long resultCount = database.insert(TABLENAME, null, contentValues);
            database.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void _PopulateContentValues(Session session) {
        try {
            contentValues.put("SessionID", session.SessionID);
            contentValues.put("StartTime", session.StartTime);
            contentValues.put("EndTime", session.EndTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // set Flag to false
    public void UpdateEndTime(String sessionID) {
        try {
            contentValues.put("EndTime", new Utility().GetCurrentDateTime(false));
            database = getWritableDatabase();
            database.update(TABLENAME, contentValues, "SessionID= '" + sessionID + "'", null);
            database.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            _PopulateLogValues(ex, "UpdateEndTime");
        }
    }


    // Fetch data if already exists
    public String entryExist(String sessionID) {

        try {
            database = getWritableDatabase();
            String endTime = new String();

            cursor = database.rawQuery("SELECT EndTime FROM Session WHERE SessionID = ? ", new String[]{sessionID});
            Session sessionObject = new Session();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                sessionObject.EndTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                endTime = sessionObject.EndTime;
                cursor.moveToNext();
            }
            cursor.close();
            database.close();

            return endTime;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "entryExist");
            return "ExceptionOccured";
        }
    }

    public String getSessionStartTime(String sessionID) {

        try {
            database = getWritableDatabase();
            String endTime = new String();

            cursor = database.rawQuery("SELECT StartTime FROM Session WHERE SessionID = ? ", new String[]{sessionID});
            Session sessionObject = new Session();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                sessionObject.StartTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                endTime = sessionObject.StartTime;
                cursor.moveToNext();
            }
            cursor.close();
            database.close();

            return endTime;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "entryExist");
            return "ExceptionOccured";
        }
    }


}
