package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

/**
 * Created by Ameya on 6/13/2017.
 */

class TextToSpeechTTS extends AppCompatActivity{
    TextToSpeech t1;
    Context c;

    public TextToSpeechTTS(Context mContext) {
        super();
        c=mContext;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void ttsFunction(final String toSpeak){
        try {
            t1=new TextToSpeech(c, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(new Locale("hin-IN"));
                        t1.setSpeechRate((float)0.5);
                        t1.setPitch((float) 1);
                    }
                }
            });
        }
        catch ( Exception e){
            e.printStackTrace();
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if(!t1.isSpeaking())
                            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                },
                100);
    }

    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }


}