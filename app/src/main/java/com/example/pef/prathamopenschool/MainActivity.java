package com.example.pef.prathamopenschool;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView, horizontalrecyclerView;
    private CardAdapter adapter;
    private List<Card> cardList;

    ArrayList<String> resIdArray = new ArrayList<String>();
    ArrayList<String> resPathArray = new ArrayList<String>();

    public static String fpath, lessonSession, startTime, resourceId;
    String newNodeList, myNodeTitle;
    Utility utility;
    JSONArray contentNavigate;
    boolean mainFlag, loadFlg = false, isBound = false;
    Intent i;
    public static TextToSp ttspeech;
    int currentResCnt = -1;

    static Context mContext, sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    TextView bread_crums;
    private HashMap<String, String> navigateJson;
    static boolean sessionFlg = false;
    static boolean LangFlag = false;
    boolean destroyed = false;
    Utility Util;

    // Aaj Ka Sawaal
    TextView tv_Que;
    Button tv_opt1, tv_opt2, tv_opt3, tv_opt4;//, tv_result;
    //    RadioGroup rg_Opt;
//    RadioButton rb_Opt1, rb_Opt2, rb_Opt3, rb_Opt4, selectedOption;
    ImageButton btn_Submit, btn_Skip, btn_videoHint;
    AttendanceDBHelper attendanceDBHelper;
    String QueId, Question, QuestionType, Subject, Option1, Option2, Option3, Option4, Answer, resourceType, resourcePath, programLanguage;
    String aajKaSawaalStartTime;
    String selectedOption = "";
    int selectedBtn;
    int videoHintWatched = 0;
    private VideoView videoView;
    MediaController mediaController;
    String selectedGroupId;
    private String gid = "";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Util = new Utility();
        attendanceDBHelper = new AttendanceDBHelper(MainActivity.this);

        // Get Selected Group ID
        Intent i = getIntent();
        selectedGroupId = i.getStringExtra("selectedGroupId");
        String aajKaSawalPlayed = i.getStringExtra("aajKaSawalPlayed");

        // Decide the next screen depending on aajKaSawalPlayed status
        // Aaj Ka Sawaal Played
        if (aajKaSawalPlayed != null && aajKaSawalPlayed.equals("1")) {
            // Do nothing
        }
        // Aaj Ka Sawaal NOT Played
        else if (aajKaSawalPlayed != null && aajKaSawalPlayed.equals("0")) {

            try {

                // Play Aaj Ka Sawaal

                // Update updateTrailerCountbyGroupID to 1 if played ( USELESS OPERATION since we are deciding on the basis of Time not played or not)
//                StatusDBHelper updateTrailerCount = new StatusDBHelper(MainActivity.this);
//                updateTrailerCount.updateTrailerCountbyGroupID(1, selectedGroupId);
                BackupDatabase.backup(MainActivity.this);

                // MediaPlayer Memory Allocation
                final MediaPlayer correct = MediaPlayer.create(MainActivity.this, R.raw.correct);
                final MediaPlayer wrong = MediaPlayer.create(MainActivity.this, R.raw.wrong);
                // Dialog Memory Allocation
                final Dialog resultDialog = new Dialog(MainActivity.this);
                final Dialog dialog = new Dialog(MainActivity.this);

                resultDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                resultDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                resultDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.sample_aajkasawaal);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                // Layout Memory Allocation
                LinearLayout mainScreen = dialog.findViewById(R.id.sampleAajkaSawaal);
                LinearLayout correctScreen = dialog.findViewById(R.id.aajkaSawaal_correct);
                LinearLayout wrongScreen = dialog.findViewById(R.id.aajkaSawaal_wrong);

                // Layout Appearance
                mainScreen.setVisibility(View.VISIBLE);
                correctScreen.setVisibility(View.GONE);
                wrongScreen.setVisibility(View.GONE);

                // Memory Allocation
                tv_Que = dialog.findViewById(R.id.tv_question);
                //tv_result = dialog.findViewById(R.id.tv_result);
                btn_Submit = dialog.findViewById(R.id.btn_submit);
                btn_Skip = dialog.findViewById(R.id.btn_skip);
                tv_opt1 = dialog.findViewById(R.id.opt1);
                tv_opt2 = dialog.findViewById(R.id.opt2);
                tv_opt3 = dialog.findViewById(R.id.opt3);
                tv_opt4 = dialog.findViewById(R.id.opt4);

                btn_Submit.setEnabled(false);
                btn_Submit.setClickable(false);

                // new changes
                btn_videoHint = dialog.findViewById(R.id.btn_videoHint);
                // Setting Dialog
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();


                // Set the Question & Options from json
                try {

                    aajKaSawaalStartTime = Util.GetCurrentDateTime(false);
                    // Load Json in Array
                    JSONArray subJsonArray = loadQueJSONFromAsset();

                    // Generate Random Subject No
                    Random rS = new Random();
                    int sLow = 0;
                    int sHigh = subJsonArray.length();
                    int randomSubject = rS.nextInt(sHigh - sLow) + sLow;

                    JSONObject sObj = subJsonArray.getJSONObject(randomSubject);
                    JSONArray queJsonArray = sObj.getJSONArray("nodelist");

                    // Generate Random Que No
                    Random rQ = new Random();
                    int Low = 0;
                    int High = queJsonArray.length();
                    int randomQuestion = rQ.nextInt(High - Low) + Low;

                    // Get Random Question Details
                    JSONObject qObj = queJsonArray.getJSONObject(randomQuestion);
                    QueId = qObj.getString("QueId");
                    Question = qObj.getString("Question");
                    QuestionType = qObj.getString("QuestionType");
                    Option1 = qObj.getString("Option1");
                    Option2 = qObj.getString("Option2");
                    Option3 = qObj.getString("Option3");
                    Option4 = qObj.getString("Option4");
                    Answer = qObj.getString("Answer");
                    resourceId = qObj.getString("resourceId");
                    resourceType = qObj.getString("resourceType");
                    resourcePath = qObj.getString("resourcePath");

                    // Set Question
                    tv_Que.setText(Question);
                    tv_opt1.setText(Option1);
                    tv_opt2.setText(Option2);
                    tv_opt3.setText(Option3);
                    tv_opt4.setText(Option4);


                } catch (Exception e) {
                    e.getMessage();
                }

                // new changes
                btn_videoHint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Play Video
                        String resSrc = splashScreenVideo.fpath + "Media/" + resourcePath;
                        File file = new File(resSrc);
                        Uri path = Uri.fromFile(file);
                        if (file.exists()) {
                            // Video Watched
                            videoHintWatched = 1;
                            // Video view
                            final Dialog dialog = new Dialog(MainActivity.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.videoview_hint);
                            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.show();

                            videoView = dialog.findViewById(R.id.videoView);

                            videoView.setOnPreparedListener(MainActivity.this);
                            videoView.setOnCompletionListener(MainActivity.this);

                            Runtime rs = Runtime.getRuntime();
                            rs.freeMemory();
                            rs.gc();
                            rs.freeMemory();

                            //Creating MediaController
                            videoView.setVideoURI(path);
                            videoView.requestFocus();

                        } else {
                            Toast.makeText(mContext, "Video not available !!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                tv_opt1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        btn_Submit.setEnabled(true);
                        btn_Submit.setClickable(true);

                        tv_opt1.setBackgroundResource(R.drawable.ans_box_left_selected);
                        tv_opt2.setBackgroundResource(R.drawable.ans_box_right);
                        tv_opt3.setBackgroundResource(R.drawable.ans_box_left);
                        tv_opt4.setBackgroundResource(R.drawable.ans_box_right);

                        selectedOption = tv_opt1.getText().toString();
                        selectedBtn = R.id.opt1;

                    }
                });
                tv_opt2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        btn_Submit.setEnabled(true);
                        btn_Submit.setClickable(true);

                        tv_opt1.setBackgroundResource(R.drawable.ans_box_left);
                        tv_opt2.setBackgroundResource(R.drawable.ans_box_right_selected);
                        tv_opt3.setBackgroundResource(R.drawable.ans_box_left);
                        tv_opt4.setBackgroundResource(R.drawable.ans_box_right);

                        selectedOption = tv_opt2.getText().toString();
                        selectedBtn = R.id.opt2;

                    }
                });
                tv_opt3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        btn_Submit.setEnabled(true);
                        btn_Submit.setClickable(true);

                        tv_opt1.setBackgroundResource(R.drawable.ans_box_left);
                        tv_opt2.setBackgroundResource(R.drawable.ans_box_right);
                        tv_opt3.setBackgroundResource(R.drawable.ans_box_left_selected);
                        tv_opt4.setBackgroundResource(R.drawable.ans_box_right);

                        selectedOption = tv_opt3.getText().toString();
                        selectedBtn = R.id.opt3;

                    }
                });
                tv_opt4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        btn_Submit.setEnabled(true);
                        btn_Submit.setClickable(true);

                        tv_opt1.setBackgroundResource(R.drawable.ans_box_left);
                        tv_opt2.setBackgroundResource(R.drawable.ans_box_right);
                        tv_opt3.setBackgroundResource(R.drawable.ans_box_left);
                        tv_opt4.setBackgroundResource(R.drawable.ans_box_right_selected);

                        selectedOption = tv_opt4.getText().toString();
                        selectedBtn = R.id.opt4;

                    }
                });


                // Skip Button Action
                btn_Skip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                        // Disable buttons after selection
                        btn_Submit.setEnabled(false);
                        btn_Skip.setEnabled(false);
                        tv_opt1.setEnabled(false);
                        tv_opt2.setEnabled(false);
                        tv_opt3.setEnabled(false);
                        tv_opt4.setEnabled(false);

                        btn_Submit.setClickable(false);
                        btn_Skip.setClickable(false);
                        tv_opt1.setClickable(false);
                        tv_opt2.setClickable(false);
                        tv_opt3.setClickable(false);
                        tv_opt4.setClickable(false);

                        // new changes
                        // video watched or not then skip
                        ScoreDBHelper score = new ScoreDBHelper(MainActivity.this);
                        Score sc = new Score();

                        boolean answer = false;
                        sc.SessionID = MultiPhotoSelectActivity.sessionId;
                        sc.ResourceID = resourceId;
                        sc.QuestionId = Integer.parseInt(QueId);
                        sc.TotalMarks = 0;
                        // new changes
                        sc.Level = Integer.valueOf(String.valueOf(99).concat(String.valueOf(videoHintWatched)));
                        sc.StartTime = aajKaSawaalStartTime;
                        sc.EndTime = Util.GetCurrentDateTime(false);
                        String gid;
                        gid = selectedGroupId;
                        if (gid.contains(",")) gid = gid.split(",")[0];
                        sc.GroupID = gid;
                        String deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
                        sc.DeviceID = deviceId.equals(null) ? "0000" : deviceId;
                        sc.ScoredMarks = 0;

                        score.Add(sc);
                        BackupDatabase.backup(MainActivity.this);

                        // Show Graph Dialog
                        Dialog aksGraphDialog = new Dialog(MainActivity.this);
                        aksGraphDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        aksGraphDialog.setContentView(R.layout.aks_graph_dialog);
                        aksGraphDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                        // Memory Allocation
                        TextView tv_name = aksGraphDialog.findViewById(R.id.tv_name);
                        TextView tv_todayScore = aksGraphDialog.findViewById(R.id.tv_todayscore);
                        TextView tv_totalScore = aksGraphDialog.findViewById(R.id.tv_totalscore);

                        // todo set AKS Scoreboard
                        ScoreDBHelper scoreDBHelper = new ScoreDBHelper(MainActivity.this);
                        GroupDBHelper grpDBHelper = new GroupDBHelper(MainActivity.this);
                        String grpName = grpDBHelper.getGroupById(gid);
                        if (grpName == null || grpName.isEmpty() || grpName.equals("ExceptionOccured"))
                            grpName = "QR";
                        List<Score> tScoreList = scoreDBHelper.GetTotalAkaScoreForGrpID(gid);
                        String scored, totalScored = "";
                        String total, totalMarks = "";
                        for (int i = 0; i < tScoreList.size(); i++) {
                            scored = String.valueOf(tScoreList.get(i).ScoredMarks);
                            totalScored = totalScored + scored;
                            total = String.valueOf(tScoreList.get(i).TotalMarks);
                            totalMarks = totalMarks + total;
                        }

                        tv_name.setText("" + grpName);
                        tv_todayScore.setText("" + sc.ScoredMarks);
                        tv_totalScore.setText("" + totalScored + "/" + totalMarks + "");

                        aksGraphDialog.setCanceledOnTouchOutside(false);
                        aksGraphDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        aksGraphDialog.show();

                        // Open Graph Activity
