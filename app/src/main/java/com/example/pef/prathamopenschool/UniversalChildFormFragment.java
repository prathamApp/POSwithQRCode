package com.example.pef.prathamopenschool;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.interfaces.DataInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UniversalChildFormFragment extends Fragment implements DataInterface {

    TextView tv_ChildID;
    StatusDBHelper statusDBHelper;
    int selectedStatus;
    EditText edt_ChildName, edt_FatherName, edt_Age, edt_ChildID;
    Switch sw_Selected;
    RadioGroup rg_Gender;
    RadioButton selectedGender;
    Spinner sp_BaselineLang, sp_NumberReco, sp_Gender, sp_Class;
    Button btn_DatePicker, btn_Capture, btn_Endline1, btn_Endline2, btn_Endline3, btn_Endline4, btn_Next;
    ImageView imgView;
    String gender;
    Utility util;
    private static final int TAKE_Thumbnail = 1;
    String randomUUIDStudent;
    int testVal = 0;
    StudentDBHelper sdb;
    AserDBHelper adb;
    Context c;
    String UniqueStudentID;
    Boolean isSelected = false;
    int BaselineLevelLang, NumberRecoLevelNum;
    String deviceID = "";
    int OprAddValue = 0, OprMulValue = 0, OprDivValue = 0, wpAddValue = 0, wpSubValue = 0;
    FragmentManager fmgr;
    EndlineDialogFragment edf;
    int OprSubValue = 0;
    Bundle args;
    static Boolean EndlineButtonClicked = false;
    List<Student> StudentData;
    List<Aser> AserData;
    RadioButton rb_Male, rb_Female;
    String EditedChild, ChangedChild;
    String OriginalGender;
    String UpdatedDate;

    String stdClass = "0";

    TextView tv_Operations;
    CheckBox oprAddBaseline, oprSubBaseline;


    Boolean bOprAdd = false, bOprSub = false, bOprMul = false, bOprDiv = false, bwpAdd = false, bwpSub = false, bswIC = false;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    StatusDBHelper statdb;
    boolean timer;

    String StdUniqueID, Fname, Mname, GrpID, StdChildID, StdGender;
    int StdAge;
    Boolean StdIsSelected;

    private HashMap dialogFragmentData;

    String AserChildID;

    String ChildName, MiddleName, selectedGenderData, theDate;
    int Age;

    int testT, langSpin, numSpin;
    int OA = 0;
    int OS = 0;
    int OM = 0;
    int OD = 0;
    int WA = 0;
    int WS = 0;
    int IC = 0;

    Date CD, SD;
    String CurrentDate, SelectedDate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        MainActivity.sessionFlg = false;
        sessionContex = getActivity();
        playVideo = new PlayVideo();
        statusDBHelper = new StatusDBHelper(sessionContex);
        final View view = inflater.inflate(R.layout.activity_universal_child_form_fragment, container, false);


        c = this.getActivity();
        sdb = new StudentDBHelper(c);
        adb = new AserDBHelper(c);
        statdb = new StatusDBHelper(c);

        deviceID = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Memory Allocation
        util = new Utility();

        tv_ChildID = (TextView) view.findViewById(R.id.tv_ChildID);
        edt_ChildName = (EditText) view.findViewById(R.id.edt_ChildName);
        edt_FatherName = (EditText) view.findViewById(R.id.edt_FatherName);
        edt_Age = (EditText) view.findViewById(R.id.edt_ChildAge);
        sw_Selected = (Switch) view.findViewById(R.id.switch_isSelected);
        edt_ChildID = (EditText) view.findViewById(R.id.edt_ChildID);

        sp_Gender = (Spinner) view.findViewById(R.id.spinner_Gender);
        //sp_BaselineLang.setPrompt("Baseline Level");
        String[] GenderLangAdapter = {"Select Gender", "Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, GenderLangAdapter);
        sp_Gender.setAdapter(genderAdapter);

        sp_BaselineLang = (Spinner) view.findViewById(R.id.spinner_BaselineLang);
        //sp_BaselineLang.setPrompt("Baseline Level");
        String[] baselineLangAdapter = {"Baseline (Lang)", "Beg", "Letter", "Word", "Para", "Story"};
        ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, baselineLangAdapter);
        sp_BaselineLang.setAdapter(baselineAdapter);

        sp_NumberReco = (Spinner) view.findViewById(R.id.spinner_NumberReco);
        String[] NumberRecoAdapter = {"Number Recognition", "Beg", "0-9", "10-99", "100-999"};
        ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, NumberRecoAdapter);
        //sp_NumberReco.setPrompt("Number Reco Level");
        sp_NumberReco.setAdapter(recoAdapter);

        sp_Class = (Spinner) view.findViewById(R.id.spinner_Class);
        String[] ClassAdapter = {"Select Class", "3", "4", "5"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, ClassAdapter);
        //sp_NumberReco.setPrompt("Number Reco Level");
        sp_Class.setAdapter(classAdapter);


        btn_DatePicker = (Button) view.findViewById(R.id.btn_DatePicker);

        btn_DatePicker.setText(util.GetCurrentDate().toString());
        btn_DatePicker.setPadding(8, 8, 8, 8);
        btn_DatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "DatePicker");

            }
        });


        btn_Capture = (Button) view.findViewById(R.id.btn_Capture);

        btn_Capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tv_ChildID.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Please Select a Child !!!", Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_Thumbnail);
                }
            }
        });

        imgView = (ImageView) view.findViewById(R.id.imageView);

        btn_Endline1 = (Button) view.findViewById(R.id.btn_Endline1);
        btn_Endline2 = (Button) view.findViewById(R.id.btn_Endline2);
        btn_Endline3 = (Button) view.findViewById(R.id.btn_Endline3);
        btn_Endline4 = (Button) view.findViewById(R.id.btn_Endline4);

        btn_Next = (Button) view.findViewById(R.id.btn_Next);

        btn_Endline1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tv_ChildID.getText().toString().length() > 0)

                {
                    EndlineButtonClicked = true;

                    setDefaults();

                    fmgr = getFragmentManager();
                    edf = new EndlineDialogFragment();
                    edf.setTargetFragment(UniversalChildFormFragment.this, 0);

                    args = new Bundle();

                    AserData = adb.GetAllByStudentID(StdUniqueID, 1);

                    if (AserData.size() > 0) {
                        for (int i = 0; i < AserData.size(); i++) {

                            testT = AserData.get(i).TestType;
                            langSpin = AserData.get(i).Lang;
                            numSpin = AserData.get(i).Num;
                            OA = AserData.get(i).OAdd;
                            OS = AserData.get(i).OSub;
                            OM = AserData.get(i).OMul;
                            OD = AserData.get(i).ODiv;
                            WA = AserData.get(i).WAdd;
                            WS = AserData.get(i).WSub;
                            IC = AserData.get(i).FLAG;
                        }
                    } else {
                        setDefaults();
                    }

                    args.putInt("lang", langSpin);
                    args.putInt("num", numSpin);
                    args.putInt("oa", OA);
                    args.putInt("os", OS);
                    args.putInt("om", OM);
                    args.putInt("od", OD);
                    args.putInt("wa", WA);
                    args.putInt("ws", WS);
                    args.putInt("ic", IC);

                    args.putString("EndlineValue", "Endline 1");
                    args.putInt("TestType", 1);
                    edf.setArguments(args);

                    edf.show(fmgr, "Endline Fragment");

                } else {
                    Toast.makeText(getActivity(), "Please Select a Child !!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_Endline2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tv_ChildID.getText().toString().length() > 0)

                {
                    EndlineButtonClicked = true;

                    setDefaults();

                    fmgr = getFragmentManager();
                    edf = new EndlineDialogFragment();
                    edf.setTargetFragment(UniversalChildFormFragment.this, 0);
                    args = new Bundle();

                    AserData = adb.GetAllByStudentID(StdUniqueID, 2);

                    for (int i = 0; i < AserData.size(); i++) {

                        testT = AserData.get(i).TestType;
                        langSpin = AserData.get(i).Lang;
                        numSpin = AserData.get(i).Num;
                        OA = AserData.get(i).OAdd;
                        OS = AserData.get(i).OSub;
                        OM = AserData.get(i).OMul;
                        OD = AserData.get(i).ODiv;
                        WA = AserData.get(i).WAdd;
                        WS = AserData.get(i).WSub;
                        IC = AserData.get(i).FLAG;

                    }


                    args.putInt("lang", langSpin);
                    args.putInt("num", numSpin);
                    args.putInt("oa", OA);
                    args.putInt("os", OS);
                    args.putInt("om", OM);
                    args.putInt("od", OD);
                    args.putInt("wa", WA);
                    args.putInt("ws", WS);
                    args.putInt("ic", IC);

                    args.putString("EndlineValue", "Endline 2");
                    args.putInt("TestType", 2);
                    edf.setArguments(args);

                    edf.show(fmgr, "Endline Fragment");
                } else {
                    Toast.makeText(getActivity(), "Please Select a Child !!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_Endline3.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                if (tv_ChildID.getText().toString().length() > 0)

                                                {
                                                    EndlineButtonClicked = true;

                                                    setDefaults();

                                                    fmgr = getFragmentManager();
                                                    edf = new EndlineDialogFragment();
                                                    edf.setTargetFragment(UniversalChildFormFragment.this, 0);
                                                    args = new Bundle();

                                                    AserData = adb.GetAllByStudentID(StdUniqueID, 3);

                                                    for (int i = 0; i < AserData.size(); i++) {

                                                        testT = AserData.get(i).TestType;
                                                        langSpin = AserData.get(i).Lang;
                                                        numSpin = AserData.get(i).Num;
                                                        OA = AserData.get(i).OAdd;
                                                        OS = AserData.get(i).OSub;
                                                        OM = AserData.get(i).OMul;
                                                        OD = AserData.get(i).ODiv;
                                                        WA = AserData.get(i).WAdd;
                                                        WS = AserData.get(i).WSub;
                                                        IC = AserData.get(i).FLAG;

                                                    }


                                                    args.putInt("lang", langSpin);
                                                    args.putInt("num", numSpin);
                                                    args.putInt("oa", OA);
                                                    args.putInt("os", OS);
                                                    args.putInt("om", OM);
                                                    args.putInt("od", OD);
                                                    args.putInt("wa", WA);
                                                    args.putInt("ws", WS);
                                                    args.putInt("ic", IC);

                                                    args.putString("EndlineValue", "Endline 3");
                                                    args.putInt("TestType", 3);
                                                    edf.setArguments(args);

                                                    edf.show(fmgr, "Endline Fragment");

                                                } else {
                                                    Toast.makeText(getActivity(), "Please Select a Child !!!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

        );

        btn_Endline4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tv_ChildID.getText().toString().length() > 0)

                {
                    EndlineButtonClicked = true;

                    setDefaults();

                    fmgr = getFragmentManager();
                    edf = new EndlineDialogFragment();
                    edf.setTargetFragment(UniversalChildFormFragment.this, 0);
                    args = new Bundle();

                    AserData = adb.GetAllByStudentID(StdUniqueID, 4);

                    for (int i = 0; i < AserData.size(); i++) {

                        testT = AserData.get(i).TestType;
                        langSpin = AserData.get(i).Lang;
                        numSpin = AserData.get(i).Num;
                        OA = AserData.get(i).OAdd;
                        OS = AserData.get(i).OSub;
                        OM = AserData.get(i).OMul;
                        OD = AserData.get(i).ODiv;
                        WA = AserData.get(i).WAdd;
                        WS = AserData.get(i).WSub;
                        IC = AserData.get(i).FLAG;

                    }


                    args.putInt("lang", langSpin);
                    args.putInt("num", numSpin);
                    args.putInt("oa", OA);
                    args.putInt("os", OS);
                    args.putInt("om", OM);
                    args.putInt("od", OD);
                    args.putInt("wa", WA);
                    args.putInt("ws", WS);
                    args.putInt("ic", IC);

                    args.putString("EndlineValue", "Endline 4");
                    args.putInt("TestType", 4);
                    edf.setArguments(args);

                    edf.show(fmgr, "Endline Fragment");


                } else {
                    Toast.makeText(getActivity(), "Please Select a Child !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_Operations = (TextView) view.findViewById(R.id.tv_OperationsBaseline);
        oprAddBaseline = (CheckBox) view.findViewById(R.id.OprAddBaseline);
        oprSubBaseline = (CheckBox) view.findViewById(R.id.OprSubBaseline);


        // Hiding Data for Class 1 & Class 2
        if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {
            sp_BaselineLang.setVisibility(View.GONE);
            sp_NumberReco.setVisibility(View.GONE);
            sp_Class.setVisibility(View.GONE);
            btn_DatePicker.setVisibility(View.GONE);
            tv_Operations.setVisibility(View.GONE);
            oprAddBaseline.setVisibility(View.GONE);
            oprSubBaseline.setVisibility(View.GONE);
            btn_Endline1.setVisibility(View.GONE);
            btn_Endline2.setVisibility(View.GONE);
            btn_Endline3.setVisibility(View.GONE);
            btn_Endline4.setVisibility(View.GONE);
            sw_Selected.setVisibility(View.GONE);
        }

        // Hiding Operations, Checkbox add& sub for Class 3,4 & Class 5
        if (SelectClassForUniversalChild.ClickedClass.equals("3") || SelectClassForUniversalChild.ClickedClass.equals("4") || SelectClassForUniversalChild.ClickedClass.equals("5")) {
            tv_Operations.setVisibility(View.GONE);
            oprAddBaseline.setVisibility(View.GONE);
            oprSubBaseline.setVisibility(View.GONE);
            sw_Selected.setVisibility(View.GONE);
        }


        return view;
    }


    // Reset
    public void setDefaults() {

        langSpin = 0;
        numSpin = 0;
        OA = 0;
        OS = 0;
        OM = 0;
        OD = 0;
        WA = 0;
        WS = 0;
        IC = 0;

    }

    //--------------------------------------------------------------------------------------------------------------------------------

    // ID is getting set here so Edit functionality should be based on tv_ChildID
    public void change(String CID) {
        tv_ChildID.setText(CID);

        EndlineButtonClicked = false;

        String From = UniversalChildList.From;

        /******************************************************* EDIT UNIT *************************************/

        if ((From.equals("Create")) || (From.equals("Edit"))) {


            //Toast.makeText(getActivity(), " Let's Edit !!!", Toast.LENGTH_SHORT).show();

            EditedChild = CID;
            String ChildID = CID;
            String GroupID = UniversalChildList.UnitID;


            // Fetching Student Data based on ChildID & GroupID
            StudentData = sdb.GetAllStudentsByChildID(ChildID, GroupID);

            //sp_Gender.setSelection(0);

            if (StudentData.size() > 0) {

                for (int i = 0; i < StudentData.size(); i++) {
                    StdUniqueID = StudentData.get(i).StudentID;
                    Fname = StudentData.get(i).FirstName;
                    Mname = StudentData.get(i).MiddleName;
                    StdAge = StudentData.get(i).Age;
                    StdGender = StudentData.get(i).Gender;
                    StdIsSelected = StudentData.get(i).IsSelected;
                    UpdatedDate = StudentData.get(i).UpdatedDate;
                    stdClass = String.valueOf(StudentData.get(i).Class);

                }

                // Set image from Device if available
                File imgFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/" + StdUniqueID + ".jpg");

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imgView.setImageDrawable(new BitmapDrawable(getResources(), myBitmap));
                } else {
                    imgView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.avatar));
                }

            } else {

                Fname = "";
                Mname = "";
                edt_ChildID.getText().clear();
                StdAge = 0;
                StdGender = "Select Gender";
                stdClass = "Select Class";
                StdIsSelected = false;
                imgView.setImageDrawable(null);
                sp_BaselineLang.setSelection(0);
                sp_NumberReco.setSelection(0);
                sp_Class.setSelection(0);
                UpdatedDate = util.GetCurrentDate().toString();

                UUID uuStdid = UUID.randomUUID();
                randomUUIDStudent = uuStdid.toString();
                StdUniqueID = randomUUIDStudent;
                imgView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.avatar));

                // Reseting Checkbox if no data found
                if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {
                    oprAddBaseline.setChecked(false);
                    oprSubBaseline.setChecked(false);
                }


            }

            btn_DatePicker.setText(UpdatedDate);
            tv_ChildID.setText(CID);
            edt_ChildName.setText(Fname);
            edt_FatherName.setText(Mname);
            if (StdAge == 0) {
                edt_Age.getText().clear();

            } else {
                edt_Age.setText(String.valueOf(StdAge));
            }
            if (StdGender.equals("Male") || StdGender.equals("M") || StdGender.equals("1")) {
                sp_Gender.setSelection(1);
                //   rb_Male.setChecked(Boolean.TRUE);
            } else if (StdGender.equals("Female") || StdGender.equals("F") || StdGender.equals("2")) {
                sp_Gender.setSelection(2);
            } else {
                sp_Gender.setSelection(0);
            }
            if (stdClass.equals("3")) {
                sp_Class.setSelection(1);
            } else if (stdClass.equals("4")) {
                sp_Class.setSelection(2);
            } else if (stdClass.equals("5")) {
                sp_Class.setSelection(3);
            } else {
                sp_Class.setSelection(0);
            }


            // Fetching Aser Data by Student Unique ID
            if (StudentData.size() > 0) {

                int tstT = 0;
                while (tstT >= 0 && tstT < 5) {
                    AserData = adb.GetAllByStudentID(StdUniqueID, tstT);
                    if (AserData.size() > 0) {
                        break;
                    }
                    tstT++;
                }

                for (int i = 0; i < AserData.size(); i++) {

                    testT = AserData.get(i).TestType;
                    langSpin = AserData.get(i).Lang;
                    numSpin = AserData.get(i).Num;
                    OA = AserData.get(i).OAdd;
                    OS = AserData.get(i).OSub;
                    OM = AserData.get(i).OMul;
                    OD = AserData.get(i).ODiv;
                    WA = AserData.get(i).WAdd;
                    WS = AserData.get(i).WSub;
                    selectedStatus = AserData.get(i).FLAG;
                    AserChildID = AserData.get(i).ChildID;

                }

                // Baseline Checkbox Status for Class 1 & Class 2
                if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {

                    if (OA == 1) {
                        oprAddBaseline.setChecked(true);
                    } else {
                        oprAddBaseline.setChecked(false);
                    }

                    if (OS == 1) {
                        oprSubBaseline.setChecked(true);
                    } else {
                        oprSubBaseline.setChecked(false);
                    }
                }

                // Fetch & fill Child ID from Aser
                edt_ChildID.setText(AserChildID);


                if (selectedStatus == 1) {
                    sw_Selected.setChecked(true);
                } else {
                    sw_Selected.setChecked(false);
                }

                // Setting Baseline/ NumReco Spinner based on TestType
                if (testT == 0) {
                    sp_BaselineLang.setSelection(langSpin);
                    sp_NumberReco.setSelection(numSpin);
                }
                if (testT == 1 || testT == 2 || testT == 3 || testT == 4) {
                    sp_BaselineLang.setSelection(0);
                    sp_NumberReco.setSelection(0);
                }


            }

            sw_Selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isSelected = isChecked;
                }
            });


            btn_Next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    // Baseline Checkbox Status for Class 1 & Class 2
                    if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {

                        if (EndlineButtonClicked == true) {
                            testVal = Integer.parseInt(String.valueOf(dialogFragmentData.get("SetTestType")));
                            EndlineButtonClicked = false;
                        } else {
                            testVal = 0;
                        }

                        if (testVal > 0) {

                            // Data From Endline
                            bOprAdd = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprAddStatus")));
                            bOprSub = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprSubStatus")));
                            bOprMul = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprMulStatus")));
                            bOprDiv = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprDivStatus")));
                            bwpAdd = Boolean.valueOf(String.valueOf(dialogFragmentData.get("wpAddStatus")));
                            bwpSub = Boolean.valueOf(String.valueOf(dialogFragmentData.get("wpSubStatus")));
                            bswIC = Boolean.valueOf(String.valueOf(dialogFragmentData.get("isSelectedStatus")));
                            BaselineLevelLang = Integer.parseInt(String.valueOf(dialogFragmentData.get("baselineLevel")));
                            NumberRecoLevelNum = Integer.parseInt(String.valueOf(dialogFragmentData.get("numberRecoLevel")));

                        } else {
                            if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {
                                bOprAdd = oprAddBaseline.isChecked();
                                bOprSub = oprSubBaseline.isChecked();
                            }
                            bswIC = isSelected;
                            BaselineLevelLang = sp_BaselineLang.getSelectedItemPosition();
                            NumberRecoLevelNum = sp_NumberReco.getSelectedItemPosition();

                        }

                        // Check AllSpinners Emptyness
                        int BaselineSpinnerValue = sp_BaselineLang.getSelectedItemPosition();
                        int NumberSpinnerValue = sp_NumberReco.getSelectedItemPosition();
                        int genderSpinnerVal = sp_Gender.getSelectedItemPosition();
                        int classSpinnerVal = sp_Class.getSelectedItemPosition();

                        theDate = btn_DatePicker.getText().toString();

                        gender = sp_Gender.getSelectedItem().toString();

                    /*int ageLimit = Integer.parseInt(String.valueOf(edt_Age.getText()));
                    String Cname = edt_ChildName.getText().toString();
                    String Fname = edt_FatherName.getText().toString();*/
                    /*(ageLimit>0) && (Cname.length()>0) && (Fname.length()>0) &&*/


                        if (((tv_ChildID.getText().toString().length() > 0) && (edt_ChildName.getText().toString().length() > 0) && (edt_FatherName.getText().toString().length() > 0) && (edt_Age.getText().toString().length() > 0) && ((edt_ChildID.getText().toString().length() > 0) && (edt_ChildID.getText().toString().length() == 4)) && (testVal == 0) && (genderSpinnerVal > 0)) || ((tv_ChildID.getText().toString().length() > 0) && (edt_ChildName.getText().toString().length() > 0) && (edt_FatherName.getText().toString().length() > 0) && (edt_Age.getText().toString().length() > 0) && ((edt_ChildID.getText().toString().length() > 0) && (edt_ChildID.getText().toString().length() == 4)) && (testVal > 0) && (genderSpinnerVal > 0)))

                        {

                            if (StdUniqueID.equals("")) {

                            } else {
                                StdUniqueID = StdUniqueID;
                            }

                            // Check if Child ID Exists in db or not
                            boolean res;
                            res = adb.CheckChildIDExists(StdUniqueID, edt_ChildID.getText().toString(), UniversalChildList.UnitID);
                            if (res == false) {

                                Student std = new Student();
                                std.StudentID = StdUniqueID;
                                std.FirstName = edt_ChildName.getText().toString();
                                std.MiddleName = edt_FatherName.getText().toString();
                                std.LastName = "";
                                std.Class = 0;
                                std.Age = Integer.parseInt(String.valueOf(edt_Age.getText()));
                                std.Gender = gender;
                                std.UpdatedDate = theDate;
                                std.GroupID = UniversalChildList.UnitID;
                                std.newStudent = true;
                                std.CreatedBy = statdb.getValue("CRL");
                                std.StudentUID = EditedChild;
                                std.IsSelected = isSelected;
                                std.CreatedOn = new Utility().GetCurrentDateTime(false);

                                sdb.insertUniversalChildData(std);


                                Aser asr = new Aser();

                                asr.StudentId = StdUniqueID;
                                asr.GroupID = UniversalChildList.UnitID;
                                asr.ChildID = edt_ChildID.getText().toString();
                                asr.TestType = testVal;
                                asr.TestDate = theDate;
                                asr.Lang = 0;
                                asr.Num = 0;
                                asr.CreatedBy = statdb.getValue("CRL");
                                asr.CreatedDate = new Utility().GetCurrentDate();
                                asr.DeviceId = deviceID.equals(null) ? "0000" : deviceID;
                                asr.FLAG = bswIC ? 1 : 0;
                                asr.OAdd = bOprAdd ? 1 : 0;
                                asr.OSub = bOprSub ? 1 : 0;
                                asr.OMul = bOprMul ? 1 : 0;
                                asr.ODiv = bOprDiv ? 1 : 0;
                                asr.WAdd = bwpAdd ? 1 : 0;
                                asr.WSub = bwpSub ? 1 : 0;

                                int oprAdd = bOprAdd ? 1 : 0;
                                int oprSub = bOprSub ? 1 : 0;
                                int oprMul = bOprMul ? 1 : 0;
                                int oprDiv = bOprDiv ? 1 : 0;
                                int wAdd = bwpAdd ? 1 : 0;
                                int wSub = bwpSub ? 1 : 0;
                                int isSelected = bswIC ? 1 : 0;

                                asr.CreatedOn = new Utility().GetCurrentDateTime(false);


                                // testT0 = Baseline, testT1 = Endline1, testT2 = Endline2, testT3 = Endline3, testT4 = Endline4
                                // if exists replace else insert
                                if (testVal == 0) {
                                    boolean result;
                                    result = adb.CheckDataExists(StdUniqueID, 0);
                                    if (result == false) {
                                        adb.insertData(asr);
                                        BackupDatabase.backup(c);
                                    } else {

                                        adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 0);
                                        BackupDatabase.backup(c);
                                    }
                                } else if (testVal == 1) {
                                    boolean result;
                                    result = adb.CheckDataExists(StdUniqueID, 1);
                                    if (result == false) {
                                        adb.insertData(asr);
                                        BackupDatabase.backup(c);
                                    } else {
                                        adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 1);
                                        BackupDatabase.backup(c);
                                    }
                                } else if (testVal == 2) {
                                    boolean result;
                                    result = adb.CheckDataExists(StdUniqueID, 2);
                                    if (result == false) {
                                        adb.insertData(asr);
                                        BackupDatabase.backup(c);
                                    } else {
                                        adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 2);
                                        BackupDatabase.backup(c);
                                    }
                                } else if (testVal == 3) {
                                    boolean result;
                                    result = adb.CheckDataExists(StdUniqueID, 3);
                                    if (result == false) {
                                        adb.insertData(asr);
                                        BackupDatabase.backup(c);
                                    } else {
                                        adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 3);
                                        BackupDatabase.backup(c);
                                    }
                                } else if (testVal == 4) {
                                    boolean result;
                                    result = adb.CheckDataExists(StdUniqueID, 4);
                                    if (result == false) {
                                        adb.insertData(asr);
                                        BackupDatabase.backup(c);
                                    } else {
                                        adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 4);
                                        BackupDatabase.backup(c);
                                    }
                                }


                                Toast.makeText(getActivity(), "Data Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                                //ClearForm();

                                BackupDatabase.backup(c);

                            } else {
                                Toast.makeText(getActivity(), "Child ID already present !!!", Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            Toast.makeText(getActivity(), "Please Fill All Fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    /************************************* FOR CLASS 3,4,5 *******************************************************************/

                    else if (SelectClassForUniversalChild.ClickedClass.equals("3") || SelectClassForUniversalChild.ClickedClass.equals("4") || SelectClassForUniversalChild.ClickedClass.equals("5")) {
                        if (EndlineButtonClicked == true) {
                            testVal = Integer.parseInt(String.valueOf(dialogFragmentData.get("SetTestType")));
                            EndlineButtonClicked = false;
                        } else {
                            testVal = 0;
                        }

                        if (testVal > 0) {

                            // Data From Endline
                            bOprAdd = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprAddStatus")));
                            bOprSub = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprSubStatus")));
                            bOprMul = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprMulStatus")));
                            bOprDiv = Boolean.valueOf(String.valueOf(dialogFragmentData.get("oprDivStatus")));
                            bwpAdd = Boolean.valueOf(String.valueOf(dialogFragmentData.get("wpAddStatus")));
                            bwpSub = Boolean.valueOf(String.valueOf(dialogFragmentData.get("wpSubStatus")));
                            bswIC = Boolean.valueOf(String.valueOf(dialogFragmentData.get("isSelectedStatus")));
                            BaselineLevelLang = Integer.parseInt(String.valueOf(dialogFragmentData.get("baselineLevel")));
                            NumberRecoLevelNum = Integer.parseInt(String.valueOf(dialogFragmentData.get("numberRecoLevel")));

                        } else {
                            if (SelectClassForUniversalChild.ClickedClass.equals("1") || SelectClassForUniversalChild.ClickedClass.equals("2")) {
                                bOprAdd = oprAddBaseline.isChecked();
                                bOprSub = oprSubBaseline.isChecked();
                            }
                            bswIC = isSelected;
                            BaselineLevelLang = sp_BaselineLang.getSelectedItemPosition();
                            NumberRecoLevelNum = sp_NumberReco.getSelectedItemPosition();

                        }

                        // Check AllSpinners Emptyness
                        int BaselineSpinnerValue = sp_BaselineLang.getSelectedItemPosition();
                        int NumberSpinnerValue = sp_NumberReco.getSelectedItemPosition();
                        int genderSpinnerVal = sp_Gender.getSelectedItemPosition();
                        int classSpinnerVal = sp_Class.getSelectedItemPosition();

                        theDate = btn_DatePicker.getText().toString();

                        gender = sp_Gender.getSelectedItem().toString();

                    /*int ageLimit = Integer.parseInt(String.valueOf(edt_Age.getText()));
                    String Cname = edt_ChildName.getText().toString();
                    String Fname = edt_FatherName.getText().toString();*/
                    /*(ageLimit>0) && (Cname.length()>0) && (Fname.length()>0) &&*/


                        if (((tv_ChildID.getText().toString().length() > 0) && (edt_ChildName.getText().toString().length() > 0) && (edt_FatherName.getText().toString().length() > 0) && (edt_Age.getText().toString().length() > 0) && ((edt_ChildID.getText().toString().length() > 0) && (edt_ChildID.getText().toString().length() == 4)) && (testVal == 0) && (BaselineSpinnerValue > 0) && (NumberSpinnerValue > 0) && (genderSpinnerVal > 0) && (classSpinnerVal > 0)) || ((tv_ChildID.getText().toString().length() > 0) && (edt_ChildName.getText().toString().length() > 0) && (edt_FatherName.getText().toString().length() > 0) && (edt_Age.getText().toString().length() > 0) && ((edt_ChildID.getText().toString().length() > 0) && (edt_ChildID.getText().toString().length() == 4)) && (testVal > 0) && (genderSpinnerVal > 0) && (classSpinnerVal > 0)))

                        {

                            if (StdUniqueID.equals("")) {

                            } else {
                                StdUniqueID = StdUniqueID;
                            }

                            String ChildIDSeries = String.valueOf(edt_ChildID.getText().toString().charAt(0));

                            // Validate Class against Child ID
                            if (ChildIDSeries.equals(sp_Class.getSelectedItem().toString())) {

                                // Check if Child ID Exists in db or not
                                boolean res;
                                res = adb.CheckChildIDExists(StdUniqueID, edt_ChildID.getText().toString(), UniversalChildList.UnitID);
                                if (res == false) {

                                    CurrentDate = util.GetCurrentDate();
                                    SelectedDate = btn_DatePicker.getText().toString();

                                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

                                    try {
                                        CD = format.parse(CurrentDate);
                                        SD = format.parse(SelectedDate);
                                    } catch (Exception e) {
                                        e.getMessage();
                                    }

                                    /*if (CurrentDate.compareTo(SelectedDate) <= 0) {
                                        System.out.println("earlier");
                                    }*/

                                    if ((CD.compareTo(SD) > 0) || SD.equals(CD)) {


                                        Student std = new Student();
                                        std.StudentID = StdUniqueID;
                                        std.FirstName = edt_ChildName.getText().toString();
                                        std.MiddleName = edt_FatherName.getText().toString();
                                        std.LastName = "";
                                        std.Class = Integer.parseInt(sp_Class.getSelectedItem().toString());
                                        std.Age = Integer.parseInt(String.valueOf(edt_Age.getText()));
                                        std.Gender = gender;
                                        std.UpdatedDate = theDate;
                                        std.GroupID = UniversalChildList.UnitID;
                                        std.newStudent = true;
                                        std.CreatedBy = statdb.getValue("CRL");
                                        std.StudentUID = EditedChild;
                                        std.IsSelected = isSelected;
                                        std.CreatedOn = new Utility().GetCurrentDateTime(false);

                                        sdb.insertUniversalChildData(std);


                                        Aser asr = new Aser();

                                        asr.StudentId = StdUniqueID;
                                        asr.GroupID = UniversalChildList.UnitID;
                                        asr.ChildID = edt_ChildID.getText().toString();
                                        asr.TestType = testVal;
                                        asr.TestDate = theDate;
                                        asr.Lang = BaselineLevelLang;
                                        asr.Num = NumberRecoLevelNum;
                                        asr.CreatedBy = statdb.getValue("CRL");
                                        asr.CreatedDate = util.GetCurrentDate();
                                        asr.DeviceId = deviceID.equals(null) ? "0000" : deviceID;
                                        asr.FLAG = bswIC ? 1 : 0;
                                        asr.OAdd = bOprAdd ? 1 : 0;
                                        asr.OSub = bOprSub ? 1 : 0;
                                        asr.OMul = bOprMul ? 1 : 0;
                                        asr.ODiv = bOprDiv ? 1 : 0;
                                        asr.WAdd = bwpAdd ? 1 : 0;
                                        asr.WSub = bwpSub ? 1 : 0;

                                        int oprAdd = bOprAdd ? 1 : 0;
                                        int oprSub = bOprSub ? 1 : 0;
                                        int oprMul = bOprMul ? 1 : 0;
                                        int oprDiv = bOprDiv ? 1 : 0;
                                        int wAdd = bwpAdd ? 1 : 0;
                                        int wSub = bwpSub ? 1 : 0;
                                        int isSelected = bswIC ? 1 : 0;

                                        asr.CreatedOn = new Utility().GetCurrentDateTime(false);

                                        // testT0 = Baseline, testT1 = Endline1, testT2 = Endline2, testT3 = Endline3, testT4 = Endline4
                                        // if exists replace else insert
                                        if (testVal == 0) {
                                            boolean result;
                                            result = adb.CheckDataExists(StdUniqueID, 0);
                                            if (result == false) {
                                                adb.insertData(asr);
                                                BackupDatabase.backup(c);
                                            } else {

                                                adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 0);
                                                BackupDatabase.backup(c);
                                            }
                                        } else if (testVal == 1) {
                                            boolean result;
                                            result = adb.CheckDataExists(StdUniqueID, 1);
                                            if (result == false) {
                                                adb.insertData(asr);
                                                BackupDatabase.backup(c);
                                            } else {
                                                adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 1);
                                                BackupDatabase.backup(c);
                                            }
                                        } else if (testVal == 2) {
                                            boolean result;
                                            result = adb.CheckDataExists(StdUniqueID, 2);
                                            if (result == false) {
                                                adb.insertData(asr);
                                                BackupDatabase.backup(c);
                                            } else {
                                                adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 2);
                                                BackupDatabase.backup(c);
                                            }
                                        } else if (testVal == 3) {
                                            boolean result;
                                            result = adb.CheckDataExists(StdUniqueID, 3);
                                            if (result == false) {
                                                adb.insertData(asr);
                                                BackupDatabase.backup(c);
                                            } else {
                                                adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 3);
                                                BackupDatabase.backup(c);
                                            }
                                        } else if (testVal == 4) {
                                            boolean result;
                                            result = adb.CheckDataExists(StdUniqueID, 4);
                                            if (result == false) {
                                                adb.insertData(asr);
                                                BackupDatabase.backup(c);
                                            } else {
                                                adb.UpdateAserData(edt_ChildID.getText().toString(), theDate, BaselineLevelLang, NumberRecoLevelNum, oprAdd, oprSub, oprMul, oprDiv, wAdd, wSub, statdb.getValue("CRL"), util.GetCurrentDate(), isSelected, StdUniqueID, 4);
                                                BackupDatabase.backup(c);
                                            }
                                        }


                                        Toast.makeText(getActivity(), "Data Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                                        //ClearForm();

                                        BackupDatabase.backup(c);
                                    } else {
                                        Toast.makeText(getActivity(), "Please Select Date before Current Date !!!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "Child ID already present !!!", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getActivity(), "Child ID should Start with " + sp_Class.getSelectedItem().toString() + " !!! \nBecause, selected class is " + sp_Class.getSelectedItem().toString() + " !!!", Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            Toast.makeText(getActivity(), "Please Fill All Fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    }


                } // btn_Next
            });


        }


    }


    //------------------------------------------------------------------------------------------------------------

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TAKE_Thumbnail) {
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bitmap thumbnail1 = data.getParcelableExtra("data");
                    imgView.setImageBitmap(thumbnail1);
                    try {

                        File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/");
                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdir();
                        }
                        if (success) {
                            // Do something on success
                            File outputFile = new File(folder, StdUniqueID + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                            thumbnail1.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            data = null;
                            thumbnail1 = null;
                            // To Refresh Contents of sharableFolder
                            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
                        } else {
                            // Do something else on failure
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void dataPassingMethod(HashMap data) {
        dialogFragmentData = data;
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
