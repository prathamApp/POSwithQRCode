package com.example.pef.prathamopenschool;

import android.content.Context;
import android.widget.Toast;

public class UserSessionDBHelper extends DBHelper {
    final String TABLENAME = "Session";
    final String ERRORTABLENAME = "Logs";
    public UserSessionDBHelper(Context context) {
        super(context); database = getWritableDatabase();
    }

    public boolean Add(UserSession sessionObj) {
        try {

            _PopulateContentValues(sessionObj);

            long resultCount = database.insert(TABLENAME, null, contentValues);
            Toast.makeText(c, "resultCount from session" + resultCount,
                    Toast.LENGTH_SHORT).show();
            database.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void _PopulateContentValues(UserSession session) {
        try {
            contentValues.put("SessionID",session.UserSessionID);
            contentValues.put("UserId",session.UserID);
        } catch (Exception ex) {
        }
    }
}
