package com.example.pef.prathamopenschool;

import android.annotation.TargetApi;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.example.pef.prathamopenschool.TextToSp.textToSpeech;

/**
 * Created by PEF-2 on 23/06/2017.
 */

// --- TEXT TO SPEECH ---

public class ttsInitListener implements TextToSpeech.OnInitListener  {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(new Locale("hi","IN"));
            textToSpeech.setSpeechRate((float)0.4);
            textToSpeech.setPitch((float) 1);

        } else {
            textToSpeech = null;
            Toast.makeText(TextToSp.c, "Failed to initialize TTS engine.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
