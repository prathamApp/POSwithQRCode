package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class EditStudent extends AppCompatActivity {

    Spinner states_spinner, blocks_spinner, villages_spinner, groups_spinner, existingStudent_Spinner;
    TextView edt_Fname, edt_Mname, edt_Lname, edt_Age, edt_Class, tv_Gender;
    Button btn_Submit, btn_Clear, btn_Capture;
    VillageDBHelper database;
    GroupDBHelper gdb;
    StudentDBHelper sdb;
    String GrpID;
    List<String> Blocks;
    int vilID;
    Context villageContext, grpContext, stdContext;
    Utility Util;
    String gender;
    List<StudentList> ExistingStudents;
    String StudentID;
    String FirstName;
    String MiddleName;
    String LastName;
    int Age;
    int Class;
    String Gender;
    private static final int TAKE_Thumbnail = 1;
    ImageView imgView;
    private static String TAG = "PermissionDemo";
    private static final int REQUEST_WRITE_STORAGE = 112;
    Uri uriSavedImage;
    String StudentUniqID;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    StatusDBHelper statdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();


        statdb = new StatusDBHelper(sessionContex);

        villageContext = this;
        database = new VillageDBHelper(villageContext);

        grpContext = this;

        stdContext = this;
        sdb = new StudentDBHelper(stdContext);


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


        edt_Fname = (TextView) findViewById(R.id.edt_FirstName);
        edt_Mname = (TextView) findViewById(R.id.edt_MiddleName);
        edt_Lname = (TextView) findViewById(R.id.edt_LastName);
        edt_Age = (TextView) findViewById(R.id.edt_Age);
        edt_Class = (TextView) findViewById(R.id.edt_Class);
        tv_Gender = (TextView) findViewById(R.id.tv_Gender);

        btn_Capture = (Button) findViewById(R.id.btn_Capture);
        imgView = (ImageView) findViewById(R.id.imageView);

        btn_Capture.setVisibility(View.GONE);
        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        btn_Clear = (Button) findViewById(R.id.btn_Clear);

        Util = new Utility();

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check AllSpinners Emptyness
                int StatesSpinnerValue = states_spinner.getSelectedItemPosition();
                int BlocksSpinnerValue = blocks_spinner.getSelectedItemPosition();
                int VillagesSpinnerValue = villages_spinner.getSelectedItemPosition();
                int GroupsSpinnerValue = groups_spinner.getSelectedItemPosition();
                int ExistingSpinnerValue = existingStudent_Spinner.getSelectedItemPosition();


                if (StatesSpinnerValue > 0 && BlocksSpinnerValue > 0 && VillagesSpinnerValue > 0 && GroupsSpinnerValue > 0 && ExistingSpinnerValue > 0) {

                    Toast.makeText(EditStudent.this, "Photo Inserted Successfully !!!", Toast.LENGTH_SHORT).show();

                    existingStudent_Spinner.setSelection(0);

                    edt_Fname.setText("");
                    edt_Mname.setText("");
                    edt_Lname.setText("");
                    edt_Age.setText("");
                    edt_Class.setText("");
                    imgView.setImageDrawable(null);


                } else {
                    Toast.makeText(EditStudent.this, "Please Select Fill all fields !!!", Toast.LENGTH_SHORT).show();
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
                populateGroups(vilID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void populateGroups(int villageID) {
        groups_spinner = (Spinner) findViewById(R.id.spinner_SelectGroups);
        //Get Groups Data for Villages filtered by Villages for Spinners
        grpContext = this;
        gdb = new GroupDBHelper(grpContext);
        final List<GroupList> GroupsVillages = gdb.GetGroups(villageID);
        //GroupsVillages.get(0).getGroupId();
        //Creating the ArrayAdapter instance having the Villages list
        final ArrayAdapter<GroupList> GroupsAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, GroupsVillages);
        // Hint for AllSpinners
        groups_spinner.setPrompt("Select Group");
        groups_spinner.setAdapter(GroupsAdapter);
        groups_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GroupName = groups_spinner.getSelectedItem().toString();
                //GrpID = GroupsVillages.get(0).getGroupId();
                GroupList SelectedGroupData = GroupsAdapter.getItem(groups_spinner.getSelectedItemPosition());
                GrpID = SelectedGroupData.getGroupId();
                String Id = GrpID;
                //Toast.makeText(EditStudent.this, "Group ID is "+Id, Toast.LENGTH_SHORT).show();
                populateExistingStudents(Id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void populateExistingStudents(String GroupID) {

        existingStudent_Spinner = (Spinner) findViewById(R.id.spinner_existingStudent);

        stdContext = this;
        sdb = new StudentDBHelper(stdContext);

        ExistingStudents = sdb.GetAllStudentsByGroupID(GroupID);

        final ArrayAdapter<StudentList> ExistingStudentAdapter = new ArrayAdapter<StudentList>(this, R.layout.custom_spinner, ExistingStudents);
        ExistingStudentAdapter.setDropDownViewResource(R.layout.custom_spinner);
        //existingStudent_Spinner.setPrompt("Select Existing Student");
        existingStudent_Spinner.setAdapter(ExistingStudentAdapter);
        existingStudent_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String StdID = existingStudent_Spinner.getSelectedItem().toString();
                StudentList SelectedStudentData = ExistingStudentAdapter.getItem(existingStudent_Spinner.getSelectedItemPosition());
                StudentUniqID = SelectedStudentData.getStudentID();

                try {
                    populateStudentData(StudentUniqID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void populateStudentData(String studentUniqID) {

        //  btn_Capture.setVisibility(View.VISIBLE);


        Student SelectedStudent = sdb.GetStudentDataByStdID(studentUniqID);

        if (SelectedStudent == null) {
            Toast.makeText(EditStudent.this, "Sorry !!! No Data Found !!!", Toast.LENGTH_SHORT).show();
        } else {

            FirstName = SelectedStudent.FirstName;
            MiddleName = SelectedStudent.MiddleName;
            LastName = SelectedStudent.LastName;
            Age = SelectedStudent.Age;

            String gen = SelectedStudent.Gender;
            if (gen.equals("Male") || gen.equals("M") || gen.equals("1")) {
                Gender = "Male";
            } else if (gen.equals("Female") || gen.equals("F") || gen.equals("2")) {
                Gender = "Female";
            } else {
                // Default
                Gender = "Male";
            }

            String cls = String.valueOf(SelectedStudent.Class);
            if (cls.length() > 0) {
                Class = SelectedStudent.Class;
            } else {
                Class = 0;
            }

        }

        if (FirstName == null) {
            btn_Capture.setVisibility(View.GONE);

        } else {

            edt_Fname.setText(FirstName);
            edt_Mname.setText(MiddleName);
            edt_Lname.setText(LastName);
            edt_Age.setText(String.valueOf(Age));
            edt_Class.setText(String.valueOf(Class));
            tv_Gender.setText(Gender);

            btn_Capture.setVisibility(View.VISIBLE);
            btn_Capture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_Thumbnail);
                }
            });

        }

    }


    public void FormReset() {

        states_spinner.setSelection(0);
        blocks_spinner.setSelection(0);
        villages_spinner.setSelection(0);
        groups_spinner.setSelection(0);
        existingStudent_Spinner.setSelection(0);

        edt_Fname.setText("");
        edt_Mname.setText("");
        edt_Lname.setText("");
        edt_Age.setText("");
        edt_Class.setText("");

        imgView.setImageDrawable(null);

    }


    // MHM Code Old
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TAKE_Thumbnail) {
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bitmap thumbnail1 = data.getParcelableExtra("data");
                    imgView.setImageBitmap(thumbnail1);
                    try {

                        Context cnt;
                        cnt = this;
                        File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/");
                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdir();
                        }
                        if (success) {
                            // Do something on success
                            File outputFile = new File(folder, StudentUniqID + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                            thumbnail1.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            data = null;
                            thumbnail1 = null;
                            // To Refresh Contents of sharableFolder
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
                        } else {
                            // Do something else on failure
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CrlAddEditScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlAddEditScreen.class);
        this.finish();
        startActivity(intent);
    }*/


}

