package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PEF on 05/06/2017.
 */

public class CrlDBHelper extends DBHelper {


    Cursor cursor;
    final String ERRORTABLENAME = "Logs";

    public CrlDBHelper(Context context) {
        super(context);
        // db = this.getWritableDatabase();
        database = getWritableDatabase();
        Util = new Utility();

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

    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        cursor = database.rawQuery("UPDATE CRL SET CRLID = IfNull(CRLID,'CRLID'), FirstName = IfNull(FirstName,'FirstName'), LastName = IfNull(LastName,'LastName'), UserName = IfNull(UserName,'UserName'), PassWord = IfNull(PassWord,'PassWord'), ProgramId = IfNull(ProgramId,'1'), Mobile = IfNull(Mobile,'0123456789'), State = IfNull(State,'State'), Email = IfNull(Email,'Email'), CreatedBy = IfNull(CreatedBy,''),NewFlag = IfNull(NewFlag,'0') ,sharedBy = IfNull(sharedBy,'') ,SharedAtDateTime = IfNull(SharedAtDateTime,'0') ,appVersion = IfNull(appVersion,'') ,appName = IfNull(appName,'') ,CreatedOn = IfNull(CreatedOn,'0') ", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }

    // Fetch data if already exists
    public String getCrlIDFromUsername(String uname) {

        try {
            database = getWritableDatabase();
            String crlid = new String();

            cursor = database.rawQuery("SELECT CRLID FROM CRL WHERE UserName=?", new String[]{uname});
            Crl crlObject = new Crl();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                crlObject.CRLId = cursor.getString(cursor.getColumnIndex("CRLID"));
                crlid = crlObject.CRLId;
                cursor.moveToNext();
            }
            cursor.close();
            database.close();

            return crlid;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getCrlIDFromUsername");
            return "ExceptionOccured";
        }
    }


