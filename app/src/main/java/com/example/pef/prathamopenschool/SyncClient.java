package com.example.pef.prathamopenschool;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class SyncClient extends AsyncTask<String, Void, Boolean> {

    private Exception exception;
    private Context mContext;
    public String state;
    public TextView v;
    Utility Util;
    ProgressDialog progressDialog;
    long res = 0;
    int x = 0;
    BulkInsertion bulkInsertion;
    List<GroupList> groupList;
    AutoCompleteTextView group1, group2;



    public SyncClient() {
    }

    public SyncClient(Context c, String state, TextView v) {
        this.mContext = c;
        this.state = state;
        this.v = v;
        Util = new Utility();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
      //  v.setText("");
        progressDialog = ProgressDialog.show(mContext, "PLEASE WAIT", "Data is being pulled from server.", true);
    }

    protected Boolean doInBackground(String... urls) {

        JSONObject _jsonObject = null;
        List<Device> _devices = new ArrayList<Device>();
        Boolean _wasSuccessful = false;
        try {
            JSONArray _villages = this.GetServerData(Utility.getProperty("pullVillagesURL", mContext) + state, "Village");
            // JSONArray _villages = this.GetServerData("http://prathamcms.org/api/village/get?state="+state,"Village");
            if (_villages != null && _villages.length() > 0) {
                if (this.ClearDatabaseEntriesDuringFetchData("Village", mContext)) {
                    _wasSuccessful = this.UpdateDatabaseWithFetchedData(_villages, "Village", mContext);
                } else _wasSuccessful = false;
            }

            JSONArray _groups = this.GetServerData(Utility.getProperty("pullGroupsURL", mContext) + state, "Group");
            //JSONArray _groups = this.GetServerData("http://prathamcms.org/api/group/get?state="+state,"Group");
            if (_groups != null && _groups.length() > 0) {
                if (this.ClearDatabaseEntriesDuringFetchData("Group", mContext)) {
                    _wasSuccessful = this.UpdateDatabaseWithFetchedData(_groups, "Group", mContext);
                } else _wasSuccessful = false;
            }

            JSONArray _students = this.GetServerData(Utility.getProperty("pullStudentsURL", mContext) + state, "Student");
            //JSONArray _students = this.GetServerData("http://prathamcms.org/api/student/get?state="+state,"Student");
            if (_students != null && _students.length() > 0) {
                if (this.ClearDatabaseEntriesDuringFetchData("Student", mContext)) {
                    _wasSuccessful = this.UpdateDatabaseWithFetchedData(_students, "Student", mContext);
                } else _wasSuccessful = false;
            }
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("doInBackground-SyncClient", e, "Error");
            BackupDatabase.backup(mContext);
            this.exception = e;
            return null;
        }
        if (_wasSuccessful) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addLog("doInBackground-SyncClient", "InformationLog");
        } else {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addLog("doInBackground-SyncClient", "Error", "Error occurred in data pull.");
        }
        return _wasSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();
        if (result != null) {
            if (result) {
                StatusDBHelper statusDBHelper = new StatusDBHelper(mContext);
                statusDBHelper.Update("pullFlag", "1");
                BackupDatabase.backup(mContext);

                v.setTextColor(Color.BLACK);
                v.setText("Data pull completed");

                Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.blink);
                v.startAnimation(animation1);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.clearAnimation();
                        v.setText("");
                    }
                }, 30000);

                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                syncActivityLogs.addLog("onPostExe-SyncClient", "InformationLog");

            } else {
                BackupDatabase.backup(mContext);
                if (this.exception == null) {
                    v.setTextColor(Color.RED);
                    v.setText("Files for data pull are not available.Please pull data using internet connectivity.");
                } else {
                    v.setTextColor(Color.RED);
                    v.setText("Data pull failed due to " + this.exception.getMessage());
                    SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                    syncActivityLogs.addLog("onPostExe-SyncClient", "InformationLog");
                }
            }
        }


    }

    JSONArray GetServerData(String ServiceURL, String fName) throws IOException {
        JSONArray _jsonArray = null;
        ConnectivityManager check = (ConnectivityManager)
                this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = check.getAllNetworkInfo();

        int i;
        for (i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                break;
            }
        }


        if (i < info.length) {


            String _output = "";
            try {
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 60000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 90000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                HttpClient client = new DefaultHttpClient(httpParameters);


                //  HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(ServiceURL);
                HttpResponse response = client.execute(request);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    _output += line;
                }

                _jsonArray = (_output != "") ? new JSONArray(_output) : null;
                WriteSettings(mContext, _output, fName);
            } catch (SocketTimeoutException ex) {
                this.exception = ex;
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                syncActivityLogs.addToDB("GetServerData-SyncClient", ex, "Error");
                BackupDatabase.backup(mContext);

            } catch (JSONException e) {
                e.printStackTrace();
                this.exception = e;
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                syncActivityLogs.addToDB("GetServerData-SyncClient", e, "Error");
                BackupDatabase.backup(mContext);
            }
        } else {
            byte[] data = null;
            File file = new File(splashScreenVideo.fpath + "Json/" + fName + ".json");
            FileInputStream fis = null;
            try {

                if (file.exists()) {
                    fis = new FileInputStream(file);
                    data = new byte[(int) file.length()];
                    fis.read(data);
                    fis.close();

                    String str = new String(data, "UTF-8");
                    try {
                        _jsonArray = (str != "") ? new JSONArray(str) : null;
                    } catch (JSONException e) {
                        SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                        syncActivityLogs.addToDB("GetServerData-SyncClient", e, "Error");
                        BackupDatabase.backup(mContext);
                        this.exception = e;
                        e.printStackTrace();
                    }
                } else {
                    //Toast.makeText(mContext,"Files for data pull are not available.Please pull data using internet connectivity.",Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();

                    _jsonArray = new JSONArray();
                }
            } catch (FileNotFoundException e) {
                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                syncActivityLogs.addToDB("GetServerData-SyncClient", e, "Error");
                BackupDatabase.backup(mContext);
                this.exception = e;
                e.printStackTrace();
            }


        }
        return _jsonArray;
    }


    public void WriteSettings(Context context, String data, String fName) {
        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        try {
            //fOut= new FileOutputStream("/storage/sdcard1/"+fName+".json");
            File dir = Environment.getExternalStorageDirectory();
            File file = new File(splashScreenVideo.fpath, "Json/" + fName + ".json");
            try {
                fOut = new FileOutputStream(file);

                // fOut = villageContext.openFileOutput("/storage/sdcard1/"+fName+".json",Context.MODE_WORLD_WRITEABLE);
                osw = new OutputStreamWriter(fOut);
                osw.write(data);
                osw.flush();
                osw.close();
                fOut.close();
            } finally {

            }
        } catch (Exception e) {
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("WriteSettings-SyncClient", e, "Error");
            BackupDatabase.backup(mContext);
            e.printStackTrace();
            this.exception = e;
            Toast.makeText(context, "Settings not saved", Toast.LENGTH_SHORT).show();
        }
    }


    Boolean ClearDatabaseEntriesDuringFetchData(String className, Context mContext) {
        if (className == "Village") {
            VillageDBHelper villageDBHelper = new VillageDBHelper(mContext);
            return villageDBHelper.DeleteAll();
        } else if (className == "Group") {
            GroupDBHelper groupDBHelper = new GroupDBHelper(mContext);
            return groupDBHelper.DeleteAll();
        } else if (className == "Student") {
            ScoreDBHelper scoreDBHelper = new ScoreDBHelper(mContext);
            scoreDBHelper.DeleteAll();
            StudentDBHelper studentDBHelper = new StudentDBHelper(mContext);
            return studentDBHelper.DeleteAll();
        }

        /********************Have to delete score table if necessary*******************/


        return false;
    }

    Boolean UpdateDatabaseWithFetchedData(JSONArray jsonArray, String className, Context mContext) {
        Boolean _flag = true;
        bulkInsertion = new BulkInsertion(mContext);
        SQLiteDatabase db = bulkInsertion.getMyDatabase();
        db.beginTransaction();
        try {

            for (x = 0; x < jsonArray.length(); x++) {
                JSONObject obj = jsonArray.getJSONObject(x);
                /*if(x==629){
                    x=0;
                }*/
                if (className == "Device") {
                    Device d = new Device();
                    DeviceDBHelper deviceDBHelper = new DeviceDBHelper(mContext);
                    d.DeviceID = obj.getString("DeviceId");
                    d.DeviceName = obj.getString("DeviceName");
                    d.DeviceType = obj.getString("DeviceType");

                    if (!deviceDBHelper.Add(d)) {
                        _flag = false;
                        break;
                    }
                } else if (className == "Village") {
                    Village v = new Village();
                    VillageDBHelper villageDBHelper = new VillageDBHelper(mContext);
                    v.VillageID = obj.getInt("VillageId");
                    v.VillageCode = obj.getString("VillageCode");
                    v.VillageName = obj.getString("VillageName");
                    v.Block = obj.getString("Block");
                    v.District = obj.getString("District");
                    v.State = obj.getString("State");
                    v.CRLID = obj.getString("CRLId");
                    villageDBHelper.Add(v, db);
                } else if (className == "Group") {
                    Group group = new Group();
                    GroupDBHelper groupDBHelper = new GroupDBHelper(mContext);
                    group.GroupID = obj.getString("GroupId");
                    group.GroupName = obj.getString("GroupName");
                    group.UnitNumber = obj.getString("UnitNumber");
                    group.DeviceID = obj.getString("DeviceId");
                    //group.Responsible = obj.getString("Responsible");
                    group.ResponsibleMobile = obj.getString("ResponsibleMobile");
                    group.VillageID = obj.getInt("VillageId");
                    groupDBHelper.Add(group, db);
                } else if (className == "Student") {

                    Student student = new Student();
                    StudentDBHelper studentDBHelper = new StudentDBHelper(mContext);
                    student.StudentID = obj.getString("StudentId");
                    student.FirstName = obj.getString("FirstName");
                    student.LastName = obj.getString("LastName");
                    //student.DateOfBirth = obj.getString("DateOfBirth");
                    student.Gender = obj.getString("Gender");
                    student.GroupID = obj.getString("GroupId");
                    student.MiddleName = obj.getString("MiddleName");
                    student.Age = obj.getInt("Age");
                    student.Class = obj.getInt("Class");
                    student.UpdatedDate = obj.getString("UpdatedDate");
                    studentDBHelper.Add(student, db);
                }
                //Toast.makeText(mContext,"rescount:"+res,Toast.LENGTH_LONG).show();
            }
            db.setTransactionSuccessful();

        } catch (Exception ex) {
            this.exception = ex;
            SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
            syncActivityLogs.addToDB("UpdateDatabaseWithFetchedData-SyncClient", ex, "Error");
            BackupDatabase.backup(mContext);
        } finally {
            db.endTransaction();
        }
        return _flag;
    }
}