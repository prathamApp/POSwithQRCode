package com.example.pef.prathamopenschool;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.pef.prathamopenschool.interfaces.WebViewInterface;

import java.util.ArrayList;

public class WebViewActivity extends AppCompatActivity implements WebViewInterface {

    WebView webView;
    private Context mContext;
    boolean Resumed = false;
    static String webResId, resName;
    static int arraySize = 0, currentResCnt;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;
    String gamePath;
    static ArrayList<String> resWebIdArray = new ArrayList<String>();
    static ArrayList<String> resWebPathArray = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = (WebView) findViewById(R.id.loadPage);


        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        gamePath = getIntent().getStringExtra("path");
        webResId = getIntent().getStringExtra("resId");
        resName = getIntent().getStringExtra("resName");

        createWebView(Uri.parse(gamePath));

    }


    public void createWebView(Uri GamePath) {

        String myPath = GamePath.toString();
/*
        Toast toast = Toast.makeText(WebViewActivity.this, "IN WEBVIEW: "+myPath, Toast.LENGTH_SHORT);
        toast.show();
*/
        System.out.println(":::::::::::::::::::::::::::::::::: gamePath " + myPath + " ::::::::::::::::: webResId " + webResId);

        webView.loadUrl(myPath);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new JSInterface(this, webView, WebViewActivity.this), "Android");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                webView.setWebContentsDebuggingEnabled(true);
            }
        }
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.clearCache(true);

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

    }


    @Override
    public void onBackPressed() {
        if (!assessmentLogin.assessmentFlg || JSInterface.nextGameFlg) {

            // Get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(Looper.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!AssessmentCrlDashBoardView.dashboardFlag)
                        JSInterface.stopTtsBackground();
                    AssessmentCrlDashBoardView.dashboardFlag = false;
                    webView.loadUrl("about:blank");

                    webView.clearCache(true);
                    Runtime rs = Runtime.getRuntime();
                    rs.freeMemory();
                    rs.gc();
                    rs.freeMemory();
                    finish();

                } // This is your code
            };
            mainHandler.post(myRunnable);

            super.onBackPressed();
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
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    finishAffinity();

                }
            }
        }.start();

    }

    @Override
    public void onNextGame(final WebView w) {
        currentResCnt += 1;
        if (currentResCnt < arraySize) {
            JSInterface.nextGameFlg = false;
            System.out.println(" currentResCnt :::::::::::::::::::::::::::::::::: " + currentResCnt);
            gamePath = resWebPathArray.get(currentResCnt);
            webResId = resWebIdArray.get(currentResCnt);
            System.out.println("gamePath :::::: " + gamePath + " :::::: " + webResId);
            w.post(new Runnable() {
                @Override
                public void run() {
                    w.loadUrl(gamePath);
                }
            });
        } else {
            JSInterface.nextGameFlg = true;
            onBackPressed();
        }
    }
}

