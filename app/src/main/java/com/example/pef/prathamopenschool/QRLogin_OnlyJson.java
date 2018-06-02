package com.example.pef.prathamopenschool;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRLogin_OnlyJson extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    ViewGroup content_frame;
    TextView tv_stud_one;
    TextView tv_stud_two;
    TextView tv_stud_three;
    TextView tv_stud_four;
    TextView tv_stud_five;
    Button btn_Start, btn_Reset;

    public ZXingScannerView startCameraScan;
    Student std;
    int totalStudents = 0;
    Dialog dialog;
    Boolean setStud = false;
    ArrayList<Student> stdList = new ArrayList<Student>();
    boolean appName = false;
    StatusDBHelper s;

    StatusDBHelper statusDBHelper;
    TextView tv_title;
    static String programID, language;

    static CountDownTimer cd;
    static Long timeout = (long) 20000 * 60;
    static Long duration = timeout;
    boolean timer;
    String newNodeList;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    public static RelativeLayout myView;
    static Boolean pauseFlg = false;
    public static String deviceID = "DeviceID";
    int aajKaSawalPlayed = 3;
    String checkQJson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrlogin);

        playVideo = new PlayVideo();
        programID = getProgramId();
        setLanguage();
        setTitle();
        setAppName();


        // Memory Allocation
        content_frame = findViewById(R.id.content_frame);
        tv_stud_one = findViewById(R.id.tv_stud_one);
        tv_stud_two = findViewById(R.id.tv_stud_two);
        tv_stud_three = findViewById(R.id.tv_stud_three);
        tv_stud_four = findViewById(R.id.tv_stud_four);
        tv_stud_five = findViewById(R.id.tv_stud_five);
        btn_Reset = findViewById(R.id.btn_Reset);
        btn_Start = findViewById(R.id.btn_Start);
        tv_stud_one.setVisibility(View.GONE);
        tv_stud_two.setVisibility(View.GONE);
        tv_stud_three.setVisibility(View.GONE);
        tv_stud_four.setVisibility(View.GONE);
        tv_stud_five.setVisibility(View.GONE);


        stdList = new ArrayList<Student>();
        initCamera();


        btn_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content_frame.setVisibility(View.VISIBLE);

                stdList.clear();
                totalStudents = 0;

                tv_stud_one.setText("");
                tv_stud_two.setText("");
                tv_stud_three.setText("");
                tv_stud_four.setText("");
                tv_stud_five.setText("");

                tv_stud_one.setVisibility(View.GONE);
                tv_stud_two.setVisibility(View.GONE);
                tv_stud_three.setVisibility(View.GONE);
                tv_stud_four.setVisibility(View.GONE);
                tv_stud_five.setVisibility(View.GONE);

                scanNextQRCode();

            }
        });


        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stdList.size() > 0)
                    setValues();
                else
                    Toast.makeText(QRLogin_OnlyJson.this, "Please Add Student !!!", Toast.LENGTH_SHORT).show();
            }
        });

    }// onCreate

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        startCameraScan.resumeCameraPreview(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraScan.resumeCameraPreview(this);
    }

    private void setValues() {

        MultiPhotoSelectActivity.selectedGroupsScore = "";
        Attendance attendance = null;

        AttendanceDBHelper attendanceDBHelper = new AttendanceDBHelper(this);


        //todo check logic
        MultiPhotoSelectActivity.presentStudents = new String[stdList.size()];
        try {

            if (stdList != null && stdList.size() > 0) {
                for (int i = 0; i < stdList.size(); i++) {
                    attendance = new Attendance();
                    attendance.SessionID = MultiPhotoSelectActivity.sessionId;
                    attendance.PresentStudentIds = stdList.get(i).getStudentID();
                    MultiPhotoSelectActivity.presentStudents[i] = stdList.get(i).getStudentID();
                    attendance.GroupID = "QR";
                    if (!MultiPhotoSelectActivity.selectedGroupsScore.contains(attendance.GroupID)) {
                        MultiPhotoSelectActivity.selectedGroupsScore += attendance.GroupID + ",";
                    }
                    attendanceDBHelper.Add(attendance);
                }
                BackupDatabase.backup(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*
        // Get Current Date
        String currentDate = new Utility().GetCurrentDate();

        // Fetch records
        StatusDBHelper sdbh = new StatusDBHelper(QRLogin.this);
        // separate comma

        // get AKS Played for QR Code or not NEW 2.1.6
        List<String> AKSRecords = sdbh.getAKSPlayedByQR(MyApplication.getAccurateDate(), stdList);


        if (AKSRecords == null) {
            Toast.makeText(QRLogin.this, "AKSRecords null", Toast.LENGTH_LONG).show();
        } else if (!AKSRecords.equals(null)) {

            if (AKSRecords.size() == 0) {
                aajKaSawalPlayed = 0;
            }

            for (int i = 0; i < AKSRecords.size(); i++) {

                if (AKSRecords.get(i).contains(currentDate)) {
                    // If Record Found i.e Aaj Ka Sawaal Played
                    aajKaSawalPlayed = 1;
                } else {
                    // If Record NOT Found i.e Aaj Ka Sawaal Played
                    aajKaSawalPlayed = 0;
                }
            }

        }

*/
        // if Questions.json not present
        JSONArray queJsonArray = null;
        try {
            checkQJson = loadQueJSONFromAsset();
        } catch (Exception e) {
            aajKaSawalPlayed = 3;
            e.printStackTrace();
        }
        if (checkQJson == null) {
            aajKaSawalPlayed = 3;
        }

        // Aaj Ka Sawaal Played
        if (aajKaSawalPlayed == 1) {
            if (startCameraScan != null) {
                startCameraScan.stopCamera();
            }
            Intent main = new Intent(QRLogin_OnlyJson.this, MainActivity.class);
            if (assessmentLogin.assessmentFlg) {
                main.putExtra("nodeList", newNodeList.toString());
            }
            MainActivity.sessionFlg = true;
            scoreDBHelper = new ScoreDBHelper(this);
            playVideo.calculateEndTime(scoreDBHelper);
            BackupDatabase.backup(this);
            try {
                System.exit(0);
                finishAffinity();

            } catch (Exception e) {
                e.printStackTrace();
            }
            main.putExtra("aajKaSawalPlayed", "3");
            main.putExtra("selectedGroupId", "QR");
            startActivity(main);
            finish();
        }
        // Aaj Ka Sawaal NOT Played
        else if (aajKaSawalPlayed == 0) {

            // Update updateTrailerCountbyGroupID to 1 if played
//            StatusDBHelper updateTrailerCount = new StatusDBHelper(QRLogin.this);
//            updateTrailerCount.updateTrailerCountbyGroupID(1, "QR");
//            BackupDatabase.backup(QRLogin.this);

            if (startCameraScan != null) {
                startCameraScan.stopCamera();
            }
            Intent main = new Intent(QRLogin_OnlyJson.this, MainActivity.class);
            if (assessmentLogin.assessmentFlg) {
                main.putExtra("nodeList", newNodeList.toString());
            }
            MainActivity.sessionFlg = true;
            scoreDBHelper = new ScoreDBHelper(this);
            playVideo.calculateEndTime(scoreDBHelper);
            BackupDatabase.backup(this);
            try {
                finishAffinity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            main.putExtra("aajKaSawalPlayed", "3");
            main.putExtra("selectedGroupId", "QR");

            startActivity(main);
            finish();
        }

        // if Questions.json not present
        else if (aajKaSawalPlayed == 3) {

            if (startCameraScan != null) {
                startCameraScan.stopCamera();
            }
            Intent main = new Intent(QRLogin_OnlyJson.this, MainActivity.class);
            if (assessmentLogin.assessmentFlg) {
                main.putExtra("nodeList", newNodeList.toString());
            }
            MainActivity.sessionFlg = true;
            scoreDBHelper = new ScoreDBHelper(QRLogin_OnlyJson.this);
            playVideo.calculateEndTime(scoreDBHelper);
            BackupDatabase.backup(QRLogin_OnlyJson.this);
            try {
                finishAffinity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            main.putExtra("aajKaSawalPlayed", "3");
            main.putExtra("selectedGroupId", "QR");

            startActivity(main);
            finish();
        }

    }


    private void setAppName() {
        if (appName == false) {
            s = new StatusDBHelper(QRLogin_OnlyJson.this);
            // app name
            if (QRLogin.programID.equals("1"))
                s.insertInitialData("appName", "Pratham Digital - H Learning");
            else if (QRLogin.programID.equals("2"))
                s.insertInitialData("appName", "Pratham Digital - Read India");
            else if (QRLogin.programID.equals("3"))
                s.insertInitialData("appName", "Pratham Digital - Second Chance");
            else if (QRLogin.programID.equals("4"))
                s.insertInitialData("appName", "Pratham Digital - Pratham Institute");

        } else {
            s = new StatusDBHelper(QRLogin_OnlyJson.this);
            // app name
            if (QRLogin.programID.equals("1"))
                s.Update("appName", "Pratham Digital - H Learning");
            else if (QRLogin.programID.equals("2"))
                s.Update("appName", "Pratham Digital - Read India");
            else if (QRLogin.programID.equals("3"))
                s.Update("appName", "Pratham Digital - Second Chance");
            else if (QRLogin.programID.equals("4"))
                s.Update("appName", "Pratham Digital - Pratham Institute");
        }
    }

    // Reading CRL Json From Internal Memory
    public String loadQueJSONFromAsset() {
        String queJsonStr = null;

        try {
            File queJsonSDCard = new File(splashScreenVideo.fpath + "AajKaSawaal/", "Questions.json");
            FileInputStream stream = new FileInputStream(queJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                queJsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return queJsonStr;
    }


    private void setTitle() {
        // set Title according to program
        if (QRLogin.programID.equals("1"))
            setTitle("Pratham Digital - H Learning");
        else if (QRLogin.programID.equals("2"))
            setTitle("Pratham Digital - Read India");
        else if (QRLogin.programID.equals("3"))
            setTitle("Pratham Digital - Second Chance");
        else if (QRLogin.programID.equals("4"))
            setTitle("Pratham Digital - Pratham Institute");

    }

    // Reading configuration Json From SDCard
    public String getProgramId() {

        String progIDString = null;
        try {
            File myJsonFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Config.json");
            FileInputStream stream = new FileInputStream(myJsonFile);
            String jsonStr = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

            JSONObject jsonObj = new JSONObject(jsonStr);
            progIDString = jsonObj.getString("programId");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return progIDString;
    }


    public void setLanguage() {
        String langString = null;
        try {
            File myJsonFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/Config.json");
            FileInputStream stream = new FileInputStream(myJsonFile);
            String jsonStr = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
            JSONObject jsonObj = new JSONObject(jsonStr);
            langString = jsonObj.getString("programLanguage");

            StatusDBHelper sdb;
            sdb = new StatusDBHelper(QRLogin_OnlyJson.this);
            sdb.Update("TabLanguage", langString);
            BackupDatabase.backup(QRLogin_OnlyJson.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (startCameraScan != null) {
            startCameraScan.stopCamera();
        }
        super.onBackPressed();
    }

    private String[] decodeStudentId(String text, String s) {
        return text.split(s);
    }

    public void scanNextQRCode() {
        if (startCameraScan != null) {
            startCameraScan.stopCamera();
        }
        startCameraScan.startCamera();
        startCameraScan.resumeCameraPreview(this);
    }

    public void showQrDialog(String studentName) {

        dialog = new Dialog(QRLogin_OnlyJson.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_dialog_for_qrscan);
        dialog.setCanceledOnTouchOutside(false);
        TextView text = (TextView) dialog.findViewById(R.id.dialog_tv_student_name);
        ImageView iv_close = (ImageView) dialog.findViewById(R.id.dialog_iv_close);
        text.setText("Hi " + studentName);

        dialog.show();

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (totalStudents == 5) {
                    content_frame.setVisibility(View.GONE);
                    showStudentName(totalStudents);
                } else {
                    if (setStud) {
                        setStud = false;
                        showStudentName(totalStudents);
                    }
                    scanNextQRCode();
                }

            }
        });

        Button scanNextQR = (Button) dialog.findViewById(R.id.dialog_btn_scan_qr);
        scanNextQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (totalStudents == 5) {
                    content_frame.setVisibility(View.GONE);
                    showStudentName(totalStudents);
                } else {
                    if (setStud) {
                        setStud = false;
                        showStudentName(totalStudents);
                    }
                    scanNextQRCode();
                }
            }
        });

    }

    private void showStudentName(int totalStudents) {

        switch (totalStudents) {
            case 1:
                tv_stud_one.setVisibility(View.VISIBLE);
                tv_stud_one.setText("" + stdList.get(0).getFirstName());
                break; // break is optional
            case 2:
                tv_stud_two.setVisibility(View.VISIBLE);
                tv_stud_two.setText("" + stdList.get(1).getFirstName());
                break; // break is optional
            case 3:
                tv_stud_three.setVisibility(View.VISIBLE);
                tv_stud_three.setText("" + stdList.get(2).getFirstName());
                break; // break is optional
            case 4:
                tv_stud_four.setVisibility(View.VISIBLE);
                tv_stud_four.setText("" + stdList.get(3).getFirstName());
                break; // break is optional
            case 5:
                tv_stud_five.setVisibility(View.VISIBLE);
                tv_stud_five.setText("" + stdList.get(4).getFirstName());
                break; // break is optional
        }
    }

    @Override
    public void handleResult(Result result) {
        try {
            boolean dulicateQR = false;
            startCameraScan.stopCamera();
            Log.d("RawResult:::", "****" + result.getText());
            // Json Parsing
            JSONObject jsonobject = new JSONObject(result.getText());
            String id = jsonobject.getString("stuId");
            String name = jsonobject.getString("name");

            /*// got result in json format
            Pattern pattern = Pattern.compile("[A-Za-z0-9]+-[A-Za-z._]{2,50}");
            Matcher mat = pattern.matcher(result.getText());

            if (mat.matches()) {
*/
            if (stdList.size() <= 0)
                qrEntryProcess(result);
            else {
                for (int i = 0; i < stdList.size(); i++) {
                    // change
                    String[] currentIdArr = {id};
                    String currId = currentIdArr[0];
                    if (stdList.get(i).getStudentID().equalsIgnoreCase("" + currId)) {
//                            Toast.makeText(this, "Already Scaned", Toast.LENGTH_SHORT).show();
                        showQrDialog(", This QR Was Already Scaned");
                        setStud = false;
                        dulicateQR = true;
                        break;
                    }
                }
                if (!dulicateQR) {
                    qrEntryProcess(result);
                }
            }
            /*} else {
                startCameraScan.startCamera();
                startCameraScan.resumeCameraPreview(this);
                BackupDatabase.backup(this);
            }*/
        } catch (Exception e) {
            Toast.makeText(this, "Invalid QR Code !!!", Toast.LENGTH_SHORT).show();
            btn_Reset.performClick();
            e.printStackTrace();
        }
    }

    public void qrEntryProcess(Result result) {
        totalStudents++;
        String sid = "", sname = "", sscore = "", salias = "";
        std = new Student(sid, sname, "QRGroupID");
//        Toast.makeText(this, "" + totalStudents, Toast.LENGTH_SHORT).show();
        if (totalStudents < 6) {

            // todo Parse json & separate id & name
            String resultID = "", resultName = "";
            try {
                JSONObject jsonobject = new JSONObject(result.getText());
                resultID = jsonobject.getString("stuId");
                resultName = jsonobject.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Valid pattern
            String[] id = {resultID};

            String stdId = id[0];
            //String stdFirstName = id[1];
            String[] name = {resultName};
            String stdFirstName = name[0];
            String stdLastName = "";
            if (name.length > 1)
                stdLastName = name[1];

            std.setStudentID(stdId);
            std.setFirstName(stdFirstName);

//            Toast.makeText(QRLogin.this, "ID" + stdId, Toast.LENGTH_LONG).show();
//            Toast.makeText(QRLogin.this, "First" + stdFirstName, Toast.LENGTH_LONG).show();
            stdList.add(std);

            //scanNextQRCode();
            setStud = true;
            showQrDialog(stdFirstName);
        }

    }

    public void initCamera() {
        startCameraScan = new ZXingScannerView(this);
        startCameraScan.setResultHandler(this);
        content_frame.addView((startCameraScan));
        startCameraScan.startCamera();
        startCameraScan.resumeCameraPreview(this);
    }


}
