package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class StartingActivity extends Activity {

    private DevicePolicyManager mgr = null;
    private ComponentName cn = null;
    static String deviceId;
    public static String appname = "";
    public static String fpath;
    RadioGroup radioGroup, radioGroup1;
    public static int groupId = 0;
    public static boolean guestFlag = false;

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500;
    String id;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);

        MainActivity.sessionFlg=false;
        sessionContex=this;
        playVideo = new PlayVideo();

        id = String.valueOf(0);
        guestFlag = false;
        try {
            deviceId = ((TelephonyManager) getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();


            //Toast.makeText(getApplicationContext(),deviceIMEI,Toast.LENGTH_LONG).show();

            cn = new ComponentName(this, AdminReceiver.class);
            mgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

            if (getIntent().getBooleanExtra("Exit me", false)) {
                finish();
            }
            getSdCardPath();
            BackupDatabase.backup(getApplicationContext());
            lockMeNow();
        } catch (Exception e) {
        }



    }

    public void getSdCardPath() {
        CharSequence c = "";

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
        try {
            c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
            appname = c.toString();
            Log.w("LABEL", c.toString());
        } catch (Exception e) {//Name Not FOund Exception
        }

        if (appname.contains("Pratham Digital")) {
            if ((new File("/storage/extSdCard/.PrathamOpenSchool/HLearning/").exists()) && (new File("/storage/extSdCard/.PrathamOpenSchool/KhelBadi/").exists()) && (new File("/storage/extSdCard/.PrathamOpenSchool/KhelPuri").exists()) && (new File("/storage/extSdCard/.PrathamOpenSchool/Media/").exists()) && (new File("/storage/extSdCard/.PrathamOpenSchool/AddNewVideos/").exists())) {
                fpath = "/storage/extSdCard/";
            } else if ((new File("/storage/sdcard1/.PrathamOpenSchool/HLearning/").exists()) && (new File("/storage/sdcard1/.PrathamOpenSchool/KhelBadi/").exists()) && (new File("/storage/sdcard1/.PrathamOpenSchool/KhelPuri").exists()) && (new File("/storage/sdcard1/.PrathamOpenSchool/Media/").exists()) && (new File("/storage/sdcard1/.PrathamOpenSchool/AddNewVideos/").exists())) {
                fpath = "/storage/sdcard1/";
            } else if ((new File("/storage/usbcard1/.PrathamOpenSchool/HLearning/").exists()) && (new File("/storage/usbcard1/.PrathamOpenSchool/KhelBadi/").exists()) && (new File("/storage/usbcard1/.PrathamOpenSchool/KhelPuri").exists()) && (new File("/storage/usbcard1/.PrathamOpenSchool/Media/").exists()) && (new File("/storage/usbcard1/.PrathamOpenSchool/AddNewVideos/").exists())) {
                fpath = "/storage/usbcard1/";

            } else if ((new File("/storage/sdcard0/.PrathamOpenSchool/HLearning/").exists()) && (new File("/storage/sdcard0/.PrathamOpenSchool/KhelBadi/").exists()) && (new File("/storage/sdcard0/.PrathamOpenSchool/KhelPuri").exists()) && (new File("/storage/sdcard0/.PrathamOpenSchool/Media/").exists()) && (new File("/storage/sdcard0/.PrathamOpenSchool/AddNewVideos/").exists())) {
                fpath = "/storage/sdcard0/";

            } else if ((new File("/storage/emulated/0/.PrathamOpenSchool/HLearning/").exists()) && (new File("/storage/emulated/0/.PrathamOpenSchool/KhelBadi/").exists()) && (new File("/storage/emulated/0/.PrathamOpenSchool/KhelPuri").exists()) && (new File("/storage/emulated/0/.PrathamOpenSchool/Media/").exists()) && (new File("/storage/emulated/0/.PrathamOpenSchool/AddNewVideos/").exists())) {
                fpath = "/storage/emulated/0/";
            }
            fpath = fpath + ".PrathamOpenSchool/";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_starting, menu);
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


    public void goToGuestPage(View v) {
        /*FirstActivity.KEY = "Guest";
        Intent intent = new Intent(StartingActivity.this, FirstActivity.class);
        StartingActivity.this.startActivity(intent);*/
    }

    public void goToStudentPage(View v) {
       /* FirstActivity.KEY = "Normal";
        Intent intent = new Intent(StartingActivity.this, FirstActivity.class);
        StartingActivity.this.startActivity(intent);*/
    }

    public void lockMeNow() {
        if (!mgr.isAdminActive(cn)) {
            Intent intent =
                    new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, cn);
            startActivityForResult(intent, 1);

        } else {
            StatusDBHelper statusDBHelper = new StatusDBHelper(this);
            String pullFlag = statusDBHelper.getValue("pullFlag");
            if (pullFlag.equals("0")) {
            } else if (statusDBHelper.getValue("group1").equals("0") && statusDBHelper.getValue("group2").equals("0")) {
            } else {
                createContentElements();
            }
        }
    }

    public void createContentElements() {
        StatusDBHelper statusDBHelper = new StatusDBHelper(this);
        if (statusDBHelper.getValue("pullFlag").equals("0")){

        }
        else{}
        if (statusDBHelper.getValue("group1").equals("0") && statusDBHelper.getValue("group2").equals("0"))
        {}        else {

            TextView tv = (TextView) findViewById(R.id.adminUser);
            String group1 = (statusDBHelper.getValue("group1"));
            String group2 = (statusDBHelper.getValue("group2"));

            GroupDBHelper groupDBHelper = new GroupDBHelper(this);
            String groupName1 = groupDBHelper.getGroupById(String.valueOf(group1));
            String groupName2 = groupDBHelper.getGroupById(String.valueOf(group2));

            if (groupName1 != null && groupName2 != null) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.logInContent);
                TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

                radioGroup1 = new RadioGroup(this);
                radioGroup1.setGravity(Gravity.CENTER);
                RadioButton radioButton = new RadioButton(this);
                radioButton.setId(Integer.parseInt("3"));
                radioButton.setTag(group1);
                radioButton.setTextColor(Color.BLACK);
                radioButton.setText(groupName1);
                radioButton.setTextSize(20);
                radioGroup1.addView(radioButton);

                RadioButton radioButton1 = new RadioButton(this);
                radioButton1.setId(Integer.parseInt("4"));
                radioButton1.setTag(group2);
                radioButton1.setText(groupName2);
                radioButton1.setTextSize(20);
                radioButton1.setTextColor(Color.BLACK);
                radioGroup1.addView(radioButton1);

                int i = 0;

                RadioButton radioButton2 = new RadioButton(this);
                radioButton2.setId(i);
                radioButton2.setTag(String.valueOf(i));
                i++;
                radioButton2.setText("Guest");
                radioButton2.setTextSize(20);
                radioButton2.setTextColor(Color.BLACK);
                radioGroup1.addView(radioButton2);

                RadioButton radioButton3 = new RadioButton(this);
                radioButton3.setId(i);
                radioButton3.setTag(String.valueOf(i));
                radioButton3.setText("Other");
                radioButton3.setTextSize(20);
                radioButton3.setTextColor(Color.BLACK);
                radioGroup1.addView(radioButton3);

                ll.addView(radioGroup1);

                Button btn = new Button(this);
                btn.setText("LogIn");
                TableRow.LayoutParams tlp = new TableRow.LayoutParams(150, 50);
                tlp.gravity = Gravity.CENTER;
                tlp.setMargins(16, 32, 16, 16);
                btn.setLayoutParams(tlp);
                btn.setTextSize(20);
                btn.setTextColor(Color.WHITE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToApp(view);
                    }
                });
                btn.setBackgroundResource(R.color.button);

                ll.addView(btn);

            } else
                Toast.makeText(getApplicationContext(), "Unable to get Group names", Toast.LENGTH_LONG).show();
        }
    }

    public void goToApp(View v) {

        int rbId;
        rbId = radioGroup1.getCheckedRadioButtonId();
        // Toast.makeText(getApplicationContext(),"id: "+id,Toast.LENGTH_LONG).show();
        if (rbId == -1) {

        } else {

            RadioButton rb = (RadioButton) findViewById(rbId);
            id = (String) rb.getTag();
            Intent intent;
            switch (rbId) {
                case 0://for guest logIn
                 //   FirstActivity.KEY = "Guest";
                    /*intent =new Intent(StartingActivity.this,FirstActivity.class);
                    StartingActivity.this.startActivity(intent);*/
                    //whoseGuest();
                    break;
                case 1://for other group logIn
                 //   FirstActivity.KEY = "Normal";
                    intent = new Intent(this, LogInPage.class);
                    startActivity(intent);

                    break;
                default://for assigned group logIn
                  //  FirstActivity.KEY = "Normal";
                    StatusDBHelper statusDBHelper1 = new StatusDBHelper(this);
                    int count = statusDBHelper1.getTrailerCount(id);

                    if (count == 0) {
                        //if condition satisfied that means this grp has logged In for first time so play trailer
                        //and generate date for future trailer display
                        Random r = new Random();
                        int duration = r.nextInt(18 - 12) + 12;

                        File file = new File(fpath + "Media/trailer.mp4");

                        if (file.exists()) {
                            Uri path = Uri.fromFile(file);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "video/mp4");

                            try {
                                StatusDBHelper statusDBHelper2 = new StatusDBHelper(this);
                                boolean res = statusDBHelper2.updateTrailerCount(duration, id);
                                statusDBHelper2.updateOldTrailerCount(Math.round(duration / 2), id);
                                BackupDatabase.backup(this);
                                this.startActivityForResult(intent, 2);
                            } catch (Exception e) {
                                e.getStackTrace();
                                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(this);
                                syncActivityLogs.addToDB("whoseGuest-JSInterface", e, "Error");
                            }
                        } else {
                        }
                    } else {
                        int oldCount = statusDBHelper1.getOldTrailerCount(id);
                        if (oldCount == count) {
                            //play old trailer

                            File file = new File(splashScreenVideo.fpath + "Media/oldTrailer.mp4");

                            if (file.exists()) {
                                Uri path = Uri.fromFile(file);
                                intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(path, "video/mp4");

                                try {
                                    StatusDBHelper statusDBHelper2 = new StatusDBHelper(this);
                                    count--;
                                    boolean res = statusDBHelper2.updateTrailerCount(count, id);
                                    statusDBHelper2.updateOldTrailerCount(0, id);
                                    BackupDatabase.backup(this);
                                    this.startActivityForResult(intent, 2);
                                } catch (Exception e) {
                                    e.getStackTrace();
                                    SyncActivityLogs syncActivityLogs = new SyncActivityLogs(this);
                                    syncActivityLogs.addToDB("whoseGuest-JSInterface", e, "Error");
                                }
                            } else {
                            }

                        } else {
                            count--;
                            StatusDBHelper statusDBHelper2 = new StatusDBHelper(this);
                            boolean res = statusDBHelper2.updateTrailerCount(count, id);
                            BackupDatabase.backup(this);

                            intent = new Intent(this, SelectStudent.class);
                            intent.putExtra("groupId", id);
                            this.startActivity(intent);
                        }

                    }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            StatusDBHelper statusDBHelper = new StatusDBHelper(this);
            String pullFlag = statusDBHelper.getValue("pullFlag");
            if (pullFlag.equals("0")) {
            }
        } else if (requestCode == 2) {
            Intent intent;
            if (guestFlag) {
               // intent = new Intent(this, MainActivity.class);
                int presentStudents[] = new int[]{0};
                //intent.putExtra("presentStudents", presentStudents);
               // intent.putExtra("groupId", groupId);
            } else {
                intent = new Intent(this, SelectStudent.class);
                intent.putExtra("groupId", id);
            }
          //  this.startActivity(intent);
        }

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