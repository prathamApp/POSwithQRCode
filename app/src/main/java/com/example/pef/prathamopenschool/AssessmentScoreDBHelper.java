package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abc on 11-Jul-17.
 */

public class AssessmentScoreDBHelper extends DBHelper {

    final String TABLENAME = "AssessmentScores";
    final String ERRORTABLENAME = "Logs";

    Context contexter;

    public AssessmentScoreDBHelper(Context context) {
        super(context);
        contexter = context;
        database = this.getWritableDatabase();
    }


    public boolean Add(AssessmentScore score) {
        try {


            long resultCount = _PopulateContentValues(score);
            //Toast.makeText(grpContext, "resultCount from add score" + resultCount,
            //      Toast.LENGTH_SHORT).show();

            if (resultCount == -1)
                return false;
            else
                return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private long _PopulateContentValues(AssessmentScore score) {

        database = getWritableDatabase();

        contentValues.put("aSessionID", score.SessionID.toString());
        contentValues.put("aGroupID", score.GroupID);
        contentValues.put("aDeviceID", score.DeviceID);
        contentValues.put("aQuestionID", score.QuestionId);
        contentValues.put("aResourceID", score.ResourceID);
        contentValues.put("aScoredMarks", score.ScoredMarks);
        contentValues.put("aTotalMarks", score.TotalMarks);
        contentValues.put("aStartDateTime", score.StartTime);
        contentValues.put("aEndDateTime", score.EndTime);
        contentValues.put("aLevel", score.Level);
        contentValues.put("aLessonSession", MainActivity.lessonSession);

        long dbentries = database.insert("AssessmentScores", null, contentValues);
        database.close();
        Log.d("values ::::: ", contentValues.toString());

        return dbentries;
    }

    public List<JSONObject> GetLastStudentSessionCount(String LastSession) {

        List<JSONObject> studentSessionCount = new ArrayList<JSONObject>();
        try {

            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select aGroupID, count(aSessionID) as aSessionID from( select distinct aSessionID, aGroupID from AssessmentScores where aSessionID = '" + LastSession + "') group by(aGroupID)", null);
            cursor.moveToFirst();
            JSONObject ssc;
            int count = cursor.getCount();

            while (cursor.isAfterLast() == false) {

                ssc = new JSONObject();
                ssc.put("aGroupID", cursor.getString(cursor.getColumnIndex("aGroupID")));
                ssc.put("aSessionID", cursor.getString(cursor.getColumnIndex("aSessionID")));

                studentSessionCount.add(ssc);
                cursor.moveToNext();
            }
            cursor.close();
            return studentSessionCount;
        } catch (Exception e) {
            e.printStackTrace();
            return studentSessionCount;
        }
    }

    public List<JSONObject> GetStudentSessionCount() {

        List<JSONObject> studentSessionCount = new ArrayList<JSONObject>();
        try {

            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select aGroupID, count(aSessionID) as aSessionID from( select distinct aSessionID, aGroupID from AssessmentScores) group by(aGroupID)", null);
            cursor.moveToFirst();
            JSONObject ssc;
            int count = cursor.getCount();

            while (cursor.isAfterLast() == false) {

                ssc = new JSONObject();
                ssc.put("aGroupID", cursor.getString(cursor.getColumnIndex("aGroupID")));
                ssc.put("aSessionID", cursor.getString(cursor.getColumnIndex("aSessionID")));

                studentSessionCount.add(ssc);
                cursor.moveToNext();
            }
            cursor.close();
            return studentSessionCount;
        } catch (Exception e) {
            e.printStackTrace();
            return studentSessionCount;
        }
    }

    public List<AssessmentScore> GetLastAssessmentScore(String lastSessionId) {
        try {
            Cursor cursor;
            database = getWritableDatabase();

            cursor = database.rawQuery("select aResourceID, aGroupID, SUM(aScoredMarks) as aScoredMarks, SUM(aTotalMarks) as aTotalMarks from AssessmentScores where aSessionID = '" + lastSessionId + "' group by aResourceID", null);

            return _PopulateCustomListFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String GetLastAssessmentSession() {
        try {
            Cursor cursor;
            database = getWritableDatabase();

            cursor = database.rawQuery("select distinct aSessionID from AssessmentScores", null);
            cursor.moveToLast();

            String lastSessionId = cursor.getString(0);

            return lastSessionId;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<AssessmentScore> GetAllAssessmentScore(Boolean AssessmentON) {
        try {
            Cursor cursor;
            database = getWritableDatabase();
            if (AssessmentON)
                cursor = database.rawQuery("select aResourceID, aDeviceID, aGroupID, SUM(aScoredMarks) as aScoredMarks, SUM(aTotalMarks) as aTotalMarks from AssessmentScores where aLessonSession = '" + MainActivity.lessonSession + "' group by aResourceID", null);
//                cursor = database.rawQuery("select * from AssessmentScores where aLessonSession = '" + MainActivity.lessonSession + "'", null);
//                return _PopulateListFromCursor(cursor);
            else
                cursor = database.rawQuery("select aResourceID, aDeviceID, aGroupID, SUM(aScoredMarks) as aScoredMarks, SUM(aTotalMarks) as aTotalMarks from AssessmentScores group by aGroupID", null);

            return _PopulateCustomListFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private List<AssessmentScore> _PopulateCustomListFromCursor(Cursor cursor) {
        try {
            List<AssessmentScore> assessmentScores = new ArrayList<AssessmentScore>();
            AssessmentScore assessmentScore;
            cursor.moveToFirst();
            int count = cursor.getCount();

            while (cursor.isAfterLast() == false) {

                assessmentScore = new AssessmentScore();

//                assessmentScore.SessionID = cursor.getString(cursor.getColumnIndex("aSessionID"));
                assessmentScore.ResourceID = cursor.getString((cursor.getColumnIndex("aResourceID")));
                assessmentScore.DeviceID = cursor.getString((cursor.getColumnIndex("aDeviceID")));
                assessmentScore.GroupID = cursor.getString((cursor.getColumnIndex("aGroupID")));
                assessmentScore.ScoredMarks = cursor.getInt((cursor.getColumnIndex("aScoredMarks")));
                assessmentScore.TotalMarks = cursor.getInt(cursor.getColumnIndex("aTotalMarks"));

                assessmentScores.add(assessmentScore);
                cursor.moveToNext();
            }
            cursor.close();
            return assessmentScores;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<AssessmentScore> GetAssessmentScoreForReports(String aGroupID, String resIDs, String dp_FromDateText, String dp_ToDateText) {
        try {
            Cursor cursor;
            database = getWritableDatabase();
            cursor = database.rawQuery("select SUM(aScoredMarks) as aScoredMarks, SUM(aTotalMarks) as aTotalMarks from AssessmentScores where (aStartDateTime and aEndDateTime between '" + dp_FromDateText + "' and '" + dp_ToDateText + "') and aGroupID = '" + aGroupID + "' and aResourceID in (" + resIDs + ")", null);
            return PopulateAssessmentFromCursor(cursor);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private List<AssessmentScore> PopulateAssessmentFromCursor(Cursor cursor) {
        try {
            List<AssessmentScore> assessmentScores = new ArrayList<AssessmentScore>();
            AssessmentScore assessmentScore;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                assessmentScore = new AssessmentScore();

                assessmentScore.ScoredMarks = cursor.getInt((cursor.getColumnIndex("aScoredMarks")));
                assessmentScore.TotalMarks = cursor.getInt(cursor.getColumnIndex("aTotalMarks"));

                assessmentScores.add(assessmentScore);
                cursor.moveToNext();
            }
            cursor.close();
            return assessmentScores;
        } catch (Exception ex) {
            return null;
        }
    }

    private List<AssessmentScore> _PopulateListFromCursor(Cursor cursor) {
        try {
            database = getWritableDatabase();
            List<AssessmentScore> assessmentScores = new ArrayList<AssessmentScore>();
            AssessmentScore assessmentScore;
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {

                assessmentScore = new AssessmentScore();

                assessmentScore.SessionID = cursor.getString(cursor.getColumnIndex("aSessionID"));
                assessmentScore.ResourceID = cursor.getString(cursor.getColumnIndex("aResourceID"));
                assessmentScore.GroupID = cursor.getString((cursor.getColumnIndex("aGroupID")));
                assessmentScore.DeviceID = cursor.getString((cursor.getColumnIndex("aDeviceID")));
                assessmentScore.QuestionId = cursor.getInt((cursor.getColumnIndex("aQuestionID")));
                assessmentScore.ScoredMarks = cursor.getInt((cursor.getColumnIndex("aScoredMarks")));
                assessmentScore.TotalMarks = cursor.getInt(cursor.getColumnIndex("aTotalMarks"));
                assessmentScore.Level = cursor.getInt(cursor.getColumnIndex("aLevel"));
                assessmentScore.StartTime = cursor.getString((cursor.getColumnIndex("aStartDateTime")));
                assessmentScore.EndTime = cursor.getString((cursor.getColumnIndex("aEndDateTime")));
                assessmentScore.LessonSession = cursor.getString((cursor.getColumnIndex("aLessonSession")));

                assessmentScores.add(assessmentScore);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            return assessmentScores;
        } catch (Exception ex) {
            return null;
        }
    }


    // replace null values with dummy
    public void replaceNulls() {
        database = getWritableDatabase();
        Cursor cursor = database.rawQuery("UPDATE AssessmentScores SET aSessionID = IfNull(aSessionID,'0'), aGroupID = IfNull(aGroupID,'0'), aDeviceID = IfNull(aDeviceID,'0'), aResourceID = IfNull(aResourceID,'0'), aQuestionID = IfNull(aQuestionID,'0'), aScoredMarks = IfNull(aScoredMarks,'0'), aTotalMarks = IfNull(aTotalMarks,'0'), aStartDateTime = IfNull(aStartDateTime,'0'), aEndDateTime = IfNull(aEndDateTime,'0'), aLevel = IfNull(aLevel,'0'), aLessonSession= IfNull(aLessonSession,'0')", null);
        cursor.moveToFirst();
        cursor.close();
        database.close();
    }
}
