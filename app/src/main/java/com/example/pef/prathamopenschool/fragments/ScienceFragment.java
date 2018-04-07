package com.example.pef.prathamopenschool.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.example.pef.prathamopenschool.R;
import com.example.pef.prathamopenschool.MainActivity;
import com.example.pef.prathamopenschool.splashScreenVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ScienceFragment extends Fragment {

    String newNodeList="", fragName="Science";
    JSONArray contentNavigate;
    String subFolderNodeList;


    public ScienceFragment() {

    }

    public ScienceFragment(String newNodeList) {
        this.newNodeList = newNodeList;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grid_view, container, false);
        final GridView gridview = (GridView)view.findViewById(R.id.gridview);


        List<ItemObject> allItems = getAllItemObject(newNodeList);

        //passing list frm the function

        CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems, fragName);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // find your fragment

/*
                List<ItemObject> allItems = getAllItemObject(subFolderNodeList);
                CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems);
                gridview.setAdapter(customAdapter);
*/

                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    private List<ItemObject> getAllItemObject(String newNodeList){

        List<ItemObject> items = new ArrayList<>();


        try {
            JSONArray jsnarray = null;
            jsnarray = new JSONArray(newNodeList);
            contentNavigate = jsnarray;
            for (int i = 0; i < jsnarray.length(); i++) {

                String notetitle;
                JSONObject c = contentNavigate.getJSONObject(i);
/*
                nodeId=c.optString("nodeId");
                nodeType=c.optString("nodeType");
*/
                String nodeTitle = c.optString("nodeTitle").toString();
                String nodeImage = splashScreenVideo.fpath+"Media/"+c.optString("nodeImage").toString();
                String nodePhase= c.optString("nodePhase").toString();
/*
                nodeAge= c.optString("nodeAge").toString();
                nodeDesc= c.optString("nodeDesc").toString();
                nodeKeywords= c.optString("nodeKeywords").toString();
                sameCode= c.optString("sameCode").toString();
                resourceId= c.optString("resourceId").toString();
                resourceType= c.optString("resourceType").toString();
                resourcePath= c.optString("resourcePath").toString();
*/
                String nodeList = c.optString("nodelist").toString();
                subFolderNodeList=nodeList;

                items.add(new ItemObject(nodeImage , nodeTitle , nodePhase, nodeList));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }

}
