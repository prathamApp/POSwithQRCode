package com.example.pef.prathamopenschool.fragments;

/**
 * Created by Ameya on 6/9/2017.
 */

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.example.pef.prathamopenschool.R;

import java.util.ArrayList;
import java.util.List;

public class PopMusicFragment extends Fragment {

    String fragName="all";

    public PopMusicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grid_view, container, false);
        GridView gridview = (GridView)view.findViewById(R.id.gridview);

        List<ItemObject> allItems = getAllItemObject();
        //passing list frm the function
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems, fragName);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }



    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject("R.drawable.ic_dots","Dip It Low", "Christina Milian","aa"));
/*
        items.add(new ItemObject("R.drawable.ic_dots","Someone like you", "Adele Adkins"));
        items.add(new ItemObject("R.drawable.ic_dots","Ride", "Ciara"));
        items.add(new ItemObject("R.drawable.ic_dots","Paparazzi", "Lady Gaga"));
        items.add(new ItemObject("R.drawable.ic_dots","Forever", "Chris Brown"));
        items.add(new ItemObject("R.drawable.ic_dots","Stay", "Rihanna"));
        items.add(new ItemObject("R.drawable.ic_dots","Marry me", "Jason Derulo"));
        items.add(new ItemObject("R.drawable.ic_dots","Waka Waka", "Shakira"));
        items.add(new ItemObject("R.drawable.ic_dots","Dark Horse", "Katy Perry"));
        items.add(new ItemObject("R.drawable.ic_dots","Dip It Low", "Christina Milian"));
        items.add(new ItemObject("R.drawable.ic_dots","Dip It Low", "Christina Milian"));
        items.add(new ItemObject("R.drawable.ic_dots","Dip It Low", "Christina Milian"));
*/
        return items;
    }

}
