package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class AddNewGroup extends AppCompatActivity {

    Spinner states_spinner, blocks_spinner, villages_spinner;
    EditText edt_NewGroupName;
    Button btn_Submit, btn_Clear;
    VillageDBHelper database;
    GroupDBHelper gdb;
    List<String> Blocks;
    int vilID;
    String deviceID = "";
    String deviceIMEI = "";
    UUID uuid;
    String randomUUIDGroup;
    Context villageContext, grpContext;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    Utility util;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_group);

        // Hide Actionbar
        getSupportActionBar().hide();

        sessionContex = this;
        playVideo = new PlayVideo();

        villageContext = this;
        database = new VillageDBHelper(villageContext);

        grpContext = this;
        util = new Utility();

        // Unique ID For GroupID
        uuid = UUID.randomUUID();
        randomUUIDGroup = uuid.toString();

        states_spinner = (Spinner) findViewById(R.id.spinner_SelectState);
        //Get Villages Data for States AllSpinners
        List<String> States = database.GetState();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, States);
        // Hint for AllSpinners
        states_spinner.setPrompt("Select State");
        states_spinner.setAdapter(StateAdapter);

        states_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states_spinner.getSelectedItem().toString();
                populateBlock(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        edt_NewGroupName = (EditText) findViewById(R.id.edt_NewGroupName);

        // Generate Unique Device ID
        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        /*//Device ID from Assign Groups
        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = tManager.getDeviceId();
        final String devID = deviceIMEI;
*/
        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        btn_Clear = (Button) findViewById(R.id.btn_Clear);

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check AllSpinners Emptyness
                int StatesSpinnerValue = states_spinner.getSelectedItemPosition();
                int BlocksSpinnerValue = blocks_spinner.getSelectedItemPosition();
                int VillagesSpinnerValue = villages_spinner.getSelectedItemPosition();
                gdb = new GroupDBHelper(grpContext);

                if (StatesSpinnerValue > 0 && BlocksSpinnerValue > 0 && VillagesSpinnerValue > 0) {

                    String GroupName = edt_NewGroupName.getText().toString();
                    if ((GroupName.matches("[a-zA-Z0-9 ]*")) && (GroupName.length() > 0 && GroupName.length() < 21)) {

                        Group grpobj = new Group();

                        grpobj.GroupID = randomUUIDGroup;
                        grpobj.GroupCode = "";
                        grpobj.GroupName = edt_NewGroupName.getText().toString();
                        grpobj.UnitNumber = "";
                        grpobj.DeviceID = deviceID.equals(null) ? "0000" : deviceID;
                        grpobj.Responsible = "";
                        grpobj.ResponsibleMobile = "";
                        grpobj.VillageID = Integer.parseInt(String.valueOf(vilID));
                        grpobj.ProgramID = Integer.parseInt(MultiPhotoSelectActivity.programID);
                        grpobj.CreatedBy = CrlDashboard.CreatedBy;
                        grpobj.VillageName = "";
                        grpobj.SchoolName = "";
                        grpobj.newGroup = true;
                        grpobj.CreatedOn = util.GetCurrentDateTime(false).toString();

                        gdb.insertData(grpobj);
                        Toast.makeText(AddNewGroup.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                        BackupDatabase.backup(grpContext);
                        FormReset();
                    } else {
                        Toast.makeText(AddNewGroup.this, "Please Enter less than 21 Alphabets only as Group Name !!!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AddNewGroup.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormReset();
            }
        });


    }

    public void populateBlock(String selectedState) {
        blocks_spinner = (Spinner) findViewById(R.id.spinner_SelectBlock);
        //Get Villages Data for Blocks AllSpinners
        Blocks = database.GetStatewiseBlock(selectedState);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> BlockAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, Blocks);
        // Hint for AllSpinners
        blocks_spinner.setPrompt("Select Block");
        blocks_spinner.setAdapter(BlockAdapter);

        blocks_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBlock = blocks_spinner.getSelectedItem().toString();
                populateVillage(selectedBlock);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlAddEditScreen.class);
        this.finish();
        startActivity(intent);
    }*/

    public void populateVillage(String selectedBlock) {
        villages_spinner = (Spinner) findViewById(R.id.spinner_selectVillage);
        //Get Villages Data for Villages filtered by block for Spinners
        List<VillageList> BlocksVillages = database.GetVillages(selectedBlock);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<VillageList> VillagesAdapter = new ArrayAdapter<VillageList>(this, R.layout.custom_spinner, BlocksVillages);
        // Hint for AllSpinners
        villages_spinner.setPrompt("Select Village");
        villages_spinner.setAdapter(VillagesAdapter);
        villages_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VillageList village = (VillageList) parent.getItemAtPosition(position);
                vilID = village.getVillageId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void FormReset() {

        states_spinner.setSelection(0);
        blocks_spinner.setSelection(0);
        villages_spinner.setSelection(0);
        edt_NewGroupName.getText().clear();
        // Unique ID For GroupID
        uuid = UUID.randomUUID();
        randomUUIDGroup = uuid.toString();
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