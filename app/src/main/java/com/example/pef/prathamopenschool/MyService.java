package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;


public class MyService extends Service {
    Context c;
    static Boolean timer = false;
    public static CountDownTimer cd;

    public MyService(Context c) {
        this.c = c;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public  void test(Long time) {
            cd = new CountDownTimer(time, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timer = true;
                }

                @Override
                public void onFinish() {
                    try{
                        if(CrlDashboard.transferFlag){

                        }
                        else{
                            if(JSInterface.pdfFlag)
                                ((Activity) JSInterface.mContext).finishActivity(1);
                            else{
                                Intent intent = new Intent(c, StartingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("Exit me", true);
                                startActivity(intent);
                            }
                        }
                    }
                    catch (Exception e){
                    }
                }
            }.start();
        }
}
