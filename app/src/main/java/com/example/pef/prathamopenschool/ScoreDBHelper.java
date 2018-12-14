package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class ScoreDBHelper extends DBHelper {
    final String TABLENAME = "Scores";
    final String ERRORTABLENAME = "Logs";

    Context contexter;

    public ScoreDBHelper(Context context) {
        super(context);
        contexter = context;
        database = getWritableDatabase();
    }


    // calculate total time usage of grp from grpid
    public List<ScoreList> getUsageDetails(String grpID) {
        try {
            database = getWritableDatabase();
            List<ScoreList> list = new ArrayList<ScoreList>();
            {
                Cursor cursor = database.rawQuery("SELECT StartDateTime,EndDateTime FROM Scores WHERE GroupID = ?", new String[]{grpID});
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {

                    list.add(new ScoreList(cursor.getString(cursor.getColumnIndex("StartDateTime")), cursor.getString(cursor.getColumnIndex("EndDateTime"))));

                    cursor.moveToNext();
                }
                database.close();

            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetTotalUsage");
            return null;
        }

    }

    public List<Score> GetTotalAkaScoreForGrpID(String GroupID) {
        try {
            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select SUM(ScoredMarks) as ScoredMarks, SUM(TotalMarks) as TotalMarks from Scores where GroupID = '" + GroupID + "' and Level In (99,990,991)", null);
            return PopulateGroupScoresFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public List<Score> GetAKSGroupScore() {
        try {
            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select distinct GroupID from Scores where Level In (99,990,991)", null);

            return PopulateScoreListFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public int GetPlayedResourcesCount() {
        try {
            Cursor cursor = database.rawQuery("select distinct ResourceID from " + TABLENAME + "", null);
            int cursorCount = cursor.getCount();
            return cursorCount;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetPlayedResourcesCount");
            return 0;
        }

    }

    public String GetPlayedResources() {
        String resourceList = "";
        try {
            Cursor cursor = database.rawQuery("select distinct ResourceID from " + TABLENAME + "", null);
            int cursorCount = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < cursorCount; i++) {
                resourceList += cursor.getString(cursor.getColumnIndex("ResourceID")) + ",";
                cursor.moveToNext();
            }
            return resourceList;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetPlayedResources");
        }
        return resourceList;
    }


    public List<ScoreList> GetTotalUsage() {
        try {
            database = getWritableDatabase();
            List<ScoreList> list = new ArrayList<ScoreList>();
            {
                Cursor cursor = database.rawQuery("SELECT StartDateTime,EndDateTime FROM Scores", null);
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {

                    list.add(new ScoreList(cursor.getString(cursor.getColumnIndex("StartDateTime")), cursor.getString(cursor.getColumnIndex("EndDateTime"))));

                    cursor.moveToNext();
                }
                database.close();

            }
            return list;

        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetTotalUsage");
            return null;
        }
    }


    private void _PopulateLogValues(Exception ex, String method) {

        Logs logs = new Logs();

        logs.currentDateTime = Util.GetCurrentDateTime(false);
        logs.errorType = "Error";
        logs.exceptionMessage = ex.getMessage().toString();
        logs.exceptionStackTrace = ex.getStackTrace().toString();
        logs.methodName = method;
        logs.groupId = (MultiPhotoSelectActivity.selectedGroupId.equals("null") || MultiPhotoSelectActivity.selectedGroupId == null) ? "GroupID" : MultiPhotoSelectActivity.selectedGroupId;
        logs.deviceId = MultiPhotoSelectActivity.deviceID;

        contentValues.put("CurrentDateTime", logs.currentDateTime);
        contentValues.put("ExceptionMsg", logs.exceptionMessage);
        contentValues.put("ExceptionStackTrace", logs.exceptionStackTrace);
        contentValues.put("MethodName", logs.methodName);
        contentValues.put("Type", logs.errorType);
        contentValues.put("GroupId", logs.groupId == null ? "" : logs.groupId);
        contentValues.put("DeviceId", logs.deviceId);

        contentValues.put("LogDetail", "ScoreLog");

        database.insert(ERRORTABLENAME, null, contentValues);
        database.close();
        BackupDatabase.backup(c);
    }


    public boolean Add(Score score) {
        try {
            database = this.getWritableDatabase();
            _PopulateContentValues(score);
            long resultCount = database.insert(TABLENAME, null, contentValues);
            //Toast.makeText(grpContext, "resultCount from add score" + resultCount,
            //      Toast.LENGTH_SHORT).show();
            database.close();
            if (resultCount == -1)
                return false;
            else
                return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "Add");
            return false;
        }
    }

    public boolean DeleteAll() {
        try {
            // database = getWritableDatabase();
            long resultCount = database.delete(TABLENAME, null, null);
            database.close();
            return true;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "DeleteAll");
            return false;
        }
    }

    public boolean Update(Score score) {
        return false;
    }

    public boolean Delete(int scoreID) {
        return true;
    }

    public Score Get(int scoreID) {
        Score score = new Score();
        return score;
    }

    public List<Score> GetAll() {
        try {
            Cursor cursor = database.rawQuery("select * from " + TABLENAME + "", null);
            return _PopulateListFromCursor(cursor);
        } catch (Exception ex) {
            _PopulateLogValues(ex, "GetAll");
            return null;
        }
    }

    private void _PopulateContentValues(Score score) {

        contentValues.put("SessionID", score.SessionID.toString());
        //contentValues.put("PlayerID", score.PlayerID);
        contentValues.put("GroupID", score.GroupID);
        contentValues.put("DeviceID", score.DeviceID);
        contentValues.put("QuestionID", score.QuestionId);
        contentValues.put("ResourceID", score.ResourceID);
        contentValues.put("ScoredMarks", score.ScoredMarks);
        contentValues.put("TotalMarks", score.TotalMarks);
        contentValues.put("StartDateTime", score.StartTime);
        contentValues.put("EndDateTime", score.EndTime);
        contentValues.put("Level", score.Level);
        contentValues.put("Label", score.Label);
    }

    private List<Score> _PopulateListFromCursor(Cursor cursor) {
        try {
            List<Score> scoreList = new ArrayList<Score>();
            Score score;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                score = new Score();
                //score.PlayerID=cursor.getInt(cursor.getColumnIndex("PlayerID"));
                score.SessionID = cursor.getString(cursor.getColumnIndex("SessionID"));
                score.GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
                score.DeviceID = cursor.getString(cursor.getColumnIndex("DeviceID"));
                score.ResourceID = cursor.getString(cursor.getColumnIndex("ResourceID"));
                score.QuestionId = cursor.getInt(cursor.getColumnIndex("QuestionID"));
                score.ScoredMarks = cursor.getInt(cursor.getColumnIndex("ScoredMarks"));
                score.TotalMarks = cursor.getInt(cursor.getColumnIndex("TotalMarks"));
                score.Level = cursor.getInt(cursor.getColumnIndex("Level"));
                score.StartTime = cursor.getString(cursor.getColumnIndex("StartDateTime"));
                score.EndTime = cursor.getString(cursor.getColumnIndex("EndDateTime"));
                score.Label = cursor.getString(cursor.getColumnIndex("Label"));

                scoreList.add(score);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return scoreList;
        } catch (Exception ex) {
            _PopulateLogValues(ex, "_populateListFromCursor");
            return null;
        }
    }

    /*
     *
     * */

    public List<Score> GetAllGroupScore() {
        try {
            Cursor cursor;
            database = getWritableDatabase();
//            cursor = database.rawQuery("select ResourceID, SUM(ScoredMarks) as ScoredMarks, SUM(TotalMarks) as TotalMarks from Scores where GroupID in ("+groupID+") and Level != 99", null);
            cursor = database.rawQuery("select distinct GroupID from Scores where Level != 99 OR Level != 990 OR Level != 991", null);

            return PopulateScoreListFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private List<Score> PopulateScoreListFromCursor(Cursor cursor) {
        try {
            List<Score> ScoresList = new ArrayList<Score>();
            Score score;
            cursor.moveToFirst();
            int count = cursor.getCount();

            while (cursor.isAfterLast() == false) {

                score = new Score();

                score.GroupID = cursor.getString((cursor.getColumnIndex("GroupID")));

                ScoresList.add(score);

                cursor.moveToNext();
            }
            cursor.close();
            return ScoresList;
        } catch (Exception ex) {
            return null;
        }
    }


    public List<Score> GetScoreForReports(String GroupID, String resIDs, String dp_FromDateText, String dp_ToDateText) {
        try {
            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select SUM(ScoredMarks) as ScoredMarks, SUM(TotalMarks) as TotalMarks from Scores where (StartDateTime and EndDateTime between '" + dp_FromDateText + "' and '" + dp_ToDateText + "') and GroupID = '" + GroupID + "' and ResourceID in (" + resIDs + ")", null);
            return PopulateGroupScoresFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Score> GetAkaScoreForReports(String GroupID, String resIDs) {
        try {
            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select SUM(ScoredMarks) as ScoredMarks, SUM(TotalMarks) as TotalMarks from Scores where GroupID = '" + GroupID + "' and ResourceID in (" + resIDs + ")", null);
            return PopulateGroupScoresFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private List<Score> PopulateGroupScoresFromCursor(Cursor cursor) {
        try {
            List<Score> ScoresList = new ArrayList<Score>();
            Score score;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                score = new Score();

                score.ScoredMarks = cursor.getInt((cursor.getColumnIndex("ScoredMarks")));
                score.TotalMarks = cursor.getInt(cursor.getColumnIndex("TotalMarks"));

                ScoresList.add(score);
                cursor.moveToNext();
            }
            cursor.close();
            return ScoresList;
        } catch (Exception ex) {
            return null;
        }
    }

    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        Cursor cursor = database.rawQuery("UPDATE Scores SET SessionID = IfNull(SessionID,'0'), GroupID = IfNull(GroupID,'0'), DeviceID = IfNull(DeviceID,'0'), ResourceID = IfNull(ResourceID,'0'), QuestionID = IfNull(QuestionID,'0'), ScoredMarks = IfNull(ScoredMarks,'0'), TotalMarks = IfNull(TotalMarks,'0'), StartDateTime = IfNull(StartDateTime,'0'), EndDateTime = IfNull(EndDateTime,'0'), Level = IfNull(Level,'0'), Label = IfNull(Label,'')", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }


}
