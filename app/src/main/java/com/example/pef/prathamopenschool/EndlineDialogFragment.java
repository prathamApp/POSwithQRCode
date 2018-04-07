package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.interfaces.DataInterface;

import java.util.HashMap;


public class EndlineDialogFragment extends android.app.DialogFragment implements DataInterface {

    TextView tv_EndlineTitle;
    Spinner sp_BaselineLang, sp_NumberReco;
    Switch sw_Selected;
    CheckBox oprAdd, oprSub, oprMul, oprDiv, wpAdd, wpSub;
    int receivedTestType;
    Boolean isSelected = false, SubmitPressed = false;

    DataInterface dataInterface;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    int ic;
    TextView tv_wp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_endline_dialog, container, false);
        //getDialog().setTitle("Simple Dialog");

        // Hide Title of Dialog Fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        sessionContex = getActivity();
        playVideo = new PlayVideo();


        getDialog().setCanceledOnTouchOutside(false);


        String EndlineValue = getArguments().getString("EndlineValue");

        int lang = getArguments().getInt("lang");
        int num = getArguments().getInt("num");
        int oa = getArguments().getInt("oa");
        int os = getArguments().getInt("os");
        int om = getArguments().getInt("om");
        int od = getArguments().getInt("od");
        int wa = getArguments().getInt("wa");
        int ws = getArguments().getInt("ws");
        ic = getArguments().getInt("ic");

        tv_EndlineTitle = (TextView) rootView.findViewById(R.id.tv_EndlineTitle);
        tv_EndlineTitle.setText(EndlineValue);

        sp_BaselineLang = (Spinner) rootView.findViewById(R.id.spinner_BaselineLang);
        //sp_BaselineLang.setPrompt("Baseline Level");
        String[] baselineLangAdapter = {"Language", "Beg", "Letter", "Word", "Para", "Story"};
        ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, baselineLangAdapter);
        sp_BaselineLang.setAdapter(baselineAdapter);

        sp_NumberReco = (Spinner) rootView.findViewById(R.id.spinner_NumberReco);
        String[] NumberRecoAdapter = {"Number Recognition", "Beg", "0-9", "10-99", "100-999"};
        ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, NumberRecoAdapter);
        //sp_NumberReco.setPrompt("Number Reco Level");
        sp_NumberReco.setAdapter(recoAdapter);

        sw_Selected = (Switch) rootView.findViewById(R.id.switch_isSelected);

        oprAdd = (CheckBox) rootView.findViewById(R.id.OprAdd);
        oprSub = (CheckBox) rootView.findViewById(R.id.OprSub);
        oprMul = (CheckBox) rootView.findViewById(R.id.OprMul);
        oprDiv = (CheckBox) rootView.findViewById(R.id.OprDiv);

        wpAdd = (CheckBox) rootView.findViewById(R.id.WordAdd);
        wpSub = (CheckBox) rootView.findViewById(R.id.WordSub);

        tv_wp = (TextView) rootView.findViewById(R.id.tv_WordProblem);

        // Hiding Checkboxes of Mul & Div for Class 1 & Class 2
        if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {
            oprMul.setVisibility(View.GONE);
            oprDiv.setVisibility(View.GONE);
            wpAdd.setVisibility(View.GONE);
            wpSub.setVisibility(View.GONE);
            tv_wp.setVisibility(View.GONE);
        }

        // Hiding because not required anymore
        sw_Selected.setVisibility(View.GONE);


        // Setting Fetched Data
        sp_BaselineLang.setSelection(lang);
        sp_NumberReco.setSelection(num);

        if(oa==0 && os ==0)
        {
            wpAdd.setVisibility(View.GONE);
            wpSub.setVisibility(View.GONE);
            tv_wp.setVisibility(View.GONE);
            wpAdd.setChecked(false);
            wpSub.setChecked(false);
        }

        if (oa == 1) {
            oprAdd.setChecked(true);
        } else {
            oprAdd.setChecked(false);
            wpAdd.setVisibility(View.GONE);

        }

        if (os == 1) {
            oprSub.setChecked(true);
        } else {
            oprSub.setChecked(false);
            wpSub.setVisibility(View.GONE);

        }

        if (om == 1) {
            oprMul.setChecked(true);
        } else {
            oprMul.setChecked(false);
        }

        if (od == 1) {
            oprDiv.setChecked(true);
        } else {
            oprDiv.setChecked(false);
        }
        if (wa == 1) {
            wpAdd.setChecked(true);
        } else {
            wpAdd.setChecked(false);
        }
        if (ws == 1) {
            wpSub.setChecked(true);
        } else {
            wpSub.setChecked(false);
        }
        if (ic == 1) {
            sw_Selected.setChecked(true);
        } else {
            sw_Selected.setChecked(false);
        }

        oprAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(oprAdd.isChecked())
                {
                    tv_wp.setVisibility(View.VISIBLE);
                    wpAdd.setVisibility(View.VISIBLE);
                }
                else {
                    wpAdd.setChecked(false);
                    wpAdd.setVisibility(View.GONE);
                }
            }
        });

        oprSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(oprSub.isChecked())
                {
                    tv_wp.setVisibility(View.VISIBLE);
                    wpSub.setVisibility(View.VISIBLE);
                }
                else {
                    wpSub.setChecked(false);
                    wpSub.setVisibility(View.GONE);
                }
            }
        });

       /* // Visibility of WAdd Based on Opr Add
        if (oprAdd.isChecked()) {
            wpAdd.setVisibility(View.VISIBLE);
        } else {
            wpAdd.setChecked(false);
            wpAdd.setVisibility(View.GONE);
        }

        // Visibility of WSub Based on Opr Sub
        if (oprSub.isChecked()) {
            wpSub.setVisibility(View.VISIBLE);
        } else {
            wpSub.setChecked(false);
            wpSub.setVisibility(View.GONE);
        }

        tv_wp = (TextView) rootView.findViewById(R.id.tv_EndlineTitle);

        if (!oprAdd.isChecked() && !oprSub.isChecked()) {
            tv_wp.setVisibility(View.GONE);
            wpAdd.setVisibility(View.GONE);
            wpSub.setVisibility(View.GONE);
            wpAdd.setChecked(false);
            wpSub.setChecked(false);
        }*/


        sw_Selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSelected = isChecked;
            }
        });

        Button submit = (Button) rootView.findViewById(R.id.btn_Submit);
        submit.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                // Check AllSpinners Emptyness
                int BaselineSpinnerValue = sp_BaselineLang.getSelectedItemPosition();
                int NumberSpinnerValue = sp_NumberReco.getSelectedItemPosition();

                if (BaselineSpinnerValue > 0 && NumberSpinnerValue > 0)

                {

                    SubmitPressed = true;
                    receivedTestType = getArguments().getInt("TestType");

                    // Sending Data back to UCFF using an Interface which uses HashMap

                    HashMap<String, String> EndlineData = new HashMap<String, String>();
                    EndlineData.put("SetTestType", String.valueOf(receivedTestType));
                    EndlineData.put("baselineLevel", String.valueOf(sp_BaselineLang.getSelectedItemPosition()));
                    EndlineData.put("numberRecoLevel", String.valueOf(sp_NumberReco.getSelectedItemPosition()));
                    EndlineData.put("isSelectedStatus", String.valueOf(isSelected));
                    EndlineData.put("oprAddStatus", String.valueOf(oprAdd.isChecked()));
                    EndlineData.put("oprSubStatus", String.valueOf(oprSub.isChecked()));
                    EndlineData.put("oprMulStatus", String.valueOf(oprMul.isChecked()));
                    EndlineData.put("oprDivStatus", String.valueOf(oprDiv.isChecked()));
                    EndlineData.put("wpAddStatus", String.valueOf(wpAdd.isChecked()));
                    EndlineData.put("wpSubStatus", String.valueOf(wpSub.isChecked()));

                    dataInterface.dataPassingMethod(EndlineData);

                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Please fill all the fields !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return rootView;
    }


    @Override
    public void onDetach() {
        if (!SubmitPressed) {

            HashMap<String, String> EndlineData = new HashMap<String, String>();
            EndlineData.put("SetTestType", String.valueOf(0));
            dataInterface.dataPassingMethod(EndlineData);
        }
        UniversalChildFormFragment.EndlineButtonClicked = true;
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        dataInterface = (DataInterface) getTargetFragment();
    }


    @Override
    public void dataPassingMethod(HashMap data) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }

    @Override
    public void onPause() {
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
                    playVideo.finish();
                }
            }
        }.start();

    }


}