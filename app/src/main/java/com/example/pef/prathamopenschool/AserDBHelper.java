package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ameya on 19-Jun-17.
 */

public class AserDBHelper extends DBHelper {


    Cursor cursor;
    final String ERRORTABLENAME = "Logs";

    public AserDBHelper(Context context) {
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

        contentValues.put("LogDetail", "AserLog");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }


    // Check Data entry in Aser DB
    public boolean CheckChildIDExists(String uniqStdID, String ChildID, String GroupID) {
        try {
            database = getWritableDatabase();

            cursor = database.rawQuery("SELECT * FROM Aser WHERE StudentId != ? AND ChildID=? AND GroupID =?", new String[]{uniqStdID, ChildID, GroupID});
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
            _PopulateLogValues(ex, "CheckChildIDExists");
            return false;
        }
    }


    public int GetBaselineCount(String GroupID) {
        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM Aser where GroupID = ? AND TestType = 0 ";
            Cursor mcursor = database.rawQuery(count, new String[]{GroupID});
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                return icount;
            } else {
                return icount;
            }
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetBaselineCount");
            return 0;
        }
    }

    public int GetEndline1Count(String GroupID) {

        try {
            String count = "SELECT count(*) FROM Aser where GroupID = ? AND TestType = 1 ";
            database = getReadableDatabase();
            Cursor mcursor = database.rawQuery(count, new String[]{GroupID});
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            mcursor.close();
            if (icount > 0) {
                return icount;
            } else {
                return icount;
            }
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetEndline1Count");
            return 0;
        }
    }

    public int GetEndline2Count(String GroupID) {

        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM Aser where GroupID = ? AND TestType = 2 ";
            Cursor mcursor = database.rawQuery(count, new String[]{GroupID});
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                return icount;
            } else {
                return icount;
            }
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetEndline2Count");
            return 0;
        }
    }

    public int GetEndline3Count(String GroupID) {

        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM Aser where GroupID = ? AND TestType = 3 ";
            Cursor mcursor = database.rawQuery(count, new String[]{GroupID});
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                return icount;
            } else {
                return icount;
            }
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetEndline3Count");
            return 0;
        }
    }

    public int GetEndline4Count(String GroupID) {

        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM Aser where GroupID = ? AND TestType = 4 ";
            Cursor mcursor = database.rawQuery(count, new String[]{GroupID});
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                return icount;
            } else {
                return icount;
            }
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetEndline4Count");
            return 0;
        }
    }


    // Check Data entry in Aser DB
    public boolean CheckDataExists(String StudentId, int TestType) {

        try {

            database = getWritableDatabase();

            cursor = database.rawQuery("SELECT * FROM Aser WHERE StudentId=? AND TestType =?", new String[]{StudentId, String.valueOf(TestType)});
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
            _PopulateLogValues(ex, "CheckDataExists");
            return false;
        }
    }

    // set Flag to false
    public void UpdateAserData(String ChildID, String testDate, int lang, int num, int oad, int osb, int oml, int odv, int wad, int wsb, String crtby, String crtdt, int isSelected, String studentID, int TstType) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("update Aser set ChildID='" + ChildID + "', TestDate='" + testDate + "', Lang = " + lang + ", Num = " + num + ", OAdd = " + oad + ", OSub = " + osb + ", OMul = " + oml + ", ODiv = " + odv + ", WAdd = " + wad + ", WSub = " + wsb + ", CreatedBy = '" + crtby + "', CreatedDate = '" + crtdt + "', FLAG =" + isSelected + "  WHERE StudentId='" + studentID + "' AND TestType =" + TstType + "", null);
            cursor.moveToFirst();
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "UpdateAserData");
        }
    }


    // set Flag to false
    public void UpdateReceivedAserData(String ChildID, String testDate, int lang, int num, int oad, int osb, int oml,
                                       int odv, int wad, int wsb, String crtby, String crtdt, int isSelected,
                                       String sharedBy, String SharedAtDateTime, String appVersion, String appName, String CreatedOn,
                                       String studentID, int TstType) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("update Aser set ChildID='" + ChildID + "', TestDate='" + testDate +
                    "', Lang = " + lang + ", Num = " + num + ", OAdd = " + oad + ", OSub = " + osb
                    + ", OMul = " + oml + ", ODiv = " + odv + ", WAdd = " + wad + ", WSub = " + wsb
                    + ", CreatedBy = '" + crtby + "', CreatedDate = '" + crtdt + "', FLAG =" + isSelected
                    + ", sharedBy = '" + sharedBy + "', SharedAtDateTime = '" + SharedAtDateTime + "', appVersion =" + appVersion
                    + ", appName = '" + appName + "', CreatedOn = '" + CreatedOn
                    + "  WHERE StudentId='" + studentID + "' AND TestType =" + TstType + "", null);
            cursor.moveToFirst();
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "UpdateAserData");
        }
    }


    // Getting Aser Data based on Unique Student ID passed by universal child form
    public List<Aser> GetAllByStudentID(String StudentID, int testV) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from Aser where StudentId = ? AND TestType = ?", new String[]{StudentID, String.valueOf(testV)});

            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAllByStudentID");
            return null;
        }
    }

    public boolean checkTableEmptyness() {
        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM ASER";
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


    public void replaceData(Aser obj) {
        try {

            database = getWritableDatabase();

            contentValues.put("StudentId", obj.StudentId);
            contentValues.put("TestType", obj.TestType);
            contentValues.put("TestDate", obj.TestDate);
            contentValues.put("Lang", obj.Lang);
            contentValues.put("Num", obj.Num);
            contentValues.put("OAdd", obj.OAdd);
            contentValues.put("OSub", obj.OSub);
            contentValues.put("OMul", obj.OMul);
            contentValues.put("ODiv", obj.ODiv);
            contentValues.put("WAdd", obj.WAdd);
            contentValues.put("WSub", obj.WSub);
            contentValues.put("CreatedBy", obj.CreatedBy);
            contentValues.put("CreatedDate", obj.CreatedDate);
            contentValues.put("DeviceId", obj.DeviceId);
            contentValues.put("FLAG", obj.FLAG);

            database.replace("Aser", null, contentValues);
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "replaceData");

        }
    }

    public void insertData(Aser obj) {

        try {
            database = getWritableDatabase();

            contentValues.put("StudentId", obj.StudentId);
            contentValues.put("GroupID", obj.GroupID);
            contentValues.put("ChildID", obj.ChildID);
            contentValues.put("TestType", obj.TestType);
            contentValues.put("TestDate", obj.TestDate);
            contentValues.put("Lang", obj.Lang);
            contentValues.put("Num", obj.Num);
            contentValues.put("OAdd", obj.OAdd);
            contentValues.put("OSub", obj.OSub);
            contentValues.put("OMul", obj.OMul);
            contentValues.put("ODiv", obj.ODiv);
            contentValues.put("WAdd", obj.WAdd);
            contentValues.put("WSub", obj.WSub);
            contentValues.put("CreatedBy", obj.CreatedBy);
            contentValues.put("CreatedDate", obj.CreatedDate);
            contentValues.put("DeviceId", obj.DeviceId);
            contentValues.put("FLAG", obj.FLAG);
            // new entries
            contentValues.put("sharedBy", obj.sharedBy == null  ? "" : obj.sharedBy);
            contentValues.put("SharedAtDateTime", obj.SharedAtDateTime == null ? "" : obj.SharedAtDateTime);
            contentValues.put("appVersion", obj.appVersion == null ? "" : obj.appVersion);
            contentValues.put("appName", obj.appName == null ? "" : obj.appName);
            contentValues.put("CreatedOn", obj.CreatedOn == null ? "" : obj.CreatedOn);

            database.insert("Aser", null, contentValues);
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "insertData");

        }
    }


    public List<Aser> GetAll() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from Aser", null);

            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAll");
            return null;
        }
    }

    public List<Aser> GetAllNewAserGroups() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from Aser", null);

            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAllNewAserGroups");
            return null;
        }
    }

    private List<Aser> _PopulateListFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            List<Aser> aser_list = new ArrayList<Aser>();
            Aser aserObject;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                aserObject = new Aser();

                aserObject.StudentId = cursor.getString(cursor.getColumnIndex("StudentId"));
                aserObject.ChildID = cursor.getString(cursor.getColumnIndex("ChildID"));
                aserObject.GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
                aserObject.TestType = cursor.getInt(cursor.getColumnIndex("TestType"));
                aserObject.TestDate = cursor.getString(cursor.getColumnIndex("TestDate"));
                aserObject.Lang = cursor.getInt((cursor.getColumnIndex("Lang")));
                aserObject.Num = cursor.getInt((cursor.getColumnIndex("Num")));
                aserObject.OAdd = cursor.getInt((cursor.getColumnIndex("OAdd")));
                aserObject.OSub = cursor.getInt((cursor.getColumnIndex("OSub")));
                //Boolean.valueOf(cursor.getString((cursor.getColumnIndex("OSub"))));
                aserObject.OMul = cursor.getInt((cursor.getColumnIndex("OMul")));
                aserObject.ODiv = cursor.getInt((cursor.getColumnIndex("ODiv")));
                aserObject.WAdd = cursor.getInt((cursor.getColumnIndex("WAdd")));
                aserObject.WSub = cursor.getInt((cursor.getColumnIndex("WSub")));
                aserObject.CreatedBy = cursor.getString((cursor.getColumnIndex("CreatedBy")));
                aserObject.CreatedDate = cursor.getString((cursor.getColumnIndex("CreatedDate")));
                aserObject.DeviceId = cursor.getString((cursor.getColumnIndex("DeviceId")));
                aserObject.FLAG = cursor.getInt((cursor.getColumnIndex("FLAG")));
                // new entries
                aserObject.sharedBy = cursor.getString(cursor.getColumnIndex("sharedBy"));
                aserObject.SharedAtDateTime = cursor.getString(cursor.getColumnIndex("SharedAtDateTime"));
                aserObject.appVersion = cursor.getString(cursor.getColumnIndex("appVersion"));
                aserObject.appName = cursor.getString(cursor.getColumnIndex("appName"));
                aserObject.CreatedOn = cursor.getString(cursor.getColumnIndex("CreatedOn"));

                aser_list.add(aserObject);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return aser_list;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "populateListFromCursor");
            return null;
        }
    }


    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        cursor = database.rawQuery("UPDATE Aser SET StudentId = IfNull(StudentId,'StudentId'), ChildID = IfNull(ChildID,'ChildID'), TestType = IfNull(TestType,'0'), TestDate = IfNull(TestDate,'0'), Lang = IfNull(Lang,'0'), Num = IfNull(Num,'0'), OAdd = IfNull(OAdd,'0'), OSub = IfNull(OSub,'0'), OMul = IfNull(OMul,'0'), ODiv = IfNull(ODiv,'0'), WAdd= IfNull(WAdd,'0'), WSub= IfNull(WSub,'0'), CreatedBy= IfNull(CreatedBy,'0'), CreatedDate= IfNull(CreatedDate,'0'), DeviceId= IfNull(DeviceId,'0'), FLAG= IfNull(FLAG,'0'), GroupID= IfNull(GroupID,'0') ,sharedBy = IfNull(sharedBy,'') ,SharedAtDateTime = IfNull(SharedAtDateTime,'0') ,appVersion = IfNull(appVersion,'') ,appName = IfNull(appName,'') ,CreatedOn = IfNull(CreatedOn,'0') ", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }
}
