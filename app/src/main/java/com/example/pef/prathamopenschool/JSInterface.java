package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.RadioGroup;

import com.example.pef.prathamopenschool.interfaces.WebViewInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class JSInterface extends Activity {
    static Context mContext;
    String path;
    DBHelper db;
    Utility Util;
    Audio recordAudio;
    public static int groupId = 0;
    public int UserId = 1;
    MediaRecorder myAudioRecorder;
    public static MediaPlayer mp, mp2 = null;
    public static videoTracking videoTracking;
    WebView webView;
    WebViewActivity webViewActivity;
    WebViewInterface webViewInterface;
    RadioGroup radioGroup;
    public static int flag = 0, VideoFlag = 0;
    static Boolean MediaFlag = false, pdfFlag = false, audioFlag = false, trailerFlag = false, completeFlag = false, nextGameFlg = false;
    static TextToSp tts;
    AttendanceDBHelper attendanceDBHelper;


    JSInterface(Context c, WebView w, WebViewInterface webViewInterface) {
        mContext = c;

        this.webViewInterface = webViewInterface;
        /*tts = new TextToSp(mContext);
        tts.ttsFunction("Welcome kids");*/
        tts = MainActivity.ttspeech;
        db = new DBHelper(mContext);
        Util = new Utility();
        createRecordingFolder();
        webViewActivity = new WebViewActivity();
        attendanceDBHelper = new AttendanceDBHelper(mContext);


/*
        this.presentStudents = presentStudents;
        this.SessionId = SessionId;
*/
        mp = new MediaPlayer();
        this.webView = w;
        VideoFlag = 0;
    }

    private void createRecordingFolder() {
        if (!new File(Environment.getExternalStorageDirectory() + "/.POSinternal/recordings").exists()) {
            new File(Environment.getExternalStorageDirectory() + "/.POSinternal/recordings").mkdir();
        }
    }

    @JavascriptInterface
    public void toggleVolume(String volume) {
        if (volume.equals("false"))
            mp.setVolume(0, 0);
        else {
            mp.setVolume(1, 1);
        }
    }

    @JavascriptInterface
    public String getLevel() {
        return CardAdapter.nodeDesc;
    }


    //Ketan
    @JavascriptInterface
    public String getMediaPath(String gameFolder) {
        String path;
        path = splashScreenVideo.fpath + "Media/" + gameFolder + "/";
        return path;
    }

    // Ketan
    @JavascriptInterface
    public void startRecording(String recName) {
        try {
            recordAudio = new Audio(recName);
            recordAudio.start();
        } catch (Exception e) {
/*
            log.error("Exception occurred at : "+e.getMessage());
            showAlert.showDialogue(mContext,"Problem occurred in audio recorder. Please contact your administrator.");
*/
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("startRecording-JSInterface", e, "Error");
            BackupDatabase.backup(mContext);
        }
    }

    @JavascriptInterface
    public void stopRecording() {
        audioFlag = false;
        try {
            recordAudio.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @JavascriptInterface
    public void getPath(String gameFolder) {
        path = splashScreenVideo.fpath + gameFolder + "/";
        webView.post(new Runnable() {
            public void run() {
                String jsString = "javascript:Utils.setPath('" + path + "')";
                webView.loadUrl(jsString);
            }
        });
    }


    /*@JavascriptInterface
    public void sendBackTojavascript() {
        w.post(new Runnable() {
            public void run() {
                String str1 = MainActivity.jsonstrOfNewVideos;
                String jsString = "javascript:loadNewJson('" + str1 + "')";
                w.loadUrl(jsString);
            }
        });

    }*/

    @JavascriptInterface
    public void exitApplication() {

        /*if(FirstActivity.KEY=="Guest"){
            Intent intent=new Intent(mContext,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // this.finish();
            mContext.startActivity(intent);
        }
        else{*/
        Intent intent = new Intent(mContext, StartingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // this.finish();
        mContext.startActivity(intent);
        //}

    }

    @JavascriptInterface
    public void showPdf(String filename, String resId) {
        try {
            File file = new File(splashScreenVideo.fpath + filename);

            if (file.exists()) {
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                try {
                    pdfFlag = true;
                    videoTracking = new videoTracking(mContext, MultiPhotoSelectActivity.presentStudents, MultiPhotoSelectActivity.sessionId, resId);
//                    videoTracking.calculateStartTime();
                    ((Activity) mContext).startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                    syncActivityLogs.addToDB("showPdf-JSInterface", e, "Error");
                }
            } else {
            }
        } catch (Exception e) {
        }
    }


    // Ketan's Code
    @JavascriptInterface
    public void audioPlayerForStory(String filename, String storyName) {
        try {
            mp.stop();
            mp.reset();
            if (tts.textToSpeech.isSpeaking()) {
                tts.stopSpeakerDuringJS();
            }
            String path = "";
            audioFlag = true;
            flag = 0;
            String mp3File;

            //  path="/storage/sdcard1/.PrathamHindi/salana baal-katai divas/";
            try {
                if (storyName != null) {
                    mp3File = "storyGame/Raw/" + storyName + "/" + filename;
                } else {
                    mp3File = "/storage/sdcard0/.POSinternal/recordings" + filename;
                }
                if (filename.charAt(0) == '/') {
                    path = "/storage/sdcard0/.POSinternal/recordings" + filename;//check for recording game and then change
                    mp.setDataSource(path);
                } else {
                    mp.setDataSource(path);
                }
                if (mp.isPlaying())
                    mp.stop();

                mp.prepare();
                mp.start();

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        audioFlag = false;
                        try {
                            webView.post(new Runnable() {
                                @Override
                                public void run() {
                                    webView.loadUrl("javascript:temp()");
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            /*            log.error("Exception occurred at : " + e.getMessage());*/
        }
    }


    // Ketan's Code
    @JavascriptInterface
    public void audioPlayer(String filename) {
        try {
            String path;
            if (filename.charAt(0) == '/') {
                path = filename;//check for recording game and then change
            } else {
                //path="/storage/sdcard1/.prathamMarathi/"+filename;
                path = splashScreenVideo.fpath + "Media" + filename;
            }
            mp = new MediaPlayer();

            try {
                mp.setDataSource(path);
                mp.prepare();
                mp.start();

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                    }
                });
            } catch (Exception e) {
/*
                log.error("Exception occurred at : " + e.getMessage());
                showAlert.showDialogue(mContext, "Problem occurred in audio player. Please contact your administrator.");
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                syncActivityLogs.addToDB("audioPlayer-JSInterface", e, "Error");
                BackupDatabase.backup(mContext);
*/
                e.printStackTrace();
            }
        } catch (Exception e) {
/*
            log.error("Exception occurred at : " + e.getMessage());
*/
        }
    }

    @JavascriptInterface
    public void audioPause() {
        if (MediaFlag == true) {
            mp.pause();
            MediaFlag = false;
        }
    }

    @JavascriptInterface
    public void audioResume() {
        if (MediaFlag == false) {
            mp.start();
            MediaFlag = true;
        }
        try {
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    mp.reset();
                    MediaFlag = false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @JavascriptInterface
    public void stopAudioPlayer() {
        try {
            if (mp != null) {
                mp.stop();
                mp.reset();
            }
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("stopAudioPlayer-JSInterface", e, "Error");
            BackupDatabase.backup(mContext);
        }
    }

    @JavascriptInterface
    public String fetchUsedResources() {
        String playedContent;

        ScoreDBHelper scoreDBHelper = new ScoreDBHelper(mContext);

        int countScore = scoreDBHelper.GetPlayedResourcesCount();

        playedContent = scoreDBHelper.GetPlayedResources();

        return playedContent;
    }

    @JavascriptInterface
    public String getLanguage() {
        String playedContent;
        playedContent = MultiPhotoSelectActivity.getLanguage(mContext);
        return playedContent;
    }

    @JavascriptInterface
    public String getConfig() {
        String playedContent;
        JSONArray contentNavigate = null;
        try {
            File myJsonFile = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Config.json");
            FileInputStream stream = null;
            stream = new FileInputStream(myJsonFile);
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
            contentNavigate = jsonObj.getJSONArray("nodelist");
            System.out.println("---------------------------------------------RETURNING CONFIG");
            return contentNavigate.toString();

        } catch (Exception e) {
            System.out.println("---------------------------------------------RETURNING CONFIG");
            e.printStackTrace();
            return contentNavigate.toString();
        }
    }


    @JavascriptInterface
    public void showVideo(String filename, String resId) {

        try {
            File file = new File(splashScreenVideo.fpath + filename);

            if (file.exists()) {
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(mContext, VideoPlay.class);
                intent.putExtra("path", path.toString());
                videoTracking = new videoTracking(mContext, MultiPhotoSelectActivity.presentStudents, MultiPhotoSelectActivity.sessionId, CardAdapter.resId);
                //videoTracking.calculateStartTime();
                MediaFlag = true;
                ((Activity) mContext).startActivityForResult(intent, 1);
            } else {
            }
        } catch (Exception e) {

        }

    }


    public static void returnFunction() {
        try {
            pdfFlag = false;
            MediaFlag = false;
            videoTracking.calculateEndTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @JavascriptInterface
    public void addScore(String resId, int questionId, int scorefromGame, int totalMarks, int level, String startTime) {
        boolean _wasSuccessful = false;
        resId = "";
        String[] splited;
        String[] splitedDate;
        String[] splitedTime;
        String customDate;
        String customTime;
        //put try catch block for error handling
        try {

            if (resId.equals(null) || startTime.equals("undefined")) {
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                if (resId.equals(null))
                    syncActivityLogs.addLog("addScore-JSInterface", "Error", "resource id is null");
                else if (startTime.equals("undefined"))
                    syncActivityLogs.addLog("addScore-JSInterface", "Error", "startTime is undefined");
                BackupDatabase.backup(mContext);
            } else {

                StatusDBHelper statusDBHelper = new StatusDBHelper(mContext);
                ScoreDBHelper scoreDBHelper = new ScoreDBHelper(mContext);

                AssessmentScoreDBHelper assessmentDBHelper = new AssessmentScoreDBHelper(mContext);
                AssessmentScore assessment = new AssessmentScore();

                Score score = new Score();

                score.SessionID = MultiPhotoSelectActivity.sessionId;

                if (assessmentLogin.assessmentFlg)
                    score.ResourceID = WebViewActivity.webResId;
                else
                    score.ResourceID = CardAdapter.resId;
                score.QuestionId = questionId;
                score.ScoredMarks = scorefromGame;
                score.TotalMarks = totalMarks;

                splited = startTime.split("\\s+");
                splitedDate = splited[0].split("\\-+");
                splitedTime = splited[1].split("\\:+");
                customDate = formatCustomDate(splitedDate, "-");
                customTime = formatCustomDate(splitedTime, ":");

//                score.StartTime = customDate + " " + customTime;

                String systime = Util.GetCurrentDateTime(true);  //here we get sys time

                String[] gps = Util.GetCurrentDateTime(false).split(" ");

                SimpleDateFormat sdfForTime = new SimpleDateFormat("HH:mm:ss");
                long diff = (sdfForTime.parse(systime).getTime() - sdfForTime.parse(customTime).getTime());/*
                Log.d("score_time::", gps[0] + "::::" + gps[1] + "::::" + customDate + "::::" + customTime + "::::" + systime);
                Log.d("start_time::", ""+diff);*/
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdfForTime.parse(gps[1]));
                long diffSeconds = diff / 1000 % 60;
                cal.add(Calendar.SECOND, (int) -diffSeconds);
                score.StartTime=  gps[0]+" "
                        +((cal.get(Calendar.HOUR_OF_DAY) < 10) ? "0" : "")+ cal.get(Calendar.HOUR_OF_DAY) +":"
                        +((cal.get(Calendar.MINUTE) < 10) ? "0" : "")+ cal.get(Calendar.MINUTE)+":"
                        +((cal.get(Calendar.SECOND) < 10) ? "0" : "")+ cal.get(Calendar.SECOND);

//                Log.d("format_time::", ""+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));

                String gid = MultiPhotoSelectActivity.selectedGroupsScore;
                if (gid.contains(","))
                    gid = gid.split(",")[0];
                score.GroupID = gid;//ketan 17/6/17
//                if (deviceId.equals("") || deviceId.contains("111111111111111")) {
                String deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
//                }
                score.DeviceID = deviceId.equals(null) ? "0000" : deviceId;
                score.EndTime = Util.GetCurrentDateTime(false);  //here we get gps time
                //calculation
                score.Level = level;
                _wasSuccessful = scoreDBHelper.Add(score);

                if (assessmentLogin.assessmentFlg) {

                    assessment.SessionID = MultiPhotoSelectActivity.sessionId;
                    assessment.ResourceID = WebViewActivity.webResId;
                    assessment.QuestionId = 0;
                    assessment.ScoredMarks = scorefromGame;
                    assessment.TotalMarks = totalMarks;

                    splited = startTime.split("\\s+");
                    splitedDate = splited[0].split("\\-+");
                    splitedTime = splited[1].split("\\:+");
                    customDate = formatCustomDate(splitedDate, "-");
                    customTime = formatCustomDate(splitedTime, ":");
//                    assessment.StartTime = customDate + " " + customTime;
                    assessment.StartTime = score.StartTime;

                    String studId = attendanceDBHelper.GetStudentId(MultiPhotoSelectActivity.sessionId);

                    assessment.GroupID = studId;//ketan 17/6/17
                    assessment.DeviceID = WebViewActivity.resName;
                    assessment.EndTime = Util.GetCurrentDateTime(false);
                    assessment.Level = level;
                    assessment.LessonSession = MainActivity.lessonSession;
                    _wasSuccessful = assessmentDBHelper.Add(assessment);
                }
            }
            BackupDatabase.backup(mContext);

        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("addScore-JSInterface", e, "Error");
            BackupDatabase.backup(mContext);
        }

    }

    public String formatCustomDate(String[] splitedDate, String delimiter) {
        for (int k = 0; k < splitedDate.length; k++) {
            if (Integer.parseInt(splitedDate[k]) < 10) {
                splitedDate[k] = "0" + splitedDate[k];
            }
        }
        return TextUtils.join(delimiter, splitedDate);
    }

    @JavascriptInterface
    public void playTts(String theWordWasAndYouSaid, String ttsLanguage) {
        mp.stop();
        mp.reset();
        if (tts.textToSpeech.isSpeaking()) {
            tts.stopSpeakerDuringJS();
        }
        if (ttsLanguage == null) {
            tts.ttsFunction(theWordWasAndYouSaid, "eng");
        }
        if (ttsLanguage.equals("eng") || ttsLanguage.equals("hin")) {
            tts.ttsFunction(theWordWasAndYouSaid, ttsLanguage);
        }
    }

    @JavascriptInterface
    public void stopTts() {
        tts.stopSpeakerDuringJS();
    }

    @JavascriptInterface
    public void GotoNextGame() {
//        webViewActivity.onBackPressed();
//        webViewActivity.goBack();
        AssessmentCrlDashBoardView.dashboardFlag = false;
        webViewInterface.onNextGame(webView);

    }

    @JavascriptInterface
    public void playTts(final String toSpeak) {
        tts.ttsFunction(toSpeak, "hin");
    }

    @JavascriptInterface
    public static boolean informCompletion() {
        return completeFlag;
    }

    public static void stopTtsBackground() {
        tts.stopSpeakerDuringJS();
    }
}