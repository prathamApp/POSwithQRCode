package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Varun Anand on 8/28/2015.
 */
public class VillageDBHelper extends DBHelper {
    final String TABLENAME = "Village";
    final String ERRORTABLENAME = "Logs";
    Context c;


    public VillageDBHelper(Context context) {
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

        contentValues.put("LogDetail", "VillageLog");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }


    public int GetVillageIDByBlock(String BlockName) {
        int val = 0;
        try {
            database = getWritableDatabase();

            Cursor cursor = database.rawQuery("select VillageID from Village where Block=?", new String[]{BlockName});

            if (cursor.moveToFirst()) // data?
                val = Integer.parseInt(cursor.getString(cursor.getColumnIndex("VillageID")));
            database.close();
            return val;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetVillageIDByBlock");
        }

        return val;
    }


    public List<VillageList> GetRIVillageIDByBlock(String BlockName) {
        try {
            database = getWritableDatabase();
            List<VillageList> list = new ArrayList<VillageList>();
            {
                Cursor cursor = database.rawQuery("select VillageID from Village where Block ='" + BlockName + "'", null);

                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {

                    list.add(new VillageList(cursor.getInt(cursor.getColumnIndex("VillageID"))));

                    cursor.moveToNext();
                }
                database.close();

            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetRIVillageIDByBlock");
            return null;
        }
    }


    public boolean checkTableEmptyness() {

        try {
            database = getReadableDatabase();

            String count = "SELECT count(*) FROM Village";
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
    public void updateJsonData(Village villageobj) {

        try {
            database = getWritableDatabase();

            contentValues.put("VillageID", villageobj.VillageID);
            contentValues.put("VillageCode", villageobj.VillageCode);
            contentValues.put("VillageName", villageobj.VillageName);
            contentValues.put("Block", villageobj.Block);
            contentValues.put("District", villageobj.District);
            contentValues.put("State", villageobj.State);
            contentValues.put("CRLID", villageobj.CRLID);

            database.replace("Village", null, contentValues);

            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "updateJsonData");
        }
    }

    public void insertData(Village villageobj) {
        try {

            database = getWritableDatabase();

            contentValues.put("VillageID", villageobj.VillageID);
            contentValues.put("VillageCode", villageobj.VillageCode);
            contentValues.put("VillageName", villageobj.VillageName);
            contentValues.put("Block", villageobj.Block);
            contentValues.put("District", villageobj.District);
            contentValues.put("State", villageobj.State);
            contentValues.put("CRLID", villageobj.CRLID);

            database.insert("Village", null, contentValues);//pravin edited

            database.close();
        } catch (Exception ex) {
            _PopulateLogValues(ex, "insertData");
        }
    }


    public int GetVillageID(String villageName) {
        int val = 0;
        try {
            database = getWritableDatabase();

            Cursor cursor = database.rawQuery("select VillageID from Village where VillageName=?", new String[]{villageName});

            if (cursor.moveToFirst()) // data?
                val = Integer.parseInt(cursor.getString(cursor.getColumnIndex("VillageID")));
            database.close();
            return val;

        } catch (Exception e) {
        }

        return val;
    }

    public boolean Add(Village village, SQLiteDatabase database1) {
        try {

            _PopulateContentValues(village);

            long resultCount = database1.insert(TABLENAME, null, contentValues);
            database1.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Add");
            return false;
        }
    }

    public boolean Update(Village village) {
        try {

            database = getWritableDatabase();
            _PopulateContentValues(village);

            long resultCount = database.update(TABLENAME, contentValues, "VillageID = ?", new String[]{((Integer) village.VillageID).toString()});
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Update");
            return false;
        }
    }

    public boolean Delete(Integer villageID) {
        try {
            database = getWritableDatabase();
            long resultCount = database.delete(TABLENAME, "VillageID = ?", new String[]{villageID.toString()});
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

    public Village Get(int villageID) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + " where VillageID='" + villageID + "'", null);
            return _PopulateObjectFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Get");
            return null;
        }
    }

    public List<Village> GetAll() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + "", null);
            database.close();
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAll");
            return null;
        }
    }

    public List<String> GetDistrict(String state) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT DISTINCT District FROM " + TABLENAME + " WHERE State = ? ORDER BY District ASC ", new String[]{state});

            List<String> list = new ArrayList<String>();
            list.add("--Select District--");
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                list.add(cursor.getString(cursor.getColumnIndex("District")));
                cursor.moveToNext();

            }
            database.close();
            return list;
        } catch (Exception ex) {
            ex.getStackTrace();

            return null;
        }
    }

    public List<String> GetState() {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT DISTINCT State FROM " + TABLENAME + " ORDER BY State ASC", null);

            List<String> list = new ArrayList<String>();
            list.add("--Select State--");
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                list.add(cursor.getString(cursor.getColumnIndex("State")));
                cursor.moveToNext();

            }
            cursor.close();
            database.close();
            return list;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetState");
            return null;
        }
    }

    public List<String> GetStatewiseBlock(String state) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT DISTINCT Block FROM Village WHERE State = ? ORDER BY Block ASC", new String[]{state});

            List<String> list = new ArrayList<String>();
            list.add("--Select Block--");
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                list.add(cursor.getString(cursor.getColumnIndex("Block")));
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return list;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetStateWiseBlock");
            return null;
        }
    }

    public List<String> GetBlock(String district) {
        try {
            database = getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT DISTINCT Block FROM " + TABLENAME + " WHERE District = ? ORDER BY Block ASC", new String[]{district});

            List<String> list = new ArrayList<String>();
            list.add("--Select Block--");
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                list.add(cursor.getString(cursor.getColumnIndex("Block")));
                cursor.moveToNext();
            }
            database.close();
            return list;
        } catch (Exception ex) {
            ex.getStackTrace();

            return null;
        }
    }

    public List<VillageList> Getvillages() {
        try {
            database = getWritableDatabase();
            List<VillageList> list = new ArrayList<VillageList>();

            Cursor cursor = database.rawQuery("SELECT VillageID, VillageName FROM " + TABLENAME + " ORDER BY VillageName ASC", null);
            list.add(new VillageList(0, "--Select Village--"));
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                list.add(new VillageList(cursor.getInt(cursor.getColumnIndex("VillageID")), cursor.getString(cursor.getColumnIndex("VillageName"))));

                cursor.moveToNext();
            }
            cursor.close();
            database.close();

            return list;

        } catch (Exception ex) {
            ex.getStackTrace();
            return null;
        }
    }

    // Block not present
    public List<VillageList> GetVillagesByGroup(String block) {
        try {
            database = getWritableDatabase();
            List<VillageList> list = new ArrayList<VillageList>();
            if (block.equals("--Select Village--")) {
                list.add(new VillageList(0, "--Select Village--"));
            } else {
                Cursor cursor = database.rawQuery("SELECT VillageID,VillageName FROM Groups WHERE Block = ? ORDER BY VillageName ASC", new String[]{block});
                list.add(new VillageList(0, "--Select Village--"));
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    list.add(new VillageList(cursor.getInt(cursor.getColumnIndex("VillageID")), cursor.getString(cursor.getColumnIndex("VillageName"))));

                    cursor.moveToNext();
                }
                cursor.close();
                database.close();
            }
            return list;

        } catch (Exception ex) {
            ex.getStackTrace();
            return null;
        }
    }


    public List<VillageList> GetVillages(String block) {
        try {
            database = getWritableDatabase();
            List<VillageList> list = new ArrayList<VillageList>();
            if (block.equals("--Select Village--")) {
                list.add(new VillageList(0, "--Select Village--"));
            } else {
                Cursor cursor = database.rawQuery("SELECT VillageID,VillageName FROM " + TABLENAME + " WHERE Block = ? ORDER BY VillageName ASC", new String[]{block});
                list.add(new VillageList(0, "--Select Village--"));
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    list.add(new VillageList(cursor.getInt(cursor.getColumnIndex("VillageID")), cursor.getString(cursor.getColumnIndex("VillageName"))));

                    cursor.moveToNext();
                }
                cursor.close();
                database.close();
            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetVillages");
            return null;
        }
    }


    private void _PopulateContentValues(Village village) {
        contentValues.put("VillageID", village.VillageID);
        contentValues.put("VillageCode", village.VillageCode);
        contentValues.put("VillageName", village.VillageName);
        contentValues.put("Block", village.Block);
        contentValues.put("District", village.District);
        contentValues.put("State", village.State);
        contentValues.put("CRLID", village.CRLID);
    }

    private Village _PopulateObjectFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            Village village = new Village();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                village.VillageID = cursor.getInt(cursor.getColumnIndex("VillageID"));
                village.VillageCode = cursor.getString(cursor.getColumnIndex("VillageCode"));
                village.VillageName = cursor.getString(cursor.getColumnIndex("VillageName"));
                village.Block = cursor.getString((cursor.getColumnIndex("Block")));
                village.District = cursor.getString((cursor.getColumnIndex("District")));
                village.State = cursor.getString((cursor.getColumnIndex("State")));
                village.CRLID = cursor.getString(cursor.getColumnIndex("CRLID"));
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return village;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "populateContentValues");
            return null;
        }
    }

    private List<Village> _PopulateListFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            List<Village> _villages = new ArrayList<Village>();
            Village village = new Village();
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                village.VillageID = cursor.getInt(cursor.getColumnIndex("VillageID"));
                village.VillageCode = cursor.getString(cursor.getColumnIndex("VillageCode"));
                village.VillageName = cursor.getString(cursor.getColumnIndex("VillageName"));
                village.Block = cursor.getString((cursor.getColumnIndex("Block")));
                village.District = cursor.getString((cursor.getColumnIndex("District")));
                village.State = cursor.getString((cursor.getColumnIndex("State")));
                village.CRLID = cursor.getString(cursor.getColumnIndex("CRLID"));

                _villages.add(village);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return _villages;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "populateListFromCursor");
            return null;
        }
    }

    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        Cursor cursor = database.rawQuery("UPDATE Village SET VillageID = IfNull(VillageID,'VillageID'), VillageCode = IfNull(VillageCode,'0'), VillageName = IfNull(VillageName,'0'), Block = IfNull(Block,'0'), District = IfNull(District,'0'), State = IfNull(State,'0'), CRLID = IfNull(CRLID,'0')", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }
}
