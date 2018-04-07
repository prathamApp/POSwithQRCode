package com.example.pef.prathamopenschool.fragments;

/**
 * Created by Ameya on 6/9/2017.
 */

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pef.prathamopenschool.R;
import com.example.pef.prathamopenschool.MainActivity;
import com.example.pef.prathamopenschool.splashScreenVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private List<ItemObject> listStorage;
    private Context context;
    String subFolderNodeList;
    Fragment fragment;


    public CustomAdapter(Context context, List<ItemObject> customizedListView, String fragmentName) {
        this.context = context;
        layoutinflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder listViewHolder;
        if(convertView == null){

            listViewHolder = new ViewHolder();
            convertView = layoutinflater.inflate(R.layout.swipe_row_layout, parent, false);
            listViewHolder.tabThumbnail = (ImageView)convertView.findViewById(R.id.tabThumbnail);
            listViewHolder.tabTitle = (TextView)convertView.findViewById(R.id.tabTitle);
            listViewHolder.tabPhase = (TextView)convertView.findViewById(R.id.tabPhase);
            listViewHolder.rlcard= (RelativeLayout) convertView.findViewById(R.id.rlCard);

            convertView.setTag(listViewHolder);


        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }
//        Setting data

        listViewHolder.rlcard.setTag(position);
        final String nodelist=listStorage.get(position).nodeList;
        Bitmap bitmap = BitmapFactory.decodeFile(listStorage.get(position).getTabThumbnail());
        listViewHolder.tabThumbnail.setImageBitmap(bitmap);
        listViewHolder.tabTitle.setText(listStorage.get(position).getNodeTitle());
        listViewHolder.tabPhase.setText(listStorage.get(position).getNodePhase());

        listViewHolder.rlcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                List<ItemObject> allItems = getAllItemObject(nodelist);

                //passing list frm the function
//        FragmentManager fragment = this.getFragmentManager();
//        fragment.




               /* Intent mainNew = new Intent(context,swipe_main_activity.class);
                mainNew.putExtra("nodeList", nodelist.toString());
                ((Activity) context).startActivityForResult(mainNew, 1);
                */

/*
try {
    EnglishFragment fragment2 = new EnglishFragment();
    FragmentManager fragmentManager = fragment2.getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.gridview, fragment2);
    fragmentTransaction.commit();
}
catch (Exception e){
    e.printStackTrace();
}
*/
/*
                String myNodeList = "";
                JSONArray jsnarray = null;
                Fragment fragment = null;

                try {
                    jsnarray = new JSONArray(nodelist);
                    JSONObject jsonObj = jsnarray.getJSONObject(0);
                    myNodeList= jsonObj.optString("nodelist").toString();
                    fragment = new EnglishFragment(myNodeList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


*/

                String position= listViewHolder.rlcard.getTag().toString();

/*
                fragmentTransaction.replace(R.id.gridview, eng , position);
                fragmentTransaction.commit();
*/

                //setting name
                Toast.makeText(context,"position:"+listStorage.get(Integer.parseInt(position)).getNodeTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    private List<ItemObject> getAllItemObject(String nodelist){

        List<ItemObject> items = new ArrayList<>();


        try {
            JSONArray jsnarray = null;
            jsnarray = new JSONArray(nodelist);
            for (int i = 0; i < jsnarray.length(); i++) {

                String notetitle;
                JSONObject c = jsnarray.getJSONObject(i);
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


    static class ViewHolder{
        ImageView tabThumbnail;
        TextView tabTitle;
        TextView tabPhase;

        RelativeLayout rlcard;

    }
}