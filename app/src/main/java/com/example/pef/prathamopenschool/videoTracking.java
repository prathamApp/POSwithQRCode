package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.Build;

/**
 * Created by PEF-2 on 23/09/2015.
 */
public class videoTracking {
    public static String SessionId;
    public static String UserId[];
    public static Context c;
    public String resId;

    videoTracking(Context c,String userId[],String sessionId,String resId){
        UserId=userId;
        SessionId=sessionId;
        this.c=c;
        this.resId=resId;
    }

/*
    public static void calculateStartTime(){
        Utility util=new Utility();
        startTime = util.GetCurrentDateTime();
    }
*/

    public  void calculateEndTime(){

        try{

            Boolean _wasSuccessful = null;
            Utility util=new Utility();
            String endTime = util.GetCurrentDateTime(false);

            if(resId.equals(null) || MainActivity.startTime.equals("undefined")){
                SyncActivityLogs syncActivityLogs=new SyncActivityLogs(c);
                if(resId.equals(null))
                    syncActivityLogs.addLog("calculateEndTime-videoTracking","Error","resource id is null");
                else if(MainActivity.startTime.equals("undefined"))
                    syncActivityLogs.addLog("calculateEndTime-videoTracking","Error","startTime is undefined");
                BackupDatabase.backup(c);
            }
            else{
                StatusDBHelper statusDBHelper = new StatusDBHelper(c);
                Score score = new Score();
                ScoreDBHelper scoreDBHelper = new ScoreDBHelper(c);
                score.SessionID = SessionId;
                //score.PlayerID = 0;
                //score.GroupID = MainActivity.groupId;
                String deviceId = statusDBHelper.getValue("deviceId");
                if(deviceId.equals("") || deviceId.contains("111111111111111")){
                    deviceId = Build.SERIAL;
                    if(deviceId.equals("") || deviceId == null){}
                    else{
                        //score.DeviceID = StartingActivity.deviceId;
                        score.DeviceID = deviceId;
                        score.ResourceID = CardAdapter.resId;// it will be different and will be captured from js file
                        score.QuestionId=0;// it will be 0 for videos and pdfs
                        score.ScoredMarks = 0;//it will be 0 for videos and pdfs
                        score.StartTime=MainActivity.startTime;
                        score.EndTime=endTime;
                        score.Level=0;
                        _wasSuccessful = scoreDBHelper.Add(score);
                        if(!_wasSuccessful){

                        }
                    }
                }
                else{
                    score.DeviceID = deviceId;
                    score.ResourceID = CardAdapter.resId;// it will be different and will be captured from js file
                    score.QuestionId=0;// it will be 0 for videos and pdfs
                    score.ScoredMarks = 0;//it will be 0 for videos and pdfs
                    score.StartTime=MainActivity.startTime;
                    score.EndTime=endTime;
                    score.Level=0;
                    _wasSuccessful = scoreDBHelper.Add(score);
                    if(!_wasSuccessful){

                    }
                }
            }
            BackupDatabase.backup(c);
        }
        catch (Exception e){
        }

    }
}
