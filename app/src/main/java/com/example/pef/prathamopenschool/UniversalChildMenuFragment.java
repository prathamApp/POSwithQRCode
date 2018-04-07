package com.example.pef.prathamopenschool;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class UniversalChildMenuFragment extends ListFragment {

    Intent receivedData;
    List<String> childID = new ArrayList<String>();

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        MainActivity.sessionFlg=false;
        sessionContex=getActivity();
        playVideo = new PlayVideo();


        childID = SelectClassForUniversalChild.selectChildIDData;

        /*receivedData = getActivity().getIntent();
        childID = receivedData.getStringArrayListExtra("childData");
*/
        /*childID = new ArrayList<String>();
         for (int i = 1; i <= 100; i++) {
             childID.add(String.valueOf(i));
         }
*/

        View view = inflater.inflate(R.layout.activity_universal_child_menu_fragment, container, false);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_listview, childID);
        setListAdapter(adapter);
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        UniversalChildFormFragment txt = (UniversalChildFormFragment) getFragmentManager().findFragmentById(R.id.formFragment);
        txt.change(childID.get(position));
        getListView().setSelector(R.drawable.custom_selector);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(MultiPhotoSelectActivity.pauseFlg){
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg=false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }

    @Override
    public void onPause() {
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
                    playVideo.finish();
                }
            }
        }.start();

    }


}
