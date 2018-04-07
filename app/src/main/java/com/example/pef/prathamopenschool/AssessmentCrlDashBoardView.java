package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AssessmentCrlDashBoardView extends AppCompatActivity {

    Context c;
    TextView tv_version_code;
    static boolean dashboardFlag;
    String forAssessmentHideShow;
    Button btn_AssessmentReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_crl_dash_board_view);
        c = this;

        forAssessmentHideShow = getIntent().getStringExtra("fromActivity");

        btn_AssessmentReport = (Button) findViewById(R.id.btn_AssessmentReport);

        if(forAssessmentHideShow.equals("main"))
            btn_AssessmentReport.setVisibility(View.GONE);


        tv_version_code = (TextView) findViewById(R.id.tv_Version);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String verCode = pInfo.versionName;
            tv_version_code.setText(String.valueOf(verCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

/*
    public void getPlayedContent(View view) {

        String Path = "file:///android_asset/Tab_Report/index.html";
        Intent intent = new Intent(c, WebViewActivity.class);
        intent.putExtra("path", Path);
        dashboardFlag = true;
        Runtime rs = Runtime.getRuntime();
        rs.freeMemory();
        rs.gc();
        rs.freeMemory();
        ((Activity) c).startActivityForResult(intent, 1);

*/
/*
        Intent intent = new Intent(c, ViewedContent.class);
        startActivity(intent);
*//*


    }
*/


    public void goToUsageReport(View view) {

        Intent i = new Intent(AssessmentCrlDashBoardView.this, TabUsageReport.class);
        startActivity(i);
    }

    public void goToAssessmentReport(View view) {

        Intent i = new Intent(AssessmentCrlDashBoardView.this, AssessmentCrlView.class);
        i.putExtra("myReport", "AllReport");
        startActivity(i);

    }

}
