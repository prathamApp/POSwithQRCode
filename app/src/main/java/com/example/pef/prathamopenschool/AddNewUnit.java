package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class AddNewUnit extends AppCompatActivity {

    Spinner states_spinner, blocks_spinner;
    EditText edt_VillageName, edt_SchoolName, edt_UnitCode;
    Button btn_Submit, btn_Clear;
    GroupDBHelper gdb;
    List<String> Blocks;
    String deviceID = "";
    VillageDBHelper database;
    String deviceIMEI = "";
    int vilID;
    String randomUUIDGroup;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String selectedBlock;
    static String From = "Create";
    String selectedState;
    UUID uuid;
    Context villageContext, grpContext;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    StatusDBHelper statdb;
    Utility util;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_unit);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        statdb = new StatusDBHelper(sessionContex);

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
                selectedState = states_spinner.getSelectedItem().toString();
                populateBlock(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Program ID (1=HLearning, 2=ReadIndia(KP))
        edt_VillageName = (EditText) findViewById(R.id.edt_VillageName);

        edt_SchoolName = (EditText) findViewById(R.id.edt_SchoolName);

        edt_UnitCode = (EditText) findViewById(R.id.edt_UnitCode);


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

                gdb = new GroupDBHelper(grpContext);

                String UnitCode = edt_UnitCode.getText().toString();
                int UnitCodeSize = UnitCode.length();
                if (UnitCodeSize != 11) {
                    Toast.makeText(AddNewUnit.this, "Unit Code Should be Exactly 11 Characters Long !!!", Toast.LENGTH_SHORT).show();
                } else if (UnitCodeSize == 11) {

                    if (StatesSpinnerValue > 0 && BlocksSpinnerValue > 0) {

                        if (!edt_VillageName.getText().toString().isEmpty() && !edt_SchoolName.getText().toString().isEmpty()) {

                            if ((edt_VillageName.getText().toString().matches("[a-zA-Z0-9 ]*")) && (edt_SchoolName.getText().toString().matches("[a-zA-Z0-9 ]*"))
                                    && (UnitCode.matches("[a-zA-Z0-9]*"))) {


                                Group grpobj = new Group();

                                grpobj.GroupID = randomUUIDGroup;
                                grpobj.GroupCode = "";
                                grpobj.GroupName = UnitCode;
                                grpobj.UnitNumber = "";
                                grpobj.VillageName = edt_VillageName.getText().toString();
                                grpobj.DeviceID = deviceID.equals(null) ? "0000" : deviceID;
                                grpobj.SchoolName = edt_SchoolName.getText().toString();
                                grpobj.Responsible = "";
                                grpobj.ResponsibleMobile = "";
                                grpobj.CreatedBy = statdb.getValue("CRL");
                                grpobj.newGroup = true;
                                grpobj.VillageID = vilID;
                                grpobj.ProgramID = Integer.parseInt(MultiPhotoSelectActivity.programID);
                                grpobj.CreatedOn = util.GetCurrentDateTime(false).toString();

                                gdb.insertData(grpobj);


                      /*  Village vilObj = new Village();

                        vilObj.VillageID = vilID;
                        vilObj.VillageName = edt_VillageName.getText().toString();
                        vilObj.VillageCode = "";
                        vilObj.District = "";
                        vilObj.Block = selectedBlock;
                        vilObj.CRLID = CrlDashboard.CreatedBy;
                        vilObj.State = selectedState;

                        database.insertData(vilObj);*/

                                // Unique ID For GroupID
                       /* uuid = UUID.randomUUID();
                        randomUUIDGroup = uuid.toString();*/
                                BackupDatabase.backup(grpContext);


                                Toast.makeText(AddNewUnit.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();

                    /*// Initialization of Session
                    pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                    editor = pref.edit();

                    // Storing Data
                    editor.putString("Group ID", randomUUIDGroup); // Storing string
                    editor.putString("Group Name", edt_UnitCode.getText().toString()); // Storing string
                    editor.putString("Village Name", edt_VillageName.getText().toString()); // Storing string
                    editor.putString("School Name", edt_SchoolName.getText().toString()); // Storing string

                    // Commitin Changes
                    editor.commit();*/

                                // Go To next Page for adding students
                                goToSelectClassForUC();


                                FormReset();

                            } else {
                                Toast.makeText(AddNewUnit.this, " Please Enter Valid Input !!!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AddNewUnit.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddNewUnit.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                    }
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


    private void goToSelectClassForUC() {

        Intent goToSelectClassForUC = new Intent(AddNewUnit.this, SelectClassForUniversalChild.class);

        goToSelectClassForUC.putExtra("Group ID", randomUUIDGroup);
        goToSelectClassForUC.putExtra("UnitName", edt_UnitCode.getText().toString());
        goToSelectClassForUC.putExtra("Village", edt_VillageName.getText().toString());
        goToSelectClassForUC.putExtra("School", edt_SchoolName.getText().toString());
        goToSelectClassForUC.putExtra("From", From);

        startActivity(goToSelectClassForUC);
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
                selectedBlock = blocks_spinner.getSelectedItem().toString();
                populateVillage(selectedBlock);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void populateVillage(String selectedBlock) {
        //Get Villages Data for Villages filtered by block for Spinners
        vilID = database.GetVillageIDByBlock(selectedBlock);
    }


    public void FormReset() {

        states_spinner.setSelection(0);
        blocks_spinner.setSelection(0);
        edt_VillageName.getText().clear();
        edt_SchoolName.getText().clear();
        edt_UnitCode.getText().clear();
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

    //**@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CrlAddEditScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}