//                        Intent graph = new Intent(MainActivity.this, AKSGraph.class);
//                        startActivity(graph);

                    }
                });

                // Submit Button Action
                btn_Submit.setOnClickListener(new View.OnClickListener()

                {
                    @Override
                    public void onClick(View v) {

                        // Flag Played if Submit
                        StatusDBHelper sdbh = new StatusDBHelper(MainActivity.this);
                        sdbh.UpdateAlarm("aajKaSawalPlayed", "1");
                        BackupDatabase.backup(MainActivity.this);

                        // Score Table Entry
                        boolean enterScore = false;
                        ScoreDBHelper score = new ScoreDBHelper(MainActivity.this);
                        final Score sc = new Score();

                        // Disable buttons after selection
                        btn_Submit.setEnabled(false);
                        btn_Skip.setEnabled(false);
                        tv_opt1.setEnabled(false);
                        tv_opt2.setEnabled(false);
                        tv_opt3.setEnabled(false);
                        tv_opt4.setEnabled(false);

                        btn_Submit.setClickable(false);
                        btn_Skip.setClickable(false);
                        tv_opt1.setClickable(false);
                        tv_opt2.setClickable(false);
                        tv_opt3.setClickable(false);
                        tv_opt4.setClickable(false);

                        boolean answer = false;
                        sc.SessionID = MultiPhotoSelectActivity.sessionId;
                        sc.ResourceID = resourceId;
                        sc.QuestionId = Integer.parseInt(QueId);
                        sc.TotalMarks = 10;
                        // new changes
                        sc.Level = Integer.valueOf(String.valueOf(99).concat(String.valueOf(videoHintWatched)));
                        sc.StartTime = aajKaSawaalStartTime;
                        sc.EndTime = Util.GetCurrentDateTime(false);

                        //selectedGroupId
                        gid = selectedGroupId;
                        if (gid.contains(","))
                            gid = gid.split(",")[0];
                        sc.GroupID = gid;
                        String deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
                        sc.DeviceID = deviceId.equals(null) ? "0000" : deviceId;


                        // get selected textview
                        if (selectedOption.equals(Answer)) {
                            // Correct Animation
                            Button selBut = dialog.findViewById(selectedBtn);
                            selBut.setBackgroundResource(R.drawable.ans_box_correct);

                            // CORRECT ANSWER
                            resultDialog.setContentView(getLayoutInflater().inflate(R.layout.sample_aajkasawaal, null));

                            LinearLayout mainScreen = resultDialog.findViewById(R.id.sampleAajkaSawaal);
                            LinearLayout correctScreen = resultDialog.findViewById(R.id.aajkaSawaal_correct);
                            LinearLayout wrongScreen = resultDialog.findViewById(R.id.aajkaSawaal_wrong);

                            mainScreen.setVisibility(View.GONE);
                            correctScreen.setVisibility(View.VISIBLE);
                            wrongScreen.setVisibility(View.GONE);

                            resultDialog.show();

                            correct.start();
                            sc.ScoredMarks = 10;

                        } else if (answer == false) {

                            // setting background red if answer is wrong
                            Button selBut = dialog.findViewById(selectedBtn);
                            selBut.setBackgroundResource(R.drawable.ans_box_wrong);

                            // Setting Correct Answer background
                            if (tv_opt1.getText().toString().equals(Answer))
                                tv_opt1.setBackgroundResource(R.drawable.ans_box_correct);
                            else if (tv_opt2.getText().toString().equals(Answer))
                                tv_opt2.setBackgroundResource(R.drawable.ans_box_correct);
                            else if (tv_opt3.getText().toString().equals(Answer))
                                tv_opt3.setBackgroundResource(R.drawable.ans_box_correct);
                            else if (tv_opt4.getText().toString().equals(Answer))
                                tv_opt4.setBackgroundResource(R.drawable.ans_box_correct);


                            // if WRONG ANS
                            resultDialog.setContentView(getLayoutInflater().inflate(R.layout.sample_aajkasawaal, null));

                            LinearLayout mainScreen = resultDialog.findViewById(R.id.sampleAajkaSawaal);
                            LinearLayout correctScreen = resultDialog.findViewById(R.id.aajkaSawaal_correct);
                            LinearLayout wrongScreen = resultDialog.findViewById(R.id.aajkaSawaal_wrong);

                            TextView tvWrong = resultDialog.findViewById(R.id.tv_wrong_ans);
                            tvWrong.setText("Correct Answer is " + Answer + " !!!");

                            mainScreen.setVisibility(View.GONE);
                            correctScreen.setVisibility(View.GONE);
                            wrongScreen.setVisibility(View.VISIBLE);

                            resultDialog.show();

                            wrong.start();

                            sc.ScoredMarks = 0;

                        }

                        // Score Entry
                        enterScore = score.Add(sc);
                        BackupDatabase.backup(MainActivity.this);

                        try {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 2500ms
                                    if (correct.isPlaying()) {
                                        correct.stop();
                                        correct.reset();
                                        correct.release();
                                    } else if (wrong.isPlaying()) {
                                        wrong.stop();
                                        wrong.reset();
                                        wrong.release();
                                    }
                                    if (resultDialog.isShowing())
                                        resultDialog.dismiss();
                                    if (dialog.isShowing())
                                        dialog.dismiss();

                                    // Show Graph Dialog
                                    Dialog aksGraphDialog = new Dialog(MainActivity.this);
                                    aksGraphDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    aksGraphDialog.setContentView(R.layout.aks_graph_dialog);
                                    aksGraphDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                                    // Memory Allocation
                                    TextView tv_name = aksGraphDialog.findViewById(R.id.tv_name);
                                    TextView tv_todayScore = aksGraphDialog.findViewById(R.id.tv_todayscore);
                                    TextView tv_totalScore = aksGraphDialog.findViewById(R.id.tv_totalscore);

                                    // todo set AKS Scoreboard
                                    ScoreDBHelper scoreDBHelper = new ScoreDBHelper(MainActivity.this);
                                    GroupDBHelper grpDBHelper = new GroupDBHelper(MainActivity.this);
                                    String grpName = grpDBHelper.getGroupById(gid);
                                    if (grpName == null || grpName.isEmpty() || grpName.equals("ExceptionOccured"))
                                        grpName = "QR";
                                    List<Score> tScoreList = scoreDBHelper.GetTotalAkaScoreForGrpID(gid);
                                    String scored, totalScored = "";
                                    String total, totalMarks = "";
                                    for (int i = 0; i < tScoreList.size(); i++) {
                                        scored = String.valueOf(tScoreList.get(i).ScoredMarks);
                                        totalScored = totalScored + scored;
                                        total = String.valueOf(tScoreList.get(i).TotalMarks);
                                        totalMarks = totalMarks + total;
                                    }

                                    tv_name.setText("" + grpName);
                                    tv_todayScore.setText("" + sc.ScoredMarks);
                                    tv_totalScore.setText("" + totalScored + "/" + totalMarks + "");

                                    aksGraphDialog.setCanceledOnTouchOutside(false);
                                    aksGraphDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    aksGraphDialog.show();


                                    // Show Graph Activity
//                                    Intent graph = new Intent(MainActivity.this, AKSGraph.class);
//                                    startActivity(graph);


                                }
                            }, 5000);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }


                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // if Questions.json not present
        else if (aajKaSawalPlayed != null && aajKaSawalPlayed.equals("3")) {
            //Toast.makeText(MainActivity.this, "Aaj Ka Sawaal Json not Found !!!", Toast.LENGTH_SHORT).show();
        }
        // aaj Ka Sawaal is null
        else {
            //Toast.makeText(MainActivity.this, "Aaj Ka Sawaal Json not Found !!!", Toast.LENGTH_SHORT).show();
        }

        mContext = this;
        mainFlag = true;
        ttspeech = new TextToSp(mContext);
        sessionContex = this;
        utility = new Utility();
        startTime = "";
        playVideo = new PlayVideo();
        // Hide Actionbar
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        *///getSdCardPath();

        initCollapsingToolbar();

        newNodeList = getIntent().getStringExtra("nodeList");
        myNodeTitle = getIntent().getStringExtra("crums");

        if (newNodeList != null) {
            mainFlag = false;
        }

        recyclerView = (RecyclerView) findViewById(R.id.verticalGridLayout);


        GridLayoutManager gridlayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(gridlayoutManager);

        cardList = new ArrayList<>();
        adapter = new CardAdapter(this, cardList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        bread_crums = (TextView) findViewById(R.id.bread_crums);
        String NavTitle = bread_crums.getText().toString();

        if ((myNodeTitle == null) || (NavTitle == null))
            bread_crums.setText("");
        else
            bread_crums.setText(NavTitle + " > " + myNodeTitle);


        ReadMyFile();

        try {
            Glide.with(this).load(R.drawable.topcover).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Reading CRL Json From Internal Memory
    public JSONArray loadQueJSONFromAsset() {
        String queJsonStr = null;
        JSONArray aksNavigate = null;
        try {
            File queJsonSDCard = new File(splashScreenVideo.fpath + "AajKaSawaal/", "Questions.json");
            FileInputStream stream = new FileInputStream(queJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                queJsonStr = Charset.defaultCharset().decode(bb).toString();

                JSONObject aksObj = new JSONObject(queJsonStr);
                aksNavigate = aksObj.getJSONArray("nodelist");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return aksNavigate;

    }


    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }


    public void ReadMyFile() {
        try {
            if (newNodeList == null) {

//            File myJsonFile= new File(MainActivity.fpath+"Json/funNavigate.json");
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
//            JSONArray contentNavigate  = jsonObj.getJSONArray("funNavigate");
                contentNavigate = jsonObj.getJSONArray("nodelist");
//            JSONObject contentNavigate2 = jsonObj.getJSONObject("nodelist");

            } else {

                JSONArray jsnarray = new JSONArray(newNodeList);
                contentNavigate = jsnarray;
                // contentNavigate = findElementsChildren(contentNavigate.getJSONObject(""),newNodeId);
            }

            // looping through All nodes
            for (int i = 0; i < contentNavigate.length(); i++) {

                String notetitle;
                JSONObject c = contentNavigate.getJSONObject(i);

                Card card = new Card();
                card.nodeId = c.optString("nodeId");
                card.nodeType = c.optString("nodeType");
                String myNodeType = c.optString("nodeType").toString();
                card.nodeTitle = c.optString("nodeTitle").toString();
                notetitle = c.optString("nodeTitle").toString();
                card.nodeImage = splashScreenVideo.fpath + "Media/" + c.optString("nodeImage").toString();
                card.nodePhase = c.optString("nodePhase").toString();
                String phase = c.optString("nodePhase").toString();
                card.nodeAge = c.optString("nodeAge").toString();
                card.nodeDesc = c.optString("nodeDesc").toString();
                card.nodeKeywords = c.optString("nodeKeywords").toString();
                card.sameCode = c.optString("sameCode").toString();
                card.resourceId = c.optString("resourceId").toString();
                card.resourceType = c.optString("resourceType").toString();
                card.resourcePath = c.optString("resourcePath").toString();
                card.nodeList = c.optString("nodelist").toString();
                String tempNodeList = c.optString("nodelist").toString();

                String resourcePath = c.optString("resourcePath").toString();
                String resId = c.optString("resourceId").toString();
                String resourceType = c.optString("resourceType").toString();
                String assNodeDesc = c.optString("nodeDesc").toString();

                String numberOnly = phase.replaceAll("[^0-9]", "");

                cardList.add(card);

                if (myNodeType.equals("Resource") && assessmentLogin.assessmentFlg) {
                    loadFlg = true;
                    lessonSession = utility.GetUniqueID().toString();
                }

            }
            if (cardList.get(0).nodePhase.contains("phase")) {
                if (cardList.size() > 0) {
                    Collections.sort(cardList, new Comparator<Card>() {
                        @Override
                        public int compare(final Card object1, final Card object2) {
                            return object1.nodePhase.compareTo(object2.nodePhase);
                        }
                    });
                }
                Collections.reverse(cardList);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (!videoView.isPlaying())
            videoView.start();
    }

    /**
     * Adding few albums for testing
     * RecyclerView item decoration - give equal margin around grid item
     */
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

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (CardAdapter.pdfFlag) {
                CardAdapter.pdfFlag = false;
                PDFTracking.calculateEndTime();
                BackupDatabase.backup(getApplicationContext());
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mainFlag) {
            super.onBackPressed();
            // close app from main screen
            sessionFlg = true;
            scoreDBHelper = new ScoreDBHelper(sessionContex);
            playVideo.calculateEndTime(scoreDBHelper);
            BackupDatabase.backup(sessionContex);
            try {
                System.exit(0);
                finishAffinity();
            } catch (Exception e) {
                e.getMessage();
            }

        } else if (loadFlg) {
            loadFlg = false;
            Intent intent = new Intent(mContext, AssessmentResult.class);
            startActivity(intent);
            finish();
        } else {
            Runtime rs = Runtime.getRuntime();
            rs.freeMemory();
            rs.gc();
            rs.freeMemory();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        destroyed = false;
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        destroyed = false;

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
                sessionFlg = true;

                if (!CardAdapter.vidFlg) {
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        finishAffinity();
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
            }
        }.start();
    }


    @Override
    protected void onDestroy() {
        sessionFlg = true;
        destroyed = true;
        super.onDestroy();
    }
}