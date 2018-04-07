package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pef.prathamopenschool.interfaces.StudentListViewInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ameya on 06-Nov-17.
 */

public class AssessmentStudentAdapter extends ArrayAdapter<JSONObject> {

    private final Activity context;
    ArrayList<JSONObject> allData;
    StudentListViewInterface studentListViewInterface;
    int selectedPosition = -1;

    public AssessmentStudentAdapter (Activity context, ArrayList<JSONObject> allData, StudentListViewInterface studentListViewInterface) {
        super(context, R.layout.assessmentstudentrow, allData);
        // TODO Auto-generated constructor stub
        this.studentListViewInterface = studentListViewInterface;
        this.context=context;
        this.allData=allData;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.assessmentstudentrow, null,true);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.stidentImg);
        TextView extratxt = (TextView) rowView.findViewById(R.id.StudentName);
        LinearLayout parent_l=(LinearLayout) rowView.findViewById(R.id.parentRoot);
        try {
            String myTag = "Adapter";
            Log.d(myTag, "SelectedPos: "+selectedPosition);
            if(selectedPosition == position){
                parent_l.setBackgroundColor(context.getResources().getColor(R.color.button));
            }
            else{
                parent_l.setBackgroundColor(context.getResources().getColor(R.color.base));
            }
            Bitmap[] bitmap = {BitmapFactory.decodeFile(allData.get(position).getString("imgPath"))};
            imageView.setImageBitmap(Bitmap.createBitmap(bitmap[0]));

            extratxt.setText(allData.get(position).getString("studentName"));
            parent_l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        studentListViewInterface.onStudentClicked(position,allData.get(position).getString("studentId"),allData.get(position).getString("studentName"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return rowView;

    };

    public void updateSelection(int selectedPosition){
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }
}
