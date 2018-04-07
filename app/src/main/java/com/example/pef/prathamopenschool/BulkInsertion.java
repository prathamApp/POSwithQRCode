package com.example.pef.prathamopenschool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;


public class BulkInsertion extends DBHelper {

    Context c;
    public BulkInsertion(Context c){
        super(c);
        this.c=c;
    }

    SQLiteDatabase getMyDatabase(){
        return getWritableDatabase();
    }
}
