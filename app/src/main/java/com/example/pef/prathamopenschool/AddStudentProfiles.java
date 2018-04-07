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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

public class AddStudentProfiles extends AppCompatActivity {

    Spinner states_spinner, blocks_spinner, villages_spinner, groups_spinner;
    EditText edt_Fname, edt_Mname, edt_Lname, edt_Age, edt_Class;
    RadioGroup rg_Gender;
    Button btn_Submit, btn_Clear, btn_Capture;
    VillageDBHelper database;
    GroupDBHelper gdb;
    StudentDBHelper sdb;
    RadioButton rb_Male, rb_Female;
    String GrpID;
    List<String> Blocks;
    int vilID;
    Context villageContext, grpContext, stdContext;
    Utility Util;
    String gender;
    List<String> ExistingStudents;
    String StudentID, FirstName, MiddleName, LastName, Age, Class, UpdatedDate, Gender;
    String randomUUIDStudent;
    private static final int TAKE_Thumbnail = 1;
    ImageView imgView;
    private static String TAG = "PermissionDemo";
    private static final int REQUEST_WRITE_STORAGE = 112;
    Uri uriSavedImage;
    UUID uuStdid;
    RadioButton selectedGender;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    StatusDBHelper statdb;

    Utility util;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_profiles);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        statdb = new StatusDBHelper(sessionContex);


        rb_Male = (RadioButton) findViewById(R.id.rb_Male);
        rb_Female = (RadioButton) findViewById(R.id.rb_Female);

        villageContext = this;
        database = new VillageDBHelper(villageContext);

        grpContext = this;

        stdContext = this;
        sdb = new StudentDBHelper(stdContext);

        // Unique ID For StudentID
        uuStdid = UUID.randomUUID();
        randomUUIDStudent = uuStdid.toString();


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


        edt_Fname = (EditText) findViewById(R.id.edt_FirstName);
        edt_Mname = (EditText) findViewById(R.id.edt_MiddleName);
        edt_Lname = (EditText) findViewById(R.id.edt_LastName);
        edt_Age = (EditText) findViewById(R.id.edt_Age);
        edt_Class = (EditText) findViewById(R.id.edt_Class);


        rg_Gender = (RadioGroup) findViewById(R.id.rg_Gender);

        /*// get selected radio button from radioGroup
        int selectedId = rg_Gender.getCheckedRadioButtonId();
        // find the radio button by returned id
        selectedGender = (RadioButton) findViewById(selectedId);*/


        // Take Photo Functionality
        btn_Capture = (Button) findViewById(R.id.btn_Capture);
        imgView = (ImageView) findViewById(R.id.imageView);


        btn_Capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /*// Ref : https://stackoverflow.com/questions/8402378/how-to-save-image-captured-with-camera-in-specific-folder
                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
                imagesFolder.mkdirs(); // <----
                File image = new File(imagesFolder, "yo.jpg");
                uriSavedImage = Uri.fromFile(image);
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                startActivityForResult(imageIntent, 1);
                // VIMP LINE
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)));
                // mediaScan();
*/



               /* //camera stuff
                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new theDate());

                //folder stuff
                File imagesFolder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/", "StudentProfiles");
                imagesFolder.mkdirs();

                //File image = new File(imagesFolder, "QR_" + timeStamp + ".png");
                File image = new File(imagesFolder, randomUUIDStudent + ".png");
                Uri uriSavedImage = Uri.fromFile(image);

                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(imageIntent, 1);
*/
                // MHM Old Code
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_Thumbnail);


            }
        });


        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        btn_Clear = (Button) findViewById(R.id.btn_Clear);

        Util = new Utility();
        util = new Utility();


        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = rg_Gender.getCheckedRadioButtonId();
                // find the radio button by returned id
                selectedGender = (RadioButton) findViewById(selectedId);
                gender = selectedGender.getText().toString();

                // Check AllSpinners Emptyness
                int StatesSpinnerValue = states_spinner.getSelectedItemPosition();
                int BlocksSpinnerValue = blocks_spinner.getSelectedItemPosition();
                int VillagesSpinnerValue = villages_spinner.getSelectedItemPosition();
                int GroupsSpinnerValue = groups_spinner.getSelectedItemPosition();


                // Spinners Selection
                if (StatesSpinnerValue > 0 && BlocksSpinnerValue > 0 && VillagesSpinnerValue > 0 && GroupsSpinnerValue > 0) {

                    // Checking Emptyness
                    if ((!edt_Fname.getText().toString().isEmpty() || !edt_Lname.getText().toString().isEmpty())) {

                        // Validations
                        if ((edt_Fname.getText().toString().matches("[a-zA-Z.? ]*")) && (edt_Lname.getText().toString().matches("[a-zA-Z.? ]*"))
                                && (edt_Mname.getText().toString().matches("[a-zA-Z.? ]*"))
                                && (edt_Age.getText().toString().matches("[0-9]+")) && (edt_Class.getText().toString().matches("[0-9]+"))) {
                            Student stdObj = new Student();

                            stdObj.StudentID = randomUUIDStudent;
                            stdObj.FirstName = edt_Fname.getText().toString();
                            stdObj.MiddleName = edt_Mname.getText().toString();
                            stdObj.LastName = edt_Lname.getText().toString();
                            stdObj.Age = Integer.parseInt(String.valueOf(edt_Age.getText()));
                            stdObj.Class = Integer.parseInt(String.valueOf(edt_Class.getText()));
                            stdObj.UpdatedDate = Util.GetCurrentDateTime(false);
                            stdObj.Gender = gender;
                            stdObj.GroupID = GrpID;
                            stdObj.CreatedBy = statdb.getValue("CRL");
                            stdObj.newStudent = true;
                            stdObj.StudentUID = "";
                            stdObj.IsSelected = true;
                            stdObj.CreatedOn = util.GetCurrentDateTime(false).toString();

                            sdb.insertData(stdObj);

                            Toast.makeText(AddStudentProfiles.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                            BackupDatabase.backup(stdContext);
                            groups_spinner.setSelection(0);

                            edt_Fname.getText().clear();
                            edt_Mname.getText().clear();
                            edt_Lname.getText().clear();
                            edt_Age.getText().clear();
                            edt_Class.getText().clear();

                            //rg_Gender.clearCheck();

                            imgView.setImageDrawable(null);

                            UUID uuStdid = UUID.randomUUID();
                            randomUUIDStudent = uuStdid.toString();


                        } else {
                            Toast.makeText(AddStudentProfiles.this, "Please Enter Valid Input !!!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddStudentProfiles.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddStudentProfiles.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        btn_Clear.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                FormReset();
            }
        });


    }


       /* rg_Gender = (RadioGroup) findViewById(R.id.rg_Gender);
        rb_Male = (RadioButton) findViewById(R.id.rb_Male);
        rb_Female = (RadioButton) findViewById(R.id.rb_Female);

*/
      /* protected void mediaScan() {

           getApplicationContext().sendBroadcast(
                   new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                           Uri.parse(uriSavedImage.toString())));
       }*/

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
        ArrayAdapter<GroupList> GroupsAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, GroupsVillages);
        // Hint for AllSpinners
        groups_spinner.setPrompt("Select Group");
        groups_spinner.setAdapter(GroupsAdapter);
        groups_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ID = String.valueOf(groups_spinner.getSelectedItemId());
                GrpID = GroupsVillages.get(Integer.parseInt(ID)).getGroupId();

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


    public void FormReset() {

        states_spinner.setSelection(0);
        blocks_spinner.setSelection(0);
        villages_spinner.setSelection(0);
        groups_spinner.setSelection(0);

        edt_Fname.getText().clear();
        edt_Mname.getText().clear();
        edt_Lname.getText().clear();
        edt_Age.getText().clear();
        edt_Class.getText().clear();

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
                      //  File folder = new File(splashScreenVideo.fpath + "/MyClicks/");
                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdir();
                        }
                        if (success) {
                            // Do something on success
                            File outputFile = new File(folder, randomUUIDStudent + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                            thumbnail1.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            data = null;
                            thumbnail1 = null;
                            // To Refresh Contents of sharableFolder
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
                        } else {
                            /*// SD Card Access
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            startActivityForResult(intent, 3);*/
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }/*else if (requestCode==3){
            try {
                Uri treeUri=data.getData();
                DocumentFile pickedDir=DocumentFile.fromTreeUri(AddStudentProfiles.this,treeUri);
                DocumentFile newFile=pickedDir.createFile("text/plain","MyClick");
                OutputStream out=getContentResolver().openOutputStream(newFile.getUri());
                out.write("True....the file is being created".getBytes());
                out.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/
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