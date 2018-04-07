package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class LogInPage extends Activity {

    ArrayAdapter<String> adapter;
    static String groupId="0";
    static String deviceId;
    int glbVillageId=0; DBHelper db;
    public String ids[]; int index=0;
    RadioGroup radioGroup,radioGroup1;
    public static SharedPreferences sharedPreferences;
    StatusDBHelper statusDBHelper;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_page);

        MainActivity.sessionFlg=false;
        sessionContex=this;
        playVideo = new PlayVideo();

        BackupDatabase.backup(getApplicationContext());

        try{

            // String android_id = Settings.Secure.getString(this.getContentResolver(),
            //        Settings.Secure.ANDROID_ID);
            deviceId = ((TelephonyManager)getApplicationContext()
                    .getSystemService( Context.TELEPHONY_SERVICE ))
                    .getDeviceId();
            //deviceId="0";
            // Toast.makeText(this,deviceId,Toast.LENGTH_LONG).show();

            VillageDBHelper villageDBHelper;
            //final AllSpinners dropdown1 = (AllSpinners)findViewById(R.id.spinner1);
            villageDBHelper = new VillageDBHelper(this);


            //sharedPreferences=getApplicationContext().getSharedPreferences("SelectedValues",getApplicationContext().MODE_PRIVATE);

            //checking if app is installed for first time or not?
            //Informing user about pulling data from server to tablet.
            statusDBHelper = new StatusDBHelper(getApplicationContext());
            String pullFlag = statusDBHelper.getValue("pullFlag");
            //if(sharedPreferences.getInt("pullFlag", 0)==0 && sharedPreferences.getString("State","")=="")
            if(pullFlag.equals("0"))
            {
                showDialogue("Data is not available. Please first LogIn as admin user.");
            }
            else{
                StatusDBHelper statusDBHelper = new StatusDBHelper(getApplicationContext());
                glbVillageId = Integer.parseInt(statusDBHelper.getValue("village"));

                if(glbVillageId==0){
                    showDialogue("Assign groups to this tablet. Please LogIn as admin user.");
                }
                else{
                    String group1 = (statusDBHelper.getValue("group1"));
                    String group2 = (statusDBHelper.getValue("group2"));

                    GroupDBHelper groupDBHelper=new GroupDBHelper(LogInPage.this);
                    List<GroupList> list =groupDBHelper.GetGroups(glbVillageId);
                    list.remove(0);

                    for(int i=0;i<list.size();i++){
                        if(list.get(i).getGroupId()==group1)
                            list.remove(i);
                        if(list.get(i).getGroupId()==group2)
                            list.remove(i);
                    }

                    if(list==null){
                        showDialogue("Group list is empty.Please contact your administrator.");
                    }
                    else{


                        LinearLayout my_layout = (LinearLayout)findViewById(R.id.otherGroups1);
                        LinearLayout my_layout1 = (LinearLayout)findViewById(R.id.otherGroups2);

                        ids= new String[list.size()];
                        int half=Math.round(list.size()/2);
                        radioGroup = new RadioGroup(this);

                        for (int i = 0; i < list.size(); i++)
                        {
                            TableRow row =new TableRow(this);
                            row.setId(i);

                            //dynamically create checkboxes. i.e no. of students in group = no. of checkboxes
                            row.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

                            CheckBox checkBox = new CheckBox(this);

                            try {
                                GroupList obj=list.get(i);
                                checkBox.setId(i);
                                checkBox.setTag(obj.getGroupId());
                                ids[i]=obj.getGroupId();
                                checkBox.setText(obj.getGroupName());


                            } catch (Exception e) {
                                SyncActivityLogs syncActivityLogs=new SyncActivityLogs(getApplicationContext());
                                syncActivityLogs.addToDB("onCreate-SelectStudent",e,"Error");
                                BackupDatabase.backup(getApplicationContext());
                                e.printStackTrace();
                            }
                            checkBox.setTextSize(35);
                            //checkBox.setButtonDrawable(R.drawable.checkbox);
                            checkBox.setTextColor(Color.BLACK);
                            row.addView(checkBox);
                            if(i%2==0)
                                my_layout.addView(row);
                            else
                                my_layout1.addView(row);
                        }

                    }
                }
            }
        }
        catch (Exception e){

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //gtMenuInflater().inflate(R.menu.menu_log_in_page, menu);
        return true;
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
        try{

            StatusDBHelper statusDBHelper = new StatusDBHelper(getApplicationContext());
            String pullFlag = statusDBHelper.getValue("pullFlag");
            if(pullFlag.equals("0"))
            {
                showDialogue("Data is not available. Please first LogIn as admin user.");
            }
            else{
                //need to check if group is previously present in status table
                //if so check trailer date...if it matches with currentdate then show trailer and update date and if doesn't match with cur date then just go to next page
                //else add new group to database, show trailer update date...

                int index=0;
                for(int i=0;i<ids.length;i++){
                    CheckBox checkBox=(CheckBox)findViewById(i);

                    if(checkBox.isChecked()){
                        //presentStudents[index]=ids[i];
                        index++;
                    }
                }

                if(index==0)
                    showDialogue("Please select your group.");
                else if(index>=2)
                    showDialogue("You can select only one group.");
                else if(index==1){
                    for(int i=0;i<ids.length;i++){
                        CheckBox checkBox=(CheckBox)findViewById(i);

                        if(checkBox.isChecked()){
                            //presentStudents[index]=ids[i];
                            groupId = (String) checkBox.getTag();break;
                        }
                    }

                    StatusDBHelper statusDBHelper2 = new StatusDBHelper(getApplicationContext());
                    boolean res = statusDBHelper2.checkGroupIdExists(groupId);
                    int count = 0;
                    if(res){
                        //current group is already present in status table now check showTrailerDate...
                        statusDBHelper2 = new StatusDBHelper(getApplicationContext());
                        count = statusDBHelper2.getTrailerCount(groupId);
                        if(count == 0){
                            //show trailer, update date and then go to next page...

                            Random r = new Random();
                            int duration = r.nextInt(18 - 12) + 12;

                            File file = new File(StartingActivity.fpath+"trailer.mp4");

                            if (file.exists()) {
                                Uri path = Uri.fromFile(file);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(path, "video/mp4");

                                try {
                                    statusDBHelper2 = new StatusDBHelper(getApplicationContext());
                                    statusDBHelper2.updateTrailerCount(duration, groupId);
                                    statusDBHelper2.updateOldTrailerCount(Math.round(duration/2),groupId);
                                    BackupDatabase.backup(getApplicationContext());
                                    this.startActivityForResult(intent, 1);
                                }
                                catch (Exception e) {
                                    e.getStackTrace();
                                    SyncActivityLogs syncActivityLogs=new SyncActivityLogs(getApplicationContext());
                                    syncActivityLogs.addToDB("goToNextPage-LogInPage",e,"Error");
                                }
                            }
                            else{
                            }
                        }
                        else{
                            int oldCount = statusDBHelper2.getOldTrailerCount(groupId);
                            if(oldCount == count){
                                File file = new File(StartingActivity.fpath+"oldTrailer.mp4");

                                if (file.exists()) {
                                    Uri path = Uri.fromFile(file);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(path, "video/mp4");

                                    try {
                                        statusDBHelper2 = new StatusDBHelper(getApplicationContext());
                                        count--;
                                        statusDBHelper2.updateTrailerCount(count, groupId);
                                        statusDBHelper2.updateOldTrailerCount(0,groupId);
                                        BackupDatabase.backup(getApplicationContext());
                                        this.startActivityForResult(intent, 1);
                                    }
                                    catch (Exception e) {
                                        e.getStackTrace();
                                        SyncActivityLogs syncActivityLogs=new SyncActivityLogs(getApplicationContext());
                                        syncActivityLogs.addToDB("goToNextPage-LogInPage",e,"Error");
                                    }
                                }
                                else{
                                }
                            }
                            else{
                                count--;
                                statusDBHelper2 = new StatusDBHelper(getApplicationContext());
                                res = statusDBHelper2.updateTrailerCount(count,groupId);
                                BackupDatabase.backup(getApplicationContext());

                                Intent intent =new Intent(this,SelectStudent.class);
                                intent.putExtra("groupId", groupId);
                                this.startActivity(intent);
                            }
                        }
                    }
                    else{
                        //selected group doesn't exists in status table so add new record for this
                        //and show trailer and update trailer date for future
                        Random r = new Random();
                        int duration = r.nextInt(18 - 12) + 12;

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        cal.add(Calendar.DAY_OF_YEAR, duration);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                        String dayAfterDate = dateFormat.format(cal.getTime());
                        // Toast.makeText(getApplicationContext(),"date"+dayAfterDate,Toast.LENGTH_LONG).show();

                        File file = new File(StartingActivity.fpath+"trailer.mp4");

                        if (file.exists()) {
                            Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "video/mp4");

                            try {
                                statusDBHelper2 = new StatusDBHelper(getApplicationContext());
                                //statusDBHelper2.updateTrailerDate(dayAfterDate,groupId);
                                //insert these values to status table
                                statusDBHelper2.addToStatusTable("newGroup",groupId,duration,Math.round(duration / 2));
                                BackupDatabase.backup(getApplicationContext());
                                this.startActivityForResult(intent, 1);
                            }
                            catch (Exception e) {
                                e.getStackTrace();
                                SyncActivityLogs syncActivityLogs=new SyncActivityLogs(getApplicationContext());
                                syncActivityLogs.addToDB("goToNextPage-LogInPage",e,"Error");
                            }
                        }
                        else{
                        }

                    }
                    BackupDatabase.backup(getApplicationContext());
                }
            }
        }
        catch (Exception e){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            Intent intent =new Intent(this,SelectStudent.class);
            intent.putExtra("groupId", groupId);
            this.startActivity(intent);
        }
    }

    public void showDialogue(String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(LogInPage.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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
    protected void onPause() {
        super.onPause();

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
