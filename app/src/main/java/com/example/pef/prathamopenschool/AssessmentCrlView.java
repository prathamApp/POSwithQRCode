package com.example.pef.prathamopenschool;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.interfaces.StudentListViewInterface;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AssessmentCrlView extends AppCompatActivity implements StudentListViewInterface {

    EditText dp_FromDate, dp_ToDate, et_datepicker;
    String fromDate="",toDate="";
    Calendar myCalendar;
    TextView studentNameTitle;
    ListView assessmentstudentList;
    StudentDBHelper studentDBHelper;
    AssessmentScoreDBHelper assessmentScoreDBHelper;
    String clickedStudentId;
    List<JSONObject> studentSessionCount;
    List<AssessmentScore> assessmentScores;
    String[] studentArray;
    String[] imgPath;
    JSONArray nextCickNodes;
    AssessmentStudentAdapter adapter;
    Utility utility;
    GraphReport graphReport;
    public static Context cont;
    BarChart graph;
    Boolean myLabels = false;
    JSONArray AssessmentNavigate;
    String myLabelsArray[];

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_crl_view);
        getSupportActionBar().hide();

        assessmentScoreDBHelper = new AssessmentScoreDBHelper(this);
        studentDBHelper = new StudentDBHelper(this);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        dp_FromDate = findViewById(R.id.dp_FromDate);
        dp_FromDate.setTag("fromDate");
        dp_ToDate = findViewById(R.id.dp_ToDate);
        dp_ToDate.setTag("toDate");
        et_datepicker = new EditText(this);
        assessmentstudentList = (ListView) findViewById(R.id.assessmentstudentList);
        studentNameTitle = (TextView) findViewById(R.id.studentNameTitle);

        utility = new Utility();
        cont = this;

        fromDate=utility.GetCurrentDate()+" 00:00:00";
        toDate=utility.GetCurrentDate()+" 23:59:59";

        assessmentScores = new AssessmentScoreDBHelper(this).GetAllAssessmentScore(false);
        studentSessionCount = new AssessmentScoreDBHelper(this).GetStudentSessionCount();

        showStudents(assessmentScores);
        graph = (BarChart) findViewById(R.id.myBarGraph);

        myCalendar = Calendar.getInstance();

        setPickerDialogs(dp_FromDate, utility.GetCurrentDate());
        setPickerDialogs(dp_ToDate, utility.GetCurrentDate());


        final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Toast.makeText(AssessmentCrlView.this, "" + updateLabel(et_datepicker), Toast.LENGTH_SHORT).show();
            }
        };

        setDatePickerListeners(dp_FromDate, datePicker);
        setDatePickerListeners(dp_ToDate, datePicker);
    }

    public void ParseAssessmentConfig() {
        JSONArray nextNodeList = null;
        try {
            File myJsonFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Config.json");
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

            // Getting data JSON Array nodes
            AssessmentNavigate = jsonObj.getJSONArray("nodelist");

            for (int i = 0; i < AssessmentNavigate.length(); i++) {

                JSONObject assessJsonData = AssessmentNavigate.getJSONObject(i);

                if (assessJsonData.get("nodeType").equals("Assessment")) {
                    nextNodeList = assessJsonData.getJSONArray("nodelist");
                    myLabels = true;
                    getAssessmentDataFromConfig(nextNodeList, myLabels);
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAssessmentDataFromConfig(JSONArray nextNodeList, Boolean myLabels) {

        if (nextNodeList != null) {
            try {

                int myCount = nextNodeList.length();

                if (myLabels) {
                    nextCickNodes = nextNodeList;
                    myLabelsArray = new String[myCount];
                    for (int i = 0; i < myCount; i++) {
                        myLabelsArray[i] = (String) nextNodeList.getJSONObject(i).get("nodeTitle");
                        Log.d("Titles ::::: ", myLabelsArray[i].toString());
                    }
                }

                ArrayList<JSONObject> assessmentResources = new ArrayList<JSONObject>();
//                JSONArray assessmentResources = new JSONArray();

                for (int i = 0; i < myCount; i++) {
                    String tempNode = nextNodeList.getJSONObject(i).get("nodeType").toString();
                    if (nextNodeList.getJSONObject(i).get("nodeType").toString().equalsIgnoreCase("Resource")) {
                        JSONObject nameId = new JSONObject();
                        String id = nextNodeList.getJSONObject(i).get("resourceId").toString();
                        nameId.put(myLabelsArray[i], id == null ? "" : id);
                        assessmentResources.add(nameId);
                    }else{
                        getAllAssessmentResIds(assessmentResources, myLabelsArray[i], (JSONArray) nextNodeList.getJSONObject(i).get("nodelist"));
                    }
                }

                for (int x = 0; x < assessmentResources.size(); x++)
                    Log.d("Ass Res ::::::>>>", x + " ::::" + assessmentResources.get(x));

                String assessResString[] = new String[myCount];
                assessResString = createResString(assessmentResources, myLabelsArray);

                String scoreArray[] =new String[myCount];

                for(int k = 0 ; k<myCount ; k++){
                    float ansPerc = 0;
                    assessmentScores  = assessmentScoreDBHelper.GetAssessmentScoreForReports(clickedStudentId , assessResString[k], fromDate , toDate);
                    float scored, totalm;
                    scored = assessmentScores.get(0).ScoredMarks;
                    totalm = assessmentScores.get(0).TotalMarks;
                    if(totalm>0){
                        ansPerc = ((scored / totalm) * 100);
                    }
                    scoreArray[k] = String.valueOf(ansPerc);
                }

                generateGraph(scoreArray, myLabelsArray);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public String[] createResString(ArrayList<JSONObject> assessmentResources, String[] myLabelsArray) {
        String [] resArray = new String[myLabelsArray.length];
        try {

            String resStr = "";

            for (int i = 0; i < myLabelsArray.length; i++) {
                resStr = "";
                for (int j = 0; j < assessmentResources.size(); j++) {

                    if (assessmentResources.get(j).has(myLabelsArray[i])) {
                        resStr += "'" + assessmentResources.get(j).getString(myLabelsArray[i]) + "',";
                    }
                }
                if (resStr!= null && resStr.length() > 0 && resStr.charAt(resStr.length() - 1) == ',') {
                    resStr = resStr.substring(0, resStr.length() - 1);
                }
                resArray[i] = resStr;
            }

            for (int i=0 ; i<resArray.length;i++)
                Log.d("temp", "============" + resArray[i] +"============");

            return resArray;
        } catch (Exception e) {
            e.printStackTrace();
            return resArray;
        }
    }

    public void getAllAssessmentResIds(ArrayList<JSONObject> assessmentResources, String s, JSONArray nodelist) {

        try {
            if (nodelist != null) {

                for (int i = 0; i < nodelist.length(); i++) {
                    if (nodelist.getJSONObject(i).get("nodeType").equals("Resource")) {
                        JSONObject nameId = new JSONObject();
                        String id = nodelist.getJSONObject(i).get("resourceId").toString();
                        nameId.put(s, id == null ? "" : id);
                        assessmentResources.add(nameId);
                    } else {
                        JSONArray newNodeList = (JSONArray) nodelist.getJSONObject(i).get("nodelist");
                        getAllAssessmentResIds(assessmentResources, s, newNodeList);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void generateGraph(String[] scoreArray, String[] myLabelsArray) {

        graph.setPinchZoom(false);

        graph.animateXY(1000, 1000);
        graph.setMaxVisibleValueCount(50);

        XAxis xAxis = graph.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter( myLabelsArray));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);

        graph.setDrawBarShadow(false);
        graph.setDrawGridBackground(false);
//        graph.getData().setHighlightEnabled(!graph.getData().isHighlightEnabled());

        graph.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                try {

                    int nodeIndex = (int) e.getX();
                    JSONArray nextList;

                    JSONObject assessJsonData = nextCickNodes.getJSONObject(nodeIndex);

                        nextList = assessJsonData.getJSONArray("nodelist");
                        myLabels = true;
                        getAssessmentDataFromConfig(nextList , myLabels);

                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected() {

            }

        });

        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setLabelCount(8, false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setDrawGridLines(true);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = graph.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        setData(scoreArray, 100);
    }

    private void setData(String [] scoreArray, float range) {

        float start = 1f;
        int count = scoreArray.length;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        List<String> myColors = new ArrayList<String>();

//        for (int i = (int) start; i < start + count + 1; i++) {
        for (int i = 0; i < count; i++) {
/*
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);
*/
            yVals1.add(new BarEntry(i, Float.parseFloat((scoreArray[i]))));
        }

        BarDataSet set1;

        if (graph.getData() != null &&
                graph.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) graph.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            set1.setColors(ColorTemplate.MATERIAL_COLORS);
            graph.getData().notifyDataChanged();
            graph.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "");
            set1.setDrawIcons(false);
            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);
            graph.setData(data);

        }
    }

    private void showStudents(List<AssessmentScore> assessmentScores) {

        String studentId, studentName = "abc";
        float marksScore, marksOutOf, assesMarks, assesTot;
        ArrayList<JSONObject> allData = new ArrayList<JSONObject>();
        JSONObject studentDataWithImgs = null;

        try {

            studentArray = new String[assessmentScores.size()];
            imgPath = new String[assessmentScores.size()];

            String fileNameWithExtension = "";//default fileName

            for (int i = 0; i < assessmentScores.size(); i++) {
                studentId = assessmentScores.get(i).GroupID;
                assesMarks = (float) assessmentScores.get(i).ScoredMarks;
                assesTot = (float) assessmentScores.get(i).TotalMarks;

                Student SelectedStudent = studentDBHelper.GetStudentDataByStdID(studentId);
                studentName = SelectedStudent.FirstName + " " + SelectedStudent.LastName;
                String gender = SelectedStudent.Gender;
                studentArray[i] = studentName;

                int flag = 0;

                File folder = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles");
                if (folder.exists()) {
                    File photos[] = folder.listFiles();
                    String stdid = studentId;
                    String stdName = studentName;

                    for (int j = 0; j < photos.length; j++) {//noOfImages
                        fileNameWithExtension = photos[j].getName();
                        String filePath = photos[j].getAbsolutePath();
                        String[] fileName = fileNameWithExtension.split("\\.");
                        if (fileName[0].equals(studentId)) {
                            flag = 1;
                            studentDataWithImgs = new JSONObject();
                            studentDataWithImgs.put("studentId", stdid);
                            studentDataWithImgs.put("studentName", stdName);
                            studentDataWithImgs.put("imgPath", filePath);
                            imgPath[i] = filePath;
                            allData.add(studentDataWithImgs);
                            break;
                        }
                    }
                    if (flag == 0) {
                        studentDataWithImgs = new JSONObject();
                        studentDataWithImgs.put("studentId", stdid);
                        studentDataWithImgs.put("studentName", stdName);
                        if ((gender.equalsIgnoreCase("M")) || (gender.equalsIgnoreCase("Male"))) {
                            studentDataWithImgs.put("imgPath", Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/Boys/" + generateRandomNumber() + ".png");
                        } else {
                            studentDataWithImgs.put("imgPath", Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles/Girls/" + generateRandomNumber() + ".png");
                        }
                        allData.add(studentDataWithImgs);

                    }

                }

            }
            adapter = new AssessmentStudentAdapter(AssessmentCrlView.this, allData, AssessmentCrlView.this);
            assessmentstudentList.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateRandomNumber() {
        Random rand = new Random();

        int n = rand.nextInt(3) + 1;
        return Integer.toString(n);
    }

    private int generateGraphRandomNumber() {
        Random rand = new Random();

        int n = rand.nextInt(10) + 1;
        return n;
    }

    private String updateLabel(EditText date1) {

        String myFormat = "dd-MM-yyyy"; //In which you need put here
        String customFormat = "dd-MM-yyyy hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        SimpleDateFormat custom = new SimpleDateFormat(customFormat,Locale.US);
        date1.setText(sdf.format(myCalendar.getTime()));
        String temp = (String) date1.getTag();
        if(temp.equals("fromDate")){
            fromDate = date1.getText() + " 00:00:00";
        }else {
            toDate = date1.getText() + " 23:59:59";
        }
        return sdf.format(myCalendar.getTime());
    }

    public void setPickerDialogs(EditText editText, String hint) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setKeyListener(null);
        editText.setHint(hint);
    }

    public void setDatePickerListeners(final EditText myDatePicker, final DatePickerDialog.OnDateSetListener datePicker) {
        myDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd = new DatePickerDialog(AssessmentCrlView.this, datePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dpd.show();
                et_datepicker = myDatePicker;
            }
        });
    }

    @Override
    public void onStudentClicked(int Position, String studentId, String studentName) {
        adapter.updateSelection(Position);
        Log.d("my tag :::::::", " studentId = " + studentId);
        clickedStudentId = studentId;
        studentNameTitle.setText(studentName);
        ParseAssessmentConfig();
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
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    finishAffinity();
                }
            }
        }.start();

    }

}