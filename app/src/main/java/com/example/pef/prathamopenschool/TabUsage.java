package com.example.pef.prathamopenschool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TabUsage extends AppCompatActivity {

    ArrayList<Usage> listForAdapter;
    ArrayAdapter<Usage> listAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_usage);
        getSupportActionBar().hide();

        listView = findViewById(R.id.list_view);
        listForAdapter = new ArrayList<Usage>();
        listAdapter = new TabUsageAdapter(this, listForAdapter);

        try {
            // get All group info list
            GroupDBHelper gdb = new GroupDBHelper(this);
            List<Group> grpList = gdb.GetAll();

            String grpName;
            String grpID;
            for (int i = 0; i < grpList.size(); i++) {
                grpID = grpList.get(i).getGroupID();
                grpName = grpList.get(i).getGroupName();

                ScoreDBHelper sdb = new ScoreDBHelper(TabUsage.this);
                List<ScoreList> usageTimeList = sdb.getUsageDetails(grpID);
                long usageTime = calculateUsageTime(usageTimeList);

                long diffSeconds = usageTime / 1000 % 60;
                long diffMinutes = usageTime / (60 * 1000) % 60;
                long diffHours = usageTime / (60 * 60 * 1000) % 24;
                long diffDays = usageTime / (24 * 60 * 60 * 1000);
                String days = diffDays + " Days, " + diffHours + " Hours, " + diffMinutes + " Minutes, " + diffSeconds + " Seconds.";
                listAdapter.add(new Usage(grpName, usageTime, days));
            }

            // sort list according to usage
            Collections.sort(listForAdapter, new Comparator<Usage>() {
                @Override
                public int compare(Usage usage, Usage t1) {
                    Log.d("compare : ", "" + usage.getUsageTime());
                    return Long.compare(t1.getUsageTime(), usage.getUsageTime());
                }
            });
            listView.setAdapter(listAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // usageTimeinMilis
    private long calculateUsageTime(List<ScoreList> usageTimeList) {

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
