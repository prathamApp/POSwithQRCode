package com.example.pef.prathamopenschool;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ameya on 06-Nov-17.
 */

public class GraphReport extends Fragment {

    TextView Title;
    String abc="My Fragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_graph_report, container, false);

        if(getArguments() != null) {
            abc = getArguments().getString("studentId");
            Log.d("tga", "" + abc);
        }
        Title = (TextView) view.findViewById(R.id.tv_Title);
        if(abc != null)
            Title.setText(abc);

        return view;
    }
}