package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class SelectUnitForEdit extends AppCompatActivity {

    // Unit = Groups
    Spinner sp_State, sp_Block, sp_Village, sp_Unit;
    Button btn_Edit;
    List<String> Blocks;
    VillageDBHelper database;
    Context villageContext, grpContext, stdContext;
    GroupDBHelper gdb;
    int vilID;
    String VillageName, SchoolName, CreatedBy;
    TextView tv_SchoolName, tv_Villagename;
    String selectedState;
    String selectedBlock;
    String selectedUnit;
    String GroupID;
    static String From = "Edit";

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_unit_for_edit);

        MainActivity.sessionFlg=false;
        sessionContex=this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        villageContext = this;
        database = new VillageDBHelper(villageContext);

        grpContext = this;

        tv_SchoolName = (TextView) findViewById(R.id.tv_Schoolname);
        tv_Villagename = (TextView) findViewById(R.id.tv_Villagename);

        sp_State = (Spinner) findViewById(R.id.spinner_UnitSelectState);
        sp_Block = (Spinner) findViewById(R.id.spinner_UnitSelectBlock);
        sp_Unit = (Spinner) findViewById(R.id.spinner_SelectUnit);

        btn_Edit = (Button) findViewById(R.id.btn_Submit);


        btn_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent goToSelectClassToEdit = new Intent(SelectUnitForEdit.this, SelectClassForUniversalChild.class);
                goToSelectClassToEdit.putExtra("School", SchoolName);
                goToSelectClassToEdit.putExtra("Village", VillageName);
                goToSelectClassToEdit.putExtra("VillageID", vilID);
                goToSelectClassToEdit.putExtra("State", selectedState);
                goToSelectClassToEdit.putExtra("Block", selectedBlock);
                goToSelectClassToEdit.putExtra("UnitName", selectedUnit);
                goToSelectClassToEdit.putExtra("From", From);
                goToSelectClassToEdit.putExtra("Group ID", GroupID);

                startActivity(goToSelectClassToEdit);


            }
        });


        //Get Villages Data for States AllSpinners
        List<String> States = database.GetState();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, States);
        // Hint for AllSpinners
        sp_State.setPrompt("Select State");
        sp_State.setAdapter(StateAdapter);

        sp_State.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = sp_State.getSelectedItem().toString();
                populateBlock(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void populateBlock(String selectedState) {
        //Get Villages Data for Blocks AllSpinners
        Blocks = database.GetStatewiseBlock(selectedState);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> BlockAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, Blocks);
        // Hint for AllSpinners
        sp_Block.setPrompt("Select Block");
        sp_Block.setAdapter(BlockAdapter);

        sp_Block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBlock = sp_Block.getSelectedItem().toString();
                populateVillage(selectedBlock);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    // Block Not present in Groups
    public void populateVillage(String selectedBlock) {

        vilID = database.GetVillageIDByBlock(selectedBlock);
        populateUnits(vilID);

    }

    private void populateUnits(int vilID) {


        grpContext = this;
        gdb = new GroupDBHelper(grpContext);
        final List<GroupList> VillagewiseUnits = gdb.GetGroups(vilID);
        ArrayAdapter<GroupList> UnitAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, VillagewiseUnits);

        sp_Unit.setPrompt("Select Unit");
        sp_Unit.setAdapter(UnitAdapter);
        sp_Unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedUnit = sp_Unit.getSelectedItem().toString();  // To display Unit i.e Group Name

                GroupID = VillagewiseUnits.get(Integer.parseInt(String.valueOf(sp_Unit.getSelectedItemId()))).getGroupId(); // for query Unit Data

                final List<GroupList> UnitData = gdb.GetUnitwiseSchoolNVillage(GroupID);

                // set Textview for school n village
                if (UnitData.size() == 1) {

                    VillageName = UnitData.get(0).getGroupId().toString();
                    SchoolName = UnitData.get(0).getGroupName().toString();

                    tv_Villagename.setText(UnitData.get(0).getGroupId().toString()); // groupid brings villagename
                    tv_SchoolName.setText(UnitData.get(0).getGroupName().toString()); // groupname brings schoolname
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    /*private void populateUnits(String selectedVillageName) {

        grpContext = this;
        gdb = new GroupDBHelper(grpContext);

        final List<GroupList> VillagewiseUnits = gdb.GetVillagewiseUnits(selectedVillageName);
        ArrayAdapter<GroupList> UnitAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, VillagewiseUnits);
        sp_Unit.setPrompt("Select Unit");
        sp_Unit.setAdapter(UnitAdapter);
        sp_Unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
*/

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlAddEditScreen.class);
        this.finish();
        startActivity(intent);
    }
}
