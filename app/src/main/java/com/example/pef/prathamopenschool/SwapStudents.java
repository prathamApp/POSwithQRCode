package com.example.pef.prathamopenschool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.List;

public class SwapStudents extends AppCompatActivity {

    Spinner sp_State_One, sp_Block_One, sp_Village_One, sp_Group_One, sp_State_Two, sp_Block_Two, sp_Village_Two, sp_Group_Two;
    RecyclerView rv_GrpOne, rv_GrpTwo;
    Button btn_Save;
    ImageButton btn_shift_right, btn_shift_left;
    VillageDBHelper vdb1, vdb2;
    StatusDBHelper sdb;
    StudentDBHelper stddb;
    GroupDBHelper gdb;
    List<String> Blocks;
    int vilID;
    String GrpOneID,GrpTwoID;
    private List<StudentList> GrpOneExistingStudents, GrpTwoExistingStudents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swap_students);

        // Hide Actionbar
        getSupportActionBar().hide();

        // memory Allocation
        sp_State_One = findViewById(R.id.sp_StateOne);
        sp_State_Two = findViewById(R.id.sp_StateTwo);
        sp_Block_One = findViewById(R.id.sp_BlockOne);
        sp_Block_Two = findViewById(R.id.sp_BlockTwo);
        sp_Village_One = findViewById(R.id.sp_VillageOne);
        sp_Village_Two = findViewById(R.id.sp_VillageTwo);
        sp_Group_One = findViewById(R.id.sp_GroupOne);
        sp_Group_Two = findViewById(R.id.sp_GroupTwo);

        rv_GrpOne = findViewById(R.id.rv_GrpOne);
        rv_GrpTwo = findViewById(R.id.rv_GrpTwo);

        btn_shift_left = findViewById(R.id.btn_shiftTwoToOne);
        btn_shift_right = findViewById(R.id.btn_shiftOneToTwo);
        btn_Save = findViewById(R.id.btn_Confirm);

        // Hide Village Spinner based on HLearning / RI
        if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("4")) // H Learning
        {
            sp_Village_One.setVisibility(View.VISIBLE);
        } else if (MultiPhotoSelectActivity.programID.equals("2")) // RI
        {
            sp_Village_One.setVisibility(View.GONE);
        }

        // Hide Village Spinner based on HLearning / RI
        if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("4")) // H Learning
        {
            sp_Village_Two.setVisibility(View.VISIBLE);
        } else if (MultiPhotoSelectActivity.programID.equals("2")) // RI
        {
            sp_Village_Two.setVisibility(View.GONE);
        }

        vdb1 = new VillageDBHelper(this);

        // Sp_state
        //Get Villages Data for States AllSpinners
        List<String> sp_one_States = vdb1.GetState();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> sp_one_StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, sp_one_States);
        // Hint for AllSpinners
        sp_State_One.setPrompt("Select State");
        sp_State_One.setAdapter(sp_one_StateAdapter);

        sp_State_One.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = sp_State_One.getSelectedItem().toString();
                populateBlockOne(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        vdb2 = new VillageDBHelper(this);
        //Get Villages Data for States AllSpinners
        List<String> sp_two_States = vdb2.GetState();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> sp_two_StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, sp_two_States);
        // Hint for AllSpinners
        sp_State_Two.setPrompt("Select State");
        sp_State_Two.setAdapter(sp_two_StateAdapter);

        sp_State_Two.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = sp_State_Two.getSelectedItem().toString();
                populateBlockTwo(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void populateBlockOne(String selectedState) {
        //Get Villages Data for Blocks AllSpinners
        Blocks = vdb1.GetStatewiseBlock(selectedState);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> BlockAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, Blocks);
        // Hint for AllSpinners
        sp_Block_One.setPrompt("Select Block");
        sp_Block_One.setAdapter(BlockAdapter);

        sp_Block_One.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBlock = sp_Block_One.getSelectedItem().toString();
                if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("4")) // H Learning
                {
                    populateHLVillageOne(selectedBlock);
                } else if (MultiPhotoSelectActivity.programID.equals("2")) // RI
                {
                    populateRIVillageOne(selectedBlock);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void populateBlockTwo(String selectedState) {
        //Get Villages Data for Blocks AllSpinners
        Blocks = vdb2.GetStatewiseBlock(selectedState);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> BlockAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, Blocks);
        // Hint for AllSpinners
        sp_Block_Two.setPrompt("Select Block");
        sp_Block_Two.setAdapter(BlockAdapter);

        sp_Block_Two.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBlock = sp_Block_Two.getSelectedItem().toString();
                if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("4")) // H Learning
                {
                    populateHLVillageTwo(selectedBlock);
                } else if (MultiPhotoSelectActivity.programID.equals("2")) // RI
                {
                    populateRIVillageTwo(selectedBlock);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void populateHLVillageOne(String selectedBlock) {
        //Get Villages Data for Villages filtered by block for Spinners
        List<VillageList> BlocksVillages = vdb1.GetVillages(selectedBlock);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<VillageList> VillagesAdapter = new ArrayAdapter<VillageList>(this, R.layout.custom_spinner, BlocksVillages);
        // Hint for AllSpinners
        sp_Village_One.setPrompt("Select Village");
        sp_Village_One.setAdapter(VillagesAdapter);
        sp_Village_One.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VillageList village = (VillageList) parent.getItemAtPosition(position);
                vilID = village.getVillageId();
                populateGroupsOne(vilID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void populateRIVillageOne(String selectedBlock) {
        vilID = vdb1.GetVillageIDByBlock(selectedBlock);
        try {
            populateGroupsOne(vilID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void populateHLVillageTwo(String selectedBlock) {
        //Get Villages Data for Villages filtered by block for Spinners
        List<VillageList> BlocksVillages = vdb2.GetVillages(selectedBlock);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<VillageList> VillagesAdapter = new ArrayAdapter<VillageList>(this, R.layout.custom_spinner, BlocksVillages);
        // Hint for AllSpinners
        sp_Village_Two.setPrompt("Select Village");
        sp_Village_Two.setAdapter(VillagesAdapter);
        sp_Village_Two.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VillageList village = (VillageList) parent.getItemAtPosition(position);
                vilID = village.getVillageId();
                populateGroupsTwo(vilID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void populateRIVillageTwo(String selectedBlock) {
        vilID = vdb2.GetVillageIDByBlock(selectedBlock);
        try {
            populateGroupsTwo(vilID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void populateGroupsOne(int villageID) {
        //Get Groups Data for Villages filtered by Villages for Spinners
        gdb = new GroupDBHelper(SwapStudents.this);
        final List<GroupList> GroupsVillages = gdb.GetGroups(villageID);
        //GroupsVillages.get(0).getGroupId();
        //Creating the ArrayAdapter instance having the Villages list
        final ArrayAdapter<GroupList> GroupsAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, GroupsVillages);
        // Hint for AllSpinners
        sp_Group_One.setPrompt("Select Group");
        sp_Group_One.setAdapter(GroupsAdapter);
        sp_Group_One.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GroupName = sp_Group_One.getSelectedItem().toString();
                //GrpID = GroupsVillages.get(0).getGroupId();
                GroupList SelectedGroupData = GroupsAdapter.getItem(sp_Group_One.getSelectedItemPosition());
                GrpOneID = SelectedGroupData.getGroupId();
                String Id = GrpOneID;
                //Toast.makeText(EditStudent.this, "Group ID is "+Id, Toast.LENGTH_SHORT).show();
                populateGroupOneStudents(Id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void populateGroupsTwo(int villageID) {
        //Get Groups Data for Villages filtered by Villages for Spinners
        gdb = new GroupDBHelper(SwapStudents.this);
        final List<GroupList> GroupsVillages = gdb.GetGroups(villageID);
        //GroupsVillages.get(0).getGroupId();
        //Creating the ArrayAdapter instance having the Villages list
        final ArrayAdapter<GroupList> GroupsAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, GroupsVillages);
        // Hint for AllSpinners
        sp_Group_Two.setPrompt("Select Group");
        sp_Group_Two.setAdapter(GroupsAdapter);
        sp_Group_Two.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GroupName = sp_Group_Two.getSelectedItem().toString();
                //GrpID = GroupsVillages.get(0).getGroupId();
                GroupList SelectedGroupData = GroupsAdapter.getItem(sp_Group_Two.getSelectedItemPosition());
                GrpTwoID = SelectedGroupData.getGroupId();
                String Id = GrpTwoID;
                //Toast.makeText(EditStudent.this, "Group ID is "+Id, Toast.LENGTH_SHORT).show();
                populateGroupTwoStudents(Id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    // refer EditStudent Line No 270 for details of student
    public void populateGroupOneStudents(String GroupID) {
        stddb = new StudentDBHelper(this);
        GrpOneExistingStudents = stddb.GetAllStudentsByGroupID(GroupID);
        int a;
        // populate student list
        //update group id in std table & aser table on shift
    }


    public void populateGroupTwoStudents(String GroupID) {
        stddb = new StudentDBHelper(this);
        GrpTwoExistingStudents = stddb.GetAllStudentsByGroupID(GroupID);
        int a;

        // populate student list
        //update group id in std table & aser table on shift

    }


}
