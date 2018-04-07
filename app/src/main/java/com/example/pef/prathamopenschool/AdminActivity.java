package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class AdminActivity extends AppCompatActivity {

    Context context;
    EditText edtAdmin, edtPass;
    Button btn_Login;

    Context sessionContex;
    String crlID;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    TextView tv_version_code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

//        Toast.makeText(this, "TimeStamp = " + MyApplication.getAccurateTimeStamp(), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Date = " + MyApplication.getAccurateDate(), Toast.LENGTH_LONG).show();

        // Displaying Version Code of App
        tv_version_code = (TextView) findViewById(R.id.tv_Version);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String verCode = pInfo.versionName;
            tv_version_code.setText(String.valueOf(verCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        context = this;

        //Opening SQLite Pipeline
        final CrlDBHelper db = new CrlDBHelper(context);
        db.getReadableDatabase();

        edtAdmin = (EditText) findViewById(R.id.txtAdminUsername);
        edtPass = (EditText) findViewById(R.id.txtAdminPassword);
        btn_Login = (Button) findViewById(R.id.adminLogIn);


        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredUserName = edtAdmin.getText().toString();
                String enteredPassWord = edtPass.getText().toString();


                boolean result;
                result = db.CrlLogin(enteredUserName, enteredPassWord);


                if (result == true) {

                    crlID = db.getCrlID(enteredUserName, enteredPassWord);

                    // Writing Created by in Status Table
                    StatusDBHelper sdb;
                    sdb = new StatusDBHelper(context);
                    sdb.Update("CRL", crlID);


                    Intent intent = new Intent(AdminActivity.this, CrlDashboard.class);
                    intent.putExtra("UserName", enteredUserName);
                    intent.putExtra("CreatedBy", crlID);
                    startActivity(intent);

                    edtAdmin.setText("");
                    edtPass.setText("");
                } else {
                    Toast.makeText(AdminActivity.this, "Invalid Credentials !!!", Toast.LENGTH_SHORT).show();
                    edtAdmin.setText("");
                    edtPass.setText("");
                }

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
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

    @Override
    public void onBackPressed() {
//        Intent main = new Intent(AdminActivity.this, MultiPhotoSelectActivity.class);
//        main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(main);
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

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
                    finishAffinity();
                }
            }
        }.start();

    }

}
