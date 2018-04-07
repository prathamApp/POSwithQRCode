package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class AddNewCrl extends AppCompatActivity {


    // NOTE: If old Username is entered then the data will be replaced else new record will be created

    EditText edt_Crlid, edt_Fname, edt_Lname, edt_Username, edt_Password, edt_Mobile, edt_Email;
    Spinner spinner_State;
    Button btn_Submit, btn_Clear;
    Context context;

    String randomUUIDCrl;
    UUID uuid;


    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    StatusDBHelper statdb;

    Utility util;

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlAddEditScreen.class);
        this.finish();
        startActivity(intent);
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_crl);
        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        context = this;


        statdb = new StatusDBHelper(context);

        //edt_Crlid = (EditText) findViewById(R.id.edt_CrlID);

        edt_Fname = (EditText) findViewById(R.id.edt_Fname);
        edt_Lname = (EditText) findViewById(R.id.edt_Lname);
        edt_Username = (EditText) findViewById(R.id.edt_Username);
        edt_Password = (EditText) findViewById(R.id.edt_Password);
        edt_Mobile = (EditText) findViewById(R.id.edt_Mobile);
        edt_Email = (EditText) findViewById(R.id.edt_Email);

        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        btn_Clear = (Button) findViewById(R.id.btn_Clear);

        spinner_State = (Spinner) findViewById(R.id.spinner_State);

        //Get Villages Data for States AllSpinners
        VillageDBHelper database = new VillageDBHelper(context);
        List<String> States = database.GetState();

        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, States);

        // Hint for AllSpinners
        spinner_State.setPrompt("Select State");
        spinner_State.setAdapter(StateAdapter);

        util = new Utility();
        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check Emptyness
                if ((!edt_Fname.getText().toString().isEmpty() || !edt_Lname.getText().toString().isEmpty()) && (!edt_Username.getText().toString().isEmpty() && !edt_Password.getText().toString().isEmpty())) {

                    // Validations
                    if ((edt_Fname.getText().toString().matches("[a-zA-Z.? ]*")) && (edt_Lname.getText().toString().matches("[a-zA-Z.? ]*"))
                            && (edt_Email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
                            && (edt_Mobile.getText().toString().matches("[0-9]+")) && (edt_Mobile.getText().toString().length() == 10)) {

                        //CRL Initial DB Process
                        CrlDBHelper db = new CrlDBHelper(context);

                        // Check existing CRLID Entry
                        Boolean result = db.GetCrlUserName(edt_Username.getText().toString());

                        if (result == true) {
                            String fetchedCrlID = db.getCrlIDFromUsername(edt_Username.getText().toString());
                            UpdateCrl(fetchedCrlID, edt_Username.getText().toString());
                        } else if (result == false) {

                            Crl crlobj = new Crl();

                            // Unique ID For GroupID
                            uuid = UUID.randomUUID();

                            // UUID in case of new crl
                            crlobj.CRLId = uuid.toString();  // Entering Random UUID for CRLID
                            crlobj.FirstName = edt_Fname.getText().toString();
                            crlobj.LastName = edt_Lname.getText().toString();
                            crlobj.UserName = edt_Username.getText().toString();
                            crlobj.Password = edt_Password.getText().toString();
                            crlobj.ProgramId = Integer.parseInt(MultiPhotoSelectActivity.programID);
                            crlobj.Mobile = edt_Mobile.getText().toString();
                            crlobj.State = spinner_State.getSelectedItem().toString(); // get Selected Item
                            crlobj.Email = edt_Email.getText().toString();
                            crlobj.CreatedBy = statdb.getValue("CRL");
                            crlobj.newCrl = true;

                            crlobj.CreatedOn = util.GetCurrentDateTime(false);

                            // Check AllSpinners Emptyness
                            int SpinnerValue = spinner_State.getSelectedItemPosition();
                            if (SpinnerValue > 0) {

                                db.insertData(crlobj);
                                Toast.makeText(AddNewCrl.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                                BackupDatabase.backup(context);
                                FormReset();
                            } else {
                                Toast.makeText(AddNewCrl.this, "Please Select the State !!!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }// Validations
                    else {
                        Toast.makeText(AddNewCrl.this, "Please Enter Valid Input !!!", Toast.LENGTH_SHORT).show();
                    }
                }// Emptyness
                else {
                    Toast.makeText(AddNewCrl.this, "Please Fill All Fields !!!", Toast.LENGTH_SHORT).show();
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

    private void UpdateCrl(String fetchedCrlID, String username) {

        //CRL Initial DB Process
        CrlDBHelper db = new CrlDBHelper(context);

        Crl crlobj = new Crl();

        crlobj.CRLId = fetchedCrlID;  // Entering Random UUID for CRLID
        crlobj.FirstName = edt_Fname.getText().toString();
        crlobj.LastName = edt_Lname.getText().toString();
        crlobj.UserName = username;
        crlobj.Password = edt_Password.getText().toString();
        crlobj.ProgramId = Integer.parseInt(MultiPhotoSelectActivity.programID);
        crlobj.Mobile = edt_Mobile.getText().toString();
        crlobj.State = spinner_State.getSelectedItem().toString(); // get Selected Item
        crlobj.Email = edt_Email.getText().toString();
        crlobj.CreatedBy = statdb.getValue("CRL");
        crlobj.newCrl = true;

        // Check AllSpinners Emptyness
        int SpinnerValue = spinner_State.getSelectedItemPosition();
        if (SpinnerValue > 0) {

            db.replaceData(crlobj);
            Toast.makeText(AddNewCrl.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
            BackupDatabase.backup(context);
            FormReset();
        } else {
            Toast.makeText(AddNewCrl.this, "Please Select State !!!", Toast.LENGTH_SHORT).show();
        }

    }

    public void FormReset() {

        //edt_Crlid.getText().clear();
        edt_Fname.getText().clear();
        edt_Lname.getText().clear();
        edt_Username.getText().clear();
        edt_Password.getText().clear();
        edt_Mobile.getText().clear();
        edt_Email.getText().clear();

        spinner_State.setSelection(0);
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
