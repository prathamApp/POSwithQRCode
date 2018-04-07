package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SelectStudent extends AppCompatActivity {

    public String ids[],index="0",studId="0",presentStudents[];
    public static String groupId="0";
    Boolean  Resumed=false;long time;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);

        MainActivity.sessionFlg=false;
        sessionContex=this;
        playVideo = new PlayVideo();

        Button btn = (Button) findViewById(R.id.btn_whosePresent);

        try{

            PowerManager powermanager = (PowerManager)getSystemService(POWER_SERVICE);
            if(!powermanager.isScreenOn())
            {
                Intent intent = new Intent(getApplicationContext(), StartingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
            }
            else{

                //Get groupId from previous intent
                groupId= String.valueOf(getIntent().getStringExtra("groupId"));

                //Get list of students from that group
                StudentDBHelper studentDBHelper=new StudentDBHelper(SelectStudent.this);
                JSONArray list = studentDBHelper.getStudetntsList(groupId);

                if(list==null)
                {

                }
                else if(list.length()==0){
                    ids= new String[0];
                }
                else{
                    LinearLayout my_layout = (LinearLayout)findViewById(R.id.my_layout);
                    LinearLayout my_layout1 = (LinearLayout)findViewById(R.id.my_layout1);

                    ids= new String[list.length()];

                    for (int i = 0; i < list.length(); i++)
                    {
                        TableRow row =new TableRow(this);
                        row.setId(Integer.parseInt("-200"));

                        //dynamically create checkboxes. i.e no. of students in group = no. of checkboxes
                        row.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                        CheckBox checkBox = new CheckBox(this);

                        try {
                            JSONObject obj=list.getJSONObject(i);
                            checkBox.setId(i);
                            checkBox.setTag(obj.getString("studentId"));

                            ids[i]= obj.getString("studentId");
                            checkBox.setText(obj.getString("studentName"));

                        } catch (JSONException e) {
                            SyncActivityLogs syncActivityLogs=new SyncActivityLogs(getApplicationContext());
                            syncActivityLogs.addToDB("onCreate-SelectStudent",e,"Error");
                            BackupDatabase.backup(getApplicationContext());
                            e.printStackTrace();
                        }
                        checkBox.setTextSize(35);
                        checkBox.setTextColor(Color.BLACK);
                        row.addView(checkBox);
                        if(i>=5)
                            my_layout1.addView(row);
                        else
                            my_layout.addView(row);
                    }
                }
            }
        }
        catch (Exception e){
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_student, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MultiPhotoSelectActivity.pauseFlg){
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg=false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public  void goToNextPage(View v){

        if(ids.length==0){
        }
        else{
            //get ids of selected checkboxes
            int index=0;
            for(int i=0;i<ids.length;i++){
                CheckBox checkBox=(CheckBox)findViewById(i);

                if(checkBox.isChecked()){
                    //presentStudents[index]=ids[i];
                    index++;
                }
            }

            presentStudents= new String[index];
            index=0;
            for(int i=0;i<ids.length;i++){
                CheckBox checkBox=(CheckBox)findViewById(i);

                if(checkBox.isChecked()){
                    presentStudents[index]=ids[i];
                    index++;
                }
            }

            if(presentStudents.length==0)
            {

            }
            else{

                // MHM Maybe used for login
                /*Intent intent =new Intent(this,MainActivity.class);
                intent.putExtra("presentStudents",presentStudents);
                intent.putExtra("groupId",groupId);
                this.startActivity(intent);*/
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try{
            PowerManager powermanager = (PowerManager)getSystemService(POWER_SERVICE);
            Resumed = powermanager.isScreenOn();
            MyService ms = new MyService(getApplicationContext());
            if(Resumed)
            {

            }
            else{
                Intent intent = new Intent(getApplicationContext(), StartingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
            }
        }
        catch (Exception e){
        }


        MultiPhotoSelectActivity.pauseFlg=true;

        MultiPhotoSelectActivity.cd = new CountDownTimer(MultiPhotoSelectActivity.duration, 1000) {
            //cd = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished)
            {
                MultiPhotoSelectActivity.duration = millisUntilFinished;
                timer = true;
            }
            @Override
            public void onFinish() {
                timer = false;
                MainActivity.sessionFlg=true;
                if(!CardAdapter.vidFlg) {
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    finishAffinity();
                }
            }
        }.start();
    }


}
