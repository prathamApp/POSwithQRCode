package com.example.pef.prathamopenschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TabUsageAdapter extends ArrayAdapter<Usage> {

    public TabUsageAdapter(Context context, ArrayList<Usage> listForAdapter) {
        super(context, 0, listForAdapter);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Usage usage = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.usage_result_row, parent, false);
        }
        // Lookup view for data population
        TextView groupRank = (TextView) convertView.findViewById(R.id.groupRank);
        TextView groupName = (TextView) convertView.findViewById(R.id.groupName);
        TextView usageTime = (TextView) convertView.findViewById(R.id.usageTime);
        // Populate the data into the template view using the data object

        groupRank.setText("#" + (position + 1));
        groupName.setText(usage.grpName);
        usageTime.setText(usage.getUsageTimeInDays()+"");
        // Return the completed view to render on screen
        return convertView;
    }
}