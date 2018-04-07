package com.example.pef.prathamopenschool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

public class UniversalChildList extends FragmentActivity {


    TextView edt_UnitName, edt_VillageName, edt_SchoolName;
    static String Village, VillageID, School, UnitName, State, Block, UnitID;
    static String From;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universal_child_list);

        // Initial Info
        Intent getValues = getIntent();

        // Used while editing Unit
        VillageID = getValues.getStringExtra("VillageID");
        Village = getValues.getStringExtra("Village");
        School = getValues.getStringExtra("School");
        State = getValues.getStringExtra("State");
        UnitName = getValues.getStringExtra("UnitName");
        Block = getValues.getStringExtra("Block");
        From = getValues.getStringExtra("From");
        UnitID = getValues.getStringExtra("Group ID");


      /*  // Used while adding new Unit
        String GroupName = getValues.getStringExtra("Group Name");
        String VillageName = getValues.getStringExtra("Village Name");
        String SchoolName = getValues.getStringExtra("School Name");*/

        // Memory Allocation for Fields
        edt_UnitName = (TextView) findViewById(R.id.edt_UnitName);
        edt_UnitName.setText(UnitName);
        edt_VillageName = (TextView) findViewById(R.id.edt_VillageName);
        edt_VillageName.setText(Village);
        edt_SchoolName = (TextView) findViewById(R.id.edt_SchoolName);
        edt_SchoolName.setText(School);

    }
}
