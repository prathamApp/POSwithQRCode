package com.example.pef.prathamopenschool;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AssessmentResult extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<Assessment> adapter;
    private ArrayList<Assessment> arrayList;
    String resId[];
    int scoredMarks[];
    int totMarks[];
    int score[];
    int overallScorePer, j;
    ArrayList<String> resArray = new ArrayList<String>();
    ArrayList<Float> scoreArray = new ArrayList<>();
    ArrayList<Float> totArray = new ArrayList<>();
    ArrayList<Float> overAllArray = new ArrayList<>();
    ArrayList<String> resName = new ArrayList<>();
    ArrayList<Integer> imageList = new ArrayList<>();

    static Context mContext, sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    Button btn_authenticate;
    boolean timer;
    boolean destroyed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_result);
        getSupportActionBar().hide();

        listView = (ListView) findViewById(R.id.list_view);

        sessionContex = this;
        playVideo = new PlayVideo();


//        btn_authenticate = (Button) findViewById(R.id.btn_authenticate);

        /** Call GetScore and get all the list**/
        List<AssessmentScore> assessmentScores = new AssessmentScoreDBHelper(this).GetAllAssessmentScore(true);

        String resIds;
        float marksScore, marksOutOf, assesMarks, assesTot, overAssesTot = 0, overAssessMarks = 0, totPercent = 0;
        marksScore = marksOutOf = 0;
        j = 0;

        for (int i = 0; i < assessmentScores.size(); i++) {
            assesMarks = (float) assessmentScores.get(i).ScoredMarks;
            assesTot = (float) assessmentScores.get(i).TotalMarks;
            overAssessMarks += assesMarks;
            overAssesTot += assesTot;
            totPercent += ((assesMarks / assesTot) * 100);
            overAllArray.add(((assesMarks / assesTot) * 100));
            resName.add(assessmentScores.get(i).DeviceID);
        }

        overAllArray.add(totPercent / assessmentScores.size());

/*
        btn_authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(AssessmentResult.this , loginPopUp.class));
            }
        });
*/

        try {
            setLisData();
            adapter = new AssessmentAdapter(this, R.layout.assessment_result_row, arrayList,imageList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLisData() {

        float ratings = 0;
        arrayList = new ArrayList<>();
        for (int j = 0; j < overAllArray.size(); j++) {

            if (overAllArray.get(j) <= 5)
                ratings = (float) 0.5;
            else if (overAllArray.get(j) > 5  && overAllArray.get(j) <= 10)
                ratings = (float) 1;
            else if (overAllArray.get(j) > 10 && overAllArray.get(j) <= 15)
                ratings = (float) 1.5;
            else if (overAllArray.get(j) > 15 && overAllArray.get(j) <= 20)
                ratings = (float) 2;
            else if (overAllArray.get(j) > 20 && overAllArray.get(j) <= 25)
                ratings = (float) 2.5;
            else if (overAllArray.get(j) > 25 && overAllArray.get(j) <= 30)
                ratings = (float) 3;
            else if (overAllArray.get(j) > 30 && overAllArray.get(j) <= 35)
                ratings = (float) 3.5;
            else if (overAllArray.get(j) > 35 && overAllArray.get(j) <= 40)
                ratings = (float) 4;
            else if (overAllArray.get(j) > 40 && overAllArray.get(j) <= 45)
                ratings = (float) 4.5;
            else if (overAllArray.get(j) > 45 && overAllArray.get(j) <= 50)
                ratings = (float) 5;
            else if (overAllArray.get(j) > 50 && overAllArray.get(j) <= 55)
                ratings = (float) 5.5;
            else if (overAllArray.get(j) > 55 && overAllArray.get(j) <= 60)
                ratings = (float) 6;
            else if (overAllArray.get(j) > 60 && overAllArray.get(j) <= 65)
                ratings = (float) 6.5;
            else if (overAllArray.get(j) > 65 && overAllArray.get(j) <= 70)
                ratings = (float) 7;
            else if (overAllArray.get(j) > 70 && overAllArray.get(j) <= 75)
                ratings = (float) 7.5;
            else if (overAllArray.get(j) > 75 && overAllArray.get(j) <= 80)
                ratings = (float) 8;
            else if (overAllArray.get(j) > 80 && overAllArray.get(j) <= 85)
                ratings = (float) 8.5;
            else if (overAllArray.get(j) > 85 && overAllArray.get(j) <= 90)
                ratings = (float) 9;
            else if (overAllArray.get(j) > 90)
                ratings = (float) 10;

/*
            float rate = overAllArray.get(j)/10;

            int before = (int)rate;
            ratings =(float) before;

            String after = String.valueOf(rate);
            after = after.substring ( after.indexOf ( "." )+ 1 , after.indexOf ( "." )+ 3 );
            if (Float.parseFloat(after)>50){
                ratings+=0.5;
            }
*/

            if (overAllArray.get(j) > 30 && overAllArray.get(j) <= 45)
                imageList.add(R.drawable.lower);
            else if (overAllArray.get(j) > 45 && overAllArray.get(j) <= 60)
                imageList.add(R.drawable.moderate);
            else if (overAllArray.get(j) > 60 && overAllArray.get(j) <= 80)
                imageList.add(R.drawable.good);
            else if (overAllArray.get(j) > 80)
                imageList.add(R.drawable.best);
            else
                imageList.add(R.drawable.lowest);

            int end = overAllArray.size() - 1;
            int gameNo = j + 1;

            if (j == end)
                arrayList.add(new Assessment(ratings, "Overall Result"));
            else
                arrayList.add(new Assessment(ratings, resName.get(j)));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        destroyed = false;
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        destroyed = false;

        MultiPhotoSelectActivity.pauseFlg = true;

        MultiPhotoSelectActivity.cd = new CountDownTimer(MultiPhotoSelectActivity.duration, 1000) {
            //cd = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                MultiPhotoSelectActivity.duration = millisUntilFinished;
                timer = true;
            }

            @Override
            public void onFinish() {
                timer = false;
                MainActivity.sessionFlg = true;

                if (!CardAdapter.vidFlg) {
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        MainActivity.sessionFlg = true;
        destroyed = true;
        super.onDestroy();
    }
}