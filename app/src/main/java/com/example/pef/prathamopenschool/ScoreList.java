package com.example.pef.prathamopenschool;

/**
 * Created by PEF on 09/10/2017.
 */

public class ScoreList {

    String StartTime;
    String EndTime;

    ScoreList(String st, String et) {
        StartTime = st;
        EndTime = et;
    }

    @Override
    public String toString() {
        return "ScoreList{" +
                "StartTime='" + StartTime + '\'' +
                ", EndTime='" + EndTime + '\'' +
                '}';
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }
}
