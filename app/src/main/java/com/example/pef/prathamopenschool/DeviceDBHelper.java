package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

public class DeviceDBHelper extends DBHelper {

    final String TABLENAME = "Device";
    final String ERRORTABLENAME = "Logs";
    Context c;

    public DeviceDBHelper(Context context) {
        super(context);
        c = context;
        database = GetWriteableDatabaseInstance();
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
        contentValues.put("GroupId", logs.groupId);
        contentValues.put("DeviceId", logs.deviceId);

        contentValues.put("LogDetail", "DeviceLog");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }


    public boolean Add(Device device) {
        try {

            _PopulateContentValues(device);

            long resultCount = database.insert(TABLENAME, null, contentValues);
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Add");
            return false;
        }
    }

    public boolean Update(Device device) {
        try {
            _PopulateContentValues(device);
            long resultCount = database.update(TABLENAME, contentValues, null, null);
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Update");
            return false;
        }
    }

    public boolean Delete() {
        try {
            long sresultCount = database.delete(TABLENAME, null, null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Device Get() {
        try {
            Cursor cursor = database.rawQuery("select * from Device", null);
            return _PopulateObjectFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Get");
            return null;
        }
    }

    private void _PopulateContentValues(Device device) {
        contentValues.put("DeviceID", device.DeviceID);
        contentValues.put("DeviceType", device.DeviceType);
        contentValues.put("DeviceName", device.DeviceName);
    }

    private Device _PopulateObjectFromCursor(Cursor cursor) {
        try {
            Device device = new Device();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                device.DeviceID = cursor.getString(cursor.getColumnIndex("DeviceID"));
                device.DeviceName = cursor.getString(cursor.getColumnIndex("DeviceName"));
                device.DeviceType = cursor.getString(cursor.getColumnIndex("DeviceType"));

                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return device;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "_PopulateObjectFromCursor");
            return null;
        }
    }


}