package com.example.pef.prathamopenschool;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ExpandableUsageListDataPump {

    static ArrayList<Usage> listForAdapter;
    static ArrayAdapter<Usage> listAdapter;

    @SuppressLint("NewApi")
    public static LinkedHashMap<String, List<String>> getData() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();

        listForAdapter = new ArrayList<Usage>();
        listAdapter = new AppUsageAdapter(MyApplication.getInstance(), listForAdapter);

        // get All group info list
        GroupDBHelper gdb = new GroupDBHelper(MyApplication.getInstance());
        List<Group> grpList = gdb.GetAll();
        List<Student> stdList = null;
        List<String> studentList;
        List<String> sortedStdList;
        List<Usage> usageStudentList;
        List<String> SessionIDs;
        String grpName;
        String grpID;

        for (int i = 0; i < grpList.size(); i++) {
            grpID = grpList.get(i).getGroupID();
            grpName = grpList.get(i).getGroupName();

            StudentDBHelper studentDBHelper = new StudentDBHelper(MyApplication.getInstance());
            stdList = studentDBHelper.getStudentsByGroup(grpID);
            studentList = new ArrayList<String>();
            sortedStdList = new ArrayList<String>();
            usageStudentList = new ArrayList<Usage>();
            SessionIDs = new ArrayList<String>();

            // get all students of particular group
            for (int j = 0; j < stdList.size(); j++) {
                // get student id & search all its occurances in Attendance table & maintain list of session ids
                String stdID = stdList.get(j).getStudentID();
                AttendanceDBHelper attendanceDBHelper = new AttendanceDBHelper(MyApplication.getInstance());
                SessionIDs = attendanceDBHelper.getAllSessionsByStdID(stdID);

                // maintain a varible of time & query session table & calculate total time for those sessions
                SessionDBHelper sessionDBHelper;
                long stdUsageTime = 0;
                sessionDBHelper = new SessionDBHelper(MyApplication.getInstance());

                for (int k = 0; k < SessionIDs.size(); k++) {
                    List<ScoreList> usageTimeList = sessionDBHelper.getUsageDetails(SessionIDs.get(k));
                    if (usageTimeList != null)
                        stdUsageTime = stdUsageTime + calculateUsageTime(usageTimeList);
                    else
                        stdUsageTime = stdUsageTime + 0;
                }

                // converting total time to days
                long usageTime = stdUsageTime;
                long diffSeconds = usageTime / 1000 % 60;
                long diffMinutes = usageTime / (60 * 1000) % 60;
                long diffHours = usageTime / (60 * 60 * 1000) % 24;
                long diffDays = usageTime / (24 * 60 * 60 * 1000);
                String days = diffDays + " Days, " + diffHours + " Hours, " + diffMinutes + " Minutes, " + diffSeconds + " Seconds.";

                // finally add to student list
//                studentList.add(stdList.get(j).getFirstName() + " " + stdList.get(j).getMiddleName() + " " + stdList.get(j).getLastName() + " " + days);
                usageStudentList.add(new Usage(stdList.get(j).getFirstName() + " " + stdList.get(j).getMiddleName() + " " + stdList.get(j).getLastName(), usageTime, days));
            }

            // sort student list by usage time
            Collections.sort(usageStudentList, new Comparator<Usage>() {
                @Override
                public int compare(Usage usage, Usage t1) {
                    return Long.compare(t1.getUsageTime(), usage.getUsageTime());
                }
            });

            // populate sorted student list & pass the same to adapter
            for (int x = 0; x < usageStudentList.size(); x++)
                sortedStdList.add("" + usageStudentList.get(x).getGrpName() + "\t" + usageStudentList.get(x).getUsageTimeInDays());

            // todo GROUP'S TOTAL USAGE CALCULATION
            // NEW LOGIC
            AttendanceDBHelper attendanceDBHelper = new AttendanceDBHelper(MyApplication.getInstance());
            SessionIDs.clear();
            SessionIDs = attendanceDBHelper.getAllSessionsByGrpID(grpID);

            // eliminate same session id
            Set<String> hs = new HashSet<>();
            hs.addAll(SessionIDs);
            SessionIDs.clear();
            SessionIDs.addAll(hs);

            // maintain a varible of time & query session table & calculate total time for those sessions
            SessionDBHelper sessionDBHelper;
            sessionDBHelper = new SessionDBHelper(MyApplication.getInstance());
            long grpUsageTime = 0;

            for (int r = 0; r < SessionIDs.size(); r++) {
                List<ScoreList> usageTimeList = sessionDBHelper.getUsageDetails(SessionIDs.get(r));
                if (usageTimeList != null) {
                    grpUsageTime = grpUsageTime + calculateUsageTime(usageTimeList);
                } else {
                    grpUsageTime = grpUsageTime + 0;
                }
            }

            // converting total time to days
            long usageTime = grpUsageTime;

            long diffSeconds = usageTime / 1000 % 60;
            long diffMinutes = usageTime / (60 * 1000) % 60;
            long diffHours = usageTime / (60 * 60 * 1000) % 24;
            long diffDays = usageTime / (24 * 60 * 60 * 1000);
            String days = diffDays + " Days, " + diffHours + " Hours, " + diffMinutes + " Minutes, " + diffSeconds + " Seconds.";

/*
            // OLD LOGIC
            ScoreDBHelper sdb = new ScoreDBHelper(MyApplication.getInstance());
            List<ScoreList> usageTimeList = sdb.getUsageDetails(grpID);
            long usageTime = calculateUsageTime(usageTimeList);

            long diffSeconds = usageTime / 1000 % 60;
            long diffMinutes = usageTime / (60 * 1000) % 60;
            long diffHours = usageTime / (60 * 60 * 1000) % 24;
            long diffDays = usageTime / (24 * 60 * 60 * 1000);
            String days = diffDays + " Days, " + diffHours + " Hours, " + diffMinutes + " Minutes, " + diffSeconds + " Seconds.";
*/

//            listAdapter.add(new Usage(grpName, usageTime, days, studentList));
            listAdapter.add(new Usage(grpName, usageTime, days, sortedStdList));
        }


        for (int i = 0; i < listAdapter.getCount(); i++) {
            Log.d("lst before :::", listAdapter.getItem(i).grpName + listAdapter.getItem(i).usageTime + listAdapter.getItem(i).usageTimeInDays);
        }

        listAdapter.sort(new Comparator<Usage>() {
            @Override
            public int compare(Usage usage, Usage t1) {
                return Long.compare(t1.getUsageTime(), usage.getUsageTime());
            }
        });

        for (int i = 0; i < listAdapter.getCount(); i++) {
            Log.d("lst after :::", listAdapter.getItem(i).grpName + listAdapter.getItem(i).usageTime + listAdapter.getItem(i).usageTimeInDays);
        }


        for (int i = 0; i < listAdapter.getCount(); i++) {
            Log.d("lst expandable :::", listAdapter.getItem(i).grpName + listAdapter.getItem(i).usageTimeInDays + listAdapter.getItem(i).students);
//            expandableListDetail.put("Group Name : " + listAdapter.getItem(i).grpName + " => Usage : " + listAdapter.getItem(i).usageTimeInDays, listAdapter.getItem(i).students);
            expandableListDetail.put("" + listAdapter.getItem(i).grpName + "\t" + listAdapter.getItem(i).usageTimeInDays, listAdapter.getItem(i).students);
        }

        return expandableListDetail;
    }

    // usageTimeinMilis
    private static long calculateUsageTime(List<ScoreList> usageTimeList) {

        long TotalTimeInMiliSeconds = 0;

        for (int i = 0; i < usageTimeList.size(); i++) {
            String from, to;
            from = usageTimeList.get(i).getStartTime();
            to = usageTimeList.get(i).getEndTime();
            TotalTimeInMiliSeconds = TotalTimeInMiliSeconds + new Utility().DateDifferentExample(from, to);
        }

        return TotalTimeInMiliSeconds;
    }
}
