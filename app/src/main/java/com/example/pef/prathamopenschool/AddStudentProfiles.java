package com.example.pef.prathamopenschool;

//http://www.deboma.com/article/mobile-development/22/android-datepicker-and-age-calculation

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddStudentProfiles extends AppCompatActivity {

    Spinner states_spinner, blocks_spinner, villages_spinner, groups_spinner;
    EditText edt_Fname, edt_Mname, edt_Lname, edt_Class;
    RadioGroup rg_Gender;
    Button btn_Submit, btn_Clear, btn_Capture, btn_BirthDatePicker;
    VillageDBHelper database;
    GroupDBHelper gdb;
    StudentDBHelper sdb;
    AserDBHelper adb;
    RadioButton rb_Male, rb_Female;
    String GrpID;
    List<String> Blocks;
    int vilID;
    String gender;
    List<String> ExistingStudents;
    String StudentID, FirstName, MiddleName, LastName, Age, Class, UpdatedDate, Gender;
    String randomUUIDStudent;
    private static final int TAKE_Thumbnail = 1;
    ImageView imgView;
    private static String TAG = "PermissionDemo";
    private static final int REQUEST_WRITE_STORAGE = 112;
    Uri uriSavedImage;
    UUID uuStdid;
    RadioButton selectedGender;

    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    int stdAge = 0;
    StatusDBHelper statdb;

    Utility util;

    Spinner sp_BaselineLang, sp_NumberReco;
    Button btn_EndlineDatePicker, btn_DatePicker, btn_Endline1, btn_Endline2, btn_Endline3, btn_Endline4;
    LinearLayout AserForm;
    public boolean EndlineButtonClicked = false;

    int testT, langSpin, numSpin;
    int OA = 0;
    int OS = 0;
    int OM = 0;
    int OD = 0;
    int WA = 0;
    int WS = 0;
    int IC = 0;
    String aserDate;

    @Subscribe
    public void onEvent(String msg) {
        if (!msg.isEmpty()) {
            btn_EndlineDatePicker.setText(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_profiles);
        getSupportActionBar().hide();

        EventBus.getDefault().register(AddStudentProfiles.this);

        initializeVariables();
        initializeStatesSpinner();
        initializeBaselineSpinner();
        initializeNumberRecoSpinner();
        initializeAserDate();
        initializeBirthDate();

        btn_Capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_Thumbnail);
            }
        });

        btn_Endline1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaults();

                // initialize dialog
                Dialog endlineDialog = new Dialog(AddStudentProfiles.this);
                endlineDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                endlineDialog.setContentView(R.layout.fragment_endline_dialog);
                endlineDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                // initialize dialog's widgets
                TextView title = endlineDialog.findViewById(R.id.tv_EndlineTitle);
                Spinner spinner_BaselineLang = endlineDialog.findViewById(R.id.spinner_BaselineLang);
                Spinner spinner_NumberReco = endlineDialog.findViewById(R.id.spinner_NumberReco);
                CheckBox OprAdd = endlineDialog.findViewById(R.id.OprAdd);
                CheckBox OprSub = endlineDialog.findViewById(R.id.OprSub);
                CheckBox OprMul = endlineDialog.findViewById(R.id.OprMul);
                CheckBox OprDiv = endlineDialog.findViewById(R.id.OprDiv);
                TextView tv_WordProblem = endlineDialog.findViewById(R.id.tv_WordProblem);
                CheckBox WordAdd = endlineDialog.findViewById(R.id.WordAdd);
                CheckBox WordSub = endlineDialog.findViewById(R.id.WordSub);
                Button btn_Submit = endlineDialog.findViewById(R.id.btn_Submit);
                btn_EndlineDatePicker = endlineDialog.findViewById(R.id.btn_EndlineDatePicker);
                btn_EndlineDatePicker.setText(util.GetCurrentDate().toString());
                btn_EndlineDatePicker.setPadding(8, 8, 8, 8);
                btn_EndlineDatePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(), "EndlineDatePicker");
                    }
                });

                // set values of endline
                title.setText("Endline 1");

                String[] baselineLangAdapter = {"Language", "Beg", "Letter", "Word", "Para", "Story"};
                ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, baselineLangAdapter);
                spinner_BaselineLang.setAdapter(baselineAdapter);

                String[] NumberRecoAdapter = {"Number Recognition", "Beg", "0-9", "10-99", "Sub", "Div"};
                ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, NumberRecoAdapter);
                spinner_NumberReco.setAdapter(recoAdapter);

                // show dialog
                endlineDialog.setCanceledOnTouchOutside(false);
                endlineDialog.show();

                if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                    tv_WordProblem.setVisibility(View.GONE);
                    WordAdd.setVisibility(View.GONE);
                    WordSub.setVisibility(View.GONE);
                }

                OprAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprAdd.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordAdd.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordAdd.setChecked(false);
                            WordAdd.setVisibility(View.GONE);
                        }
                    }
                });

                OprSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprSub.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordSub.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordSub.setChecked(false);
                            WordSub.setVisibility(View.GONE);
                        }
                    }
                });

                btn_Submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int BaselineSpinnerValue = spinner_BaselineLang.getSelectedItemPosition();
                        int NumberSpinnerValue = spinner_NumberReco.getSelectedItemPosition();

                        if (BaselineSpinnerValue > 0 && NumberSpinnerValue > 0) {
                            sp_BaselineLang.setSelection(0);
                            sp_NumberReco.setSelection(0);
                            EndlineButtonClicked = true;

                            testT = 1;
                            langSpin = BaselineSpinnerValue;
                            numSpin = NumberSpinnerValue;
                            aserDate = btn_EndlineDatePicker.getText().toString();
                            OA = OprAdd.isChecked() ? 1 : 0;
                            OS = OprSub.isChecked() ? 1 : 0;
                            OM = OprMul.isChecked() ? 1 : 0;
                            OD = OprDiv.isChecked() ? 1 : 0;
                            WA = WordAdd.isChecked() ? 1 : 0;
                            WS = WordSub.isChecked() ? 1 : 0;

                            Toast.makeText(AddStudentProfiles.this, "Endline 1 is filled.", Toast.LENGTH_SHORT).show();

                            if (endlineDialog.isShowing())
                                endlineDialog.dismiss();
                        } else {
                            Toast.makeText(AddStudentProfiles.this, "Please fill all the fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btn_Endline2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaults();

                // initialize dialog
                Dialog endlineDialog = new Dialog(AddStudentProfiles.this);
                endlineDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                endlineDialog.setContentView(R.layout.fragment_endline_dialog);
                endlineDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                // initialize dialog's widgets
                TextView title = endlineDialog.findViewById(R.id.tv_EndlineTitle);
                Spinner spinner_BaselineLang = endlineDialog.findViewById(R.id.spinner_BaselineLang);
                Spinner spinner_NumberReco = endlineDialog.findViewById(R.id.spinner_NumberReco);
                CheckBox OprAdd = endlineDialog.findViewById(R.id.OprAdd);
                CheckBox OprSub = endlineDialog.findViewById(R.id.OprSub);
                CheckBox OprMul = endlineDialog.findViewById(R.id.OprMul);
                CheckBox OprDiv = endlineDialog.findViewById(R.id.OprDiv);
                TextView tv_WordProblem = endlineDialog.findViewById(R.id.tv_WordProblem);
                CheckBox WordAdd = endlineDialog.findViewById(R.id.WordAdd);
                CheckBox WordSub = endlineDialog.findViewById(R.id.WordSub);
                Button btn_Submit = endlineDialog.findViewById(R.id.btn_Submit);
                btn_EndlineDatePicker = endlineDialog.findViewById(R.id.btn_EndlineDatePicker);
                btn_EndlineDatePicker.setText(util.GetCurrentDate().toString());
                btn_EndlineDatePicker.setPadding(8, 8, 8, 8);
                btn_EndlineDatePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(), "EndlineDatePicker");
                    }
                });

                // set values of endline
                title.setText("Endline 2");

                String[] baselineLangAdapter = {"Language", "Beg", "Letter", "Word", "Para", "Story"};
                ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, baselineLangAdapter);
                spinner_BaselineLang.setAdapter(baselineAdapter);

                String[] NumberRecoAdapter = {"Number Recognition", "Beg", "0-9", "10-99", "Sub", "Div"};
                ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, NumberRecoAdapter);
                spinner_NumberReco.setAdapter(recoAdapter);

                // show dialog
                endlineDialog.setCanceledOnTouchOutside(false);
                endlineDialog.show();

                if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                    tv_WordProblem.setVisibility(View.GONE);
                    WordAdd.setVisibility(View.GONE);
                    WordSub.setVisibility(View.GONE);
                }

                OprAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprAdd.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordAdd.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordAdd.setChecked(false);
                            WordAdd.setVisibility(View.GONE);
                        }
                    }
                });

                OprSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprSub.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordSub.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordSub.setChecked(false);
                            WordSub.setVisibility(View.GONE);
                        }
                    }
                });

                btn_Submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int BaselineSpinnerValue = spinner_BaselineLang.getSelectedItemPosition();
                        int NumberSpinnerValue = spinner_NumberReco.getSelectedItemPosition();

                        if (BaselineSpinnerValue > 0 && NumberSpinnerValue > 0) {
                            sp_BaselineLang.setSelection(0);
                            sp_NumberReco.setSelection(0);
                            EndlineButtonClicked = true;
                            testT = 2;
                            langSpin = BaselineSpinnerValue;
                            numSpin = NumberSpinnerValue;
                            aserDate = btn_EndlineDatePicker.getText().toString();

                            OA = OprAdd.isChecked() ? 1 : 0;
                            OS = OprSub.isChecked() ? 1 : 0;
                            OM = OprMul.isChecked() ? 1 : 0;
                            OD = OprDiv.isChecked() ? 1 : 0;
                            WA = WordAdd.isChecked() ? 1 : 0;
                            WS = WordSub.isChecked() ? 1 : 0;

                            Toast.makeText(AddStudentProfiles.this, "Endline 2 is filled.", Toast.LENGTH_SHORT).show();

                            if (endlineDialog.isShowing())
                                endlineDialog.dismiss();
                        } else {
                            Toast.makeText(AddStudentProfiles.this, "Please fill all the fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btn_Endline3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaults();

                // initialize dialog
                Dialog endlineDialog = new Dialog(AddStudentProfiles.this);
                endlineDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                endlineDialog.setContentView(R.layout.fragment_endline_dialog);
                endlineDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                // initialize dialog's widgets
                TextView title = endlineDialog.findViewById(R.id.tv_EndlineTitle);
                Spinner spinner_BaselineLang = endlineDialog.findViewById(R.id.spinner_BaselineLang);
                Spinner spinner_NumberReco = endlineDialog.findViewById(R.id.spinner_NumberReco);
                CheckBox OprAdd = endlineDialog.findViewById(R.id.OprAdd);
                CheckBox OprSub = endlineDialog.findViewById(R.id.OprSub);
                CheckBox OprMul = endlineDialog.findViewById(R.id.OprMul);
                CheckBox OprDiv = endlineDialog.findViewById(R.id.OprDiv);
                TextView tv_WordProblem = endlineDialog.findViewById(R.id.tv_WordProblem);
                CheckBox WordAdd = endlineDialog.findViewById(R.id.WordAdd);
                CheckBox WordSub = endlineDialog.findViewById(R.id.WordSub);
                Button btn_Submit = endlineDialog.findViewById(R.id.btn_Submit);
                btn_EndlineDatePicker = endlineDialog.findViewById(R.id.btn_EndlineDatePicker);
                btn_EndlineDatePicker.setText(util.GetCurrentDate().toString());
                btn_EndlineDatePicker.setPadding(8, 8, 8, 8);
                btn_EndlineDatePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(), "EndlineDatePicker");
                    }
                });

                // set values of endline
                title.setText("Endline 3");

                String[] baselineLangAdapter = {"Language", "Beg", "Letter", "Word", "Para", "Story"};
                ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, baselineLangAdapter);
                spinner_BaselineLang.setAdapter(baselineAdapter);

                String[] NumberRecoAdapter = {"Number Recognition", "Beg", "0-9", "10-99", "Sub", "Div"};
                ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, NumberRecoAdapter);
                spinner_NumberReco.setAdapter(recoAdapter);

                // show dialog
                endlineDialog.setCanceledOnTouchOutside(false);
                endlineDialog.show();

                if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                    tv_WordProblem.setVisibility(View.GONE);
                    WordAdd.setVisibility(View.GONE);
                    WordSub.setVisibility(View.GONE);
                }

                OprAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprAdd.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordAdd.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordAdd.setChecked(false);
                            WordAdd.setVisibility(View.GONE);
                        }
                    }
                });

                OprSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprSub.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordSub.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordSub.setChecked(false);
                            WordSub.setVisibility(View.GONE);
                        }
                    }
                });

                btn_Submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int BaselineSpinnerValue = spinner_BaselineLang.getSelectedItemPosition();
                        int NumberSpinnerValue = spinner_NumberReco.getSelectedItemPosition();

                        if (BaselineSpinnerValue > 0 && NumberSpinnerValue > 0) {
                            sp_BaselineLang.setSelection(0);
                            sp_NumberReco.setSelection(0);
                            EndlineButtonClicked = true;
                            testT = 3;
                            langSpin = BaselineSpinnerValue;
                            numSpin = NumberSpinnerValue;
                            aserDate = btn_EndlineDatePicker.getText().toString();

                            OA = OprAdd.isChecked() ? 1 : 0;
                            OS = OprSub.isChecked() ? 1 : 0;
                            OM = OprMul.isChecked() ? 1 : 0;
                            OD = OprDiv.isChecked() ? 1 : 0;
                            WA = WordAdd.isChecked() ? 1 : 0;
                            WS = WordSub.isChecked() ? 1 : 0;

                            Toast.makeText(AddStudentProfiles.this, "Endline 3 is filled.", Toast.LENGTH_SHORT).show();

                            if (endlineDialog.isShowing())
                                endlineDialog.dismiss();
                        } else {
                            Toast.makeText(AddStudentProfiles.this, "Please fill all the fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btn_Endline4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaults();

                // initialize dialog
                Dialog endlineDialog = new Dialog(AddStudentProfiles.this);
                endlineDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                endlineDialog.setContentView(R.layout.fragment_endline_dialog);
                endlineDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                // initialize dialog's widgets
                TextView title = endlineDialog.findViewById(R.id.tv_EndlineTitle);
                Spinner spinner_BaselineLang = endlineDialog.findViewById(R.id.spinner_BaselineLang);
                Spinner spinner_NumberReco = endlineDialog.findViewById(R.id.spinner_NumberReco);
                CheckBox OprAdd = endlineDialog.findViewById(R.id.OprAdd);
                CheckBox OprSub = endlineDialog.findViewById(R.id.OprSub);
                CheckBox OprMul = endlineDialog.findViewById(R.id.OprMul);
                CheckBox OprDiv = endlineDialog.findViewById(R.id.OprDiv);
                TextView tv_WordProblem = endlineDialog.findViewById(R.id.tv_WordProblem);
                CheckBox WordAdd = endlineDialog.findViewById(R.id.WordAdd);
                CheckBox WordSub = endlineDialog.findViewById(R.id.WordSub);
                Button btn_Submit = endlineDialog.findViewById(R.id.btn_Submit);
                btn_EndlineDatePicker = endlineDialog.findViewById(R.id.btn_EndlineDatePicker);
                btn_EndlineDatePicker.setText(util.GetCurrentDate().toString());
                btn_EndlineDatePicker.setPadding(8, 8, 8, 8);
                btn_EndlineDatePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(), "EndlineDatePicker");
                    }
                });

                // set values of endline
                title.setText("Endline 4");

                String[] baselineLangAdapter = {"Language", "Beg", "Letter", "Word", "Para", "Story"};
                ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, baselineLangAdapter);
                spinner_BaselineLang.setAdapter(baselineAdapter);

                String[] NumberRecoAdapter = {"Number Recognition", "Beg", "0-9", "10-99", "Sub", "Div"};
                ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(AddStudentProfiles.this, R.layout.custom_spinner, NumberRecoAdapter);
                spinner_NumberReco.setAdapter(recoAdapter);

                // show dialog
                endlineDialog.setCanceledOnTouchOutside(false);
                endlineDialog.show();

                if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                    tv_WordProblem.setVisibility(View.GONE);
                    WordAdd.setVisibility(View.GONE);
                    WordSub.setVisibility(View.GONE);
                }

                OprAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprAdd.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordAdd.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordAdd.setChecked(false);
                            WordAdd.setVisibility(View.GONE);
                        }
                    }
                });

                OprSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (OprSub.isChecked()) {
                            tv_WordProblem.setVisibility(View.VISIBLE);
                            WordSub.setVisibility(View.VISIBLE);
                        } else {
                            if (!OprAdd.isChecked() && !OprSub.isChecked()) {
                                tv_WordProblem.setVisibility(View.GONE);
                                WordAdd.setVisibility(View.GONE);
                                WordSub.setVisibility(View.GONE);
                            }
                            WordSub.setChecked(false);
                            WordSub.setVisibility(View.GONE);
                        }
                    }
                });

                btn_Submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int BaselineSpinnerValue = spinner_BaselineLang.getSelectedItemPosition();
                        int NumberSpinnerValue = spinner_NumberReco.getSelectedItemPosition();

                        if (BaselineSpinnerValue > 0 && NumberSpinnerValue > 0) {
                            sp_BaselineLang.setSelection(0);
                            sp_NumberReco.setSelection(0);
                            EndlineButtonClicked = true;
                            testT = 4;
                            langSpin = BaselineSpinnerValue;
                            numSpin = NumberSpinnerValue;
                            aserDate = btn_EndlineDatePicker.getText().toString();

                            OA = OprAdd.isChecked() ? 1 : 0;
                            OS = OprSub.isChecked() ? 1 : 0;
                            OM = OprMul.isChecked() ? 1 : 0;
                            OD = OprDiv.isChecked() ? 1 : 0;
                            WA = WordAdd.isChecked() ? 1 : 0;
                            WS = WordSub.isChecked() ? 1 : 0;

                            Toast.makeText(AddStudentProfiles.this, "Endline 4 is filled.", Toast.LENGTH_SHORT).show();

                            if (endlineDialog.isShowing())
                                endlineDialog.dismiss();
                        } else {
                            Toast.makeText(AddStudentProfiles.this, "Please fill all the fields !!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected radio button from radioGroup
                int selectedId = rg_Gender.getCheckedRadioButtonId();
                // find the radio button by returned id
                selectedGender = (RadioButton) findViewById(selectedId);
                gender = selectedGender.getText().toString();

                // Check AllSpinners Emptyness
                int StatesSpinnerValue = states_spinner.getSelectedItemPosition();
                int BlocksSpinnerValue = blocks_spinner.getSelectedItemPosition();
                int VillagesSpinnerValue = villages_spinner.getSelectedItemPosition();
                int GroupsSpinnerValue = groups_spinner.getSelectedItemPosition();

                // Spinners Selection
                if (StatesSpinnerValue > 0 && BlocksSpinnerValue > 0 && VillagesSpinnerValue > 0 && GroupsSpinnerValue > 0) {
                    // Checking Emptyness
                    if ((!edt_Fname.getText().toString().isEmpty() || !edt_Lname.getText().toString().isEmpty())) {
                        // Validations
                        if ((edt_Fname.getText().toString().matches("[a-zA-Z.? ]*")) && (edt_Lname.getText().toString().matches("[a-zA-Z.? ]*"))
                                && (edt_Mname.getText().toString().matches("[a-zA-Z.? ]*"))
                                && (!btn_BirthDatePicker.getText().toString().contains("Birth")) && (edt_Class.getText().toString().matches("[0-9]+"))) {

                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                            try {
                                cal.setTime(sdf.parse(btn_BirthDatePicker.getText().toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            stdAge = Integer.parseInt(Integer.toString(calculateAge(cal.getTimeInMillis())));

                            if (MultiPhotoSelectActivity.programID.equalsIgnoreCase("13")) {
                                // either baseline spinners are fully filled or not filled at all
                                if ((sp_BaselineLang.getSelectedItemPosition() > 0 && sp_NumberReco.getSelectedItemPosition() > 0)
                                        || (sp_BaselineLang.getSelectedItemPosition() == 0 && sp_NumberReco.getSelectedItemPosition() == 0)) {
                                    // Populate Std Data
                                    Student stdObj = new Student();
                                    stdObj.StudentID = randomUUIDStudent;
                                    stdObj.FirstName = edt_Fname.getText().toString();
                                    stdObj.MiddleName = edt_Mname.getText().toString();
                                    stdObj.LastName = edt_Lname.getText().toString();
                                    stdObj.Age = stdAge;
                                    stdObj.stdClass = Integer.parseInt(String.valueOf(edt_Class.getText()));
                                    stdObj.UpdatedDate = util.GetCurrentDateTime(false);
                                    stdObj.Gender = gender;
                                    stdObj.GroupID = GrpID;
                                    stdObj.CreatedBy = statdb.getValue("CRL");
                                    stdObj.newStudent = true;
                                    stdObj.StudentUID = "";
                                    stdObj.IsSelected = true;
                                    stdObj.CreatedOn = util.GetCurrentDateTime(false).toString();
                                    stdObj.DOB = btn_BirthDatePicker.getText().toString();
                                    sdb.insertData(stdObj);
                                    BackupDatabase.backup(AddStudentProfiles.this);
                                    if (MultiPhotoSelectActivity.programID.equalsIgnoreCase("13")) {
                                        if (sp_BaselineLang.getSelectedItemPosition() > 0 || sp_NumberReco.getSelectedItemPosition() > 0)
                                            EndlineButtonClicked = false;

                                        if (!EndlineButtonClicked) {
                                            // Baseline
                                            testT = 0;
                                            langSpin = sp_BaselineLang.getSelectedItemPosition();
                                            numSpin = sp_NumberReco.getSelectedItemPosition();
                                            aserDate = btn_DatePicker.getText().toString();
                                            OA = 0;
                                            OS = 0;
                                            OM = 0;
                                            OD = 0;
                                            WA = 0;
                                            WS = 0;
                                            IC = 0;
                                        }
                                        // Populate Aser Data
                                        Aser asr = new Aser();
                                        asr.StudentId = randomUUIDStudent;
                                        asr.GroupID = GrpID;
                                        asr.ChildID = "";
                                        asr.TestType = testT;
                                        asr.TestDate = aserDate;
                                        asr.Lang = langSpin;
                                        asr.Num = numSpin;
                                        asr.CreatedBy = statdb.getValue("CRL");
                                        asr.CreatedDate = new Utility().GetCurrentDate();
                                        asr.DeviceId = util.getDeviceID();
                                        asr.FLAG = IC;
                                        asr.OAdd = OA;
                                        asr.OSub = OS;
                                        asr.OMul = OM;
                                        asr.ODiv = OD;
                                        asr.WAdd = WA;
                                        asr.WSub = WS;
                                        asr.CreatedOn = new Utility().GetCurrentDateTime(false);
                                        adb.insertData(asr);

                                        Toast.makeText(AddStudentProfiles.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                                        BackupDatabase.backup(AddStudentProfiles.this);
                                        resetFormPartially();
                                    }
                                } else {
                                    Toast.makeText(AddStudentProfiles.this, "Please Fill All Fields !", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // default
                                // Populate Std Data
                                Student stdObj = new Student();
                                stdObj.StudentID = randomUUIDStudent;
                                stdObj.FirstName = edt_Fname.getText().toString();
                                stdObj.MiddleName = edt_Mname.getText().toString();
                                stdObj.LastName = edt_Lname.getText().toString();
                                stdObj.Age = stdAge;
                                stdObj.stdClass = Integer.parseInt(String.valueOf(edt_Class.getText()));
                                stdObj.UpdatedDate = util.GetCurrentDateTime(false);
                                stdObj.Gender = gender;
                                stdObj.GroupID = GrpID;
                                stdObj.CreatedBy = statdb.getValue("CRL");
                                stdObj.newStudent = true;
                                stdObj.StudentUID = "";
                                stdObj.IsSelected = true;
                                stdObj.CreatedOn = util.GetCurrentDateTime(false).toString();
                                stdObj.DOB = btn_BirthDatePicker.getText().toString();
                                sdb.insertData(stdObj);

                                Toast.makeText(AddStudentProfiles.this, "Record Inserted Successfully !!!", Toast.LENGTH_SHORT).show();
                                BackupDatabase.backup(AddStudentProfiles.this);
                                resetFormPartially();
                            }
                        } else {
                            Toast.makeText(AddStudentProfiles.this, "Please Enter Valid Input !!!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddStudentProfiles.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddStudentProfiles.this, "Please Fill all fields !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormReset();
            }
        });
    }


    int calculateAge(long date) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(date);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }
        return age;
    }

    private void resetFormPartially() {
        edt_Fname.getText().clear();
        edt_Mname.getText().clear();
        edt_Lname.getText().clear();
        edt_Class.getText().clear();
        imgView.setImageDrawable(null);
        UUID uuStdid = UUID.randomUUID();
        randomUUIDStudent = uuStdid.toString();
        EndlineButtonClicked = false;
        btn_BirthDatePicker.setText("Birth Date");
        btn_DatePicker.setText(util.GetCurrentDate().toString());
        sp_BaselineLang.setSelection(0);
        sp_NumberReco.setSelection(0);
        setDefaults();
    }

    private void updateEndlineDataTemp() {
        // fetch data
        AserDBHelper adb = new AserDBHelper(AddStudentProfiles.this);
        List<Aser> AserData;
        AserData = adb.GetAllByStudentID(randomUUIDStudent, 1);
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

                // set Data
            }
        } else {
            setDefaults();
        }
    }

    public void setDefaults() {
        testT = 0;
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

    private void initializeVariables() {
        MainActivity.sessionFlg = false;
        sp_NumberReco = findViewById(R.id.spinner_NumberReco);
        sp_BaselineLang = findViewById(R.id.spinner_BaselineLang);
        states_spinner = (Spinner) findViewById(R.id.spinner_SelectState);
        blocks_spinner = (Spinner) findViewById(R.id.spinner_SelectBlock);
        villages_spinner = (Spinner) findViewById(R.id.spinner_selectVillage);
        groups_spinner = (Spinner) findViewById(R.id.spinner_SelectGroups);
        playVideo = new PlayVideo();
        statdb = new StatusDBHelper(this);
        rb_Male = (RadioButton) findViewById(R.id.rb_Male);
        rb_Female = (RadioButton) findViewById(R.id.rb_Female);
        database = new VillageDBHelper(this);
        sdb = new StudentDBHelper(this);
        adb = new AserDBHelper(this);
        uuStdid = UUID.randomUUID();
        randomUUIDStudent = uuStdid.toString();
        edt_Fname = (EditText) findViewById(R.id.edt_FirstName);
        edt_Mname = (EditText) findViewById(R.id.edt_MiddleName);
        edt_Lname = (EditText) findViewById(R.id.edt_LastName);
        edt_Class = (EditText) findViewById(R.id.edt_Class);
        rg_Gender = (RadioGroup) findViewById(R.id.rg_Gender);
        btn_Capture = (Button) findViewById(R.id.btn_Capture);
        imgView = (ImageView) findViewById(R.id.imageView);
        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        btn_Clear = (Button) findViewById(R.id.btn_Clear);
        util = new Utility();
        btn_DatePicker = findViewById(R.id.btn_DatePicker);
        btn_BirthDatePicker = findViewById(R.id.btn_BirthDatePicker);
        btn_Endline1 = findViewById(R.id.btn_Endline1);
        btn_Endline2 = findViewById(R.id.btn_Endline2);
        btn_Endline3 = findViewById(R.id.btn_Endline3);
        btn_Endline4 = findViewById(R.id.btn_Endline4);
        AserForm = findViewById(R.id.AserForm);
        if (MultiPhotoSelectActivity.programID.equalsIgnoreCase("13"))
            AserForm.setVisibility(View.VISIBLE);
        else
            AserForm.setVisibility(View.GONE);
    }

    private void initializeAserDate() {
        btn_DatePicker.setText(util.GetCurrentDate().toString());
        btn_DatePicker.setPadding(8, 8, 8, 8);
        btn_DatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "DatePicker");

            }
        });
    }

    private void initializeBirthDate() {
        btn_BirthDatePicker.setPadding(8, 8, 8, 8);
        btn_BirthDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new BirthDatePickerFragment();
                newFragment.show(getFragmentManager(), "BirthDatePicker");
            }
        });
    }

    private void initializeNumberRecoSpinner() {
        String[] NumberRecoAdapter = {"Baseline (Number Recognition)", "Beg", "0-9", "10-99", "Sub", "Div"};
        ArrayAdapter<String> recoAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, NumberRecoAdapter);
        //sp_NumberReco.setPrompt("Number Reco Level");
        sp_NumberReco.setAdapter(recoAdapter);
    }

    private void initializeBaselineSpinner() {
        //sp_BaselineLang.setPrompt("Baseline Level");
        String[] baselineLangAdapter = {"Baseline (Lang)", "Beg", "Letter", "Word", "Para", "Story"};
        ArrayAdapter<String> baselineAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, baselineLangAdapter);
        sp_BaselineLang.setAdapter(baselineAdapter);
    }

    private void initializeStatesSpinner() {
        //Get Villages Data for States AllSpinners
        List<String> States = database.GetState();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, States);
        // Hint for AllSpinners
        states_spinner.setPrompt("Select State");
        states_spinner.setAdapter(StateAdapter);

        states_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states_spinner.getSelectedItem().toString();
                populateBlock(selectedState);
                groups_spinner.setSelection(0);
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
        blocks_spinner.setPrompt("Select Block");
        blocks_spinner.setAdapter(BlockAdapter);

        blocks_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBlock = blocks_spinner.getSelectedItem().toString();
                populateVillage(selectedBlock);
                groups_spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void populateVillage(String selectedBlock) {
        //Get Villages Data for Villages filtered by block for Spinners
        List<VillageList> BlocksVillages = database.GetVillages(selectedBlock);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<VillageList> VillagesAdapter = new ArrayAdapter<VillageList>(this, R.layout.custom_spinner, BlocksVillages);
        // Hint for AllSpinners
        villages_spinner.setPrompt("Select Village");
        villages_spinner.setAdapter(VillagesAdapter);
        villages_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VillageList village = (VillageList) parent.getItemAtPosition(position);
                vilID = village.getVillageId();
                populateGroups(vilID);
                groups_spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void populateGroups(int villageID) {
        //Get Groups Data for Villages filtered by Villages for Spinners
        gdb = new GroupDBHelper(this);
        final List<GroupList> GroupsVillages = gdb.GetGroups(villageID);
        //GroupsVillages.get(0).getGroupId();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<GroupList> GroupsAdapter = new ArrayAdapter<GroupList>(this, R.layout.custom_spinner, GroupsVillages);
        // Hint for AllSpinners
        groups_spinner.setPrompt("Select Group");
        groups_spinner.setAdapter(GroupsAdapter);
        groups_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ID = String.valueOf(groups_spinner.getSelectedItemId());
                GrpID = GroupsVillages.get(Integer.parseInt(ID)).getGroupId();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void FormReset() {
        UUID uuStdid = UUID.randomUUID();
        randomUUIDStudent = uuStdid.toString();
        states_spinner.setSelection(0);
        blocks_spinner.setSelection(0);
        villages_spinner.setSelection(0);
        groups_spinner.setSelection(0);
        edt_Fname.getText().clear();
        edt_Mname.getText().clear();
        edt_Lname.getText().clear();
        edt_Class.getText().clear();
        sp_BaselineLang.setSelection(0);
        sp_NumberReco.setSelection(0);
        btn_DatePicker.setText(util.GetCurrentDate().toString());
        btn_BirthDatePicker.setText("Birth Date");
        imgView.setImageDrawable(null);
        EndlineButtonClicked = false;
        setDefaults();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_Thumbnail) {
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bitmap thumbnail1 = data.getParcelableExtra("data");
                    imgView.setImageBitmap(thumbnail1);
                    try {

                        Context cnt;
                        cnt = this;
                        File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/");
                        //  File folder = new File(splashScreenVideo.fpath + "/MyClicks/");
                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdir();
                        }
                        if (success) {
                            // Do something on success
                            File outputFile = new File(folder, randomUUIDStudent + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                            thumbnail1.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            data = null;
                            thumbnail1 = null;
                            // To Refresh Contents of sharableFolder
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
                        } else {
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }

    @Override
    protected void onPause() {
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
                    scoreDBHelper = new ScoreDBHelper(AddStudentProfiles.this);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(AddStudentProfiles.this);
                    System.exit(0);
                    finishAffinity();

                }
            }
        }.start();

    }
}