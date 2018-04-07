package com.example.pef.prathamopenschool;

import android.os.Build;

/**
 * Created by Ameya on 20-Jun-17.
 */

public class PDFTracking {
    static ScoreDBHelper scoreDBHelper;
    public static String startTime;



    PDFTracking(ScoreDBHelper scoreDBHelper) {
        this.scoreDBHelper = scoreDBHelper;
    }

    public static void calculateStartTime() {
        Utility util = new Utility();
        startTime = util.GetCurrentDateTime(false);
    }

    public static void calculateEndTime() {

        try {

            Boolean _wasSuccessful = null;
            Utility util = new Utility();
            String endTime = util.GetCurrentDateTime(false);
            Score score = new Score();
            score.SessionID = MultiPhotoSelectActivity.sessionId;
            score.ResourceID = CardAdapter.resId;
            score.QuestionId = 0;
            score.ScoredMarks = 1;
            score.TotalMarks = 1;
            score.StartTime = startTime;
            String gid = MultiPhotoSelectActivity.selectedGroupsScore;
            if(gid.contains(","))
                gid = gid.split(",")[0];
            score.GroupID = gid;//ketan 17/6/17
            score.DeviceID = Build.SERIAL;
            score.EndTime = endTime;
            score.Level = 0;
            _wasSuccessful = scoreDBHelper.Add(score);
            if (!_wasSuccessful) {
                BackupDatabase.backup(MainActivity.mContext);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

