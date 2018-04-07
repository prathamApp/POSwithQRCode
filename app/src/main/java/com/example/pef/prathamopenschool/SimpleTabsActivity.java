package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;



import com.example.pef.prathamopenschool.fragments.AllFragment;
import com.example.pef.prathamopenschool.fragments.CoursesFragment;
import com.example.pef.prathamopenschool.fragments.EnglishFragment;
import com.example.pef.prathamopenschool.fragments.FunFragment;
import com.example.pef.prathamopenschool.fragments.MathFragment;
import com.example.pef.prathamopenschool.fragments.ScienceFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SimpleTabsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    String newNodeList, UpdatednodeList;
    private RecyclerView recyclerView, horizontalrecyclerView ;
    private Context mContext;


    JSONArray contentNavigate;
    private FragmentCardAdapter adapter;
    private List<Card> cardList;

    String[] myTitleNameArray = new String[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_tabs);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newNodeList=getIntent().getStringExtra("nodeList");

        JSONArray jsnarray = null;
        try {
            jsnarray = new JSONArray(newNodeList);
        contentNavigate = jsnarray;

/*
                    for (int i = 0; i < contentNavigate.length(); i++) {
*/
        TextView mTextView = (TextView) findViewById(R.id.title);
        JSONObject jsonObj = contentNavigate.getJSONObject(0);

        UpdatednodeList= jsonObj.optString("nodelist").toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        cardList = new ArrayList<>();
        adapter = new FragmentCardAdapter(this, cardList);

        recyclerView = (RecyclerView) findViewById(R.id.verticalGridLayout);
        horizontalrecyclerView = (RecyclerView) findViewById(R.id.horizontalgridlayout);


        GridLayoutManager gridlayoutManager = new GridLayoutManager(this , 1 , GridLayoutManager.HORIZONTAL, false);
        horizontalrecyclerView.setLayoutManager(gridlayoutManager);

        cardList = new ArrayList<>();
        adapter = new FragmentCardAdapter(this, cardList);

        String str= String.valueOf(adapter);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);



        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);


        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public String[] ReadMyFile (String newNodeList){
        try {

                JSONArray jsnarray = new JSONArray(newNodeList);
                contentNavigate = jsnarray;
                // contentNavigate = findElementsChildren(contentNavigate.getJSONObject(""),newNodeId);



            // looping through All nodes
            for (int i = 0; i < contentNavigate.length(); i++) {

                String notetitle;
                JSONObject c = contentNavigate.getJSONObject(i);

                Card card = new Card();
                card.nodeId=c.optString("nodeId");
                card.nodeType=c.optString("nodeType");
                card.nodeTitle = c.optString("nodeTitle").toString();
                notetitle = c.optString("nodeTitle").toString();
                myTitleNameArray[i+1]= notetitle;
                card.nodeImage = MainActivity.fpath+"Media/"+c.optString("nodeImage").toString();
                card.nodePhase= c.optString("nodePhase").toString();
                card.nodeAge= c.optString("nodeAge").toString();
                card.nodeDesc= c.optString("nodeDesc").toString();
                card.nodeKeywords= c.optString("nodeKeywords").toString();
                card.sameCode= c.optString("sameCode").toString();
                card.resourceId= c.optString("resourceId").toString();
                card.resourceType= c.optString("resourceType").toString();
                card.resourcePath= c.optString("resourcePath").toString();
                card.nodeList = c.optString("nodelist").toString();

                cardList.add(card);


/*                // tmp hashmap for single node
                HashMap<String, String> parsedData = new HashMap<String, String>();

                // adding each child node to HashMap key => value

                parsedData.put("resourceId",resourceId);
                parsedData.put("resourceName",resourceName);
                parsedData.put("resourceType",resourceType);
                parsedData.put("source",source);
                parsedData.put("resourceImage",resourceImage);
                parsedData.put("sameCode",sameCode);
                parsedData.put("demo",demo);
                parsedData.put("resourcePhase",resourcePhase);
*/

                // do what do you want on your interface
            }
            //adapter.notifyDataSetChanged();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return myTitleNameArray;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        String title[] = ReadMyFile(UpdatednodeList);

        adapter.addFragment(new AllFragment(), "ALL");
        adapter.addFragment(new EnglishFragment(), "ENGLISH");
        adapter.addFragment(new CoursesFragment(), "COURSES");
        adapter.addFragment(new MathFragment(), "MATH");
        adapter.addFragment(new FunFragment(), "FUN");
        adapter.addFragment(new ScienceFragment(), "SCIENCE");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

          /*  if(position == 2){
                JSONArray jsnarray = null;
                try {
                    jsnarray = new JSONArray(newNodeList);
                    contentNavigate = jsnarray;
                    TextView mTextView = (TextView) findViewById(R.id.title);
                    JSONObject jsonObj = contentNavigate.getJSONObject(0);
                    String nodeList= jsonObj.optString("nodelist").toString();

                    Fragment fragment = new EnglishFragment();
                    android.app.FragmentManager fragmentManager = getFragmentManager();
                    android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //fragmentTransaction.replace(R.id.EnglishFragment , fragment2);
                    fragmentTransaction.commit();

//                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(position == 3){
                JSONArray jsnarray = null;
                try {

                    jsnarray = new JSONArray(newNodeList);
                    contentNavigate = jsnarray;

                    for (int i = 0; i < contentNavigate.length(); i++) {
                        TextView mTextView = (TextView) findViewById(R.id.title);

                        JSONObject jsonObj = contentNavigate.getJSONObject(i);

                        String nodeList= jsonObj.optString("nodelist").toString();

                        jsnarray = new JSONArray(nodeList);
                        JSONArray contentNavigate2 = jsnarray;
                        for(int j=0; j<contentNavigate2.length(); j++){
                            JSONObject jsonObj2 = contentNavigate2.getJSONObject(j);
                            String abc = jsonObj2.optString("nodeTitle").toString();
                            mTextView.setText(abc);
                        }


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
