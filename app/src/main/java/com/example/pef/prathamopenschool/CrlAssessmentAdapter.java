package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ameya on 26-Sep-17.
 */

class CrlAssessmentAdapter extends ArrayAdapter<Assessment> {

    private AppCompatActivity activity;
    private List<Assessment> gameList;
    private float ratings;

    public CrlAssessmentAdapter(AppCompatActivity context, int ratings, List<Assessment> objects) {
        super(context, ratings, objects);
        this.activity = context;
        this.gameList = objects;
        this.ratings=(float)ratings;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        Assessment assessment = gameList.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.crl_assessment_result_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            //holder.ratingBar.getTag(position);
        }

        holder.studentName.setText(assessment.name);
        holder.TotalAssessments.setText(assessment.nosOfAssessments);
        holder.ScoredMarks.setText(assessment.ScoredMarks);
        holder.TotalMarks.setText(assessment.TotalMarks);
        holder.OverAllResult.setText(assessment.TotalPercentage);

        return convertView;
    }

    private static class ViewHolder {
        private TextView studentName,TotalAssessments,ScoredMarks,TotalMarks,OverAllResult;
        public ViewHolder(View view) {
            studentName = (TextView) view.findViewById(R.id.StudentName);
            TotalAssessments = (TextView) view.findViewById(R.id.TotAssessment);
            ScoredMarks = (TextView) view.findViewById(R.id.ScoredMarks);
            TotalMarks = (TextView) view.findViewById(R.id.TotalMarks);
            OverAllResult = (TextView) view.findViewById(R.id.OverAllResult);
        }
    }
}
