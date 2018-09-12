package com.example.pef.prathamopenschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AppUsageAdapter extends ArrayAdapter<Usage> {

    public AppUsageAdapter(Context context, ArrayList<Usage> listForAdapter) {
        super(context, 0, listForAdapter);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Usage usage = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_group_usage, parent, false);
        }
        // Lookup view for data population
        TextView groupRank = (TextView) convertView.findViewById(R.id.groupRank);
        TextView groupName = (TextView) convertView.findViewById(R.id.groupName);

        groupRank.setText("#" + (position + 1));
        groupName.setText(usage.grpName + usage.usageTimeInDays);
        // Return the completed view to render on screen
        return convertView;
    }
}