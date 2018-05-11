package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PEF-2 on 30/05/2016.
 */
public class StatusDBHelper extends DBHelper {
    Context c;
    final String TABLENAME = "Status";
    final String ERRORTABLENAME = "Logs";
    public static int newGroupCount = 1;

    StatusDBHelper(Context c) {
        super(c);
        this.c = c;
    }

    public boolean DeleteAll() {
        try {
            database = getWritableDatabase();
            long resultCount = database.delete("Status", null, null);
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "DeleteAll");
            return false;
        }
    }


    public boolean initialDataAvailable(String Key) {

        try {
            database = getWritableDatabase();

            Cursor cursor = database.rawQuery("SELECT * FROM Status WHERE Key=?", new String[]{Key});
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    cursor.close();
                    database.close();
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "initialDataAvailable");
            return false;
        }
    }

    public void insertInitialData(String key, String value) {

        try {
            database = getWritableDatabase();

            contentValues.put("key", key);
            contentValues.put("value", value);

            database.replace("Status", null, contentValues);
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "insertInitialData");
        }
    }

    private void _PopulateLogValues(Exception ex, String method) {

        Logs logs = new Logs();

        logs.currentDateTime = Util.GetCurrentDateTime(false);
        logs.errorType = "Error";
        logs.exceptionMessage = ex.getMessage().toString();
        logs.exceptionStackTrace = ex.getStackTrace().toString();
        logs.methodName = method;
        logs.groupId = MultiPhotoSelectActivity.selectedGroupId == null ? "GroupID" : MultiPhotoSelectActivity.selectedGroupId;
        logs.deviceId = MultiPhotoSelectActivity.deviceID;

        contentValues.put("CurrentDateTime", logs.currentDateTime);
        contentValues.put("ExceptionMsg", logs.exceptionMessage);
        contentValues.put("ExceptionStackTrace", logs.exceptionStackTrace);
        contentValues.put("MethodName", logs.methodName);
        contentValues.put("Type", logs.errorType);
        contentValues.put("GroupId", logs.groupId == null ? "" : logs.groupId);
        contentValues.put("DeviceId", logs.deviceId);

        contentValues.put("LogDetail", "StatusLog");

        if (database != null) {
            database.insert(ERRORTABLENAME, null, contentValues);

            database.close();
        }
        BackupDatabase.backup(c);
    }


    public boolean updateTrailerCountbyGroupID(int count, String grpId) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Update " + TABLENAME + " set trailerCount = ? where value = ?", new String[]{String.valueOf(count), String.valueOf(grpId)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();
            if (cursor.getCount() >= 0)
                return true;
            else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "updateTrailerCountbyGroupID");
            return false;
        }
    }


    // get Status of Alarm
    public String AlarmStatus(String key) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select value from " + TABLENAME + " where key = ?", new String[]{key});
            cursor.moveToFirst();

            return cursor.getString(0);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "AlarmStatus");
            return "0";
        }
    }

    // CRL Entry for Created by
    public void UpdateAlarm(String alarmTime, String alarmValue) {
        try {
            database = getWritableDatabase();
            if (database != null) {

                Cursor cursor = database.rawQuery("UPDATE " + TABLENAME + " SET value = ? where key = ? ", new String[]{alarmValue, alarmTime});
                cursor.moveToFirst();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            _PopulateLogValues(ex, "UpdateAlarm");
        }
    }


    // Pravin
    public String[] getGroupIDs() {     /*function for getting All Group IDs */
        String groups = "", key = "ActivatedForGroups";
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select value from " + TABLENAME + " where key = ? ", new String[]{key});
            cursor.moveToFirst();
            groups = new String(cursor.getString(0));
            String[] groupIDs = groups.split(",");
            return groupIDs;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getGroupIDs");
            return null;
        }
    }


    // CRL Entry for Created by
    public boolean Update(String keyName, String value) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("UPDATE " + TABLENAME + " SET value = ? where key = ? ", new String[]{value, keyName});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();

            if (cursor.getCount() >= 0)
                return true;
            else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Update");
            return false;
        }
    }

    // Get Trailer countvalue by GroupID for checking Aaj Ka Sawaal Played or not
    // 0 = not played
    // 1 = played
    public int getAajKaSawalPlayedStatus(String value) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select trailerCount from " + TABLENAME + " where value = ?", new String[]{value});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();

            return cursor.getInt(0);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getAajKaSawalPlayedStatus");
            return 0;
        }
    }

    public String getValue(String key) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select value from " + TABLENAME + " where key = ?", new String[]{key});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();

            return cursor.getString(0);
        } catch (Exception ex) {
//            _PopulateLogValues(ex, "getValue");
            return "null";
        }
    }

    public boolean checkGroupIdExists(String grpId) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select * from " + TABLENAME + " where value = ?", new String[]{String.valueOf(grpId)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                return true;
            } else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "checkGroupIdExists");
            return false;
        }
    }

    public int getTrailerCount(String grpId) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select trailerCount from " + TABLENAME + " where value = ?", new String[]{String.valueOf(grpId)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();

            //return cursor.getString(0);
            return cursor.getInt(0);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getTrailerCount");
            return 0;
        }
    }

    public int getOldTrailerCount(String grpId) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Select oldTrailerCount from " + TABLENAME + " where value = ?", new String[]{String.valueOf(grpId)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();

            //return cursor.getString(0);
            return cursor.getInt(0);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getOldTrailerCount");
            return 0;
        }
    }

    public boolean updateTrailerCountByKey(int count, String key) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Update " + TABLENAME + " set trailerCount = ? where key = ?", new String[]{String.valueOf(count), String.valueOf(key)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();
            if (cursor.getCount() >= 0)
                return true;
            else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "updateTrailerCountByKey");
            return false;
        }
    }

    public boolean updateTrailerCount(int count, String grpId) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Update " + TABLENAME + " set trailerCount = ? where value = ?", new String[]{String.valueOf(count), String.valueOf(grpId)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();
            if (cursor.getCount() >= 0)
                return true;
            else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "updateTrailerCount");
            return false;
        }
    }

    public boolean updateOldTrailerCount(int count, String grpId) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("Update " + TABLENAME + " set oldTrailerCount = ? where value = ?", new String[]{String.valueOf(count), String.valueOf(grpId)});
            //long resultCount = database.update(TABLENAME, contentValues, keyName +" = ?", new String[]{value});
            cursor.moveToFirst();
            if (cursor.getCount() >= 0)
                return true;
            else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "updateOldTrailerCount");

            return false;
        }
    }

    public boolean addToStatusTable(String key, String value, int count, int oldTrailerCount) {
        try {
            database = getWritableDatabase();
            // Toast.makeText(grpContext,"newGroupCount"+newGroupCount,Toast.LENGTH_LONG).show();
            Cursor cursor = database.rawQuery("Insert into " + TABLENAME + " values(?,?,?,?)", new String[]{key, String.valueOf(value), String.valueOf(count), String.valueOf(oldTrailerCount)});
            cursor.moveToFirst();
            if (cursor.getCount() >= 0) {

                return true;
            } else return false;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "addToStatusTable");
            return false;
        }
    }


    // get AKS played status for QR Code
    public List<String> getAKSPlayedByQR(String accurateDate, ArrayList<Student> stdList) {
        database = getWritableDatabase();

        List<String> list = new ArrayList<String>();
        try {
            for (int i = 0; i < stdList.size(); i++) {
                String stdID = stdList.get(i).StudentID;
                {
                    Cursor cursor = database.rawQuery("SELECT s.StartDateTime FROM Scores s INNER JOIN Attendance a ON a.SessionID = s.SessionID WHERE s.StartDateTime LIKE '" + accurateDate + "%' AND a.PresentStudentIds LIKE '" + stdID + "' AND s.Level IN (99,990,991) ", null);
                    cursor.moveToFirst();
                    while (cursor.isAfterLast() == false) {
                        list.add(cursor.getString(cursor.getColumnIndex("StartDateTime")));
                        cursor.moveToNext();
                    }
                }
            }
            database.close();
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "getAKSPlayedByQR");
            return null;
        }

    }

    // Get records of Aaj Ka Sawaal from Score DB
    public List<String> getAKSRecordsOfSelectedGroup(String selectedGroupId) {
        try {
            database = getWritableDatabase();
            List<String> list = new ArrayList<String>();
            {
                Cursor cursor = database.rawQuery("SELECT StartDateTime FROM Scores WHERE Level In (99,990,991)  AND GroupID = ? ", new String[]{selectedGroupId});
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    list.add(cursor.getString(cursor.getColumnIndex("StartDateTime")));
                    cursor.moveToNext();
                }
                database.close();
            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "getAKSRecordsOfSelectedGroup");
            return null;
        }
    }


    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        Cursor cursor = database.rawQuery("UPDATE Status SET key = IfNull(key,'0'), value = IfNull(value,'0'), trailerCount = IfNull(trailerCount,'0'), oldTrailerCount = IfNull(oldTrailerCount,'0')", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }

}
