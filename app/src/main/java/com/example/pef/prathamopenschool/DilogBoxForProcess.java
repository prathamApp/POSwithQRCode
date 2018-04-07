package com.example.pef.prathamopenschool;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by PEF-2 on 24/06/2017.
 */

public class DilogBoxForProcess {

    public ProgressDialog progressDialog;

    public void showDilog(Context context,String message){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissDilog(){
        progressDialog.dismiss();
    }
}
