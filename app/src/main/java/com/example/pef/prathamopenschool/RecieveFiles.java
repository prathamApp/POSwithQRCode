package com.example.pef.prathamopenschool;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;

import static com.example.pef.prathamopenschool.CrlDashboard.filename;

/**
 * Created by HP on 01-05-2018.
 */
public class RecieveFiles extends AsyncTask<Void, Integer, String> {

    String targetPath;
    String recieveProfilePath;
    //        ProgressDialog dialog;
    File newProfile;
    Context context;
    String type;

    public RecieveFiles(Context context, String targetPath, String recieveProfilePath, String type) {
        this.context = context;
        this.targetPath = targetPath;
        this.recieveProfilePath = recieveProfilePath;
        this.type = type;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//            dialog = new ProgressDialog(CrlShareReceiveProfiles.this);
//            dialog.setMessage("Receiving Profiles");
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.setCancelable(false);
//            dialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        // Extraction of contents
        if (type.equalsIgnoreCase("profiles")) {
            newProfile = new File(recieveProfilePath);
            Compress extract = new Compress();
//            ReceivePath = recieveProfilePath.replace("content://com.estrongs.files", "");

            Log.d("ReceivePath :::", recieveProfilePath);
            Log.d("TargetPath :::", targetPath);

            // Exctracting Data
            List<String> unzippedFileNames = extract.unzip(recieveProfilePath, targetPath);

            // Inserting All Jsons in Database
            try {
                // todo
                UpdateDB();
                // Error causing here
                newProfile.delete();
                // Transfer Student's Profiles from Receive folder to Student Profiles
                File src = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage");
                File dest = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/StudentProfiles");
                try {
                    if (!src.exists()) {
                        //Toast.makeText(c, "No folder exist in Internal Storage to copy", Toast.LENGTH_LONG).show();
                    } else if (dest.exists()) {
                        copyDirectory(src, dest);
                        //                        CrlShareReceiveProfiles.this.runOnUiThread(new Runnable() {
                        //                            public void run() {
                        //                                Toast.makeText(c, "Files copied successfully!", Toast.LENGTH_LONG).show();
                        //                            }
                        //                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase("json")) {
            try {
//                new File(recieveProfilePath).delete();
                // Add Initial Entries of CRL & Village Json to Database
                SetInitialValuesReceiveOff();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "true";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (type.equalsIgnoreCase("profiles")) {
            EventBus.getDefault().post(new MessageEvent("showDetails"));
        }
    }


    private void copyDirectory(File source, File target) throws IOException {
        try {
            if (!target.exists()) {
                target.mkdir();
            }

            for (String f : source.list()) {
                copy(new File(source, f), new File(target, f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Copy Function to Copy file
    public static void copy(File src, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);

            // buffer size 1K
            byte[] buf = new byte[1024];

            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            is.close();
            os.close();
        }
    }

    public void UpdateDB() throws JSONException {
        JSONArray crlJsonArray, studentsJsonArray, grpJsonArray;

        // For Loading CRL Json From External Storage (Assets)
        String crljsonData = loadCrlJSONFromAsset();
        Log.d("crljsonData::", "" + crljsonData);
        if (crljsonData != null && !crljsonData.equals("")) {
            crlJsonArray = new JSONArray(crljsonData);
            filename += "\n CRLs Recieved : " + crlJsonArray.length();
            for (int i = 0; i < crlJsonArray.length(); i++) {
                JSONObject clrJsonObject = crlJsonArray.getJSONObject(i);
                Crl crlobj = new Crl();
                crlobj.CRLId = clrJsonObject.getString("CRLID");
                crlobj.FirstName = clrJsonObject.getString("FirstName");
                crlobj.LastName = clrJsonObject.getString("LastName");
                crlobj.UserName = clrJsonObject.getString("UserName");
                crlobj.Password = clrJsonObject.getString("PassWord");
                crlobj.ProgramId = clrJsonObject.getInt("ProgramId");
                crlobj.Mobile = clrJsonObject.getString("Mobile");
                crlobj.State = clrJsonObject.getString("State");
                crlobj.Email = clrJsonObject.getString("Email");
                crlobj.CreatedBy = clrJsonObject.getString("CreatedBy");
                crlobj.newCrl = true;

                // new entries

                try {
                    crlobj.sharedBy = clrJsonObject.getString("sharedBy");
                    crlobj.SharedAtDateTime = clrJsonObject.getString("SharedAtDateTime");
                    crlobj.appVersion = clrJsonObject.getString("appVersion");
                    crlobj.appName = clrJsonObject.getString("appName");
                    crlobj.CreatedOn = clrJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    crlobj.sharedBy = "";
                    crlobj.SharedAtDateTime = "";
                    crlobj.appVersion = "";
                    crlobj.appName = "";
                    crlobj.CreatedOn = "";
                    e.printStackTrace();
                }

                new CrlDBHelper(context).replaceData(crlobj);
                BackupDatabase.backup(context);
            }
        }


        // For Loading Aser Json From External Storage (Assets)
        String aserjsonData = loadAserJSONFromAsset();
        if (aserjsonData != null && !aserjsonData.equals("")) {
            JSONArray aserJsonArray = new JSONArray(aserjsonData);
            for (int i = 0; i < aserJsonArray.length(); i++) {

                JSONObject aserJsonObject = aserJsonArray.getJSONObject(i);
                Aser asrobj = new Aser();

                asrobj.StudentId = aserJsonObject.getString("StudentId");
                asrobj.ChildID = aserJsonObject.getString("ChildID");
                asrobj.GroupID = aserJsonObject.getString("GroupID");
                asrobj.TestType = aserJsonObject.getInt("TestType");
                asrobj.TestDate = aserJsonObject.getString("TestDate");
                asrobj.Lang = aserJsonObject.getInt("Lang");
                asrobj.Num = aserJsonObject.getInt("Num");
                asrobj.OAdd = aserJsonObject.getInt("OAdd");
                asrobj.OSub = aserJsonObject.getInt("OSub");
                asrobj.OMul = aserJsonObject.getInt("OMul");
                asrobj.ODiv = aserJsonObject.getInt("ODiv");
                asrobj.WAdd = aserJsonObject.getInt("WAdd");
                asrobj.WSub = aserJsonObject.getInt("WSub");
                asrobj.CreatedBy = aserJsonObject.getString("CreatedBy");
                asrobj.CreatedDate = aserJsonObject.getString("CreatedDate");
                asrobj.DeviceId = aserJsonObject.getString("DeviceId");
                asrobj.FLAG = aserJsonObject.getInt("FLAG");

                // new entries
                try {
                    asrobj.sharedBy = aserJsonObject.getString("sharedBy");
                    asrobj.SharedAtDateTime = aserJsonObject.getString("SharedAtDateTime");
                    asrobj.appVersion = aserJsonObject.getString("appVersion");
                    asrobj.appName = aserJsonObject.getString("appName");
                    asrobj.CreatedOn = aserJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    asrobj.sharedBy = "";
                    asrobj.SharedAtDateTime = "";
                    asrobj.appVersion = "";
                    asrobj.appName = "";
                    asrobj.CreatedOn = "";
                    e.printStackTrace();
                }

                if (asrobj.TestType == 0) {
                    boolean result;
                    result = new AserDBHelper(context).CheckDataExists(asrobj.StudentId, 0);
                    if (result == false) {
                        new AserDBHelper(context).insertData(asrobj);
                        BackupDatabase.backup(context);
                    } else {
                        new AserDBHelper(context).UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(context);
                    }
                } else if (asrobj.TestType == 1) {
                    boolean result;
                    result = new AserDBHelper(context).CheckDataExists(asrobj.StudentId, 1);
                    if (result == false) {
                        new AserDBHelper(context).insertData(asrobj);
                        BackupDatabase.backup(context);
                    } else {
                        new AserDBHelper(context).UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(context);
                    }
                } else if (asrobj.TestType == 2) {
                    boolean result;
                    result = new AserDBHelper(context).CheckDataExists(asrobj.StudentId, 2);
                    if (result == false) {
                        new AserDBHelper(context).insertData(asrobj);
                        BackupDatabase.backup(context);
                    } else {
                        new AserDBHelper(context).UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(context);
                    }
                } else if (asrobj.TestType == 3) {
                    boolean result;
                    result = new AserDBHelper(context).CheckDataExists(asrobj.StudentId, 3);
                    if (result == false) {
                        new AserDBHelper(context).insertData(asrobj);
                        BackupDatabase.backup(context);
                    } else {
                        new AserDBHelper(context).UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(context);
                    }
                } else if (asrobj.TestType == 4) {
                    boolean result;
                    result = new AserDBHelper(context).CheckDataExists(asrobj.StudentId, 4);
                    if (result == false) {
                        new AserDBHelper(context).insertData(asrobj);
                        BackupDatabase.backup(context);
                    } else {
                        new AserDBHelper(context).UpdateReceivedAserData(asrobj.ChildID, asrobj.TestDate, asrobj.Lang, asrobj.Num, asrobj.OAdd,
                                asrobj.OSub, asrobj.OMul, asrobj.ODiv, asrobj.WAdd, asrobj.WSub, asrobj.CreatedBy,
                                asrobj.CreatedDate, asrobj.FLAG, asrobj.sharedBy, asrobj.SharedAtDateTime, asrobj.appVersion,
                                asrobj.appName, asrobj.CreatedOn, asrobj.StudentId, 0);
                        BackupDatabase.backup(context);
                    }

                    //adb.insertData(asrobj);
                    //BackupDatabase.backup(c);

                }


            }// For Loop

        }


        // For Loading Student Json From External Storage (Assets)
        String studentjsonData = loadStudentJSONFromAsset();
        if (studentjsonData != null && !studentjsonData.equals("")) {
            studentsJsonArray = new JSONArray(studentjsonData);

            filename += "\n Students Recieved : " + studentsJsonArray.length();
            for (int j = 0; j < studentsJsonArray.length(); j++) {

                JSONObject stdJsonObject = studentsJsonArray.getJSONObject(j);

                Student stdObj = new Student();

                stdObj.StudentID = stdJsonObject.getString("StudentID");
                stdObj.FirstName = stdJsonObject.getString("FirstName");
                stdObj.MiddleName = stdJsonObject.getString("MiddleName");
                stdObj.LastName = stdJsonObject.getString("LastName");
                stdObj.Age = stdJsonObject.getInt("Age");
                stdObj.stdClass = stdJsonObject.getInt("Class");
                stdObj.UpdatedDate = stdJsonObject.getString("UpdatedDate");
                stdObj.Gender = stdJsonObject.getString("Gender");
                stdObj.GroupID = stdJsonObject.getString("GroupID");
                stdObj.CreatedBy = stdJsonObject.getString("CreatedBy");
                stdObj.StudentUID = stdJsonObject.getString("StudentUID");
                stdObj.newStudent = true;
                //todo new
                stdObj.IsSelected = stdJsonObject.getBoolean("IsSelected");

                // new entries
                try {
                    stdObj.sharedBy = stdJsonObject.getString("sharedBy");
                    stdObj.SharedAtDateTime = stdJsonObject.getString("SharedAtDateTime");
                    stdObj.appVersion = stdJsonObject.getString("appVersion");
                    stdObj.appName = stdJsonObject.getString("appName");
                    stdObj.CreatedOn = stdJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    stdObj.sharedBy = "";
                    stdObj.SharedAtDateTime = "";
                    stdObj.appVersion = "";
                    stdObj.appName = "";
                    stdObj.CreatedOn = "";
                    e.printStackTrace();
                }

                new StudentDBHelper(context).replaceData(stdObj);
                BackupDatabase.backup(context);
            }
        }

        // For Loading Group Json From External Storage (Assets)
        String groupjsonData = loadGroupJSONFromAsset();
        if (groupjsonData != null && !groupjsonData.equals("")) {
            grpJsonArray = new JSONArray(groupjsonData);
            filename += "\n Groups Recieved : " + grpJsonArray.length();
            for (int j = 0; j < grpJsonArray.length(); j++) {

                JSONObject grpJsonObject = grpJsonArray.getJSONObject(j);

                Group grpObj = new Group();

                grpObj.GroupID = grpJsonObject.getString("GroupID");
                grpObj.GroupCode = grpJsonObject.getString("GroupCode");
                grpObj.GroupName = grpJsonObject.getString("GroupName");
                grpObj.UnitNumber = grpJsonObject.getString("UnitNumber");
                grpObj.DeviceID = grpJsonObject.getString("DeviceID");
                grpObj.Responsible = grpJsonObject.getString("Responsible");
                grpObj.ResponsibleMobile = grpJsonObject.getString("ResponsibleMobile");
                grpObj.VillageID = grpJsonObject.getInt("VillageID");
                grpObj.ProgramID = grpJsonObject.getInt("ProgramId");
                grpObj.CreatedBy = grpJsonObject.getString("CreatedBy");
                grpObj.SchoolName = grpJsonObject.getString("SchoolName");
                grpObj.VillageName = grpJsonObject.getString("VillageName");
                grpObj.newGroup = true;

                // new entries
                try {
                    grpObj.sharedBy = grpJsonObject.getString("sharedBy");
                    grpObj.SharedAtDateTime = grpJsonObject.getString("SharedAtDateTime");
                    grpObj.appVersion = grpJsonObject.getString("appVersion");
                    grpObj.appName = grpJsonObject.getString("appName");
                    grpObj.CreatedOn = grpJsonObject.getString("CreatedOn");
                } catch (JSONException e) {
                    grpObj.sharedBy = "";
                    grpObj.SharedAtDateTime = "";
                    grpObj.appVersion = "";
                    grpObj.appName = "";
                    grpObj.CreatedOn = "";
                    e.printStackTrace();
                }

                new GroupDBHelper(context).replaceData(grpObj);

            }
        }
        BackupDatabase.backup(context);
    }


    // Reading CRL Json From internal memory
    public String loadCrlJSONFromAsset() {
        String crlJsonStr = "";

        try {
            File crlJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage/", "Crl.json");
            if (crlJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(crlJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    crlJsonStr = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }

        return crlJsonStr;
    }


    // Reading Aser Json From internal memory
    public String loadAserJSONFromAsset() {
        String aserJson = "";
        try {
            File AserJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage/", "Aser.json");
            if (AserJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(AserJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    aserJson = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }

        } catch (Exception e) {
        }

        return aserJson;

    }

    // Reading Student Json From internal memory
    public String loadStudentJSONFromAsset() {
        String studentJson = "";
        try {
            File studentJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage/", "Student.json");
            if (studentJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(studentJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    studentJson = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }

        } catch (Exception e) {
        }

        return studentJson;

    }

    // Reading Student Json From internal memory
    public String loadGroupJSONFromAsset() {
        String groupJson = "";
        try {
            File groupJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/receivedUsage/", "Group.json");
            if (groupJsonSDCard.exists()) {
                FileInputStream stream = new FileInputStream(groupJsonSDCard);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    groupJson = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            }
        } catch (Exception e) {
        }

        return groupJson;

    }


    void SetInitialValuesReceiveOff() throws JSONException {
        // insert your code to run only when application is started first time here
        //CRL Initial DB Process
        CrlDBHelper db = new CrlDBHelper(context);
        // For Loading CRL Json From External Storage (Assets)
        JSONArray crlJsonArray = new JSONArray(loadCrlJSONFromAssetReceiveOff());
        for (int i = 0; i < crlJsonArray.length(); i++) {
            JSONObject clrJsonObject = crlJsonArray.getJSONObject(i);
            Crl crlobj = new Crl();
            crlobj.CRLId = clrJsonObject.getString("CRLId");
            crlobj.FirstName = clrJsonObject.getString("FirstName");
            crlobj.LastName = clrJsonObject.getString("LastName");
            crlobj.UserName = clrJsonObject.getString("UserName");
            crlobj.Password = clrJsonObject.getString("Password");
            crlobj.ProgramId = clrJsonObject.getInt("ProgramId");
            crlobj.Mobile = clrJsonObject.getString("Mobile");
            crlobj.State = clrJsonObject.getString("State");
            crlobj.Email = clrJsonObject.getString("Email");
            crlobj.newCrl = true;
            // new entries default values
            try {
                crlobj.sharedBy = clrJsonObject.getString("sharedBy");
                crlobj.SharedAtDateTime = clrJsonObject.getString("SharedAtDateTime");
                crlobj.appVersion = clrJsonObject.getString("appVersion");
                crlobj.appName = clrJsonObject.getString("appName");
                crlobj.CreatedOn = clrJsonObject.getString("CreatedOn");
            } catch (Exception e) {
                crlobj.sharedBy = "";
                crlobj.SharedAtDateTime = "";
                crlobj.appVersion = "";
                crlobj.appName = "";
                crlobj.CreatedOn = "";
                e.printStackTrace();
            }
            db.updateJsonData(crlobj);
            BackupDatabase.backup(context);
        }
        //Villages Initial DB Process
        VillageDBHelper database = new VillageDBHelper(context);
        // For Loading Villages Json From External Storage (Assets)
        JSONArray villagesJsonArray = new JSONArray(loadVillageJSONFromAssetReceiveOff());
        for (int j = 0; j < villagesJsonArray.length(); j++) {
            JSONObject villagesJsonObject = villagesJsonArray.getJSONObject(j);
            Village villageobj = new Village();
            villageobj.VillageID = villagesJsonObject.getInt("VillageId");
            villageobj.VillageCode = villagesJsonObject.getString("VillageCode");
            villageobj.VillageName = villagesJsonObject.getString("VillageName");
            villageobj.Block = villagesJsonObject.getString("Block");
            villageobj.District = villagesJsonObject.getString("District");
            villageobj.State = villagesJsonObject.getString("State");
            villageobj.CRLID = villagesJsonObject.getString("CRLId");
            database.updateJsonData(villageobj);
            BackupDatabase.backup(context);
        }
    }

    // Reading CRL Json From Internal Memory
    public String loadCrlJSONFromAssetReceiveOff() {
        String crlJsonStr = null;
        try {
            File crlJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Crl.json");
            FileInputStream stream = new FileInputStream(crlJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                crlJsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return crlJsonStr;
    }

    // Reading Village Json From SDCard
    public String loadVillageJSONFromAssetReceiveOff() {
        String villageJson = null;
        try {
            File villageJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Village.json");
            FileInputStream stream = new FileInputStream(villageJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                villageJson = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return villageJson;

    }

}
