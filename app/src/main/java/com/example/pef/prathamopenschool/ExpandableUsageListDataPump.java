package com.example.pef.prathamopenschool;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ExpandableUsageListDataPump {

    static ArrayList<Usage> listForAdapter;
    static ArrayAdapter<Usage> listAdapter;

    public static LinkedHashMap<String, List<String>> getData() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();

        listForAdapter = new ArrayList<Usage>();
        listAdapter = new AppUsageAdapter(MyApplication.getInstance(), listForAdapter);

        // get All group info list
        GroupDBHelper gdb = new GroupDBHelper(MyApplication.getInstance());
        List<Group> grpList = gdb.GetAll();
        List<Student> stdList = null;
        List<String> studentList;
        String grpName;
        String grpID;

        for (int i = 0; i < grpList.size(); i++) {
            grpID = grpList.get(i).getGroupID();
            grpName = grpList.get(i).getGroupName();

            StudentDBHelper studentDBHelper = new StudentDBHelper(MyApplication.getInstance());
            stdList = studentDBHelper.getStudentsByGroup(grpID);
            studentList = new ArrayList<String>();

            for (int j = 0; j < stdList.size(); j++)
                studentList.add(stdList.get(j).getFirstName() + " " + stdList.get(j).getMiddleName() + " " + stdList.get(j).getLastName());

            ScoreDBHelper sdb = new ScoreDBHelper(MyApplication.getInstance());
            List<ScoreList> usageTimeList = sdb.getUsageDetails(grpID);
            long usageTime = calculateUsageTime(usageTimeList);

            long diffSeconds = usageTime / 1000 % 60;
            long diffMinutes = usageTime / (60 * 1000) % 60;
            long diffHours = usageTime / (60 * 60 * 1000) % 24;
            long diffDays = usageTime / (24 * 60 * 60 * 1000);
            String days = diffDays + " Days, " + diffHours + " Hours, " + diffMinutes + " Minutes, " + diffSeconds + " Seconds.";

            listAdapter.add(new Usage(grpName, usageTime, days, studentList));
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
            expandableListDetail.put("Group Name : " + listAdapter.getItem(i).grpName + " => Usage : " + listAdapter.getItem(i).usageTimeInDays, listAdapter.getItem(i).students);
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
