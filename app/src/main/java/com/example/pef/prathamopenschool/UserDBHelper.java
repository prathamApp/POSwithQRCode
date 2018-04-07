package com.example.pef.prathamopenschool;


import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Varun Anand on 8/28/2015.
 */
public class UserDBHelper extends DBHelper {
    final String TABLENAME = "Users";
    final String ERRORTABLENAME = "Logs";
    Context c;

    public UserDBHelper(Context context) {
        super(context);
        c = context;
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
        contentValues.put("GroupId", logs.groupId);
        contentValues.put("DeviceId", logs.deviceId);

        contentValues.put("LogDetail", "CrlLog");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }

    public boolean Add(User user) {
        try {
            _PopulateContentValues(user);
            long resultCount = database.insert(TABLENAME, null, contentValues);

            database.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean Update(User user) {
        try {
            // database = getWritableDatabase();
            _PopulateContentValues(user);
            long resultCount = database.update(TABLENAME, contentValues, "UserID = ?", new String[]{user.UserID});
            database.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean Delete(String userID) {
        try {
            // database = getWritableDatabase();
            long resultCount = database.delete(TABLENAME, "UserID = ?", new String[]{userID.toString()});
            database.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public User Get() {
        try {
            //  database = getReadableDatabase();
            //   Cursor cursor = database.rawQuery("select * from " + TABLENAME + " where UserID='" + userID + "'", null);
            Cursor cursor = database.rawQuery("select * from " + TABLENAME, null);
            database.close();
            return _PopulateObjectFromCursor(cursor);
        } catch (Exception ex) {
            return null;
        }
    }


    public String GetUserNPassword(String username, String password) {
        try {
            String studId = null;
            // database = getReadableDatabase();
            // Toast.makeText(grpContext,"From userDbHelper "+username+"        "+password,Toast.LENGTH_SHORT).show();
            //select UserId from " + TABLENAME + " where Username='" + username+ "' and Password='"+password+"'
            Cursor cursor = database.rawQuery("SELECT UserID FROM " + TABLENAME + " WHERE Username = ? AND Password = ?", new String[]{username, password});

            User user = new User();
            cursor.moveToFirst();
            user.UserID = cursor.getString(cursor.getColumnIndex("UserID"));
            cursor.close();
            database.close();
            //  Toast.makeText(grpContext,"userId->"+user.UserID,Toast.LENGTH_SHORT).show();


            //  return _PopulateObjectFromCursor(cursor);
            return user.UserID;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<User> GetAll() {
        try {
            // database = getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + "", null);
            database.close();
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            return null;
        }
    }

    private void _PopulateContentValues(User user) {
        // Toast.makeText(grpContext,"User is "+"user.deviceId"+user.DeviceID,Toast.LENGTH_SHORT).show();
        contentValues.put("UserID", Util.GetUniqueID().toString());
        contentValues.put("StudentID", user.StudentID);
        contentValues.put("Username", user.Username);
        contentValues.put("Password", user.Password);
        contentValues.put("UserTypeID", user.UserTypeID);
        contentValues.put("GroupID", user.GroupID);
        contentValues.put("DeviceID", user.DeviceID);
        contentValues.put("CreatedOn", Util.GetCurrentDateTime(false));
        contentValues.put("DeletedOn", "");
        contentValues.put("IsActive", user.IsActive);

        // Toast.makeText(grpContext,"User is "+contentValues.get("Username")+"password is"+contentValues.get("Password"),Toast.LENGTH_SHORT).show();
    }

    private User _PopulateObjectFromCursor(Cursor cursor) {
        try {
            User user = new User();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                user.UserID = cursor.getString(cursor.getColumnIndex("UserID"));
                user.StudentID = cursor.getString(cursor.getColumnIndex("StudentID"));
                user.Username = cursor.getString(cursor.getColumnIndex("Username"));
                user.Password = cursor.getString((cursor.getColumnIndex("Password")));
                user.UserTypeID = cursor.getInt((cursor.getColumnIndex("UserTypeID")));
                user.DeviceID = cursor.getInt((cursor.getColumnIndex("DeviceID")));
                user.GroupID = cursor.getString((cursor.getColumnIndex("GroupID")));
                user.CreatedOn = cursor.getString((cursor.getColumnIndex("CreatedOn")));
                user.DeletedOn = cursor.getString((cursor.getColumnIndex("DeletedOn")));
                user.IsActive = (cursor.getString((cursor.getColumnIndex("IsActive"))).equals("1")) ? 1 : 0;
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return user;
        } catch (Exception ex) {
            return null;
        }
    }

    private List<User> _PopulateListFromCursor(Cursor cursor) {
        try {
            List<User> userList = new ArrayList<User>();
            User user = new User();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                user.UserID = cursor.getString(cursor.getColumnIndex("UserID"));
                user.StudentID = cursor.getString(cursor.getColumnIndex("StudentID"));
                user.Username = cursor.getString(cursor.getColumnIndex("Username"));
                user.Password = cursor.getString((cursor.getColumnIndex("Password")));
                user.UserTypeID = cursor.getInt((cursor.getColumnIndex("UserTypeID")));
                user.DeviceID = cursor.getInt((cursor.getColumnIndex("DeviceID")));
                user.GroupID = cursor.getString((cursor.getColumnIndex("GroupID")));
                user.CreatedOn = cursor.getString((cursor.getColumnIndex("CreatedOn")));
                user.DeletedOn = cursor.getString((cursor.getColumnIndex("DeletedOn")));
                user.IsActive = (cursor.getString((cursor.getColumnIndex("IsActive"))).equals("1")) ? 1 : 0;
                userList.add(user);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return userList;
        } catch (Exception ex) {
            return null;
        }
    }

}