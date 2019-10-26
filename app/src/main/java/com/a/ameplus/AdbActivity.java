package com.a.ameplus;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AdbActivity extends Activity implements View.OnClickListener {

    Button butAdb;
    TextView adbLog;
    AMEplusApplication app;
    Button memutil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AMEplusApplication) getApplication();
        setContentView(R.layout.activity_adb);
        butAdb=(Button)findViewById(R.id.buttonAdb);
        memutil=(Button)findViewById(R.id.memutils);
        memutil.setOnClickListener(this);
        adbLog=findViewById(R.id.adbLog);
        butAdb.setOnClickListener(this);
        butAdb.setText("start");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAdb:
                if (butAdb.getText()=="start")
                {
                butAdb.setText("stop");

                    app.sudo("setprop service.adb.tcp.port 5555");
                    app.sudo("stop adbd");
                    app.sudo("start adbd");

                adbLog.setText("ADB is working");
                    adbLog.append("\n\nOn computer:\nadb connect "+IpUtils.getIPAddress(true)+":5555");} else
                if (butAdb.getText()=="stop")
                {
                    butAdb.setText("start");

                    app.sudo("setprop service.adb.tcp.port -1");
                    app.sudo("stop adbd");
                    app.sudo("start adbd");


                    adbLog.setText("ADB is stoped");

                }
                break;
            case R.id.memutils:
                Intent intent = new Intent(this, MemUtils.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
