package com.example.pef.prathamopenschool.ftpSettings;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

public class CreateWifiAccessPointOnHigherAPI extends AsyncTask<Void, Void, Boolean> {

    private ProgressDialog pd;
    public Context context;

    public CreateWifiAccessPointOnHigherAPI(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage("Starting Server ... Please wait !!!");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName(
                "com.android.settings",
                "com.android.settings.TetherSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        // delay for creating hotspot
        try {
            // Snackbar instead of Toast
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (pd != null)
            pd.dismiss();

        // Start Server
        context.sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
        Toast.makeText(context, "Hotspot created", Toast.LENGTH_SHORT).show();
    }
}

