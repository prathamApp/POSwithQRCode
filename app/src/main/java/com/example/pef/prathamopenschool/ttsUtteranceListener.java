package com.example.pef.prathamopenschool;

import android.speech.tts.UtteranceProgressListener;

/**
 * Created by PEF-2 on 23/06/2017.
 */

class ttsUtteranceListener extends UtteranceProgressListener {

    @Override
    public void onDone(String utteranceId) {
        JSInterface.completeFlag=true;
    }

    @Override
    public void onError(String utteranceId) {

    }

    @Override
    public void onStart(String utteranceId) {
        JSInterface.completeFlag=false;
    }
}