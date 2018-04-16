package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class GroupDBHelper extends DBHelper {
    final String TABLENAME = "Groups";
    final String ERRORTABLENAME = "Logs";
    Context c;

    public GroupDBHelper(Context context) {
        super(context);
        c = context;
        database = getWritableDatabase();
        Util = new Utility();

    }

    private void _PopulateLogValues(Exception ex, String method) {

        database = getWritableDatabase();

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

        contentValues.put("LogDetail", "GroupLog");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }

    // Populate Groups for RI Case
    public List<GroupList> GetAllRIGroups(List<VillageList> villageId) {
        try {

            database = getWritableDatabase();
            List<GroupList> list = new ArrayList<GroupList>();

            for (int i = 0; i < villageId.size(); i++) {
                int villID = villageId.get(i).villageId;
                {
                    Cursor cursor = database.rawQuery("SELECT GroupID,GroupName FROM " + TABLENAME + " WHERE VillageID =" + villID + " ORDER BY GroupName ASC", null);//Ketan
                    cursor.moveToFirst();
                    while (cursor.isAfterLast() == false) {

                        list.add(new GroupList(cursor.getString(cursor.getColumnIndex("GroupID")), cursor.getString(cursor.getColumnIndex("GroupName"))));

                        cursor.moveToNext();
                    }


                }

            }
            database.close();
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAllRIGroups");
            return null;
        }
    }


    // set Flag to false
    public void SetFlagFalse(String GroupID) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("update Groups set NewFlag=false where GroupID = ? ", new String[]{GroupID});
            cursor.moveToFirst();
            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "SetFlagFalse");
        }
    }

    public String getGroupId(String grpName) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select GroupID from Groups where GroupName = ?", new String[]{grpName});
            cursor.moveToFirst();
            database.close();
            return cursor.getString(0);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "getGroupId");
            return "ExceptionOccured";
        }
    }

    public void replaceData(Group obj) {

        try {
            database = getWritableDatabase();

            contentValues.put("GroupID", obj.GroupID);
            contentValues.put("GroupCode", obj.GroupCode);
            contentValues.put("GroupName", obj.GroupName);
            contentValues.put("UnitNumber", obj.UnitNumber);
            contentValues.put("DeviceID", obj.DeviceID);
            contentValues.put("Responsible", obj.Responsible);
            contentValues.put("ResponsibleMobile", obj.ResponsibleMobile);
            contentValues.put("VillageID", obj.VillageID);
            contentValues.put("ProgramId", obj.ProgramID);
            contentValues.put("CreatedBy", obj.CreatedBy);
            contentValues.put("NewFlag", obj.newGroup);
            contentValues.put("VillageName", obj.VillageName);
            contentValues.put("SchoolName", obj.SchoolName);
            // new entries
            contentValues.put("sharedBy", obj.sharedBy == null ? "" : obj.sharedBy);
            contentValues.put("SharedAtDateTime", obj.SharedAtDateTime == null ? "" : obj.SharedAtDateTime);
            contentValues.put("appVersion", obj.appVersion == null ? "" : obj.appVersion);
            contentValues.put("appName", obj.appName == null ? "" : obj.appName);
            contentValues.put("CreatedOn", obj.CreatedOn == null ? "" : obj.CreatedOn);

            database.replace("Groups", null, contentValues);

            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "replaceData");

        }

    }

    public void insertData(Group obj) {
        try {

            database = getWritableDatabase();

            contentValues.put("GroupID", obj.GroupID);
            contentValues.put("GroupCode", obj.GroupCode);
            contentValues.put("GroupName", obj.GroupName);
            contentValues.put("UnitNumber", obj.UnitNumber);
            contentValues.put("DeviceID", obj.DeviceID);
            contentValues.put("Responsible", obj.Responsible);
            contentValues.put("ResponsibleMobile", obj.ResponsibleMobile);
            contentValues.put("VillageID", obj.VillageID);
            contentValues.put("ProgramId", obj.ProgramID);
            contentValues.put("CreatedBy", obj.CreatedBy);
            contentValues.put("NewFlag", obj.newGroup);
            contentValues.put("VillageName", obj.VillageName);
            contentValues.put("SchoolName", obj.SchoolName);
            contentValues.put("CreatedOn", obj.CreatedOn);


            database.insert("Groups", null, contentValues);

            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "insertData");

        }

    }


    // MHM 21 June
    public List<GroupList> GetUnitwiseSchoolNVillage(String selectedUnit) {
        try {
            database = getWritableDatabase();
            List<GroupList> list = new ArrayList<GroupList>();
            {
                //Cursor cursor = database.rawQuery("SELECT GroupID,GroupName FROM " + TABLENAME + " WHERE VillageID = ? and NewFlag ORDER BY GroupName ASC", new String[]{String.valueOf(villageId)});
                Cursor cursor = database.rawQuery("SELECT VillageName,SchoolName,CreatedBy FROM Groups WHERE GroupID = ?", new String[]{selectedUnit});//Ketan
                //list.add(new GroupList("0", "--Select Group--"));
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {

                    list.add(new GroupList(cursor.getString(cursor.getColumnIndex("VillageName")), cursor.getString(cursor.getColumnIndex("SchoolName"))));

                    cursor.moveToNext();
                }
                database.close();

            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetUnitwiseSchoolNVillage");
            return null;
        }
    }


    public boolean Add(Group group, SQLiteDatabase database1) {
        try {
            _PopulateContentValues(group);
            long resultCount = database1.insert(TABLENAME, null, contentValues);
            database1.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Add");
            return false;
        }
    }

    public boolean Update(Group group) {
        try {
            database = getWritableDatabase();
            _PopulateContentValues(group);
            long resultCount = database.update(TABLENAME, contentValues, "GroupID = ?", new String[]{((String) group.GroupID).toString()});
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Update");
            return false;
        }
    }

    public boolean Delete(String groupID) {
        try {
            database = getWritableDatabase();
            long resultCount = database.delete(TABLENAME, "GroupID = ?", new String[]{groupID.toString()});
            database.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean DeleteAll() {
        try {
            database = getWritableDatabase();
            long resultCount = database.delete(TABLENAME, null, null);
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "DeleteAll");
            return false;
        }
    }

    public Group Get(String groupID) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + " where GroupID='" + groupID + "'", null);
            return _PopulateObjectFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Get");
            return null;
        }
    }

    public String getGroupById(String id) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select GroupName from " + TABLENAME + " where GroupID='" + id + "'", null);
            cursor.moveToFirst();
            database.close();
            return cursor.getString(0);
        } catch (Exception ex) {
//            _PopulateLogValues(ex, "getGroupById");
            return "ExceptionOccured";
        }
    }

    //Pravin
    public String getGroupIdByName(String name) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select GroupID from " + TABLENAME + " where GroupName='" + name + "'", null);
            cursor.moveToFirst();
            database.close();
            return cursor.getString(0);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<Group> GetAll() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + "", null);
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAll");
            return null;
        }
    }

    public List<Group> GetAllNewGroups() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from Groups where NewFlag = 1", null);
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAllNewGroups");
            return null;
        }
    }


    public List<GroupList> GetGroups(int villageId) {
        try {
            database = getWritableDatabase();
            List<GroupList> list = new ArrayList<GroupList>();
            if (villageId == 0) {
                list.add(new GroupList("0", "--Select Group--"));
            } else {
                //Cursor cursor = database.rawQuery("SELECT GroupID,GroupName FROM " + TABLENAME + " WHERE VillageID = ? and NewFlag ORDER BY GroupName ASC", new String[]{String.valueOf(villageId)});
                Cursor cursor = database.rawQuery("SELECT GroupID,GroupName FROM " + TABLENAME + " WHERE VillageID =" + villageId + " ORDER BY GroupName ASC", null);//Ketan
                list.add(new GroupList("0", "--Select Group--"));
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {

                    list.add(new GroupList(cursor.getString(cursor.getColumnIndex("GroupID")), cursor.getString(cursor.getColumnIndex("GroupName"))));

                    cursor.moveToNext();
                }
                database.close();

            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetGroups");
            return null;
        }
    }

    private void _PopulateContentValues(Group group) {
        try {
            contentValues.put("GroupID", group.GroupID);
            contentValues.put("GroupName", group.GroupName);
            contentValues.put("UnitNumber", group.UnitNumber);
            contentValues.put("DeviceID", group.DeviceID);
            contentValues.put("Responsible", group.Responsible);
            contentValues.put("ResponsibleMobile", group.ResponsibleMobile);
            contentValues.put("VillageID", group.VillageID);
            contentValues.put("GroupCode", group.GroupCode);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "_PopulateContentValues");
        }
    }

    private Group _PopulateObjectFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            Group group = new Group();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                group.GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
                group.GroupName = cursor.getString(cursor.getColumnIndex("GroupName"));
                group.UnitNumber = cursor.getString((cursor.getColumnIndex("UnitNumber")));
                group.DeviceID = cursor.getString((cursor.getColumnIndex("DeviceID")));
                group.VillageID = cursor.getInt((cursor.getColumnIndex("VillageID")));
                group.Responsible = cursor.getString((cursor.getColumnIndex("Responsible")));
                group.ResponsibleMobile = cursor.getString(cursor.getColumnIndex("ResponsibleMobile"));
                group.GroupCode = cursor.getString((cursor.getColumnIndex("GroupCode")));
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return group;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "_PopulateObjectFromCursor");
            return null;
        }
    }

    private List<Group> _PopulateListFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            List<Group> groups = new ArrayList<Group>();
            Group group;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                group = new Group();

                group.GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
                group.GroupName = cursor.getString(cursor.getColumnIndex("GroupName"));
                group.UnitNumber = cursor.getString((cursor.getColumnIndex("UnitNumber")));
                group.DeviceID = cursor.getString((cursor.getColumnIndex("DeviceID")));
                group.VillageID = cursor.getInt((cursor.getColumnIndex("VillageID")));
                group.GroupCode = cursor.getString((cursor.getColumnIndex("GroupCode")));
                group.ResponsibleMobile = cursor.getString(cursor.getColumnIndex("ResponsibleMobile"));
                group.Responsible = cursor.getString((cursor.getColumnIndex("Responsible")));
                group.ProgramID = cursor.getInt((cursor.getColumnIndex("ProgramId")));
                group.CreatedBy = cursor.getString((cursor.getColumnIndex("CreatedBy")));
                group.SchoolName = cursor.getString((cursor.getColumnIndex("SchoolName")));
                group.VillageName = cursor.getString((cursor.getColumnIndex("VillageName")));
                group.newGroup = Boolean.valueOf(cursor.getString((cursor.getColumnIndex("NewFlag"))));
                // new entries
                group.sharedBy = cursor.getString(cursor.getColumnIndex("sharedBy"));
                group.SharedAtDateTime = cursor.getString(cursor.getColumnIndex("SharedAtDateTime"));
                group.appVersion = cursor.getString(cursor.getColumnIndex("appVersion"));
                group.appName = cursor.getString(cursor.getColumnIndex("appName"));
                group.CreatedOn = cursor.getString(cursor.getColumnIndex("CreatedOn"));

                groups.add(group);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return groups;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "_PopulateListFromCursor");
            return null;
        }
    }

    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        Cursor cursor = database.rawQuery("UPDATE Groups SET GroupID = IfNull(GroupID,'0'), GroupName = IfNull(GroupName,'0'), DeviceID = IfNull(DeviceID,'0'), VillageID = IfNull(VillageID,'0'), programId = IfNull(ProgramId,'0'), CreatedBy = IfNull(CreatedBy,'0'), NewFlag = IfNull(NewFlag,'0'), VillageName = IfNull(VillageName,'0'), SchoolName = IfNull(SchoolName,'0') ,sharedBy = IfNull(sharedBy,'') ,SharedAtDateTime = IfNull(SharedAtDateTime,'0') ,appVersion = IfNull(appVersion,'') ,appName = IfNull(appName,'') ,CreatedOn = IfNull(CreatedOn,'0') ", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }
}