    public void replaceData(Crl obj) {

        try {
            database = getWritableDatabase();

            contentValues.put("CRLID", obj.CRLId);
            contentValues.put("FirstName", obj.FirstName);
            contentValues.put("LastName", obj.LastName);
            contentValues.put("UserName", obj.UserName);
            contentValues.put("PassWord", obj.Password);
            contentValues.put("ProgramId", obj.ProgramId);
            contentValues.put("Mobile", obj.Mobile);
            contentValues.put("State", obj.State);
            contentValues.put("Email", obj.Email);
            contentValues.put("CreatedBy", obj.CreatedBy);
            contentValues.put("NewFlag", obj.newCrl);
            // new entries
            contentValues.put("sharedBy", obj.sharedBy == null ? "" : obj.sharedBy);
            contentValues.put("SharedAtDateTime", obj.SharedAtDateTime == null ? "" : obj.SharedAtDateTime);
            contentValues.put("appVersion", obj.appVersion == null ? "" : obj.appVersion);
            contentValues.put("appName", obj.appName == null ? "" : obj.appName);
            contentValues.put("CreatedOn", obj.CreatedOn == null ? "" : obj.CreatedOn);

            database.replace("CRL", null, contentValues);
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "replaceData");

        }
    }


    public boolean checkTableEmptyness() {

        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM CRL";
            Cursor mcursor = database.rawQuery(count, null);
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            _PopulateLogValues(ex, "checkTableEmptyness");
            return false;
        }

    }

    // Executed when Pull Data will be called
    public void updateJsonData(Crl obj) {

        try {
            database = getWritableDatabase();

            contentValues.put("CRLID", obj.CRLId);
            contentValues.put("FirstName", obj.FirstName);
            contentValues.put("LastName", obj.LastName);
            contentValues.put("UserName", obj.UserName);
            contentValues.put("Password", obj.Password);
            contentValues.put("ProgramId", obj.ProgramId);
            contentValues.put("Mobile", obj.Mobile);
            contentValues.put("State", obj.State);
            contentValues.put("Email", obj.Email);
            contentValues.put("NewFlag", obj.newCrl);

            // new entries// new entries
            contentValues.put("sharedBy", obj.sharedBy == null ? "" : obj.sharedBy);
            contentValues.put("SharedAtDateTime", obj.SharedAtDateTime == null ? "" : obj.SharedAtDateTime);
            contentValues.put("appVersion", obj.appVersion == null ? "" : obj.appVersion);
            contentValues.put("appName", obj.appName == null ? "" : obj.appName);
            contentValues.put("CreatedOn", obj.CreatedOn == null ? "" : obj.CreatedOn);

            database.replace("CRL", null, contentValues);
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "updateJsonData");

        }
    }


    public void insertData(Crl obj) {

        try {
            database = getWritableDatabase();

            contentValues.put("CRLID", obj.CRLId);
            contentValues.put("FirstName", obj.FirstName);
            contentValues.put("LastName", obj.LastName);
            contentValues.put("UserName", obj.UserName);
            contentValues.put("PassWord", obj.Password);
            contentValues.put("ProgramId", obj.ProgramId);
            contentValues.put("Mobile", obj.Mobile);
            contentValues.put("State", obj.State);
            contentValues.put("Email", obj.Email);
            contentValues.put("CreatedBy", obj.CreatedBy);
            contentValues.put("NewFlag", obj.newCrl);
            contentValues.put("CreatedOn", obj.CreatedOn == null ? "" : obj.CreatedOn);

            database.insert("CRL", null, contentValues);
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "insertData");
        }
    }


    public boolean GetCrlUserName(String username) {

        try {
            database = getWritableDatabase();

            cursor = database.rawQuery("SELECT UserName FROM CRL WHERE UserName = ? ", new String[]{String.valueOf(username)});
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
            _PopulateLogValues(ex, "GetCrlUserName");
            return false;
        }
    }

    public boolean CrlLogin(String uname, String pass) {

        try {
            database = getWritableDatabase();

            cursor = database.rawQuery("SELECT * FROM CRL WHERE UserName=? AND PassWord =?", new String[]{uname, pass});
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
            _PopulateLogValues(ex, "CrlLogin");
            return false;
        }
    }


    public String getCrlID(String uname, String pass) {

        try {
            database = getWritableDatabase();
            String crlid = new String();

            cursor = database.rawQuery("SELECT CRLID FROM CRL WHERE UserName=? AND PassWord =?", new String[]{uname, pass});
            Crl crlObject = new Crl();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                crlObject.CRLId = cursor.getString(cursor.getColumnIndex("CRLID"));
                crlid = crlObject.CRLId;
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return crlid;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getCrlID");
            return "ExceptionOccured";
        }
    }


    public List<Crl> GetAll() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from CRL ", null);
            database.close();
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAll");
            return null;
        }
    }


    public List<Crl> GetAllNewCrl() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from CRL where NewFlag = 1", null);
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAllNewCrl");
            return null;
        }
    }

    private List<Crl> _PopulateListFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            List<Crl> crl_list = new ArrayList<Crl>();
            Crl crlObject;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                crlObject = new Crl();

                crlObject.CRLId = cursor.getString(cursor.getColumnIndex("CRLID"));
                crlObject.FirstName = cursor.getString(cursor.getColumnIndex("FirstName"));
                crlObject.LastName = cursor.getString(cursor.getColumnIndex("LastName"));
                crlObject.UserName = cursor.getString((cursor.getColumnIndex("UserName")));
                crlObject.Password = cursor.getString((cursor.getColumnIndex("PassWord")));
                crlObject.State = cursor.getString((cursor.getColumnIndex("State")));
                crlObject.ProgramId = cursor.getInt(cursor.getColumnIndex("ProgramId"));
                crlObject.Mobile = cursor.getString((cursor.getColumnIndex("Mobile")));
                crlObject.State = cursor.getString((cursor.getColumnIndex("State")));
                crlObject.Email = cursor.getString((cursor.getColumnIndex("Email")));
                crlObject.CreatedBy = cursor.getString((cursor.getColumnIndex("CreatedBy")));
                crlObject.newCrl = Boolean.valueOf(cursor.getString((cursor.getColumnIndex("NewFlag"))));
                // new entries
                crlObject.sharedBy = cursor.getString(cursor.getColumnIndex("sharedBy"));
                crlObject.SharedAtDateTime = cursor.getString(cursor.getColumnIndex("SharedAtDateTime"));
                crlObject.appVersion = cursor.getString(cursor.getColumnIndex("appVersion"));
                crlObject.appName = cursor.getString(cursor.getColumnIndex("appName"));
                crlObject.CreatedOn = cursor.getString(cursor.getColumnIndex("CreatedOn"));

                crl_list.add(crlObject);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return crl_list;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "_PopulateListFromCursor");
            return null;
        }
    }

    public void UpdateCrl(String fetchedCrlID, String username) {

    }

}
